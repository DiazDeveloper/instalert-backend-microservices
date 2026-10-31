Feature: Emergency lifecycle owned by an authenticated account
  @HU4 @HU5
  Scenario: Create an alert with a valid location
    Given a recent valid emergency location
    When the owner creates an emergency
    Then one active alert and its pending event are persisted

  @HU4
  Scenario: Retry creation after losing the response
    Given a recent valid emergency location
    When the owner repeats the same emergency request
    Then one active alert and its pending event are persisted

  @HU7 @AC03
  Scenario: Another account cannot cancel an alert
    Given a recent valid emergency location
    When another account tries to cancel the owner's emergency
    Then access is forbidden and the alert remains active
