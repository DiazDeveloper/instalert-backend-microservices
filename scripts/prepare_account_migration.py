"""Validate a reconciled offline export and produce MySQL SQL. Never connects to a database.

The source must explicitly associate IAM IDs and profile IDs. No password recovery,
automatic matching, deletion, or direct cross-service table access takes place.
"""
import argparse
import datetime as dt
import json
import re
import uuid
from pathlib import Path


def sql(value):
    if value is None:
        return "NULL"
    if isinstance(value, int):
        return str(value)
    # Hex UTF-8 literals avoid SQL-mode-dependent escaping and injection.
    return "CONVERT(X'" + str(value).encode("utf-8").hex() + "' USING utf8mb4)"


def indexed(rows, name):
    result = {}
    for row in rows:
        value = row.get("id")
        if type(value) is not int or value <= 0 or value > 9223372036854775807 or value in result:
            raise ValueError(f"Invalid or duplicate {name} ID")
        result[value] = row
    return result


def prepare(data):
    identities = indexed(data["identities"], "identity")
    profiles = indexed(data["profiles"], "profile")
    pairs = data["associations"]
    seen_iam, seen_profiles, seen_accounts, emails = set(), set(), set(), set()
    rows = ["-- Sensitive credential hashes. Apply only to an empty Account database after Flyway.",
            "SET time_zone = '+00:00';", "START TRANSACTION;"]
    for pair in pairs:
        iam_id, profile_id = pair["iamUserId"], pair["profileUserId"]
        if type(iam_id) is not int or type(profile_id) is not int:
            raise ValueError("Association IDs must be integers")
        if iam_id not in identities or profile_id not in profiles or iam_id in seen_iam or profile_id in seen_profiles:
            raise ValueError("Associations must be explicit and one-to-one")
        seen_iam.add(iam_id); seen_profiles.add(profile_id)
        identity, profile = identities[iam_id], profiles[profile_id]
        email = identity["email"].strip().lower()
        if not re.fullmatch(r"[^\s@]+@[^\s@]+\.[^\s@]+", email) or len(email) > 254 or email in emails:
            raise ValueError("Invalid or duplicate identity email")
        if email != profile["email"].strip().lower():
            raise ValueError("Identity/profile email discrepancy needs manual reconciliation")
        emails.add(email)
        password_hash = identity["passwordHash"]
        if not re.fullmatch(r"\$2[aby]\$\d{2}\$[./A-Za-z0-9]{53}", password_hash):
            raise ValueError("Only existing compatible BCrypt hashes may be imported")
        authority = identity.get("roles", [])
        if not authority or len(authority) != len(set(authority)) or not set(authority) <= {"ROLE_USER", "ROLE_ADMIN"}:
            raise ValueError("Review the actual IAM authority roles; profile display roles cannot grant access")
        account_id = str(uuid.UUID(pair["accountId"])) if pair.get("accountId") else str(uuid.uuid5(uuid.NAMESPACE_URL, f"instalert/account/{iam_id}/{profile_id}"))
        if account_id in seen_accounts:
            raise ValueError("Duplicate public account UUID")
        seen_accounts.add(account_id)
        name = profile["name"].strip()
        if not name or len(name) > 120:
            raise ValueError("Invalid profile display name")
        birth = profile.get("birthDate") or None
        if birth and (dt.date.fromisoformat(birth) > dt.date.today()):
            raise ValueError("Invalid birth date")
        fields = [("phone", 30), ("gender", 40), ("avatar", 2048), ("currentLocation", 255)]
        values = {key: profile.get(key) or "" for key, _ in fields}
        if any(len(values[key]) > limit for key, limit in fields):
            raise ValueError("Profile field exceeds schema limit")
        status = identity.get("status", "ACTIVE")
        if status not in {"ACTIVE", "DISABLED"}:
            raise ValueError("Unknown account status")
        rows.append("INSERT INTO account_identities(identity_id,legacy_iam_user_id,email,password_hash,account_status,token_version,version,created_at,updated_at) VALUES (" +
                    ",".join(map(sql, [iam_id, iam_id, email, password_hash, status, 0, 0])) + ",UTC_TIMESTAMP(6),UTC_TIMESTAMP(6));")
        rows.append("INSERT INTO account_profiles(profile_id,legacy_profile_user_id,display_name,phone,birth_date,gender,avatar,current_location,version,created_at,updated_at) VALUES (" +
                    ",".join(map(sql, [profile_id, profile_id, name, values["phone"], birth, values["gender"], values["avatar"], values["currentLocation"], 0])) + ",UTC_TIMESTAMP(6),UTC_TIMESTAMP(6));")
        rows.append("INSERT INTO account_associations(account_id,identity_id,profile_id) VALUES (" + ",".join(map(sql, [account_id, iam_id, profile_id])) + ");")
        for role in authority:
            rows.append(f"INSERT INTO identity_roles(identity_id,role_id) SELECT {iam_id},role_id FROM roles WHERE role_name={sql(role)};")
    if seen_iam != set(identities) or seen_profiles != set(profiles):
        raise ValueError("Unmatched identities or profiles: resolve orphans before importing")
    if not set(data.get("referencedProfileIds", [])) <= seen_profiles:
        raise ValueError("A monolith reference has no reconciled Account profile")
    rows.append("COMMIT;")
    return "\n".join(rows) + "\n"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path)
    parser.add_argument("destination", type=Path)
    args = parser.parse_args()
    try:
        content = prepare(json.loads(args.source.read_text(encoding="utf-8")))
        with args.destination.open("x", encoding="utf-8") as output:
            output.write(content)
    except (ValueError, KeyError, TypeError, OSError):
        # Neither exception details nor records/hashes are logged.
        parser.exit(1, "Migration rejected. Check associations, orphans, emails, roles, dates and output path.\n")
    print("Validated SQL prepared; no database was accessed. Keep the output private.")


if __name__ == "__main__":
    main()
