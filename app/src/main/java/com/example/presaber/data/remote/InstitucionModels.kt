package com.example.presaber.data.remote

data class CrearInstitucionRequest(
    val nombre: String,
    val departamento: String,
    val municipio: String,
    val direccion: String,
    val correo: String,
    val telefono: String,
    val director_documento: String,
    val director_nombre: String,
    val director_apellido: String,
    val director_correo: String,
    val director_telefono: String,
    val director_fecha_nacimiento: String, // "1980-03-15"
    val director_id_tipo_documento: Int
)

// Institución simple (lista)
data class InstitucionSimple(
    val id_institucion: Int,
    val nombre: String
)

data class InstitucionesSimpleResponse(
    val success: Boolean,
    val data: List<InstitucionSimple>
)

// Institución completa
data class InstitucionCompleta(
    val id_institucion: Int,
    val nombre: String,
    val departamento: String,
    val municipio: String,
    val direccion: String,
    val correo: String,
    val telefono: String,
    val director: DirectorInfo?
)

data class DirectorInfo(
    val documento: String,
    val nombre_completo: String,
    val correo: String,
    val telefono: String
)

data class InstitucionesCompletasResponse(
    val success: Boolean,
    val data: List<InstitucionCompleta>
)

// Response de crear institución
data class CrearInstitucionData(
    val institucion: InstitucionCreada,
    val director: DirectorCreado
)

data class InstitucionCreada(
    val id_institucion: Int,
    val nombre: String,
    val departamento: String,
    val municipio: String,
    val direccion: String,
    val correo: String,
    val telefono: String
)

data class DirectorCreado(
    val documento: String,
    val nombre: String,
    val apellido: String,
    val correo: String
)

data class CrearInstitucionResponse(
    val success: Boolean,
    val data: CrearInstitucionData,
    val mensaje: String
)

// ========== DEPARTAMENTOS Y MUNICIPIOS ==========

data class Departamento(
    val id: Int,
    val nombre: String,
    val descripcion: String
)

data class DepartamentosResponse(
    val success: Boolean,
    val data: List<Departamento>
)

data class Municipio(
    val id: Int,
    val nombre: String,
    val descripcion: String
)

data class MunicipiosResponse(
    val success: Boolean,
    val data: List<Municipio>
)