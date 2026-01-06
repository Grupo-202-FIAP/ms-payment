package com.postech.payment.fastfood.infrastructure.config;

import java.net.URI;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class SqsConfig {

    @Value("${spring.cloud.aws.region.static:us-east-1}")
    private String region;

    @Bean
    @Profile({"local", "dev"})
    public SqsAsyncClient sqsAsyncClientLocal(
            @Value("${spring.cloud.aws.sqs.endpoint}") String endpoint
    ) {
        return SqsAsyncClient.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("test", "test")
                        )
                )
                .build();
    }

    @Bean
    @Profile({"local", "dev"})
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(SqsAsyncClient sqsAsyncClient) {
        return SqsMessageListenerContainerFactory.builder()
                .sqsAsyncClient(sqsAsyncClient)
                .build();
    }

    @Bean
    @Profile("!local & !dev")
    public SqsAsyncClient sqsAsyncClientEks() {
        return SqsAsyncClient.builder()
                .region(Region.of(region))
                .build();
    }

    @Bean
    @Profile("!local & !dev")
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactoryEks(SqsAsyncClient sqsAsyncClient) {
        return SqsMessageListenerContainerFactory.builder()
                .sqsAsyncClient(sqsAsyncClient)
                .build();
    }
}

