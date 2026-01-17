# language: pt
Funcionalidade: Processamento de Eventos de Pagamento
  Como consumidor de mensagens
  Eu quero processar eventos da fila de pagamentos
  Para executar as ações corretas baseadas no status do evento

  Contexto:
    Dado que o sistema de pagamentos está inicializado

  @consumer @success
  Cenário: Processar mensagem com evento SUCCESS
    Dado uma mensagem JSON com status SUCCESS e pedido válido
    Quando a mensagem é consumida pelo listener
    Então o use case de geração de QR Code deve ser executado
    E o use case de rollback não deve ser executado

  @consumer @fail
  Cenário: Processar mensagem com evento FAIL
    Dado uma mensagem JSON com status FAIL
    Quando a mensagem é consumida pelo listener
    Então o use case de rollback deve ser executado
    E o use case de geração de QR Code não deve ser executado

  @consumer @erro @conversao
  Cenário: Tratar erro de conversão de mensagem
    Dado uma mensagem JSON inválida
    Quando a mensagem é consumida pelo listener
    Então o erro de conversão deve ser logado
    E a mensagem deve ser removida da fila

  @handler @success
  Cenário: Handler retorna true para evento SUCCESS processado com sucesso
    Dado um evento SUCCESS com pedido válido
    Quando o handler processa o evento
    Então o resultado deve ser true
    E o pagamento deve ser criado

  @handler @fail
  Cenário: Handler retorna true para evento FAIL processado com sucesso
    Dado um evento FAIL para pedido existente
    E existe um pagamento para o pedido
    Quando o handler processa o evento
    Então o resultado deve ser true
    E o pagamento deve ser removido

  @handler @erro
  Cenário: Handler retorna false e publica rollback quando ocorre erro
    Dado um evento SUCCESS com pedido válido
    E que a integração com MercadoPago irá falhar
    Quando o handler processa o evento
    Então o resultado deve ser false
    E um evento de rollback deve ser publicado

  @handler @historico
  Cenário: Handler inclui motivo do erro no histórico do rollback
    Dado um evento SUCCESS com pedido válido
    E que a integração com MercadoPago irá falhar com mensagem "Timeout na conexão"
    Quando o handler processa o evento
    Então um evento de rollback deve ser publicado
    E o histórico do evento deve conter "Timeout na conexão"

