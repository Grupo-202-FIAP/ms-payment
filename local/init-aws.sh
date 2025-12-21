#!/bin/bash

echo "########### Criando recursos no LocalStack ###########"

set -e

echo "########### Criando filas SQS no LocalStack ###########"

AWS_REGION="us-east-1"
ENDPOINT_URL="http://localhost:4566"
ORDER_QUEUE_NAME="order-queue"
ORDER_CALLBACK_QUEUE_NAME="order-callback-queue"

aws --endpoint-url=${ENDPOINT_URL} sqs create-queue \
    --queue-name ${ORDER_QUEUE_NAME} \
    --region ${AWS_REGION}

echo "Fila criada: ${ORDER_QUEUE_NAME}"

aws --endpoint-url=${ENDPOINT_URL} sqs create-queue \
    --queue-name ${ORDER_CALLBACK_QUEUE_NAME} \
    --region ${AWS_REGION}

echo "Fila criada: ${ORDER_CALLBACK_QUEUE_NAME}"

echo "########### Filas criadas com sucesso ###########"

echo "########### Listando filas SQS ###########"
aws --endpoint-url=http://localhost:4566 sqs list-queues --region us-east-1

# 2. Criar o tópico SNS payment-callback (onde o ms-payment publica o resultado)
    aws --endpoint-url=http://localhost:4566 sns create-topic \
        --name payment-callback \
        --region us-east-1


# 4. Inscrever a fila do Orquestrador no tópico SNS
# Isso garante que quando o ms-payment avisar o SNS, o Orquestrador receba a mensagem.
aws --endpoint-url=http://localhost:4566 sns subscribe \
    --topic-arn arn:aws:sns:us-east-1:000000000000:payment-callback \
    --protocol sqs \
    --notification-endpoint arn:aws:sqs:us-east-1:000000000000:orchestrator-callback-queue

echo "########### Recursos criados com sucesso ###########"