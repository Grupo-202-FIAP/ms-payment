package com.postech.payment.fastfood.infrastructure.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import io.awspring.cloud.sns.core.SnsTemplate;

@Configuration
public class SnsMessagingConfig {

    @Value("${spring.cloud.aws.region.static:us-east-1}")
    private String region;

    @Bean
    @Profile("local")
    public SnsClient snsClientLocal(
            @Value("${spring.cloud.aws.sqs.endpoint}") String endpoint
    ) {
        return SnsClient.builder()
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
    @Profile("!local")
    public SnsClient snsClientEks() {
        return SnsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    public SnsTemplate snsTemplate(SnsClient snsClient) {
        return new SnsTemplate(snsClient);
    }
}
