package com.example.notificationtest

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

const val channel_Name = "com.example.notificationtest"
const val channel_Id = "Campaign messages"

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        if (message.notification != null) {
            val title = message.notification!!.title
            val body = message.notification!!.body
            if (title != null && body != null) {
                generateNotification(title, body)
            }
        }else{
            Toast.makeText(this, "No any message", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("RemoteViewLayout")
    private fun sendNotification(title: String, message: String): RemoteViews {
        val remoteView = RemoteViews(packageName, R.layout.notification_design)

        remoteView.setTextViewText(R.id.notification_titleTextView, title)
        remoteView.setTextViewText(R.id.notification_messageTextView, message)
        remoteView.setImageViewResource(R.id.notification_appLogo, R.drawable.ic_launcher_foreground)

        return remoteView
    }

    @SuppressLint("UnspecifiedImmutableFlag", "ServiceCast")
    fun generateNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE
        )

        val builder = NotificationCompat.Builder(applicationContext, channel_Id)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setContent(sendNotification(title, message))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channel_Id,
                channel_Name,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}




