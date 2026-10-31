Feature: US02 - Consultar deudas y estado de pago
  Como residente
  quiero visualizar mis deudas y su estado de pago
  para saber cuánto y cuándo debo pagar

  Scenario: Residente con cuotas pendientes
    Given el residente tiene cuotas registradas
    When consulta su estado de cuenta
    Then el sistema muestra las cuotas pendientes con su monto y estado de pago

  Scenario: Residente sin deudas pendientes
    Given el residente no tiene cuotas pendientes
    When consulta su estado de cuenta
    Then el sistema indica que no existen cuotas pendientes
