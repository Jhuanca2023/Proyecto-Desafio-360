# Red Social 📱

Una aplicación social moderna que permite a los usuarios participar en desafíos, compartir evidencias y conectarse con otros participantes.

## 🚀 Características Principales

- **Desafíos Interactivos:** Crea y participa en desafíos de diferentes categorías.
- **Evidencias Multimedia:** Comparte fotos, textos y audios como evidencia de tus logros.
- **Perfiles de Usuario:** Personaliza tu perfil y muestra tus logros.
- **Feed Social:** Explora las participaciones de otros usuarios.
- **Notificaciones:** Recibe notificaciones relevantes en tiempo real.

## 🛠 Tecnologías Utilizadas

- **Kotlin**: Lenguaje principal de desarrollo.
- **Jetpack Compose**: Framework moderno para UI nativa de Android.
- **Firebase**: Autenticación, base de datos (Firestore) y notificaciones.
- **Supabase**: Integración para autenticación y/o almacenamiento.
- **Imgur API**: Gestión y almacenamiento de imágenes.
- **Lottie**: Animaciones modernas en la interfaz.
- **MVVM**: Patrón de arquitectura para separar lógica y UI.
- **Gradle**: Sistema de construcción y gestión de dependencias.

## 📱 Requisitos del Sistema

- Android 6.0 (API level 23) o superior.
- Conexión a Internet.

## 🚀 Instalación

1. Clona el repositorio:
   ```bash
   git clone https://github.com/Jhuanca2023/Proyecto-Desafio-360.git
   ```
2. Abre el proyecto en Android Studio.
3. Configura Firebase:
   - Crea un proyecto en Firebase Console.
   - Descarga el archivo `google-services.json`.
   - Colócalo en la carpeta `app/`.
4. Configura Imgur y Supabase si es necesario:
   - Añade las credenciales correspondientes en los archivos de utilidades.
5. Ejecuta la aplicación en tu dispositivo o emulador.

## 📁 Estructura del Proyecto

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/redsocial/
│   │   │   ├── models/         # Modelos de datos
│   │   │   ├── navigation/     # Navegación de la app
│   │   │   ├── ui/             # Pantallas y componentes UI
│   │   │   ├── utils/          # Utilidades (Firebase, Imgur, Supabase, etc.)
│   │   │   ├── viewmodel/      # ViewModels (lógica y estado)
│   │   │   └── RedSocialApp.kt # Inicialización de la app
│   │   └── res/                # Recursos (imágenes, strings, animaciones, etc.)
│   └── test/                   # Tests unitarios
├── build.gradle.kts            # Configuración de Gradle
└── google-services.json         # Configuración de Firebase
```

## 👨‍💻 Autor

**Jose Huanca Otiniano**  
- GitHub: [@Jhuanca2023](https://github.com/Jhuanca2023)
- LinkedIn: [Jose Huanca Otiniano](https://www.linkedin.com/in/jose-huanca-061392274)

## 📄 Licencia

Este proyecto está bajo la Licencia MIT.

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Para cambios importantes, por favor abre primero un issue para discutir lo que te gustaría cambiar.

## 📞 Soporte

Si tienes alguna pregunta o necesitas ayuda, por favor abre un issue en el repositorio o contacta al autor.

---
Desarrollador del software por Jose Huanca Otiniano 