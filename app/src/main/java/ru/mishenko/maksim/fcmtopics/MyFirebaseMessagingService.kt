package ru.mishenko.maksim.fcmtopics

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.flow.update

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        EventList.mutableEventList.update { it + (message.data["value"] ?: "") }
    }
}