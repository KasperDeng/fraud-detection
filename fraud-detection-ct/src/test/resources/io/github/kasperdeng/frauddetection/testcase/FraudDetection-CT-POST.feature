@fraud-detection
Feature: Fraud Detection API - POST
  As a Fraud Analyst, I hope Fraud Detection API can be used for fraud detection.

  Scenario Outline: Fraud Detection API - POST
    When Fraud Analyst send the http POST request to Fraud Detection API with <postContent>
    Then Fraud Analyst should get a fraud detection result <fraudDetectionResult>
    Then Fraud Analyst reset the case

    Examples:
      | postContent                                                                                                                                                    | fraudDetectionResult                                                                                               |
      | {"transactionId": "TXN-123","accountId": "ACCT-001", "amount": 100.00,"merchant": "Safe Merchant","location": "New York","timestamp": "2023-10-01T12:00:00"}   | {"transactionId": "TXN-123", "fraudulent": "false","reason": "Transaction appears legitimate"}                     |
      | {"transactionId": "TXN-456","accountId": "ACCT-002", "amount": 15000.00,"merchant": "Safe Merchant","location": "New York","timestamp": "2023-10-01T12:00:00"} | {"transactionId": "TXN-456", "fraudulent": "true","reason": "Transaction amount 15000.00 exceeds threshold 10000"} |
      | {"transactionId": "TXN-789","accountId": "ACCT-003", "amount": 50.00,"merchant": "Safe Merchant","location": "New York","timestamp": "2023-10-01T12:00:00"}    | {"transactionId": "TXN-789", "fraudulent": "true","reason": "Account ACCT-003 is in suspicious accounts list"}     |
