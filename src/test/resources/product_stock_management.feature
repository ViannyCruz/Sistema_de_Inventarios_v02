Feature: Product Stock Management
  As an inventory manager
  I want to manage product stock levels
  So that I can track inventory and prevent stockouts

  Background:
    Given the inventory system is initialized

  Scenario: Product is identified as low stock when below minimum threshold
    Given a product "Laptop" with stock 5 and minimum stock 10
    When I check if the product has low stock
    Then the product should be flagged as low stock
    And the stock status should be "LOW_STOCK"

  Scenario: Product is not identified as low stock when above minimum threshold
    Given a product "Mouse" with stock 15 and minimum stock 10
    When I check if the product has low stock
    Then the product should not be flagged as low stock
    And the stock status should be "IN_STOCK"

  Scenario: Product is identified as out of stock when stock is zero
    Given a product "Keyboard" with stock 0 and minimum stock 5
    When I check if the product is out of stock
    Then the product should be flagged as out of stock
    And the stock status should be "OUT_OF_STOCK"

  Scenario: Product is identified as out of stock when stock is null
    Given a product "Monitor" with null stock and minimum stock 3
    When I check if the product is out of stock
    Then the product should be flagged as out of stock
    And the stock status should be "OUT_OF_STOCK"

  Scenario: Update product stock successfully using StockUpdateDTO
    Given a product "Tablet" with stock 20 and minimum stock 5
    When I update the product stock to 30
    Then the product stock should be 30
    And the product should not be flagged as low stock
    And the stock status should be "IN_STOCK"

  Scenario: Update product stock to low level using StockUpdateDTO
    Given a product "Headphones" with stock 25 and minimum stock 10
    When I update the product stock to 8
    Then the product stock should be 8
    And the product should be flagged as low stock
    And the stock status should be "LOW_STOCK"

  Scenario: Update product stock with specific reason
    Given a product "Smartphone" with stock 15 and minimum stock 5
    When I update the product stock to 25 with reason "Monthly restock"
    Then the product stock should be 25
    And the stock update reason should be "Monthly restock"
    And the stock status should be "IN_STOCK"

  Scenario: Update product stock to zero creates out of stock status
    Given a product "Camera" with stock 10 and minimum stock 3
    When I update the product stock to 0
    Then the product stock should be 0
    And the product should be flagged as out of stock
    And the stock status should be "OUT_OF_STOCK"

  Scenario: Product at exact minimum stock threshold is considered low stock
    Given a product "Printer" with stock 5 and minimum stock 5
    When I check the product stock status
    Then the low stock flag should be true
    And the out of stock flag should be false
    And the stock status should be "LOW_STOCK"

  Scenario Outline: Check stock status for different scenarios with ProductResponseDTO
    Given a product "TestProduct" with stock <current_stock> and minimum stock <min_stock>
    When I check the product stock status
    Then the low stock flag should be <low_stock_expected>
    And the out of stock flag should be <out_of_stock_expected>
    And the stock status should be "<status_expected>"

    Examples:
      | current_stock | min_stock | low_stock_expected | out_of_stock_expected | status_expected |
      | 0             | 5         | false              | true                  | OUT_OF_STOCK    |
      | 3             | 10        | true               | false                 | LOW_STOCK       |
      | 15            | 10        | false              | false                 | IN_STOCK        |
      | 10            | 10        | true               | false                 | LOW_STOCK       |
      | 1             | 5         | true               | false                 | LOW_STOCK       |
      | 20            | 3         | false              | false                 | IN_STOCK        |

  Scenario: Verify DTO consistency between service and repository
    Given a product "TestConsistency" with stock 8 and minimum stock 12
    When I check the product stock status
    Then the product should be flagged as low stock
    And the product should have stock status "LOW_STOCK"

  Scenario: Update stock and verify all DTOs are synchronized
    Given a product "SyncTest" with stock 20 and minimum stock 10
    When I update the product stock to 5
    Then the product stock should be 5
    And the product should be flagged as low stock
    And the product should have stock status "LOW_STOCK"