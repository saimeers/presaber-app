# 📱 Presaber App

[![📥 Descargar
APK](https://img.shields.io/badge/Descargar-APK-blue?style=for-the-badge)](https://github.com/saimeers/presaber-app/releases/download/v0.1.0/presaber.apk)

> Versión: **v0.1.0 (Debug)**\
> Esta APK es una versión de prueba generada desde Android Studio. No es
> apta para producción.

------------------------------------------------------------------------

## 🚀 Instalación y Ejecución

### 1️⃣ Clonar el repositorio

``` bash
git clone https://github.com/saimeers/presaber.git
cd presaber
```

### 2️⃣ Abrir en Android Studio

1.  Abrir Android Studio\
2.  Ir a **File → Open → presaber**\
3.  Esperar a que Gradle sincronice el proyecto.

### 3️⃣ Configurar variables locales

Crea o edita el archivo `local.properties` en la raíz:

``` properties
sdk.dir=<ruta_sdk>
BASE_URL_DEV=<tu_url_backend>
```

Este archivo está en `.gitignore`, por lo que cada desarrollador debe
configurarlo localmente.

### 4️⃣ Compilar el proyecto

``` bash
./gradlew assembleDebug
```

### 5️⃣ Ejecutar la app

Conecta un dispositivo Android o inicia un emulador, luego presiona
**Run ▶** en Android Studio.

------------------------------------------------------------------------

## 🧩 Tecnologías utilizadas

-   Kotlin\
-   Jetpack Compose (Material 3)\
-   Retrofit + Gson\
-   Firebase Authentication\
-   Gradle BuildConfig

------------------------------------------------------------------------

## 🤝 Contribuidores

Agradecimientos a los miembros del equipo que trabajan en el desarrollo
de Presaber:

  ------------------------------------------------------------------------------------
  Nombre                              GitHub
  ----------------------------------- ------------------------------------------------
  **Saimer Adrian Saavedra Rojas**    [@saimeers](https://github.com/saimeers)
  
  **Andrés Felipe López Triana**     [@ElMopri](https://github.com/ElMopri)

  **Leydi Alejandra Durán**        [@LeydiD](https://github.com/LeydiD)

  ------------------------------------------------------------------------------------

------------------------------------------------------------------------

## 📝 Notas

-   Esta versión está marcada como **pre-release** porque corresponde a
    una build **Debug**.\
-   La versión para producción se generará posteriormente con un
    keystore propio.

------------------------------------------------------------------------

## 📄 Licencia

Este proyecto es de uso académico/desarrollo interno.
