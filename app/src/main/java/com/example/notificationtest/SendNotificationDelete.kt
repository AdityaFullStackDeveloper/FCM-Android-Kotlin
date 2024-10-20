package com.example.notificationtest

data class SendNotificationDelete(
    val data: Data? = null,
    val notification: Notification? = null,
    val registration_ids: List<String>? = null
)
