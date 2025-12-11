package com.example.presaber.data.remote

// Request para crear administrador
data class CrearAdministradorRequest(
    val documento: String,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val telefono: String,
    val fecha_nacimiento: String,
    val id_tipo_documento: Int,
    val id_institucion: Int
)

// Administrador completo
data class Administrador(
    val documento: String,
    val nombre: String,
    val apellido: String,
    val nombre_completo: String,
    val correo: String,
    val telefono: String,
    val fecha_nacimiento: String,
    val photoURL: String?,
    val tipo_documento: TipoDocumentoInfo,
    val rol: RolInfo,
    val institucion: InstitucionInfo
)

data class InstitucionInfo(
    val id_institucion: Int,
    val nombre: String
)

data class TipoDocumentoInfo(
    val id_tipo_documento: Int,
    val descripcion: String
)

data class RolInfo(
    val id_rol: Int,
    val descripcion: String
)

// Response crear administrador
data class CrearAdministradorData(
    val documento: String,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val telefono: String,
    val fecha_nacimiento: String,
    val id_tipo_documento: Int,
    val id_rol: Int,
    val id_institucion: Int,
    val uid_firebase: String
)

data class CrearAdministradorResponse(
    val success: Boolean,
    val data: CrearAdministradorData,
    val mensaje: String
)

// Response listar administradores
data class AdministradoresResponse(
    val success: Boolean,
    val data: List<Administrador>
)

// Response obtener un administrador
data class AdministradorResponse(
    val success: Boolean,
    val data: Administrador
)