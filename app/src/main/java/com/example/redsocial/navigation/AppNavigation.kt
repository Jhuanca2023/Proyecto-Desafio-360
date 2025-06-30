package com.example.redsocial.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.redsocial.ui.screens.*
import com.example.redsocial.ui.BienvenidaScreen
import com.example.redsocial.ui.LoginScreen
import com.example.redsocial.ui.RegistroScreen
import com.example.redsocial.ui.InteresesScreen
import com.example.redsocial.viewmodel.AuthViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    NavHost(navController = navController, startDestination = "bienvenida") {
        composable("bienvenida") {
            BienvenidaScreen(navController, authViewModel)
        }
        
        composable("login") {
            LoginScreen(navController, authViewModel)
        }
        
        composable("registro") {
            RegistroScreen(navController, authViewModel)
        }

        composable("intereses") {
            InteresesScreen(navController, authViewModel)
        }

        composable("home") {
            HomeScreen(
                onNavigateToExplore = { navController.navigate("explore") },
                onNavigateToCreate = { navController.navigate("create") },
                onNavigateToNotifications = { navController.navigate("notifications") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToChallengeDetail = { challengeId ->
                    navController.navigate("detalleDesafio/$challengeId")
                }
            )
        }

        composable("explore") {
            ExploreScreen(navController)
        }

        composable("create") {
            CreateScreen()
        }

        composable("notifications") {
            NotificationsScreen(navController)
        }

        composable("profile") {
            ProfileScreen(
                navController = navController,
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate("bienvenida") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable("ajustes") {
            SettingsScreen(
                navController = navController,
                authViewModel = authViewModel,
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate("bienvenida") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("detalleDesafio/{challengeId}") { backStackEntry ->
            val challengeId = backStackEntry.arguments?.getString("challengeId") ?: ""
            DetalleDesafioScreen(challengeId = challengeId, navController = navController)
        }

        composable("editarDesafio/{desafioId}") { backStackEntry ->
            val desafioId = backStackEntry.arguments?.getString("desafioId") ?: ""
            com.example.redsocial.ui.screens.EditChallengeScreen(desafioId = desafioId, navController = navController)
        }

        composable("userProfile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UserProfileScreen(userId = userId, navController = navController, authViewModel = authViewModel)
        }

        composable("faqs") { FaqsScreen(navController) }
        composable("soporte") { SoporteScreen(navController) }
        composable("privacidad") { PrivacidadScreen(navController) }
        composable("terminos") { TerminosScreen(navController) }
        composable("acerca") { AcercaScreen(navController) }
    }
}

@Composable
fun FaqsScreen(navController: NavController) {
    HelpScreenTemplate(
        navController, 
        "Preguntas Frecuentes (FAQs)", 
        """
        ¿Qué es FLUXI?
        FLUXI es una red social educativa innovadora basada en desafíos interactivos para aprender, compartir y crecer junto a una comunidad apasionada por el conocimiento.

        ¿Quién puede participar?
        Cualquier persona mayor de 13 años interesada en aprender, compartir experiencias y participar en desafíos educativos de diversas categorías.

        ¿Cómo gano puntos e insignias?
        Completando desafíos exitosamente, participando activamente en la comunidad, compartiendo evidencias de tus logros y ayudando a otros usuarios.

        ¿Qué tipos de contenido puedo compartir?
        Puedes compartir textos, imágenes y videos que demuestren tu participación en los desafíos. Asegúrate de que el contenido sea apropiado y respetuoso.

        ¿Cómo funciona el sistema de puntos?
        Cada desafío tiene un valor en puntos que se reparte entre todos los participantes cuando se completa. Los puntos se acumulan y te dan acceso a insignias especiales.

        ¿Quién desarrolla FLUXI?
        FLUXI es desarrollada por Jose Huanca, apasionado por la tecnología y la educación, comprometido con crear experiencias de aprendizaje innovadoras.

        ¿Puedo eliminar mi cuenta?
        Sí, puedes eliminar tu cuenta en cualquier momento desde la sección de Ajustes. Esta acción es permanente e irreversible.

        ¿Cómo reporto contenido inapropiado?
        Si encuentras contenido que viola nuestras políticas, puedes reportarlo contactándonos directamente a soporte@fluxi.com.

        ¿Tienes más preguntas? ¡Contáctanos!
        """.trimIndent()
    )
}

@Composable
fun SoporteScreen(navController: NavController) {
    HelpScreenTemplate(
        navController, 
        "Soporte y Contacto", 
        """
        ¿Tienes dudas, sugerencias o necesitas ayuda?
        Nuestro equipo está aquí para ayudarte en cualquier momento.

        📧 Correo electrónico:
        soporte@fluxi.com

        👨‍💻 Desarrollador principal:
        Jose Huanca

        🌐 Sitio web oficial:
        Próximamente disponible

        ⏰ Tiempo de respuesta:
        Nos comprometemos a responderte en un máximo de 24 horas.

        📱 Soporte técnico:
        Para problemas técnicos, incluye en tu mensaje:
        - Tu nombre de usuario
        - Descripción detallada del problema
        - Capturas de pantalla (si aplica)
        - Versión de la app que usas

        💡 Sugerencias y mejoras:
        ¡Nos encanta recibir tus ideas! Comparte con nosotros cómo podemos mejorar FLUXI para ti.

        🐛 Reporte de errores:
        Si encuentras algún error, por favor descríbelo detalladamente para poder solucionarlo rápidamente.

        ¡Tu experiencia es nuestra prioridad!
        """.trimIndent()
    )
}

@Composable
fun PrivacidadScreen(navController: NavController) {
    HelpScreenTemplate(
        navController, 
        "Política de Privacidad", 
        """
        En FLUXI, tu privacidad es fundamental y nos comprometemos a protegerla.

        🔒 Información que recopilamos:
        • Datos de tu cuenta (email, nombre de usuario)
        • Contenido que compartes (textos, imágenes, videos)
        • Actividad en la app (desafíos completados, puntos ganados)
        • Información técnica básica para mejorar el servicio

        🛡️ Cómo protegemos tu información:
        • Encriptación de datos en tránsito y en reposo
        • Acceso restringido a información personal
        • Cumplimiento con estándares de seguridad internacionales
        • Auditorías regulares de seguridad

        📋 Cómo usamos tu información:
        • Para proporcionar y mejorar nuestros servicios
        • Para personalizar tu experiencia en la app
        • Para comunicarnos contigo sobre actualizaciones importantes
        • Para garantizar la seguridad de la comunidad

        🚫 Lo que NO hacemos:
        • Vendemos tu información personal a terceros
        • Compartimos datos sin tu consentimiento explícito
        • Usamos tu información para publicidad no solicitada

        👤 Tus derechos:
        • Acceder a tu información personal
        • Corregir datos inexactos
        • Solicitar la eliminación de tu cuenta y datos
        • Exportar tu información
        • Revocar consentimientos otorgados

        🍪 Cookies y tecnologías similares:
        Utilizamos cookies esenciales para el funcionamiento de la app. Puedes gestionar estas preferencias en la configuración de tu dispositivo.

        🌍 Transferencias internacionales:
        Tus datos pueden ser procesados en servidores ubicados en diferentes países, siempre cumpliendo con estándares de protección adecuados.

        📞 Contacto para privacidad:
        Para cualquier consulta sobre privacidad, contáctanos a:
        soporte@fluxi.com

        Desarrollador responsable:
        Jose Huanca

        Última actualización: Junio 2025
        """.trimIndent()
    )
}

@Composable
fun TerminosScreen(navController: NavController) {
    HelpScreenTemplate(
        navController, 
        "Términos de Servicio", 
        """
        Al usar FLUXI, aceptas los siguientes términos y condiciones:

        📋 ACEPTACIÓN DE TÉRMINOS
        Al acceder y usar FLUXI, confirmas que has leído, comprendido y aceptas estar sujeto a estos términos de servicio.

        👤 ELEGIBILIDAD
        • Debes tener al menos 13 años de edad
        • Si eres menor de 18 años, necesitas consentimiento parental
        • No puedes usar la app si estás prohibido por ley

        📱 USO ACEPTABLE
        Puedes usar FLUXI para:
        • Participar en desafíos educativos
        • Compartir contenido apropiado y respetuoso
        • Interactuar con otros usuarios de manera constructiva
        • Reportar contenido inapropiado

        🚫 USO PROHIBIDO
        No está permitido:
        • Compartir contenido ofensivo, violento o ilegal
        • Acosar, intimidar o discriminar a otros usuarios
        • Usar la app para spam o publicidad no autorizada
        • Intentar hackear o comprometer la seguridad
        • Infringir derechos de propiedad intelectual
        • Crear múltiples cuentas para evadir suspensiones

        📸 CONTENIDO DEL USUARIO
        • Eres responsable del contenido que compartes
        • Mantienes tus derechos sobre tu contenido
        • Nos otorgas licencia para mostrar tu contenido en la app
        • No compartas contenido de terceros sin permiso

        🏆 SISTEMA DE PUNTOS Y RECOMPENSAS
        • Los puntos se otorgan según la participación
        • Las insignias son simbólicas y no tienen valor monetario
        • Nos reservamos el derecho de ajustar el sistema
        • No se garantiza la disponibilidad de recompensas específicas

        🔒 PRIVACIDAD Y SEGURIDAD
        • Tu privacidad es importante (ver Política de Privacidad)
        • Debes mantener segura tu cuenta
        • Reporta inmediatamente cualquier actividad sospechosa

        ⚖️ SUSPENSIÓN Y TERMINACIÓN
        Podemos suspender o terminar tu cuenta por:
        • Violación de estos términos
        • Comportamiento inapropiado
        • Actividad fraudulenta
        • Cualquier razón legal válida

        📞 CONTACTO Y DISPUTAS
        Para consultas sobre estos términos:
        soporte@fluxi.com

        Desarrollador responsable:
        Jose Huanca

        🔄 MODIFICACIONES
        Nos reservamos el derecho de modificar estos términos. Los cambios serán notificados a través de la app.

        📅 VIGENCIA
        Estos términos están vigentes desde Junio de 2025.

        Al continuar usando FLUXI, confirmas tu aceptación de estos términos.
        """.trimIndent()
    )
}

@Composable
fun AcercaScreen(navController: NavController) {
    HelpScreenTemplate(
        navController, 
        "Acerca de FLUXI", 
        """
        🌟 FLUXI v1.0

        FLUXI es una red social educativa innovadora que conecta a personas apasionadas por el aprendizaje y el crecimiento personal a través de desafíos interactivos. Nuestra misión es inspirar, motivar y premiar el esfuerzo de quienes buscan superarse día a día, creando una comunidad segura, inclusiva y colaborativa.

        🎯 ¿Qué puedes hacer en FLUXI?
        • Participar en desafíos educativos de distintas categorías
        • Compartir tus logros y evidencias en formato de texto, imagen o video
        • Ganar puntos, insignias y reconocimientos por tu participación
        • Conectar con otros usuarios, aprender juntos y divertirte
        • Crear y gestionar tus propios desafíos
        • Explorar contenido educativo de calidad

        💎 Nuestros valores
        • Educación accesible y divertida para todos
        • Seguridad y privacidad de nuestros usuarios
        • Innovación constante y escucha activa de la comunidad
        • Respeto, inclusión y diversidad
        • Transparencia en todas nuestras operaciones

        👨‍💻 ¿Quiénes somos?
        FLUXI es desarrollada por Jose Huanca, un desarrollador apasionado por la tecnología y la educación, comprometido con crear experiencias digitales que transformen la forma en que aprendemos y nos conectamos.

        🏆 Características destacadas
        • Interfaz intuitiva y moderna
        • Sistema de gamificación educativo
        • Comunidad activa y colaborativa
        • Contenido diverso y de calidad
        • Seguridad y privacidad garantizadas

        📞 Contacto y soporte
        ¿Tienes dudas, sugerencias o necesitas ayuda?
        • Email: soporte@fluxi.com
        • Desarrollador: Jose Huanca
        • Soporte técnico disponible 24/7

        🌍 Nuestra visión
        Ser la plataforma líder en educación social, conectando a millones de personas que comparten la pasión por aprender y crecer juntos.

        🙏 Agradecimientos
        Gracias a toda la comunidad beta por sus valiosos aportes y sugerencias. ¡Juntos hacemos de FLUXI una mejor experiencia cada día!

        🚀 Próxima actualización: FLUXI v2.0 (Septiembre 2025)
        • Desafíos Extremos: Nuevos retos exclusivos para mayores de 18 años, con recompensas especiales y niveles de dificultad avanzados.
        • Ranking global y por categorías: Compite con usuarios de todo el mundo y sube en las tablas de clasificación.
        • Retos colaborativos: Forma equipos con otros usuarios para superar desafíos grupales y ganar recompensas únicas.
        • Sistema de logros y trofeos: Desbloquea insignias especiales y trofeos por hitos importantes.
        • Mejoras en la seguridad y privacidad: Nuevas opciones de control parental y reportes automáticos de contenido.
        • Personalización avanzada de perfil: Nuevos avatares, fondos y temas para tu perfil.
        • Notificaciones inteligentes: Recibe alertas personalizadas sobre desafíos, logros y actividad relevante.
        • Integración con plataformas educativas externas: Sube tus logros a LinkedIn, Google Classroom y más.
        • Soporte multilingüe ampliado: Más idiomas disponibles para llegar a una comunidad global.
        • Accesibilidad mejorada: Mejoras para usuarios con discapacidad visual o auditiva.

        ¿Tienes ideas para la próxima versión? ¡Envíanos tus sugerencias a soporte@fluxi.com!

        ¡Únete a la revolución educativa con FLUXI!
        """.trimIndent()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreenTemplate(navController: NavController, title: String, content: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1F2E))
            )
        },
        containerColor = Color(0xFF0A0F1C)
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(content, color = Color.White, fontSize = 16.sp)
        }
    }
} 