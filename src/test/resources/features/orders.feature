Feature: Ordering
  As a customer I can place, confirm and cancel orders,
  and large orders get a 10% discount.

  Background:
    Given the order service is running

  Scenario: Place a small order
    When a customer orders 3 of "Notebook" at 4.50 each
    Then the order is accepted with a total of 13.50
    And the order status is "NEW"

  Scenario: Large orders get a discount
    When a customer orders 10 of "Monitor" at 120.00 each
    Then the order is accepted with a total of 1080.00

  Scenario: Confirm an order
    When a customer orders 1 of "Desk" at 250.00 each
    And the customer confirms the order
    Then the order status is "CONFIRMED"

  Scenario: A confirmed order can't be cancelled
    When a customer orders 1 of "Chair" at 80.00 each
    And the customer confirms the order
    And the customer cancels the order
    Then the change is refused

  Scenario: An order without an item is refused
    When a customer orders 1 of "" at 5.00 each
    Then the order is refused
