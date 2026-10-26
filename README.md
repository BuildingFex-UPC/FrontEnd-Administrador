# FrontEnd

## BuildingFex — app de administradores (Jetpack Compose)

Esta app es **solo para administradores**. La vista y la lógica de residentes
(Dashboard de residente, finanzas de residente, espacios comunes / reservas,
invitados + QR, incidencias de residente, soporte de residente) se trasladaron
a la app Flutter de residentes (`../frontend_residente`).

- El login de residentes está bloqueado con el mensaje `err.RESIDENT_LOGIN_BLOCKED`
  (en `LoginUseCase`); el modo de activación por código de invitación se eliminó.
- Las cuentas `role == "resident"` no pueden iniciar sesión aquí; deben usar la
  app Flutter de residentes. Ambas apps comparten el mismo backend
  (`https://backend-1-lgr7.onrender.com`) sin cambios.

### Fase futura: notificaciones SMS / correo

Las notificaciones por SMS y correo quedan pendientes para una fase futura:
el backend aún no expone endpoints de SMS/correo, por lo que se posponen hasta
que existan.