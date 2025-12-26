#!/bin/sh
# Removed -e to prevent the script from stopping if a resource already exists
set -ux

echo ">>> INIT AWS SCRIPT STARTED <<<"

AWS_REGION="us-east-1"
# If running inside Docker, you might need http://localstack:4566
ENDPOINT_URL="http://localhost:4566"
PAYMENT_NOTIFICATION_NAME="payment-callback"
PAYMENT_EVENT_CALLBACK_QUEUE_NAME="payment-callback-queue"

echo "########### Criando fila SQS ###########"
aws --endpoint-url=${ENDPOINT_URL} sqs create-queue \
  --queue-name ${PAYMENT_EVENT_CALLBACK_QUEUE_NAME} \
  --region ${AWS_REGION}

# Get URL and ARN
QUEUE_URL="${ENDPOINT_URL}/000000000000/${PAYMENT_EVENT_CALLBACK_QUEUE_NAME}"
QUEUE_ARN="arn:aws:sqs:${AWS_REGION}:000000000000:${PAYMENT_EVENT_CALLBACK_QUEUE_NAME}"

echo "########### Criando tópico SNS ###########"
TOPIC_ARN=$(aws --endpoint-url=${ENDPOINT_URL} sns create-topic \
  --name ${PAYMENT_NOTIFICATION_NAME} \
  --region ${AWS_REGION} \
  --query TopicArn --output text)

echo "########### Configurando policy da fila SQS ###########"
# We escape the quotes to ensure the shell passes the JSON correctly
POLICY="{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":\"*\",\"Action\":\"sqs:SendMessage\",\"Resource\":\"${QUEUE_ARN}\",\"Condition\":{\"ArnEquals\":{\"aws:SourceArn\":\"${TOPIC_ARN}\"}}}]}"

aws --endpoint-url=${ENDPOINT_URL} sqs set-queue-attributes \
  --queue-url "${QUEUE_URL}" \
  --attributes "Policy=${POLICY}"

echo "########### Inscrevendo SQS no SNS ###########"
aws --endpoint-url=${ENDPOINT_URL} sns subscribe \
  --topic-arn "${TOPIC_ARN}" \
  --protocol sqs \
  --notification-endpoint "${QUEUE_ARN}"

echo "########### Recursos criados com sucesso ###########"
aws --endpoint-url=${ENDPOINT_URL} sqs list-queues
aws --endpoint-url=${ENDPOINT_URL} sns list-topics