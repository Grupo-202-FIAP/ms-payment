# ms-payment-fastfood

A Spring Boot microservice for managing payment processing in a fast food ordering system. Integrates with AWS SQS,
MercadoPago, and PostgreSQL.

## Features

- Payment processing and QR code generation
- Integration with MercadoPago for payment requests
- Asynchronous event handling via AWS SQS
- Database migrations with Flyway
- Environment-specific configuration (dev/prod)
- Health checks and monitoring endpoints

## Requirements

- Java 17+
- Maven 3.8+
- Docker (for local development)
- AWS account (for production)

## Getting Started

### Local Development

1. **Clone the repository:**
   ```sh
   git clone <repo-url>
   cd ms-payment-fastfood
   ```
2. **Start dependencies with Docker Compose:**
   ```sh
   docker-compose up -d
   ```
3. **Run the application:**
   ```sh
   ./mvnw spring-boot:run
   ```

### Configuration

- All sensitive values are set via environment variables. See `src/main/resources/application.yml` for details.
- Profiles: `dev` (default, uses localstack and test DB), `prod` (for production).

### Environment Variables

- `SPRING_PROFILES_ACTIVE` (default: dev)
- `DB_URL`, `DB_USER`, `DB_PASS`
- `PUBLIC_KEY`, `ACCESS_TOKEN`, `CLIENT_ID`, `CLIENT_SECRET`, `EXTERNAL_POS_ID`, `WEBHOOK_SECRET`
- `AWS_REGION`, `AWS_ACCESS_KEY`, `AWS_SECRET_KEY`
- `PAYMENT_QUEUE_NAME` (for SQS queue name in prod)

## Endpoints

- Application runs on port `8091` by default (configurable via the `SERVER_PORT` environment variable)
- Swagger UI: `/swagger-ui.html`
- Health: `/actuator/health`
- Prometheus: `/actuator/prometheus`

## Testing

- Unit and integration tests are under `src/test/java`
- Run tests with:
  ```sh
  ./mvnw test
  ```

## Deployment

- For production, ensure all environment variables are set and AWS credentials are configured (preferably via IAM
  roles).
- Use `application-prod.yml` for production settings.

## License

MIT
