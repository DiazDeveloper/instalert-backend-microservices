Feature: Account registration, authentication and profile authorization
  Scenario: Registration creates an identity with an associated profile
    When a citizen registers a complete account
    Then registration succeeds with an associated profile and citizen authority

  Scenario: Invalid credentials cannot open a session
    Given a registered citizen
    When the citizen signs in with an incorrect password
    Then the request is rejected with status 401

  Scenario: A citizen cannot edit another account
    Given two registered citizens
    When the first citizen edits the second citizen profile
    Then the request is rejected with status 403
    And the second citizen email remains unchanged
