#!/bin/sh
set -x

AWS_REGION="us-east-1"
ENDPOINT_URL="http://localhost:4566"

PAYMENT_QUEUE_NAME="payment-queue"
PAYMENT_CALLBACK_QUEUE_NAME="payment-callback-queue"
PAYMENT_TOPIC_NAME="payment-callback"

echo "### Criando filas SQS ###"

# Criando a fila que seu MS consome
aws --endpoint-url=${ENDPOINT_URL} sqs create-queue \
  --queue-name ${PAYMENT_QUEUE_NAME} \
  --region ${AWS_REGION}

# Criando a fila de callback
aws --endpoint-url=${ENDPOINT_URL} sqs create-queue \
  --queue-name ${PAYMENT_CALLBACK_QUEUE_NAME} \
  --region ${AWS_REGION}

# Capturando ARNs
CALLBACK_QUEUE_ARN="arn:aws:sqs:${AWS_REGION}:000000000000:${PAYMENT_CALLBACK_QUEUE_NAME}"

echo "### Criando tópico SNS ###"
TOPIC_ARN=$(aws --endpoint-url=${ENDPOINT_URL} sns create-topic \
  --name ${PAYMENT_TOPIC_NAME} \
  --query TopicArn --output text)

echo "### Configurando policy da fila CALLBACK ###"
# A correção principal: usamos aspas simples '' em volta do JSON
POLICY='{
  "Version": "2012-10-17",
  "Statement": [{
    "Effect": "Allow",
    "Principal": "*",
    "Action": "sqs:SendMessage",
    "Resource": "'$CALLBACK_QUEUE_ARN'",
    "Condition": {
      "ArnEquals": {
        "aws:SourceArn": "'$TOPIC_ARN'"
      }
    }
  }]
}'

aws --endpoint-url=${ENDPOINT_URL} sqs set-queue-attributes \
  --queue-url ${ENDPOINT_URL}/000000000000/${PAYMENT_CALLBACK_QUEUE_NAME} \
  --attributes Policy="$POLICY"

echo "### Inscrevendo SQS no SNS ###"
aws --endpoint-url=${ENDPOINT_URL} sns subscribe \
  --topic-arn ${TOPIC_ARN} \
  --protocol sqs \
  --notification-endpoint ${CALLBACK_QUEUE_ARN}

echo "########### Recursos finais criados ###########"
aws --endpoint-url=${ENDPOINT_URL} sqs list-queues