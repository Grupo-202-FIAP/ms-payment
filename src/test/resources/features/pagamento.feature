# language: pt

Funcionalidade: Processamento de Pagamentos via Fila SQS

  Como um sistema de pagamentos
  Eu quero processar eventos de pagamento recebidos via fila SQS
  Para que os pagamentos sejam criados e os QR codes gerados corretamente

  Contexto:
    Dado que o Mercado Pago está configurado para retornar sucesso

  @sucesso
  Cenario: Criar pagamento com sucesso para um novo pedido
    Dado que um pedido válido é criado com valor de 100.00
    Quando o evento é enviado para a fila de pagamento
    E o processamento do pagamento é executado
    Entao o pagamento deve ser criado com status PENDING
    E o QR Code deve ser gerado
    E o valor total do pagamento deve ser 100.00
    E o método de pagamento deve ser QR_CODE
    E o pagamento deve estar associado ao pedido
    E nenhum erro deve ocorrer

  @sucesso
  Cenario: Criar pagamento com valor diferente
    Dado que um pedido válido é criado com valor de 250.50
    Quando o evento é enviado para a fila de pagamento
    E o processamento do pagamento é executado
    Entao o pagamento deve ser criado com status PENDING
    E o valor total do pagamento deve ser 250.50
    E nenhum erro deve ocorrer

  @sucesso
  Esquema do Cenario: Criar pagamento com diferentes valores
    Dado que um pedido válido é criado com valor de <valor>
    Quando o evento é enviado para a fila de pagamento
    E o processamento do pagamento é executado
    Entao o pagamento deve ser criado com status PENDING
    E o valor total do pagamento deve ser <valor>
    E nenhum erro deve ocorrer

    Exemplos:
      | valor   |
      | 50.00   |
      | 100.00  |
      | 500.00  |
      | 1000.00 |

  @erro
  Cenario: Falha na integração com Mercado Pago
    Dado que um pedido válido é criado com valor de 100.00
    E que o Mercado Pago está configurado para retornar erro
    Quando o evento é enviado para a fila de pagamento
    E o processamento do pagamento é executado
    Entao o pagamento não deve ser criado

