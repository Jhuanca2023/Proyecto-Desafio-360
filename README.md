# Desafio Score 📱

Una aplicación social moderna que permite a los usuarios participar en desafíos, compartir sus logros y conectarse con otros participantes.

## 🚀 Características Principales

- **Desafíos Interactivos**: Crea y participa en desafíos de diferentes categorías
- **Evidencias Multimedia**: Comparte fotos, videos, textos y audios como evidencia de tus logros
- **Sistema de Puntos**: Gana puntos por participar y completar desafíos
- **Perfiles de Usuario**: Personaliza tu perfil y muestra tus logros
- **Feed Social**: Explora las participaciones de otros usuarios
- **Notificaciones**: Mantente al día con las actualizaciones de tus desafíos

## 🛠 Tecnologías Utilizadas

### Frontend
- **Kotlin**: Lenguaje principal de desarrollo
- **Jetpack Compose**: Framework moderno para UI nativa de Android
- **Material Design 3**: Sistema de diseño para una experiencia visual coherente
- **Coil**: Biblioteca para carga eficiente de imágenes

### Backend
- **Firebase Authentication**: Sistema de autenticación seguro
- **Cloud Firestore**: Base de datos NoSQL para almacenamiento de datos
- **Imgur API**: Servicio para almacenamiento y gestión de imágenes
- **Firebase Cloud Messaging**: Sistema de notificaciones push

### Arquitectura
- **MVVM**: Patrón de arquitectura Model-View-ViewModel
- **Clean Architecture**: Separación clara de responsabilidades
- **Repository Pattern**: Patrón para manejo de datos
- **Coroutines**: Programación asíncrona en Kotlin

## 📱 Requisitos del Sistema

- Android 6.0 (API level 23) o superior
- Conexión a Internet
- Cuenta de Google para autenticación

## 🚀 Instalación

1. Clona el repositorio:
```bash
git clone https://github.com/tu-usuario/desafio-score.git
```

2. Abre el proyecto en Android Studio

3. Configura Firebase:
   - Crea un proyecto en Firebase Console
   - Descarga el archivo `google-services.json`
   - Colócalo en la carpeta `app/`

4. Configura Imgur:
   - Crea una cuenta en Imgur
   - Obtén tu Client ID
   - Configura las credenciales en el proyecto

5. Ejecuta la aplicación en tu dispositivo o emulador

## 📁 Estructura del Proyecto

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/redsocial/
│   │   │   ├── data/
│   │   │   │   ├── models/           # Modelos de datos
│   │   │   │   ├── repositories/     # Repositorios para acceso a datos
│   │   │   │   └── services/         # Servicios (Firebase, Imgur)
│   │   │   ├── ui/
│   │   │   │   ├── components/       # Componentes reutilizables
│   │   │   │   ├── screens/          # Pantallas principales
│   │   │   │   ├── theme/            # Temas y estilos
│   │   │   │   └── viewmodels/       # ViewModels
│   │   │   └── utils/                # Utilidades y helpers
│   │   └── res/                      # Recursos (imágenes, strings, etc.)
│   └── test/                         # Tests unitarios
├── build.gradle.kts                  # Configuración de Gradle
└── google-services.json             # Configuración de Firebase
```

### Descripción de Carpetas

- **models/**: Contiene las clases de datos como `Challenge`, `Evidencia`, `User`
- **repositories/**: Implementa el patrón Repository para acceso a datos
- **services/**: Contiene la lógica de integración con servicios externos
- **components/**: Componentes UI reutilizables como `ChallengeCard`, `EvidenciaCard`
- **screens/**: Pantallas principales de la aplicación
- **theme/**: Configuración de temas y estilos de Material Design
- **viewmodels/**: ViewModels para manejo de estado y lógica de negocio
- **utils/**: Funciones de utilidad y helpers

## 👨‍💻 Autor

**Jose Huanca Otiiano**
- GitHub: [@tu-usuario](https://github.com/tu-usuario)
- LinkedIn: [Jose Huanca Otiiano](https://linkedin.com/in/tu-perfil)

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE.md](LICENSE.md) para más detalles.

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Para cambios importantes, por favor abre primero un issue para discutir lo que te gustaría cambiar.

## 📞 Soporte

Si tienes alguna pregunta o necesitas ayuda, por favor abre un issue en el repositorio o contacta al autor.

---
Desarrollado con ❤️ por Jose Huanca Otiiano 