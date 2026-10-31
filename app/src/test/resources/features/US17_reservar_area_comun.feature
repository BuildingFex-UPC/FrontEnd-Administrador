Feature: US17 - Reservar un área común
  Como residente
  quiero reservar un área común en una fecha y hora
  para asegurar su uso sin conflictos

  Scenario: Reserva en un horario disponible
    Given el área común está disponible en la fecha y hora solicitadas
    When el residente registra la reserva
    Then el sistema crea la reserva asociada al residente

  Scenario: Reserva en un horario no disponible
    Given el horario solicitado ya está reservado
    When el residente intenta registrar la reserva
    Then el sistema rechaza la solicitud
