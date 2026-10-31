Feature: US13 - Invitar residentes y asignar unidad
  Como administrador
  quiero invitar residentes y asignarles una unidad
  para que accedan al sistema

  Scenario: Generación de invitación para una unidad existente
    Given existe una unidad registrada en el edificio
    And los datos del residente son válidos
    When el administrador genera la invitación
    Then el sistema crea un código de invitación asociado a la unidad

  Scenario: Activación de cuenta con código de invitación
    Given el residente recibió un código de invitación válido
    When ingresa el código y define su correo y contraseña
    Then el sistema activa su cuenta asociada a la unidad
