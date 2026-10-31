Feature: US14 - Iniciar sesión de forma segura
  Como usuario
  quiero iniciar sesión de forma segura
  para proteger la información del edificio

  Scenario: Inicio de sesión con credenciales válidas
    Given el usuario posee una cuenta registrada con credenciales válidas
    When inicia sesión con su correo y contraseña
    Then el sistema autentica al usuario
    And le permite acceder al dashboard correspondiente a su rol

  Scenario: Inicio de sesión con credenciales inválidas
    Given el usuario ingresa una contraseña incorrecta
    When intenta iniciar sesión
    Then el sistema rechaza la autenticación
    And muestra un mensaje de credenciales inválidas
