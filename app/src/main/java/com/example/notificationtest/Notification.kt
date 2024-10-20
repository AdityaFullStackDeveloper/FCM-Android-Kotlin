package com.example.notificationtest

data class Notification(
    val android_channel_id: String = "Campaign messages",
    val body: String = "",
    val sound: Boolean = true,
    val title: String = ""
)
