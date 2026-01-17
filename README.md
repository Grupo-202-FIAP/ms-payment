# ms-payment-fastfood

A Spring Boot microservice for managing payment processing in a fast food ordering system. Built with hexagonal architecture principles, this service handles payment generation, QR code creation, payment notifications, and integrates with MercadoPago, AWS SQS/SNS, and PostgreSQL.

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Requirements](#requirements)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Infrastructure](#infrastructure)
- [Deployment](#deployment)
- [Monitoring](#monitoring)
- [License](#license)

## ✨ Features

- **Payment Processing**: Generate and manage payment requests with QR code generation
- **MercadoPago Integration**: Full integration with MercadoPago API for payment processing
- **Webhook Handling**: Secure webhook endpoint for payment notifications with signature validation
- **Event-Driven Architecture**: Asynchronous event handling via AWS SQS for payment processing
- **Payment Callbacks**: Publish payment status updates to AWS SNS topics
- **Database Migrations**: Automated schema management with Flyway
- **Multi-Environment Support**: Separate configurations for dev, test, and production
- **Health Checks & Metrics**: Actuator endpoints with Prometheus metrics
- **Observability**: Structured logging with Logstash encoder and Datadog APM integration
- **Cloud-Native**: Kubernetes-ready with HPA, external secrets, and service mesh support

## 🏗️ Architecture

This microservice follows **Hexagonal Architecture (Ports & Adapters)** pattern:

```
src/main/java/com/postech/payment/fastfood/
├── domain/                    # Business logic layer
│   ├── model/                # Domain entities (Payment, QrCode, Order)
│   ├── enums/                # Domain enumerations (PaymentStatus, PaymentMethod)
│   └── services/             # Use case implementations
│       ├── GenerateQrCodePaymentUseCaseImpl
│       ├── ProcessPaymentNotificationUseCaseImpl
│       ├── FindPaymentByOrderIdUseCaseImpl
│       └── RollbackPaymentUseCaseImpl
├── application/              # Application layer
│   ├── ports/
│   │   ├── input/           # Use case interfaces
│   │   └── output/          # Repository & external service interfaces
│   └── mapper/              # DTOs and mappers
└── infrastructure/           # Infrastructure layer
    ├── adapters/
    │   ├── input/           # Controllers, webhooks, message consumers
    │   └── output/          # Repository implementations, message producers
    ├── persistence/         # JPA entities and repositories
    ├── http/                # External HTTP clients (MercadoPago)
    └── config/              # Spring configuration
```

### Core Use Cases

1. **Generate QR Code Payment**: Creates payment request and generates QR code via MercadoPago
2. **Process Payment Notification**: Handles webhook notifications and updates payment status
3. **Find Payment by Order ID**: Retrieves payment information for a specific order
4. **Rollback Payment**: Handles payment cancellation and rollback scenarios

## 🛠️ Technology Stack

### Core Framework
- **Spring Boot 4.0.0** - Application framework
- **Java 17** - Programming language
- **Maven 3.9+** - Dependency management

### Database
- **PostgreSQL 17** - Primary database
- **Flyway** - Database migration tool
- **Spring Data JPA** - ORM layer
- **H2** - In-memory database for testing

### AWS Services
- **AWS SQS** - Message queue for payment processing
- **AWS SNS** - Topic for payment status callbacks
- **AWS SSM Parameter Store** - Secret management (production)
- **LocalStack** - Local AWS service emulation

### External Integrations
- **MercadoPago API** - Payment gateway
- **Spring Cloud OpenFeign** - HTTP client

### Testing
- **JUnit 5** - Unit testing framework
- **Cucumber BDD** - Behavior-driven development
- **Testcontainers** - Integration testing with Docker
- **WireMock** - API mocking
- **Mockito** - Mocking framework

### Observability
- **Spring Boot Actuator** - Health checks and metrics
- **Micrometer** - Metrics collection
- **Prometheus** - Metrics exposition
- **Logback with Logstash Encoder** - Structured logging
- **Datadog APM** - Application performance monitoring

### Infrastructure
- **Docker** - Containerization
- **Kubernetes** - Container orchestration
- **Terraform** - Infrastructure as Code
- **Amazon ECR** - Container registry

## 📦 Requirements

- **Java 17+**
- **Maven 3.8+**
- **Docker & Docker Compose** (for local development)
- **AWS Account** (for production deployment)
- **kubectl** (for Kubernetes deployment)
- **Terraform 1.0+** (for infrastructure provisioning)

## 🚀 Getting Started

### Local Development

1. **Clone the repository:**
   ```bash
   git clone <repo-url>
   cd ms-payment-fastfood
   ```

2. **Start dependencies with Docker Compose:**
   ```bash
   docker-compose up -d
   ```
   This will start:
   - PostgreSQL database on port `5435`
   - LocalStack (SQS/SNS) on port `4566`

3. **Initialize LocalStack resources (optional):**
   ```bash
   ./local/init-aws.sh
   ```

4. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```
   
   Or with specific profile:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

5. **Access the application:**
   - Application: http://localhost:8091
   - Swagger UI: http://localhost:8091/swagger-ui.html
   - Health Check: http://localhost:8091/actuator/health
   - Prometheus Metrics: http://localhost:8091/actuator/prometheus

### Building Docker Image

```bash
docker build -t ms-payment-fastfood:latest .
```

### Running with Docker

```bash
docker run -p 8091:8091 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5435/fastfood-payment \
  ms-payment-fastfood:latest
```

## ⚙️ Configuration

### Environment Variables

#### Application Settings
- `SPRING_PROFILES_ACTIVE` - Active profile (dev/test/prod) - Default: `dev`
- `SERVER_PORT` - Server port - Default: `8091`

#### Database Configuration
- `SPRING_DATASOURCE_URL` - Database connection URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password

#### MercadoPago Configuration
- `MERCADO_PAGO_BASE_URL` - MercadoPago API base URL - Default: `https://api.mercadopago.com`
- `PUBLIC_KEY` - MercadoPago public key
- `ACCESS_TOKEN` - MercadoPago access token
- `CLIENT_ID` - MercadoPago client ID
- `CLIENT_SECRET` - MercadoPago client secret
- `EXTERNAL_POS_ID` - External POS identifier
- `WEBHOOK_SECRET` - Webhook signature validation secret

#### AWS Configuration
- `AWS_REGION` - AWS region - Default: `us-east-1`
- `AWS_ACCESS_KEY` - AWS access key ID
- `AWS_SECRET_KEY` - AWS secret access key
- `PAYMENT_QUEUE_NAME` - SQS queue name for payment processing (production only)

### Profiles

#### Development (`dev`)
- Uses LocalStack for AWS services
- Local PostgreSQL database on port 5435
- SQL logging enabled
- Auto-creates/updates database schema

#### Test (`test`)
- H2 in-memory database
- Testcontainers for integration tests
- Isolated test environment

#### Production (`prod`)
- Real AWS services (SQS, SNS, SSM)
- RDS PostgreSQL database
- Flyway migrations only
- Schema validation mode
- Optimized logging

## 📡 API Documentation

### Payment Endpoints

#### Get Payment by Order ID
```http
GET /payment/{orderId}
```

**Description**: Retrieves payment information for a specific order.

**Path Parameters:**
- `orderId` (UUID) - The order identifier

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "orderId": "123e4567-e89b-12d3-a456-426614174000",
  "amount": 45.50,
  "status": "PROCESSED",
  "paymentMethod": "QR_CODE",
  "paymentDateTime": "2026-01-17T10:30:00",
  "updatedAt": "2026-01-17T10:35:00",
  "qrCodeId": "789e0123-e89b-12d3-a456-426614174000"
}
```

**Response (404 Not Found):**
```json
{
  "timestamp": "2026-01-17T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Payment not found for orderId: 123e4567-e89b-12d3-a456-426614174000"
}
```

**Example:**
```bash
curl -X GET http://localhost:8091/payment/123e4567-e89b-12d3-a456-426614174000
```

### Webhook Endpoints

#### MercadoPago Payment Notification
```http
POST /webhook/mercadopago/notification
```

**Description**: Receives payment status notifications from MercadoPago.

**Headers:**
- `x-signature` - MercadoPago webhook signature
- `x-request-id` - Request identifier

**Request Body:**
```json
{
  "action": "payment.updated",
  "api_version": "v1",
  "data": {
    "id": "1234567890"
  },
  "date_created": "2026-01-17T10:30:00Z",
  "id": 123456789,
  "live_mode": false,
  "type": "payment",
  "user_id": "987654321"
}
```

**Response (200 OK):**
```json
{
  "status": "received"
}
```

### Swagger UI

Interactive API documentation is available at:
```
http://localhost:8091/swagger-ui.html
```

### Health Check Endpoints

```bash
# Application health
curl http://localhost:8091/actuator/health

# Detailed health (shows DB, disk space, etc.)
curl http://localhost:8091/actuator/health?show-details=true

# Application info
curl http://localhost:8091/actuator/info

# Prometheus metrics
curl http://localhost:8091/actuator/prometheus
```

## 🧪 Testing

### Test Structure

```
src/test/
├── java/
│   └── com/postech/payment/fastfood/
│       ├── application/mapper/        # Mapper unit tests
│       ├── domain/services/           # Use case unit tests
│       ├── infrastructure/
│       │   ├── adapters/             # Integration tests
│       │   └── bdd/                  # Cucumber step definitions
│       └── support/                  # Test utilities and factories
└── resources/
    ├── features/                     # Cucumber BDD scenarios
    │   ├── find-payment.feature
    │   ├── generate-qrcode.feature
    │   ├── process-notification.feature
    │   └── rollback-payment.feature
    └── application-test.yml          # Test configuration
```

### Running Tests

**All tests:**
```bash
./mvnw test
```

**Unit tests only:**
```bash
./mvnw test -Dtest=*UnitTest
```

**Integration tests:**
```bash
./mvnw test -Dtest=*IntegrationTest
```

**BDD tests:**
```bash
./mvnw test -Dtest=CucumberRunnerTest
```

**With coverage report:**
```bash
./mvnw clean verify
```

### Test Technologies

- **JUnit 5**: Core testing framework
- **Mockito**: Mocking dependencies
- **AssertJ**: Fluent assertions
- **Cucumber**: BDD scenarios in Gherkin
- **Testcontainers**: PostgreSQL and LocalStack containers
- **WireMock**: Mock external APIs (MercadoPago)
- **Spring Boot Test**: Integration test support

### Cucumber BDD Features

Example feature file:
```gherkin
Feature: Generate QR Code Payment

  Scenario: Successfully generate QR code for valid order
    Given an order with id "123e4567-e89b-12d3-a456-426614174000"
    And the order total is 45.50 BRL
    When I request QR code generation
    Then a payment should be created with status "PENDING"
    And a QR code should be generated
    And the QR code should expire in 30 minutes
```

### Test Reports

After running tests, view the Cucumber HTML report:
```
target/cucumber-reports/cucumber.html
```

## 🏢 Infrastructure

### Kubernetes Deployment

The service includes complete Kubernetes manifests in `infra/k8s/`:

```
infra/k8s/
├── deployment.yaml              # Application deployment
├── service.yaml                 # Service definition
├── configmap.yaml              # Non-sensitive configuration
├── hpa.yaml                    # Horizontal Pod Autoscaler
├── service-account.yaml        # Service account for IRSA
├── externalsecret.yaml         # External Secrets Operator config
├── external-secrets-role.yaml  # IAM role for secrets access
├── external-secrets-binding.yaml # Role binding
├── clustersecretstore-ssm.yaml # SSM Parameter Store integration
└── db/                         # Database manifests
```

**Deploy to Kubernetes:**
```bash
# Create namespace
kubectl create namespace fastfood

# Apply manifests
kubectl apply -f infra/k8s/ -n fastfood

# Check deployment status
kubectl get pods -n fastfood
kubectl get svc -n fastfood

# View logs
kubectl logs -f deployment/ms-payment-fastfood -n fastfood
```

### Terraform Infrastructure

Infrastructure as Code for AWS resources in `infra/terraform/`:

```
infra/terraform/
├── provider.tf          # AWS provider configuration
├── variables.tf         # Input variables
├── outputs.tf          # Output values
├── data.tf             # Data sources
├── ecr.tf              # ECR repository
├── iam-policy-sqs.tf   # SQS access policies
├── iam-policy-ssm.tf   # SSM Parameter Store policies
└── irsa.tf             # IAM Roles for Service Accounts
```

**Provision infrastructure:**
```bash
cd infra/terraform

# Initialize Terraform
terraform init

# Plan changes
terraform plan -var="environment=prod" -var="region=us-east-1"

# Apply infrastructure
terraform apply -var="environment=prod" -var="region=us-east-1"

# View outputs
terraform output
```

### Docker Compose

For local development, `docker-compose.yml` provides:

- **PostgreSQL 17**: Payment database
- **LocalStack**: AWS services emulation (SQS, SNS)

```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Clean volumes
docker-compose down -v
```

## 🚢 Deployment

### Local Deployment

1. Start infrastructure:
   ```bash
   docker-compose up -d
   ```

2. Run application:
   ```bash
   ./mvnw spring-boot:run
   ```

### Docker Deployment

1. Build image:
   ```bash
   docker build -t ms-payment-fastfood:1.0.0 .
   ```

2. Push to registry:
   ```bash
   docker tag ms-payment-fastfood:1.0.0 <your-registry>/ms-payment-fastfood:1.0.0
   docker push <your-registry>/ms-payment-fastfood:1.0.0
   ```

### Kubernetes Deployment

1. **Create secrets in AWS SSM Parameter Store:**
   ```bash
   aws ssm put-parameter --name "/fastfood/payment/db-password" --value "your-password" --type "SecureString"
   aws ssm put-parameter --name "/fastfood/payment/mercadopago-access-token" --value "your-token" --type "SecureString"
   # ... add other secrets
   ```

2. **Update image in deployment:**
   ```bash
   kubectl set image deployment/ms-payment-fastfood \
     ms-payment-fastfood=<your-registry>/ms-payment-fastfood:1.0.0 \
     -n fastfood
   ```

3. **Monitor rollout:**
   ```bash
   kubectl rollout status deployment/ms-payment-fastfood -n fastfood
   ```

4. **Check health:**
   ```bash
   kubectl exec -it deployment/ms-payment-fastfood -n fastfood -- \
     curl http://localhost:8091/actuator/health
   ```

### Production Checklist

- [ ] All secrets stored in AWS SSM Parameter Store
- [ ] External Secrets Operator installed in cluster
- [ ] IRSA configured for service account
- [ ] Database migrated with Flyway
- [ ] HPA configured based on load patterns
- [ ] Prometheus metrics being scraped
- [ ] Datadog APM agent configured
- [ ] MercadoPago webhook URL registered
- [ ] SQS queue created and configured
- [ ] SNS topic created with subscribers
- [ ] CloudWatch alarms configured
- [ ] Backup strategy in place

## 📊 Monitoring

### Health Checks

**Liveness Probe:**
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8091
  initialDelaySeconds: 30
  periodSeconds: 10
```

**Readiness Probe:**
```yaml
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8091
  initialDelaySeconds: 20
  periodSeconds: 5
```

### Metrics

Prometheus metrics available at `/actuator/prometheus`:

- **JVM Metrics**: Memory, threads, garbage collection
- **HTTP Metrics**: Request count, duration, status codes
- **Database Metrics**: Connection pool, query performance
- **SQS Metrics**: Message processing, queue depth
- **Custom Metrics**: Payment processing rates, success/failure rates

### Logging

Structured JSON logging with Logstash encoder:

```json
{
  "@timestamp": "2026-01-17T10:30:00.123Z",
  "level": "INFO",
  "logger": "com.postech.payment.fastfood.domain.services",
  "message": "[UseCase][GenerateQrCode] QR code generated successfully",
  "thread": "http-nio-8091-exec-1",
  "orderId": "123e4567-e89b-12d3-a456-426614174000",
  "paymentId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Datadog APM

Application includes Datadog Java agent for distributed tracing:

```bash
# Agent loaded via JAVA_TOOL_OPTIONS in Dockerfile
-javaagent:/dd-java-agent.jar
```

Configure via environment variables:
- `DD_SERVICE=ms-payment-fastfood`
- `DD_ENV=production`
- `DD_VERSION=1.0.0`
- `DD_AGENT_HOST=<datadog-agent-host>`

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

This project uses Checkstyle for code quality. Configuration: `checkstyle.xml`

Run checkstyle:
```bash
./mvnw checkstyle:check
```

## 📄 License

MIT
