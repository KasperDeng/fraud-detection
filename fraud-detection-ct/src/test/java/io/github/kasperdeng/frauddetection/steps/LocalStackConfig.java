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

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@TestConfiguration
@SuppressWarnings("unused")
public class LocalStackConfig {

  @Bean(initMethod = "start", destroyMethod = "stop")
  public LocalStackContainer localStackContainer() {
    return new LocalStackContainer(
        DockerImageName.parse("localstack/localstack:3.8.0"))
        .withExposedPorts(4566)
        .withServices(LocalStackContainer.Service.SQS)
        .withCreateContainerCmdModifier(cmd -> cmd.getHostConfig().withPortBindings(
        new PortBinding(Ports.Binding.bindPort(4566), new ExposedPort(4566))));
  }

  @Bean
  public SqsClient sqsClient(LocalStackContainer localStackContainer) {
    return SqsClient.builder()
        .endpointOverride(localStackContainer.getEndpointOverride(LocalStackContainer.Service.SQS))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(localStackContainer.getAccessKey(),
                localStackContainer.getSecretKey())
        ))
        .region(Region.of(localStackContainer.getRegion()))
        .build();
  }

}
