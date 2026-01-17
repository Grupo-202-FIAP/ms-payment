package integration.config;

import com.postech.payment.fastfood.PaymentFastfoodApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(
        classes = {
                PaymentFastfoodApplication.class,
                TestConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.profiles.active=test",
                "spring.datasource.url=jdbc:h2:mem:bddtestdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.flyway.enabled=false",
                "mercadoPago.publicKey=test-key",
                "mercadoPago.accessToken=test-token",
                "mercadoPago.clientId=test-id",
                "mercadoPago.clientSecret=test-secret",
                "mercadoPago.externalPosID=test-pos",
                "mercadoPago.webhook.secretKey=test-webhook",
                "spring.cloud.aws.region.static=us-east-1",
                "spring.cloud.aws.credentials.access-key=test",
                "spring.cloud.aws.credentials.secret-key=test",
                "spring.cloud.aws.sqs.endpoint=http://localhost:4566",
                "spring.cloud.aws.sqs.queues.process-payment-queue=test-queue"
        }
)
@ActiveProfiles("test")
public class CucumberSpringConfig {
}

