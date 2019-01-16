package com.mdp.innovation.obd_driving.internal.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mdp.innovation.obd_driving.R
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService()  {

    private lateinit var notificationManager: NotificationManager
    private val ADMIN_CHANNEL_ID = "01"

    override fun onMessageReceived(message: RemoteMessage){
        super.onMessageReceived(message)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels()
        }
        val notificationId = Random().nextInt(60000)

        val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
            .setSmallIcon(R.mipmap.icon)
            .setContentTitle(message.notification!!.title)
            .setContentText(message.notification!!.body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle())
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        notificationManager.notify(0, notificationBuilder.build())

        val handler = Handler(Looper.getMainLooper())
        handler.post{
            Toast.makeText(applicationContext, "Llegó un PUSH!", Toast.LENGTH_SHORT).show()
        }


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels(){
        val adminChannelName = "Canal 01"
        val adminChannelDescription = "Canal principal"

        val adminChannel = NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_LOW)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel)
        }
    }

}