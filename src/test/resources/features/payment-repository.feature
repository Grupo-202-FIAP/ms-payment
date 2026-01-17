# language: pt
Funcionalidade: Repositório de Pagamentos
  Como sistema de pagamentos
  Eu quero persistir e recuperar pagamentos
  Para manter o histórico e estado dos pagamentos

  Contexto:
    Dado que o sistema de pagamentos está inicializado

  @repositorio @salvar
  Cenário: Salvar pagamento com todos os campos
    Dado um pagamento com orderId único, valor 199.99 e status PENDING
    Quando o pagamento é salvo no repositório
    Então o pagamento deve ser recuperável pelo orderId
    E o pagamento recuperado deve ter o mesmo valor
    E o pagamento recuperado deve ter o mesmo status
    E o pagamento recuperado deve ter método QR_CODE
    E o pagamento recuperado deve ter um id gerado

  @repositorio @buscar
  Cenário: Buscar pagamento inexistente retorna vazio
    Dado um orderId que não existe no banco
    Quando busco o pagamento pelo orderId
    Então o resultado deve ser vazio

  @repositorio @deletar
  Cenário: Deletar pagamento existente
    Dado um pagamento com orderId único, valor 50.00 e status PENDING
    E o pagamento foi salvo no repositório
    Quando o pagamento é deletado pelo orderId
    Então o pagamento não deve mais existir no banco

  @repositorio @multiplos
  Cenário: Gerenciar múltiplos pagamentos
    Dado os seguintes pagamentos:
      | valor  | status    |
      | 100.00 | PENDING   |
      | 200.00 | PROCESSED |
      | 300.00 | EXPIRED   |
    Quando os pagamentos são salvos no repositório
    Então devem existir 3 pagamentos no banco
    E cada pagamento deve ter seu status correspondente

