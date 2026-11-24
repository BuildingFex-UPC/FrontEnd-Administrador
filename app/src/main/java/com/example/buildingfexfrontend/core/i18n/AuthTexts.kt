package com.example.buildingfexfrontend.core.i18n

/**
 * ES/EN texts for IAM / login (`auth.*`).
 * Feature-owned by iam/presentation (AuthScreen, AuthViewModel).
 */
val AuthTexts: Map<String, TextPair> = mapOf(
    "auth.tagline.main" to ("Gestión inteligente de tu condominio" to "Smart management of your condominium"),
    "auth.tagline.invite" to ("Activación de cuenta de residente" to "Resident account activation"),
    "auth.tab.login" to ("Iniciar sesión" to "Sign in"),
    "auth.tab.register" to ("Registro admin" to "Admin sign-up"),
    "auth.field.email" to ("Correo electrónico" to "Email"),
    "auth.field.password" to ("Contraseña" to "Password"),
    "auth.field.name" to ("Nombre completo" to "Full name"),
    "auth.field.dni" to ("DNI" to "National ID"),
    "auth.field.address" to ("Dirección" to "Address"),
    "auth.field.company" to ("Empresa" to "Company"),
    "auth.field.ruc" to ("RUC" to "Tax ID"),
    "auth.field.invite_code" to ("Código de invitación" to "Invitation code"),
    "auth.button.login" to ("Entrar" to "Sign in"),
    "auth.button.register" to ("Registrar admin" to "Register admin"),
    "auth.button.find_invite" to ("Buscar invitación" to "Find invitation"),
    "auth.button.access" to ("Acceder" to "Continue"),
    "auth.link.have_invite" to ("¿Tienes un código de invitación? Actívalo" to "Have an invitation code? Activate it"),
    "auth.link.back_to_login" to ("Volver a iniciar sesión" to "Back to sign in"),
    "auth.invite.placeholder" to ("Ej. 304-k7m2" to "E.g. 304-k7m2"),
    "auth.invite.supporting" to ("Pega el mensaje de invitación completo o escribe solo el código." to "Paste the full invitation message or type just the code."),
    "auth.invite.welcome" to ("Hola, te invitaron a unirte a este condominio" to "Hi, you've been invited to join this condominium"),
    "auth.invite.data_ok" to ("Tus datos son correctos:" to "Your details are correct:"),
    "auth.invite.name" to ("Nombre: {name}" to "Name: {name}"),
    "auth.invite.code" to ("Código: {code}" to "Code: {code}"),
    "auth.invite.floor" to ("Piso: {floor}" to "Floor: {floor}"),
    "auth.invite.create_credentials" to ("Crea tus credenciales" to "Create your credentials"),
    "auth.invite.no_code" to ("No reconozco un código en el texto. Copia solo el código (ej. 304-k7m2)." to "I can't find a code in the text. Copy just the code (e.g. 304-k7m2)."),
    "auth.error.invalid_email" to ("Introduce un correo electrónico válido." to "Enter a valid email address."),
    "auth.error.password_required" to ("Ingresa tu contraseña." to "Enter your password."),
    "auth.error.min_6" to ("Mínimo 6 caracteres." to "At least 6 characters."),
    "auth.error.name_required" to ("Ingresa tu nombre completo." to "Enter your full name."),
    "auth.error.invite_code_required" to ("Ingresa tu código de invitación." to "Enter your invitation code."),
    "auth.error.resident_not_found" to ("No encontramos residentes con el código \"{code}\". Revisa el mensaje de invitación." to "We couldn't find any residents with the code \"{code}\". Check the invitation message."),
    "auth.terms.accept" to ("He leído y acepto los Términos y Condiciones" to "I have read and accept the Terms and Conditions"),
    "auth.terms.read" to ("Leer términos y condiciones" to "Read terms and conditions"),
    "auth.error.terms_required" to ("Debes aceptar los Términos y Condiciones para crear la cuenta." to "You must accept the Terms and Conditions to create the account."),
)
