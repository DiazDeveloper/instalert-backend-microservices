package com.instalert.account;

import com.fasterxml.jackson.databind.*;
import com.instalert.account.infrastructure.*;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class AccountIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate db;
    @Autowired AccountRepository accounts;
    private static final String PASSWORD="Passw0rd!";
    private static final String SECRET="YWNj b3VudC10ZXN0LWtleS0zMi1ieXRlcy1sb25nLTAwMDA=".replace(" ","");
    @BeforeEach void clean() {
        db.update("DELETE FROM identity_roles"); db.update("DELETE FROM account_associations");
        db.update("DELETE FROM account_profiles"); db.update("DELETE FROM account_identities");
    }
    JsonNode register(String email) throws Exception {
        return json.readTree(mvc.perform(post("/api/v1/authentication/sign-up").contentType("application/json")
            .content(json.writeValueAsString(Map.of("name","Citizen","email",email,"password",PASSWORD,"birthDate","2000-01-01"))))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }
    String token(JsonNode n) { return "Bearer "+n.get("token").asText(); }
    String id(JsonNode n) { return n.get("accountId").asText(); }
    @Test void atomicRegistrationAndDuplicateEmail() throws Exception {
        var a=register("a@example.org");
        assertThat(a.get("profile").get("roles").get(0).asText()).isEqualTo("ROLE_USER");
        assertThat(db.queryForObject("SELECT count(*) FROM account_associations",Long.class)).isEqualTo(1);
        assertThat(db.queryForObject("SELECT password_hash FROM account_identities",String.class)).startsWith("$2").isNotEqualTo(PASSWORD);
        mvc.perform(post("/api/v1/authentication/sign-up").contentType("application/json")
            .content("{\"name\":\"Other\",\"email\":\"A@example.org\",\"password\":\"Passw0rd!\"}"))
            .andExpect(status().isConflict());
        assertThat(db.queryForObject("SELECT count(*) FROM account_identities",Long.class)).isEqualTo(1);
        assertThat(db.queryForObject("SELECT count(*) FROM account_profiles",Long.class)).isEqualTo(1);
    }
    @Test void associatesIndependentIdentityAndProfileIds() throws Exception {
        db.execute("ALTER TABLE account_identities ALTER COLUMN identity_id RESTART WITH 100");
        db.execute("ALTER TABLE account_profiles ALTER COLUMN profile_id RESTART WITH 200");
        var a=register("mapping@example.org");
        assertThat(db.queryForObject("SELECT identity_id FROM account_associations",Long.class)).isEqualTo(100);
        assertThat(db.queryForObject("SELECT profile_id FROM account_associations",Long.class)).isEqualTo(200);
        assertThat(a.get("profile").get("id").asLong()).isEqualTo(200);
        mvc.perform(get("/api/v1/account/session").header("Authorization",token(a)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.referenceId").value(200));
    }
    @Test void databaseFailureRollsBackIdentityAndProfile() throws Exception {
        db.execute("ALTER TABLE account_profiles ADD CONSTRAINT reject_test_name CHECK(display_name <> 'Citizen')");
        try {
            mvc.perform(post("/api/v1/authentication/sign-up").contentType("application/json")
                .content("{\"name\":\"Citizen\",\"email\":\"rollback@example.org\",\"password\":\"Passw0rd!\"}"))
                .andExpect(status().isConflict());
            assertThat(db.queryForObject("SELECT count(*) FROM account_identities",Long.class)).isZero();
            assertThat(db.queryForObject("SELECT count(*) FROM account_profiles",Long.class)).isZero();
            assertThat(db.queryForObject("SELECT count(*) FROM account_associations",Long.class)).isZero();
        } finally { db.execute("ALTER TABLE account_profiles DROP CONSTRAINT reject_test_name"); }
    }
    @Test void rejectsPrivilegedSignupAndInvalidInputs() throws Exception {
        for(var extra:List.of(",\"roles\":[\"ROLE_ADMIN\"]", ",\"role\":\"Admin\""))
            mvc.perform(post("/api/v1/authentication/sign-up").contentType("application/json")
                .content("{\"name\":\"Citizen\",\"email\":\"a@example.org\",\"password\":\"Passw0rd!\""+extra+"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/authentication/sign-up").contentType("application/json")
            .content("{\"name\":\"Citizen\",\"email\":\"a@example.org\",\"password\":\"short\"}"))
            .andExpect(status().isBadRequest());
        assertThat(accounts.count()).isZero();
    }
    @Test void loginAndEmailChangeUseOneIdentity() throws Exception {
        var a=register("a@example.org");
        mvc.perform(post("/api/v1/authentication/sign-in").contentType("application/json")
            .content("{\"email\":\"a@example.org\",\"password\":\"wrong\"}")).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/v1/users/"+id(a)).header("Authorization",token(a)).contentType("application/json")
            .content("{\"name\":\"New Name\",\"email\":\"new@example.org\",\"phone\":\"123\",\"birthDate\":\"\",\"gender\":\"\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("New Name"));
        mvc.perform(post("/api/v1/authentication/sign-in").contentType("application/json")
            .content("{\"email\":\"new@example.org\",\"password\":\"Passw0rd!\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.accountId").value(id(a)));
    }
    @Test void negativePermissionMatrixDoesNotModifyData() throws Exception {
        var a=register("a@example.org"); var b=register("b@example.org");
        String before=db.queryForObject("SELECT email FROM account_identities WHERE email='b@example.org'",String.class);
        mvc.perform(get("/api/v1/users/"+id(b)).header("Authorization",token(a))).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/users/"+id(b)).header("Authorization",token(a)).contentType("application/json")
            .content("{\"email\":\"stolen@example.org\"}")).andExpect(status().isForbidden());
        mvc.perform(delete("/api/v1/users/"+id(b)).header("Authorization",token(a))).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/users/"+id(a)+"/role").header("Authorization",token(a)).contentType("application/json")
            .content("{\"role\":\"Admin\"}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/authentication/"+id(b)+"/change-password").header("Authorization",token(a)).contentType("application/json")
            .content("{\"currentPassword\":\"Passw0rd!\",\"newPassword\":\"NewPassw0rd!\"}")).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/users").header("Authorization",token(a))).andExpect(status().isForbidden());
        assertThat(db.queryForObject("SELECT email FROM account_identities WHERE email='b@example.org'",String.class)).isEqualTo(before);
        assertThat(db.queryForObject("SELECT count(*) FROM account_identities WHERE account_status='ACTIVE'",Long.class)).isEqualTo(2);
        mvc.perform(get("/api/v1/account/directory").header("Authorization",token(a)))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].email").doesNotExist()).andExpect(jsonPath("$[0].phone").doesNotExist());
    }
    @Test void missingInvalidAndExpiredTokensReturn401() throws Exception {
        var a=register("a@example.org");
        mvc.perform(get("/api/v1/account/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/account/me").header("Authorization","Bearer invalid")).andExpect(status().isUnauthorized());
        String expired=Jwts.builder().setSubject(id(a)).setIssuer("ms-account").setAudience("instalert-api")
            .setExpiration(new Date(System.currentTimeMillis()-1000)).claim("ver",0)
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)),SignatureAlgorithm.HS256).compact();
        mvc.perform(get("/api/v1/account/me").header("Authorization","Bearer "+expired)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/users/"+id(a)).header("Authorization",token(a))).andExpect(status().isOk());
    }
    @Test void passwordAndDisableRevokeSessions() throws Exception {
        var a=register("a@example.org");
        mvc.perform(post("/api/v1/authentication/"+id(a)+"/change-password").header("Authorization",token(a)).contentType("application/json")
            .content("{\"currentPassword\":\"Passw0rd!\",\"newPassword\":\"NewPassw0rd!\"}")).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/account/me").header("Authorization",token(a))).andExpect(status().isUnauthorized());
        var login=mvc.perform(post("/api/v1/authentication/sign-in").contentType("application/json")
            .content("{\"email\":\"a@example.org\",\"password\":\"NewPassw0rd!\"}")).andExpect(status().isOk()).andReturn();
        var newSession=json.readTree(login.getResponse().getContentAsString());
        db.update("UPDATE account_identities SET account_status='DISABLED' WHERE email='a@example.org'");
        mvc.perform(get("/api/v1/account/me").header("Authorization",token(newSession))).andExpect(status().isUnauthorized());
    }
    @Test void adminCanManageButCannotChangeOthersPassword() throws Exception {
        var admin=register("admin@example.org"); var citizen=register("user@example.org");
        db.update("INSERT INTO identity_roles(identity_id,role_id) SELECT identity_id,role_id FROM account_identities,roles WHERE email='admin@example.org' AND role_name='ROLE_ADMIN'");
        mvc.perform(get("/api/v1/users").header("Authorization",token(admin))).andExpect(status().isOk());
        mvc.perform(post("/api/v1/authentication/"+id(citizen)+"/change-password").header("Authorization",token(admin)).contentType("application/json")
            .content("{\"currentPassword\":\"Passw0rd!\",\"newPassword\":\"NewPassw0rd!\"}")).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/users/"+id(citizen)+"/role").header("Authorization",token(admin)).contentType("application/json")
            .content("{\"role\":\"Admin\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
        mvc.perform(get("/api/v1/account/me").header("Authorization",token(citizen))).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/v1/users/"+id(citizen)).header("Authorization",token(admin))).andExpect(status().isNoContent());
    }
}
