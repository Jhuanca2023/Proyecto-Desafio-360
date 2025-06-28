package com.example.redsocial.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

object NotificationUtils {
    
    /**
     * Envía una notificación al usuario especificado
     */
    fun sendNotification(
        targetUserId: String,
        tipo: String,
        mensaje: String,
        actorPhotoUrl: String? = null,
        challengeId: String? = null,
        challengeTitle: String? = null
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        Log.d("NotificationUtils", "sendNotification - targetUserId: $targetUserId, tipo: $tipo, currentUser: ${currentUser?.uid}")
        
        if (currentUser == null || currentUser.uid == targetUserId) {
            Log.d("NotificationUtils", "No enviando notificación - usuario null o mismo usuario")
            return // No enviar notificación a uno mismo
        }
        
        val db = FirebaseFirestore.getInstance()
        
        // Obtener información del usuario que realiza la acción
        db.collection("usuarios").document(currentUser.uid).get()
            .addOnSuccessListener { actorUserDoc ->
                val actorUserName = actorUserDoc.getString("nombreUsuario") ?: "Usuario"
                val actorPhoto = actorPhotoUrl ?: actorUserDoc.getString("photoUrl")
                
                Log.d("NotificationUtils", "Actor info - userName: $actorUserName, photo: $actorPhoto")
                
                // Crear la notificación
                val notificacion = hashMapOf(
                    "tipo" to tipo,
                    "mensaje" to mensaje,
                    "fecha" to System.currentTimeMillis(),
                    "leido" to false,
                    "actorId" to currentUser.uid,
                    "actorUserName" to actorUserName,
                    "actorPhotoUrl" to actorPhoto,
                    "challengeId" to challengeId,
                    "challengeTitle" to challengeTitle
                )
                
                Log.d("NotificationUtils", "Creando notificación: $notificacion")
                
                // Guardar en la colección de notificaciones del usuario objetivo
                db.collection("usuarios")
                    .document(targetUserId)
                    .collection("notificaciones")
                    .add(notificacion)
                    .addOnSuccessListener { documentReference ->
                        Log.d("NotificationUtils", "Notificación enviada exitosamente con ID: ${documentReference.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("NotificationUtils", "Error enviando notificación: ${e.message}")
                        println("Error enviando notificación: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("NotificationUtils", "Error obteniendo información del usuario: ${e.message}")
                println("Error obteniendo información del usuario: ${e.message}")
            }
    }
    
    /**
     * Envía notificación de like
     */
    fun sendLikeNotification(
        challengeAuthorId: String,
        challengeId: String,
        challengeTitle: String
    ) {
        Log.d("NotificationUtils", "sendLikeNotification - authorId: $challengeAuthorId, challengeId: $challengeId, title: $challengeTitle")
        val mensaje = "le dio like a tu desafío \"$challengeTitle\""
        sendNotification(
            targetUserId = challengeAuthorId,
            tipo = "like",
            mensaje = mensaje,
            challengeId = challengeId,
            challengeTitle = challengeTitle
        )
    }
    
    /**
     * Envía notificación de comentario
     */
    fun sendCommentNotification(
        challengeAuthorId: String,
        challengeId: String,
        challengeTitle: String,
        commentText: String
    ) {
        val mensaje = "comentó en tu desafío \"$challengeTitle\": \"$commentText\""
        sendNotification(
            targetUserId = challengeAuthorId,
            tipo = "comentario",
            mensaje = mensaje,
            challengeId = challengeId,
            challengeTitle = challengeTitle
        )
    }
    
    /**
     * Envía notificación de seguimiento
     */
    fun sendFollowNotification(targetUserId: String, isFollowing: Boolean) {
        val mensaje = if (isFollowing) "te está siguiendo" else "dejó de seguirte"
        sendNotification(
            targetUserId = targetUserId,
            tipo = "seguimiento",
            mensaje = mensaje
        )
    }
    
    /**
     * Elimina notificación de like cuando se quita el like
     */
    fun removeLikeNotification(
        challengeAuthorId: String,
        challengeId: String
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()
        
        // Buscar y eliminar la notificación de like
        db.collection("usuarios")
            .document(challengeAuthorId)
            .collection("notificaciones")
            .whereEqualTo("tipo", "like")
            .whereEqualTo("actorId", currentUser.uid)
            .whereEqualTo("challengeId", challengeId)
            .get()
            .addOnSuccessListener { notificaciones ->
                notificaciones.documents.forEach { doc ->
                    doc.reference.delete()
                }
            }
            .addOnFailureListener { e ->
                println("Error eliminando notificación de like: ${e.message}")
            }
    }
    
    /**
     * Función de debug para verificar notificaciones existentes
     */
    fun debugNotifications(userId: String) {
        val db = FirebaseFirestore.getInstance()
        Log.d("NotificationUtils", "Debug - Verificando notificaciones para usuario: $userId")
        
        db.collection("usuarios")
            .document(userId)
            .collection("notificaciones")
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d("NotificationUtils", "Debug - Total notificaciones: ${snapshot.size()}")
                snapshot.documents.forEach { doc ->
                    Log.d("NotificationUtils", "Debug - Notificación: ${doc.data}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("NotificationUtils", "Debug - Error obteniendo notificaciones: ${e.message}")
            }
    }
    
    /**
     * Función de prueba simple para verificar conectividad con Firestore
     */
    fun testFirestoreConnection() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()
        
        Log.d("NotificationUtils", "=== PRUEBA DE CONECTIVIDAD FIRESTORE ===")
        Log.d("NotificationUtils", "Usuario autenticado: ${currentUser?.uid ?: "NO AUTENTICADO"}")
        
        if (currentUser == null) {
            Log.e("NotificationUtils", "ERROR: No hay usuario autenticado")
            return
        }
        
        // Prueba 1: Leer documento del usuario
        Log.d("NotificationUtils", "Prueba 1: Leyendo documento del usuario...")
        db.collection("usuarios").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d("NotificationUtils", "✅ ÉXITO: Documento del usuario leído correctamente")
                    Log.d("NotificationUtils", "Datos del usuario: ${document.data}")
                } else {
                    Log.e("NotificationUtils", "❌ ERROR: Documento del usuario no existe")
                }
            }
            .addOnFailureListener { e ->
                Log.e("NotificationUtils", "❌ ERROR: No se pudo leer documento del usuario: ${e.message}")
            }
        
        // Prueba 2: Contar notificaciones no leídas
        Log.d("NotificationUtils", "Prueba 2: Contando notificaciones no leídas...")
        db.collection("usuarios")
            .document(currentUser.uid)
            .collection("notificaciones")
            .whereEqualTo("leido", false)
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d("NotificationUtils", "✅ ÉXITO: Notificaciones no leídas contadas: ${snapshot.size()}")
                snapshot.documents.forEach { doc ->
                    Log.d("NotificationUtils", "Notificación no leída: ${doc.data}")
                }
            }
            .addOnFailureListener { e ->
                Log.e("NotificationUtils", "❌ ERROR: No se pudieron contar notificaciones: ${e.message}")
            }
        
        // Prueba 3: Intentar escribir un documento de prueba
        Log.d("NotificationUtils", "Prueba 3: Intentando escribir documento de prueba...")
        val testData = hashMapOf(
            "test" to true,
            "timestamp" to System.currentTimeMillis(),
            "message" to "Prueba de conectividad"
        )
        
        db.collection("test_connection")
            .document(currentUser.uid)
            .set(testData)
            .addOnSuccessListener {
                Log.d("NotificationUtils", "✅ ÉXITO: Documento de prueba escrito correctamente")
                // Limpiar el documento de prueba
                db.collection("test_connection").document(currentUser.uid).delete()
                    .addOnSuccessListener {
                        Log.d("NotificationUtils", "✅ Documento de prueba eliminado")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("NotificationUtils", "❌ ERROR: No se pudo escribir documento de prueba: ${e.message}")
            }
    }
} 