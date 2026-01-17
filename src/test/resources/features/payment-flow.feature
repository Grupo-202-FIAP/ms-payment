# language: pt
Funcionalidade: Fluxo de Pagamento com QR Code
  Como sistema de pagamentos
  Eu quero processar eventos de pagamento
  Para gerar QR Codes e gerenciar o ciclo de vida dos pagamentos

  Contexto:
    Dado que o sistema de pagamentos está inicializado

  @sucesso @qrcode
  Cenário: Gerar QR Code para um novo pedido
    Dado um pedido com id único e valor de 150.00
    E que não existe pagamento para este pedido
    Quando o evento SUCCESS é processado para geração de QR Code
    Então o pagamento deve ser salvo no banco de dados
    E o pagamento deve ter status PENDING
    E o pagamento deve ter método QR_CODE
    E o QR Code deve ser gerado com sucesso

  @sucesso @qrcode
  Cenário: Não duplicar pagamento para pedido existente
    Dado um pedido com id único e valor de 100.00
    E que já existe um pagamento PENDING para este pedido
    Quando o evento SUCCESS é processado para geração de QR Code
    Então deve existir apenas um pagamento para o pedido
    E o sistema não deve chamar a integração com MercadoPago novamente

  @sucesso @qrcode
  Cenário: Processar pedido com valor alto
    Dado um pedido com id único e valor de 9999.99
    E que não existe pagamento para este pedido
    Quando o evento SUCCESS é processado para geração de QR Code
    Então o pagamento deve ser salvo no banco de dados
    E o valor do pagamento deve ser 9999.99

  @falha @rollback
  Cenário: Executar rollback quando evento FAIL é recebido
    Dado um pedido com id único e valor de 100.00
    E que já existe um pagamento PENDING para este pedido
    Quando o evento FAIL é processado para rollback
    Então o pagamento deve ser removido do banco de dados

  @falha @rollback
  Cenário: Rollback de pagamento inexistente não deve falhar
    Dado um pedido com id único e valor de 50.00
    E que não existe pagamento para este pedido
    Quando o evento FAIL é processado para rollback
    Então nenhum erro deve ser lançado
    E nenhum pagamento deve existir para o pedido

  @erro @integracao
  Cenário: Publicar rollback quando integração com MercadoPago falha
    Dado um pedido com id único e valor de 200.00
    E que não existe pagamento para este pedido
    E que a integração com MercadoPago irá falhar
    Quando o evento SUCCESS é processado para geração de QR Code
    Então um evento de rollback deve ser publicado
    E o status do evento de rollback deve ser ROLLBACK_PENDING
    E o source do evento de rollback deve ser PAYMENT
    E nenhum pagamento deve existir para o pedido

  @erro @database
  Cenário: Publicar rollback quando ocorre erro de banco de dados
    Dado um pedido com id único e valor de 300.00
    E que não existe pagamento para este pedido
    E que ocorrerá erro de banco de dados ao salvar
    Quando o evento SUCCESS é processado para geração de QR Code
    Então um evento de rollback deve ser publicado
    E o histórico do evento deve conter a razão do erro

  @evento @historico
  Cenário: Preservar histórico ao publicar rollback
    Dado um pedido com id único e valor de 100.00
    E que não existe pagamento para este pedido
    E um evento com histórico existente de ORDER com status SUCCESS
    E que a integração com MercadoPago irá falhar
    Quando o evento com histórico é processado
    Então um evento de rollback deve ser publicado
    E o histórico do evento deve conter 2 entradas
    E a primeira entrada do histórico deve ser de ORDER
    E a última entrada do histórico deve ser de PAYMENT

  @evento @naosuportado
  Cenário: Ignorar eventos com status não suportado
    Dado um pedido com id único e valor de 100.00
    Quando o evento ROLLBACK_PENDING é processado
    Então o evento deve ser ignorado
    E nenhum pagamento deve ser processado

