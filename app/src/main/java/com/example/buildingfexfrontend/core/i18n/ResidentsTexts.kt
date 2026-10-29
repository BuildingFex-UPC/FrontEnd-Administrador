package com.example.buildingfexfrontend.core.i18n

/**
 * ES/EN texts for the residents management screen (`res.*`).
 * Feature-owned by residents/presentation (ResidentsScreen, ResidentsViewModel).
 */
val ResidentsTexts: Map<String, TextPair> = mapOf(
    "res.fab.add" to ("Agregar residente" to "Add resident"),
    "res.planUsage" to ("Tope del plan: {current} / {max} residentes (ajusta el plan en Ajustes)." to "Plan limit: {current} / {max} residents (adjust the plan in Settings)."),
    "res.search.placeholder" to ("Buscar por nombre, departamento o piso" to "Search by name, department or floor"),
    "res.loading" to ("Cargando residentes…" to "Loading residents…"),
    "res.empty.none" to ("Aún no hay residentes agregados." to "No residents added yet."),
    "res.empty.noMatch" to ("No se encontraron residentes con ese criterio." to "No residents found matching that criteria."),
    "res.delete.title" to ("Eliminar residente" to "Delete resident"),
    "res.delete.message" to ("¿Eliminar a {name}? Esta acción no se puede deshacer." to "Delete {name}? This action cannot be undone."),
    "res.delete.calculating" to ("Calculando datos vinculados…" to "Calculating linked data…"),
    "res.delete.preview" to ("\n\nDatos que se borrarán en cascada:\n• {reservations} reservas asociadas\n• Su cuenta de acceso y credenciales" to "\n\nData that will be cascade-deleted:\n• {reservations} associated reservations\n• Their access account and credentials"),
    "res.delete.previewNoAccount" to ("\n\nSe eliminará su cuenta de acceso." to "\n\nTheir access account will be deleted."),
    "res.delete.confirm" to ("Eliminar todo" to "Delete all"),
    "res.delete.cd" to ("Eliminar" to "Delete"),
    "res.message.title" to ("Listo" to "Done"),
    "res.message.confirm" to ("Entendido" to "OK"),
    "res.row.floorDept" to ("Piso {floor} – Dept. {dept}" to "Floor {floor} – Apt. {dept}"),
    "res.row.activeAccount" to ("Cuenta activa" to "Active account"),
    "res.row.noAccount" to ("Sin activar · comparte el código para que cree su acceso" to "Not activated · share the code so they can set up their access"),
    "res.share.chooser" to ("Compartir invitación" to "Share invitation"),
    "res.share.cd" to ("Compartir código de invitación de {name}" to "Share invitation code of {name}"),
    "res.copy.cd" to ("Copiar solo el código de {name}" to "Copy only the code of {name}"),
    "res.msg.codeCopied" to ("Código copiado al portapapeles. Pégalo en la app de residente para crear la cuenta." to "Code copied to clipboard. Paste it in the resident app to create the account."),
    "res.invite.header" to ("BuildingFex – Invitación para {name} (Piso {floor}, Dept. {dept}).\n\n" to "BuildingFex – Invitation for {name} (Floor {floor}, Apt. {dept}).\n\n"),
    "res.invite.code" to ("Código de invitación: {code}\n\n" to "Invitation code: {code}\n\n"),
    "res.invite.instructions" to ("Instala la app BuildingFex y, en la pantalla de acceso, toca \"¿Tienes un código de invitación? Actívalo\" para crear tu contraseña." to "Install the BuildingFex app and, on the access screen, tap \"Do you have an invitation code? Activate it\" to create your password."),
    "res.add.title" to ("Nuevo residente" to "New resident"),
    "res.field.name" to ("Nombre" to "Name"),
    "res.field.department" to ("Número de departamento" to "Department number"),
    "res.field.departmentPlaceholder" to ("Ej. 304" to "e.g. 304"),
    "res.field.floorDetected" to ("Piso detectado: {floor}" to "Detected floor: {floor}"),
    "res.field.departmentHelp" to ("3 o 4 dígitos. Ej. 304 = piso 3, unidad 04. El código de invitación único se genera solo." to "3 or 4 digits. E.g. 304 = floor 3, unit 04. The unique invitation code is generated automatically."),
    "res.add.saving" to ("Guardando…" to "Saving…"),
    "res.add.confirm" to ("Guardar residente" to "Save resident"),
    "res.add.cancel" to ("Cancelar" to "Cancel"),
    "res.msg.added" to ("Residente agregado." to "Resident added."),
    "res.msg.removed" to ("Residente eliminado ({count} reservas en cascada)." to "Resident removed ({count} cascade-deleted reservations)."),
)
