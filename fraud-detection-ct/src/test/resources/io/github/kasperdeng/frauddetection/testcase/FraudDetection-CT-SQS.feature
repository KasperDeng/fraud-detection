@fraud-detection
Feature: Fraud Detection on SQS transaction
  As Fraud Detection, it shall do fraud detection on transaction from SQS.

  Background: environment is available
    Given The AWS SQS is available for service

  Scenario Outline: Fraud Detection on transaction from SQS

    When SQS receive a transaction with <postContent>
    Then Fraud Detection analyze the transaction and consume the message
    Then Fraud Analyst reset the case

    Examples:
      | postContent                                                                                                                                                    |
      | {"transactionId": "TXN-123","accountId": "ACCT-001", "amount": 100.00,"merchant": "Safe Merchant","location": "New York","timestamp": "2023-10-01T12:00:00"}   |
      | {"transactionId": "TXN-456","accountId": "ACCT-002", "amount": 15000.00,"merchant": "Safe Merchant","location": "New York","timestamp": "2023-10-01T12:00:00"} |
      | {"transactionId": "TXN-789","accountId": "ACCT-003", "amount": 50.00,"merchant": "Safe Merchant","location": "New York","timestamp": "2023-10-01T12:00:00"}    |
