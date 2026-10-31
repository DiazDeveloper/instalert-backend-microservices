package com.instalert.emergencies;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
@RestController
public class EmergencyDocumentation {
 @GetMapping(value="/v3/api-docs",produces=MediaType.APPLICATION_JSON_VALUE)
 public String specification() throws IOException {
  try(var input=new ClassPathResource("openapi.json").getInputStream()) {
   return new String(input.readAllBytes(),StandardCharsets.UTF_8);
  }
 }
}
