# 📱 Presaber App

## 🚀 Instalación y ejecución

### 1️⃣ Clonar el repositorio
```bash
git clone https://github.com/tu-usuario/presaber.git
cd presaber
```

### 2️⃣ Abrir en Android Studio
1. Abrir Android Studio  
2. Ir a **File → Open → presaber**  
3. Esperar a que Gradle sincronice el proyecto

### 3️⃣ Configurar variables locales
Crear o editar el archivo `local.properties` en la raíz del proyecto:

```properties
sdk.dir
BASE_URL_DEV
```

> ⚠️ Este archivo está en `.gitignore`, por lo que cada desarrollador debe configurarlo localmente.

### 4️⃣ Compilar el proyecto
```bash
./gradlew assembleDebug
```

### 5️⃣ Ejecutar la app
Conectar un dispositivo Android o usar un emulador y presionar **Run ▶** en Android Studio.

---

## 🧩 Tecnologías
- Kotlin  
- Jetpack Compose (Material 3)  
- Retrofit + Gson  
- Firebase Authentication  
- Gradle BuildConfig  
