import copy
import unittest
from prepare_account_migration import prepare


class MigrationTests(unittest.TestCase):
    def setUp(self):
        self.data = {
            "identities": [{"id": 7, "email": "a@example.org", "passwordHash": "$2a$10$" + "a" * 53, "roles": ["ROLE_USER"]}],
            "profiles": [{"id": 42, "name": "Citizen", "email": "a@example.org", "birthDate": "2000-01-01"}],
            "associations": [{"iamUserId": 7, "profileUserId": 42}],
            "referencedProfileIds": [42],
        }

    def test_preserves_independent_ids_hash_and_reference(self):
        result = prepare(self.data)
        self.assertIn("VALUES (7,7,", result)
        self.assertIn("VALUES (42,42,", result)
        self.assertIn(self.data["identities"][0]["passwordHash"].encode().hex(), result)
        self.assertTrue(result.endswith("COMMIT;\n"))
        self.assertEqual(result, prepare(self.data))

    def test_rejects_orphan_email_mismatch_plain_password_and_unknown_reference(self):
        for case in range(4):
            data = copy.deepcopy(self.data)
            if case == 0: data["associations"] = []
            if case == 1: data["profiles"][0]["email"] = "other@example.org"
            if case == 2: data["identities"][0]["passwordHash"] = "plaintext"
            if case == 3: data["referencedProfileIds"] = [99]
            with self.assertRaises(ValueError): prepare(data)

    def test_display_admin_does_not_grant_authority(self):
        self.data["profiles"][0]["role"] = "Admin"
        result = prepare(self.data)
        self.assertNotIn("ROLE_ADMIN".encode().hex(), result)


if __name__ == "__main__": unittest.main()
