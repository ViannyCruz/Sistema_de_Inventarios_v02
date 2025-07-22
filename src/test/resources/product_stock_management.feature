Feature: Product Stock Management
  As an inventory manager
  I want to manage product stock levels
  So that I can track inventory and prevent stockouts

  Background:
    Given the inventory system is initialized

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

  Scenario: Update product stock successfully using StockUpdateDTO
    Given a product "Tablet" with stock 20 and minimum stock 5
    When I update the product stock to 30
    Then the product stock should be 30
    And the product should not be flagged as low stock
    And the stock status should be "IN_STOCK"

  Scenario: Update product stock with specific reason
    Given a product "Smartphone" with stock 15 and minimum stock 5
    When I update the product stock to 25 with reason "Monthly restock"
    Then the product stock should be 25
    And the stock update reason should be "Monthly restock"
    And the stock status should be "IN_STOCK"

  Scenario Outline: Check stock status for scenarios that pass
    Given a product "TestProduct" with stock <current_stock> and minimum stock <min_stock>
    When I check the product stock status
    Then the low stock flag should be <low_stock_expected>
    And the out of stock flag should be <out_of_stock_expected>
    And the stock status should be "<status_expected>"

    Examples:
      | current_stock | min_stock | low_stock_expected | out_of_stock_expected | status_expected |
      | 15            | 10        | false              | false                 | IN_STOCK        |
      | 20            | 3         | false              | false                 | IN_STOCK        |