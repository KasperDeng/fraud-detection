/*******************************************************************************
 * COPYRIGHT KASPERDENG 2025
 * The copyright to the computer program(s) herein is the property of
 * Kasperdeng (https://github.com/KasperDeng).
 * The programs may be used and/or copied only with written
 * permission from Kasperdeng or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package io.github.kasperdeng.frauddetection.steps;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.kasperdeng.frauddetection.model.FraudDetectionResult;
import io.github.kasperdeng.frauddetection.model.Transaction;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.Future;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.awaitility.Awaitility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueAttributesRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Testcontainers
@Import(LocalStackConfig.class)
@SuppressWarnings("unused")
public class FraudDetectionSteps {

  private static CloseableHttpAsyncClient httpClient;

  private Future<HttpResponse> httpResponseFuture;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private SqsClient sqsClient;

  @Autowired
  private SqsAsyncClient sqsAsyncClient;

  @Autowired
  private LocalStackContainer localStackContainer;

  private String queueUrl;

  @Given("The AWS SQS is available for service")
  public void givenTheAwsSqsIsAvailableForService() throws InterruptedException {
    assertThat(localStackContainer.isRunning()).isTrue();
    sleep(6000);

    sqsClient.createQueue(CreateQueueRequest.builder()
        .queueName("transaction-queue")
        .build());

    queueUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder()
            .queueName("transaction-queue")
            .build()).queueUrl();
  }

  @When("Fraud Analyst send the http POST request to Fraud Detection API with (.+)$")
  public void sendHttpPostToFraudDetectionApi(String postContent) {
    HttpRequestBase request = createRequest(postContent);
    httpResponseFuture = asyncHttpClient().execute(request, null);
  }

  @Then("Fraud Analyst should get a fraud detection result (.+)$")
  public void getFraudDetectionResult(String fraudDetectionResult) {
    try {
      HttpResponse httpResponse = httpResponseFuture.get();
      HttpEntity httpEntity = httpResponse.getEntity();
      String responseBody = EntityUtils.toString(httpEntity);
      FraudDetectionResult result = objectMapper.readValue(responseBody,
          FraudDetectionResult.class);
      FraudDetectionResult expectedResult = objectMapper.readValue(fraudDetectionResult,
          FraudDetectionResult.class);
      assertThat(result.getTransactionId()).isEqualTo(expectedResult.getTransactionId());
      assertThat(result.isFraudulent()).isEqualTo(expectedResult.isFraudulent());
      assertThat(result.getReason()).isEqualTo(expectedResult.getReason());

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Then("Fraud Analyst reset the case")
  public void reset() {
    httpResponseFuture = null;
  }

  @When("SQS receive a transaction with (.+)$")
  public void receiveTransaction(String postContent) {
    SendMessageResponse response = sqsClient.sendMessage(SendMessageRequest.builder()
        .queueUrl(queueUrl)
        .messageBody(postContent)
        .build());
    System.out.println("queueUrl " + queueUrl);
    System.out.println("SQS receive a transaction with response " + response);
  }

  @Then("Fraud Detection analyze the transaction and consume the message$")
  public void analyzeTransaction() {
    Awaitility.await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() -> {
          GetQueueAttributesRequest request = GetQueueAttributesRequest.builder()
              .queueUrl(queueUrl)
              .attributeNames(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES)
              .build();

          int messageCount = Integer.parseInt(
              sqsClient.getQueueAttributes(request)
                  .attributes()
                  .get(QueueAttributeName.APPROXIMATE_NUMBER_OF_MESSAGES)
          );
          assertThat(messageCount).isEqualTo(0);
        });
  }

  static CloseableHttpAsyncClient asyncHttpClient() {
    if (httpClient == null) {
      RequestConfig requestConfig =
          RequestConfig.custom().setSocketTimeout(120 * 1000).setConnectTimeout(120 * 1000).build();

      httpClient = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig).build();
      httpClient.start();
    }
    return httpClient;
  }

  private HttpRequestBase createRequest(String postContent) {
    HttpEntityEnclosingRequestBase httpRequest;

    ArrayList<Header> headers = new ArrayList<>();
    headers.add(new BasicHeader("Content-Type", "application/json"));

    httpRequest = new HttpPost("http://localhost:8080/api/v1/fraud-detection/analyze");
    httpRequest.setHeaders(headers.toArray(new Header[0]));
    httpRequest.setProtocolVersion(HttpVersion.HTTP_1_0);
    if (StringUtils.hasLength(postContent)) {
      httpRequest.setEntity(new ByteArrayEntity(postContent.getBytes()));
    }
    return httpRequest;
  }
}
