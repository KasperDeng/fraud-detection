/*******************************************************************************
 * COPYRIGHT KASPERDENG 2025
 * The copyright to the computer program(s) herein is the property of
 * Kasperdeng (https://github.com/KasperDeng).
 * The programs may be used and/or copied only with written
 * permission from Kasperdeng or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************/

package io.github.kasperdeng.frauddetection.config;


import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClientBuilder;

@Configuration
@SuppressWarnings("unused")
public class SQSConfig {

  @Value("${cloud.aws.region.static}")
  private String region;

  @Value("${cloud.aws.credentials.access-key}")
  private String awsAccessKey;

  @Value("${cloud.aws.credentials.secret-key}")
  private String awsSecretKey;

  @Value("${cloud.aws.queue.transactions}")
  private String transactionsQueue;

  @Value("${cloud.aws.queue.endpoint:}")
  private String awsEndpoint;

  @Bean
  public SqsAsyncClient sqsAsyncClient() {
    SqsAsyncClientBuilder builder = SqsAsyncClient.builder()
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(awsAccessKey, awsSecretKey)
            )
        ).region(Region.of(region));

    if (StringUtils.hasLength(awsEndpoint)) {
      builder.endpointOverride(URI.create(awsEndpoint));
    }
    return builder.build();
  }
}
