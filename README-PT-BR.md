# ms-payment-fastfood

Um microsserviço Spring Boot para gerenciar o processamento de pagamentos em um sistema de pedidos de fast food. Construído com princípios de arquitetura hexagonal, este serviço gerencia a geração de pagamentos, criação de QR code, notificações de pagamento e se integra com MercadoPago, AWS SQS/SNS e PostgreSQL.

## 📋 Índice

- [Funcionalidades](#funcionalidades)
- [Arquitetura](#arquitetura)
- [Stack Tecnológica](#stack-tecnológica)
- [Requisitos](#requisitos)
- [Começando](#começando)
- [Configuração](#configuração)
- [Documentação da API](#documentação-da-api)
- [Testes](#testes)
- [Infraestrutura](#infraestrutura)
- [Implantação](#implantação)
- [Monitoramento](#monitoramento)
- [Licença](#licença)

## ✨ Funcionalidades

- **Processamento de Pagamentos**: Gera e gerencia solicitações de pagamento com geração de QR code
- **Integração MercadoPago**: Integração completa com API do MercadoPago para processamento de pagamentos
- **Manipulação de Webhooks**: Endpoint seguro de webhook para notificações de pagamento com validação de assinatura
- **Arquitetura Orientada a Eventos**: Manipulação assíncrona de eventos via AWS SQS para processamento de pagamentos
- **Callbacks de Pagamento**: Publica atualizações de status de pagamento em tópicos AWS SNS
- **Migrações de Banco de Dados**: Gerenciamento automatizado de schema com Flyway
- **Suporte Multi-Ambiente**: Configurações separadas para dev, test e production
- **Health Checks & Métricas**: Endpoints Actuator com métricas Prometheus
- **Observabilidade**: Logging estruturado com encoder Logstash e integração Datadog APM
- **Cloud-Native**: Pronto para Kubernetes com HPA, external secrets e suporte a service mesh

## 🏗️ Arquitetura

Este microsserviço segue o padrão de **Arquitetura Hexagonal (Ports & Adapters)**:

```
src/main/java/com/postech/payment/fastfood/
├── domain/                    # Camada de lógica de negócio
│   ├── model/                # Entidades de domínio (Payment, QrCode, Order)
│   ├── enums/                # Enumerações de domínio (PaymentStatus, PaymentMethod)
│   └── services/             # Implementações de casos de uso
│       ├── GenerateQrCodePaymentUseCaseImpl
│       ├── ProcessPaymentNotificationUseCaseImpl
│       ├── FindPaymentByOrderIdUseCaseImpl
│       └── RollbackPaymentUseCaseImpl
├── application/              # Camada de aplicação
│   ├── ports/
│   │   ├── input/           # Interfaces de casos de uso
│   │   └── output/          # Interfaces de repositório e serviços externos
│   └── mapper/              # DTOs e mapeadores
└── infrastructure/           # Camada de infraestrutura
    ├── adapters/
    │   ├── input/           # Controllers, webhooks, consumidores de mensagens
    │   └── output/          # Implementações de repositório, produtores de mensagens
    ├── persistence/         # Entidades JPA e repositórios
    ├── http/                # Clientes HTTP externos (MercadoPago)
    └── config/              # Configuração Spring
```

### Casos de Uso Principais

1. **Gerar Pagamento com QR Code**: Cria solicitação de pagamento e gera QR code via MercadoPago
2. **Processar Notificação de Pagamento**: Manipula notificações de webhook e atualiza status do pagamento
3. **Buscar Pagamento por ID do Pedido**: Recupera informações de pagamento para um pedido específico
4. **Reverter Pagamento**: Manipula cancelamento de pagamento e cenários de reversão

## 🛠️ Stack Tecnológica

### Framework Core
- **Spring Boot 4.0.0** - Framework de aplicação
- **Java 17** - Linguagem de programação
- **Maven 3.9+** - Gerenciamento de dependências

### Banco de Dados
- **PostgreSQL 17** - Banco de dados principal
- **Flyway** - Ferramenta de migração de banco de dados
- **Spring Data JPA** - Camada ORM
- **H2** - Banco de dados em memória para testes

### Serviços AWS
- **AWS SQS** - Fila de mensagens para processamento de pagamentos
- **AWS SNS** - Tópico para callbacks de status de pagamento
- **AWS SSM Parameter Store** - Gerenciamento de segredos (produção)
- **LocalStack** - Emulação local de serviços AWS

### Integrações Externas
- **MercadoPago API** - Gateway de pagamento
- **Spring Cloud OpenFeign** - Cliente HTTP

### Testes
- **JUnit 5** - Framework de testes unitários
- **Cucumber BDD** - Desenvolvimento orientado a comportamento
- **Testcontainers** - Testes de integração com Docker
- **WireMock** - Mock de APIs
- **Mockito** - Framework de mocking

### Observabilidade
- **Spring Boot Actuator** - Health checks e métricas
- **Micrometer** - Coleta de métricas
- **Prometheus** - Exposição de métricas
- **Logback com Logstash Encoder** - Logging estruturado
- **Datadog APM** - Monitoramento de performance de aplicação

### Infraestrutura
- **Docker** - Containerização
- **Kubernetes** - Orquestração de containers
- **Terraform** - Infraestrutura como Código
- **Amazon ECR** - Registro de containers

## 📦 Requisitos

- **Java 17+**
- **Maven 3.8+**
- **Docker & Docker Compose** (para desenvolvimento local)
- **Conta AWS** (para deploy em produção)
- **kubectl** (para deploy Kubernetes)
- **Terraform 1.0+** (para provisionamento de infraestrutura)

## 🚀 Começando

### Desenvolvimento Local

1. **Clone o repositório:**
   ```bash
   git clone <repo-url>
   cd ms-payment-fastfood
   ```

2. **Inicie as dependências com Docker Compose:**
   ```bash
   docker-compose up -d
   ```
   Isso irá iniciar:
   - Banco de dados PostgreSQL na porta `5435`
   - LocalStack (SQS/SNS) na porta `4566`

3. **Inicialize os recursos do LocalStack (opcional):**
   ```bash
   ./local/init-aws.sh
   ```

4. **Execute a aplicação:**
   ```bash
   ./mvnw spring-boot:run
   ```
   
   Ou com perfil específico:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```

5. **Acesse a aplicação:**
   - Aplicação: http://localhost:8091
   - Swagger UI: http://localhost:8091/swagger-ui.html
   - Health Check: http://localhost:8091/actuator/health
   - Métricas Prometheus: http://localhost:8091/actuator/prometheus

### Construindo Imagem Docker

```bash
docker build -t ms-payment-fastfood:latest .
```

### Executando com Docker

```bash
docker run -p 8091:8091 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5435/fastfood-payment \
  ms-payment-fastfood:latest
```

## ⚙️ Configuração

### Variáveis de Ambiente

#### Configurações da Aplicação
- `SPRING_PROFILES_ACTIVE` - Perfil ativo (dev/test/prod) - Padrão: `dev`
- `SERVER_PORT` - Porta do servidor - Padrão: `8091`

#### Configuração do Banco de Dados
- `SPRING_DATASOURCE_URL` - URL de conexão do banco de dados
- `SPRING_DATASOURCE_USERNAME` - Nome de usuário do banco de dados
- `SPRING_DATASOURCE_PASSWORD` - Senha do banco de dados

#### Configuração do MercadoPago
- `MERCADO_PAGO_BASE_URL` - URL base da API do MercadoPago - Padrão: `https://api.mercadopago.com`
- `PUBLIC_KEY` - Chave pública do MercadoPago
- `ACCESS_TOKEN` - Token de acesso do MercadoPago
- `CLIENT_ID` - ID do cliente MercadoPago
- `CLIENT_SECRET` - Secret do cliente MercadoPago
- `EXTERNAL_POS_ID` - Identificador do POS externo
- `WEBHOOK_SECRET` - Secret para validação de assinatura do webhook

#### Configuração AWS
- `AWS_REGION` - Região AWS - Padrão: `us-east-1`
- `AWS_ACCESS_KEY` - ID da chave de acesso AWS
- `AWS_SECRET_KEY` - Chave de acesso secreta AWS
- `PAYMENT_QUEUE_NAME` - Nome da fila SQS para processamento de pagamentos (somente produção)

### Perfis

#### Desenvolvimento (`dev`)
- Usa LocalStack para serviços AWS
- Banco de dados PostgreSQL local na porta 5435
- Logging SQL habilitado
- Cria/atualiza schema do banco de dados automaticamente

#### Teste (`test`)
- Banco de dados H2 em memória
- Testcontainers para testes de integração
- Ambiente de teste isolado

#### Produção (`prod`)
- Serviços AWS reais (SQS, SNS, SSM)
- Banco de dados PostgreSQL RDS
- Somente migrações Flyway
- Modo de validação de schema
- Logging otimizado

## 📡 Documentação da API

### Endpoints de Pagamento

#### Buscar Pagamento por ID do Pedido
```http
GET /payment/{orderId}
```

**Descrição**: Recupera informações de pagamento para um pedido específico.

**Parâmetros de Path:**
- `orderId` (UUID) - O identificador do pedido

**Resposta (200 OK):**
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

**Resposta (404 Not Found):**
```json
{
  "timestamp": "2026-01-17T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Payment not found for orderId: 123e4567-e89b-12d3-a456-426614174000"
}
```

**Exemplo:**
```bash
curl -X GET http://localhost:8091/payment/123e4567-e89b-12d3-a456-426614174000
```

### Endpoints de Webhook

#### Notificação de Pagamento MercadoPago
```http
POST /webhook/mercadopago/notification
```

**Descrição**: Recebe notificações de status de pagamento do MercadoPago.

**Headers:**
- `x-signature` - Assinatura do webhook MercadoPago
- `x-request-id` - Identificador da requisição

**Corpo da Requisição:**
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

**Resposta (200 OK):**
```json
{
  "status": "received"
}
```

### Swagger UI

Documentação interativa da API disponível em:
```
http://localhost:8091/swagger-ui.html
```

### Endpoints de Health Check

```bash
# Saúde da aplicação
curl http://localhost:8091/actuator/health

# Saúde detalhada (mostra DB, espaço em disco, etc.)
curl http://localhost:8091/actuator/health?show-details=true

# Informações da aplicação
curl http://localhost:8091/actuator/info

# Métricas Prometheus
curl http://localhost:8091/actuator/prometheus
```

## 🧪 Testes

### Estrutura de Testes

```
src/test/
├── java/
│   └── com/postech/payment/fastfood/
│       ├── application/mapper/        # Testes unitários de mapeadores
│       ├── domain/services/           # Testes unitários de casos de uso
│       ├── infrastructure/
│       │   ├── adapters/             # Testes de integração
│       │   └── bdd/                  # Definições de passos Cucumber
│       └── support/                  # Utilitários de teste e factories
└── resources/
    ├── features/                     # Cenários BDD Cucumber
    │   ├── find-payment.feature
    │   ├── generate-qrcode.feature
    │   ├── process-notification.feature
    │   └── rollback-payment.feature
    └── application-test.yml          # Configuração de teste
```

### Executando Testes

**Todos os testes:**
```bash
./mvnw test
```

**Apenas testes unitários:**
```bash
./mvnw test -Dtest=*UnitTest
```

**Testes de integração:**
```bash
./mvnw test -Dtest=*IntegrationTest
```

**Testes BDD:**
```bash
./mvnw test -Dtest=CucumberRunnerTest
```

**Com relatório de cobertura:**
```bash
./mvnw clean verify
```

### Tecnologias de Teste

- **JUnit 5**: Framework principal de testes
- **Mockito**: Mock de dependências
- **AssertJ**: Asserções fluentes
- **Cucumber**: Cenários BDD em Gherkin
- **Testcontainers**: Containers PostgreSQL e LocalStack
- **WireMock**: Mock de APIs externas (MercadoPago)
- **Spring Boot Test**: Suporte a testes de integração

### Funcionalidades BDD Cucumber

Exemplo de arquivo de feature:
```gherkin
Feature: Gerar Pagamento com QR Code

  Scenario: Gerar QR code com sucesso para pedido válido
    Given um pedido com id "123e4567-e89b-12d3-a456-426614174000"
    And o total do pedido é 45.50 BRL
    When eu solicito a geração do QR code
    Then um pagamento deve ser criado com status "PENDING"
    And um QR code deve ser gerado
    And o QR code deve expirar em 30 minutos
```

### Relatórios de Teste

Após executar os testes, visualize o relatório HTML do Cucumber:
```
target/cucumber-reports/cucumber.html
```

## 🏢 Infraestrutura

### Deploy no Kubernetes

O serviço inclui manifestos completos do Kubernetes em `infra/k8s/`:

```
infra/k8s/
├── deployment.yaml              # Deploy da aplicação
├── service.yaml                 # Definição de serviço
├── configmap.yaml              # Configuração não sensível
├── hpa.yaml                    # Horizontal Pod Autoscaler
├── service-account.yaml        # Service account para IRSA
├── externalsecret.yaml         # Configuração External Secrets Operator
├── external-secrets-role.yaml  # IAM role para acesso a segredos
├── external-secrets-binding.yaml # Role binding
├── clustersecretstore-ssm.yaml # Integração SSM Parameter Store
└── db/                         # Manifestos do banco de dados
```

**Deploy no Kubernetes:**
```bash
# Criar namespace
kubectl create namespace fastfood

# Aplicar manifestos
kubectl apply -f infra/k8s/ -n fastfood

# Verificar status do deployment
kubectl get pods -n fastfood
kubectl get svc -n fastfood

# Visualizar logs
kubectl logs -f deployment/ms-payment-fastfood -n fastfood
```

### Infraestrutura Terraform

Infraestrutura como Código para recursos AWS em `infra/terraform/`:

```
infra/terraform/
├── provider.tf          # Configuração do provider AWS
├── variables.tf         # Variáveis de entrada
├── outputs.tf          # Valores de saída
├── data.tf             # Data sources
├── ecr.tf              # Repositório ECR
├── iam-policy-sqs.tf   # Políticas de acesso SQS
├── iam-policy-ssm.tf   # Políticas SSM Parameter Store
└── irsa.tf             # IAM Roles para Service Accounts
```

**Provisionar infraestrutura:**
```bash
cd infra/terraform

# Inicializar Terraform
terraform init

# Planejar mudanças
terraform plan -var="environment=prod" -var="region=us-east-1"

# Aplicar infraestrutura
terraform apply -var="environment=prod" -var="region=us-east-1"

# Visualizar outputs
terraform output
```

### Docker Compose

Para desenvolvimento local, `docker-compose.yml` fornece:

- **PostgreSQL 17**: Banco de dados de pagamentos
- **LocalStack**: Emulação de serviços AWS (SQS, SNS)

```bash
# Iniciar serviços
docker-compose up -d

# Visualizar logs
docker-compose logs -f

# Parar serviços
docker-compose down

# Limpar volumes
docker-compose down -v
```

## 🚢 Implantação

### Implantação Local

1. Iniciar infraestrutura:
   ```bash
   docker-compose up -d
   ```

2. Executar aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```

### Implantação Docker

1. Construir imagem:
   ```bash
   docker build -t ms-payment-fastfood:1.0.0 .
   ```

2. Enviar para registry:
   ```bash
   docker tag ms-payment-fastfood:1.0.0 <seu-registry>/ms-payment-fastfood:1.0.0
   docker push <seu-registry>/ms-payment-fastfood:1.0.0
   ```

### Implantação Kubernetes

1. **Criar segredos no AWS SSM Parameter Store:**
   ```bash
   aws ssm put-parameter --name "/fastfood/payment/db-password" --value "sua-senha" --type "SecureString"
   aws ssm put-parameter --name "/fastfood/payment/mercadopago-access-token" --value "seu-token" --type "SecureString"
   # ... adicionar outros segredos
   ```

2. **Atualizar imagem no deployment:**
   ```bash
   kubectl set image deployment/ms-payment-fastfood \
     ms-payment-fastfood=<seu-registry>/ms-payment-fastfood:1.0.0 \
     -n fastfood
   ```

3. **Monitorar rollout:**
   ```bash
   kubectl rollout status deployment/ms-payment-fastfood -n fastfood
   ```

4. **Verificar saúde:**
   ```bash
   kubectl exec -it deployment/ms-payment-fastfood -n fastfood -- \
     curl http://localhost:8091/actuator/health
   ```

### Checklist de Produção

- [ ] Todos os segredos armazenados no AWS SSM Parameter Store
- [ ] External Secrets Operator instalado no cluster
- [ ] IRSA configurado para service account
- [ ] Banco de dados migrado com Flyway
- [ ] HPA configurado com base nos padrões de carga
- [ ] Métricas Prometheus sendo coletadas
- [ ] Agente Datadog APM configurado
- [ ] URL do webhook MercadoPago registrada
- [ ] Fila SQS criada e configurada
- [ ] Tópico SNS criado com assinantes
- [ ] Alarmes CloudWatch configurados
- [ ] Estratégia de backup implementada

## 📊 Monitoramento

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

### Métricas

Métricas Prometheus disponíveis em `/actuator/prometheus`:

- **Métricas JVM**: Memória, threads, garbage collection
- **Métricas HTTP**: Contagem de requisições, duração, códigos de status
- **Métricas de Banco de Dados**: Pool de conexões, performance de queries
- **Métricas SQS**: Processamento de mensagens, profundidade da fila
- **Métricas Customizadas**: Taxas de processamento de pagamento, taxas de sucesso/falha

### Logging

Logging estruturado JSON com encoder Logstash:

```json
{
  "@timestamp": "2026-01-17T10:30:00.123Z",
  "level": "INFO",
  "logger": "com.postech.payment.fastfood.domain.services",
  "message": "[UseCase][GenerateQrCode] QR code gerado com sucesso",
  "thread": "http-nio-8091-exec-1",
  "orderId": "123e4567-e89b-12d3-a456-426614174000",
  "paymentId": "550e8400-e29b-41d4-a716-446655440000"
}
```

### Datadog APM

A aplicação inclui o agente Java Datadog para rastreamento distribuído:

```bash
# Agente carregado via JAVA_TOOL_OPTIONS no Dockerfile
-javaagent:/dd-java-agent.jar
```

Configurar via variáveis de ambiente:
- `DD_SERVICE=ms-payment-fastfood`
- `DD_ENV=production`
- `DD_VERSION=1.0.0`
- `DD_AGENT_HOST=<datadog-agent-host>`

## 🤝 Contribuindo

1. Faça um fork do repositório
2. Crie uma branch para sua feature (`git checkout -b feature/funcionalidade-incrivel`)
3. Commit suas mudanças (`git commit -m 'Adiciona funcionalidade incrível'`)
4. Push para a branch (`git push origin feature/funcionalidade-incrivel`)
5. Abra um Pull Request

### Estilo de Código

Este projeto usa Checkstyle para qualidade de código. Configuração: `checkstyle.xml`

Executar checkstyle:
```bash
./mvnw checkstyle:check
```

## 📄 Licença

MIT
