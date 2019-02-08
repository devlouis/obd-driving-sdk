package com.mdp.innovation.obd_driving.internal.firebase

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mdp.innovation.obd_driving.R
import com.mdp.innovation.obd_driving.interactor.TokenInteractor
import com.mdp.innovation.obd_driving.service.model.UpdateTokenResponse
import com.mdp.innovation.obd_driving.ui.activity.HomeActivity
import com.mdp.innovation.obd_driving.util.Constants
import com.mdp.innovation.obd_driving.util.Preferences
import org.koin.android.ext.android.inject
import java.util.*

class MyFirebaseMessagingService : FirebaseMessagingService()  {

    private val TAG = javaClass.simpleName
    private lateinit var notificationManager: NotificationManager
    private val ADMIN_CHANNEL_ID = "01"

    private val TYPE_TRIP_FINISHED = "1"

    private val preferences by inject<Preferences>()

    override fun onMessageReceived(message: RemoteMessage){
        super.onMessageReceived(message)

        //addNotification()

        val title = message.data["title"].toString()
        val body = message.data["body"].toString()
        val type = message.data["type"].toString()
        val score = message.data["score"].toString()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels()
        }
        val notificationId = Random().nextInt(60000)

        /*val resultIntent = Intent(this, HomeActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(HomeActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)*/

        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        val contentIntent = PendingIntent.getActivity(this, Random().nextInt(), intent, 0)

        val notificationBuilder = NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
            .setSmallIcon(R.mipmap.icon)
            //.setContentTitle(message.notification!!.title)
            //.setContentText(message.notification!!.body)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(contentIntent)
            .setStyle(NotificationCompat.BigTextStyle())
            .setAutoCancel(true)
            .setOngoing(false)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        val notification = notificationBuilder.build()
        notification.flags = Notification.FLAG_ONGOING_EVENT or Notification.FLAG_AUTO_CANCEL

        notificationManager.notify(0, notification)

        when(type){
            TYPE_TRIP_FINISHED -> tripFinished(score)
        }

    }

    private fun tripFinished(score: String){
        val handler = Handler(Looper.getMainLooper())
        handler.post{
            Toast.makeText(applicationContext, "Llegó el PUSH!: $score", Toast.LENGTH_SHORT).show()

            preferences.setMyScore(applicationContext, score)

            val intent = Intent(Constants.ACTION_BROADCAST_PUSH)
            intent.putExtra(Constants.ACTION_TYPE_PUSH, Constants.TYPE_TRIP_FINISHED)
            intent.putExtra(Constants.DATA_NEW_SCORE, score)
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun setupChannels(){
        val adminChannelName = "Canal 01"
        val adminChannelDescription = "Canal principal"

        val adminChannel = NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        if(preferences.getDataUser(applicationContext) != null){
            //ENDPOINT ADD TOKEN PUSH

            /*val dataUser = preferences.getDataUser(applicationContext)

            val interactor = TokenInteractor()
            interactor.updateToken(object: TokenInteractor.OnUpdateTokenFinishedListener{
                override fun onUpdateTokenSuccess(response: UpdateTokenResponse) {
                    Log.d(TAG,"El token se actualizó correctamente.")
                }

                override fun onUpdateTokenError(message: String) {
                    Log.d(TAG,"Error: $message")
                }
            }, dataUser!!.userId!!, token, dataUser!!.token!!)*/
        }

        preferences.setTokenPush(applicationContext, token)
    }

    private fun addNotification() {

        val CHANNEL_ID = "my_channel_01"
        val name = "my_channel"
        val Description = "This is my channel"

        val NOTIFICATION_ID = 234

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = Description
            mChannel.enableLights(true)
            mChannel.lightColor = resources.getColor(R.color.colorToolbar)
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mChannel.setShowBadge(true)

            notificationManager?.createNotificationChannel(mChannel)

        }


        val resultIntent = Intent(this, HomeActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(HomeActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        /*val stopIntent = Intent(this, CollectTripDataService::class.java)
        stopIntent.action = Constants.STOPFOREGROUND_ACTION
        val pstopIntent = PendingIntent.getIntentService(this,0, stopIntent, 0)*/

        /*var views = RemoteViews(packageName, R.layout.notification_layout)
        var playIntent = Intent(this, CollectTripDataService::class.java)
        playIntent.action = "ACTION_PLAY"
        var pplayIntent = PendingIntent.getIntentService(this, 0, playIntent, 0);
        views.setOnClickPendingIntent(R.id.radio, pplayIntent)*/

        val icon = BitmapFactory.decodeResource(resources, android.R.drawable.ic_media_pause)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Estamos recolectando tus datos")
            //.setContentText("Puedes finalizar el viaje desde aquí.")
            //.setStyle(NotificationCompat.BigTextStyle().bigText("Notice that the NotificationCompat.Builder constructor requires that you provide a channel ID. This is required for compatibility with Android 8.0 (API level 26) and higher, but is ignored by older versions By default, the notification's text content is truncated to fit one line. If you want your notification to be longer, you can enable an expandable notification by adding a style template with setStyle(). For example, the following code creates a larger text area"))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(resultPendingIntent)
            //.setCustomContentView(views)
            .setAutoCancel(true)
            .setProgress(100, 30, true)
            //.setCategory(Notification.CATEGORY_PROGRESS)
            //.addExtras( Bundle())
            .setColor(resources.getColor(R.color.colorToolbar))
            //.addAction(R.drawable.ic_launcher_foreground, "Terminar viaje", pstopIntent)

        val notification = builder.build()

        notification.flags = Notification.FLAG_ONGOING_EVENT

        notificationManager?.notify(NOTIFICATION_ID, notification)

        //startForeground(543, notification)

    }

}