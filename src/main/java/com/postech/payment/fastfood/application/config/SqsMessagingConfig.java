package com.postech.payment.fastfood.application.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class SqsMessagingConfig {

//    @Value("${spring.cloud.aws.sqs.endpoint}")
//    private String sqsEndpoint;
//
//    @Value("${spring.cloud.aws.region.static}")
//    private String region;
//
//    @Value("${spring.cloud.aws.credentials.access-key}")
//    private String accessKey;
//
//    @Value("${spring.cloud.aws.credentials.secret-key}")
//    private String secretKey;
//
//    @Bean
//    public SqsAsyncClient sqsAsyncClient() {
//        return SqsAsyncClient.builder()
//                .endpointOverride(URI.create(sqsEndpoint))
//                .region(Region.of(region))
//                .credentialsProvider(
//                        StaticCredentialsProvider.create(
//                                AwsBasicCredentials.create(accessKey, secretKey)
//                        )
//                )
//                .build();
//    }

}
