package com.mdp.innovation.obd_driving.internal

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.mdp.innovation.obd_driving.util.Message
import java.util.*
import android.app.PendingIntent
import android.content.Intent.ACTION_MAIN
import com.mdp.innovation.obd_driving.ui.activity.HomeActivity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import com.mdp.innovation.obd_driving.R
import android.R.string.cancel
import android.app.Activity
import android.app.NotificationManager
import android.support.v4.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Color
import android.os.*
import android.support.v4.app.TaskStackBuilder
import android.support.v4.content.ContextCompat
import android.widget.RemoteViews
import com.mdp.innovation.obd_driving.ui.InterfaceView
import com.mdp.innovation.obd_driving.util.Constants
import com.mdp.innovation.obd_driving.util.Global


class CollectTripDataService : BaseService(){

    private lateinit var mHandler: Handler
    private lateinit var mRunnable: Runnable

    private var number = 0

    lateinit var resultReceiver : ResultReceiver

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        when(intent.action){
            Constants.STARTFOREGROUND_ACTION -> {
                Message.toastShort("Service started.", applicationContext)
                Log.i("[INFO]", "Service started.")

                mHandler = Handler()
                mRunnable = Runnable { showRandomNumber() }
                mHandler.postDelayed(mRunnable, 5000)

                addNotification()

                resultReceiver = intent.getParcelableExtra("onStop")

            }
            Constants.STOPFOREGROUND_ACTION -> {
                stopForeground(true)
                stopSelf()

                if(Global.appIsOpen){
                    if(resultReceiver != null) resultReceiver.send(0, Bundle())
                }else{
                    Global.isPendingToBack = true
                }


            }
        }



/*
        val notificationIntent = Intent(applicationContext, HomeActivity::class.java)
        notificationIntent.action = "SERVICE_COLLECT_DATA"
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val contentPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val playIntent = Intent(this, CollectTripDataService::class.java)
        playIntent.action = "GOOO"
        val pplayIntent = PendingIntent.getService(this,0, playIntent, 0)

        val icon = BitmapFactory.decodeResource(resources, R.drawable.abc_ab_share_pack_mtrl_alpha)

        val notification = NotificationCompat.Builder(this)
            .setContentTitle("Content title")
            .setTicker("Ticker")
            .setContentText("Content text")
            .setSmallIcon(R.drawable.ic_menu)
            .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_play, "Play",
                pplayIntent)
            //                .setDeleteIntent(contentPendingIntent)  // if needed
            .build()
        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR     // NO_CLEAR makes the notification stay when the user performs a "delete all" command
        startForeground(543, notification)*/

        //createAndShowForegroundNotification(this, 543)



        //startAppInForeground()

        return Service.START_STICKY
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

        val stopIntent = Intent(this, CollectTripDataService::class.java)
        stopIntent.action = Constants.STOPFOREGROUND_ACTION
        val pstopIntent = PendingIntent.getService(this,0, stopIntent, 0)

        /*var views = RemoteViews(packageName, R.layout.notification_layout)
        var playIntent = Intent(this, CollectTripDataService::class.java)
        playIntent.action = "ACTION_PLAY"
        var pplayIntent = PendingIntent.getService(this, 0, playIntent, 0);
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
            .addAction(R.drawable.ic_launcher_foreground, "Terminar viaje", pstopIntent)

        val notification = builder.build()

        notification.flags = Notification.FLAG_ONGOING_EVENT

        //notificationManager?.notify(NOTIFICATION_ID, notification)

        startForeground(543, notification)

    }

    fun startAppInForeground() {
        var views = RemoteViews(packageName, R.layout.notification_layout)

        // Define play control intent
           var playIntent = Intent(this, CollectTripDataService::class.java)
            playIntent.action = "ACTION_PLAY"

        // Use the above play intent to set into PendingIntent
            var pplayIntent = PendingIntent.getService(this, 0, playIntent, 0);

        // binding play button from layout to pending play intent defined above
        views.setOnClickPendingIntent(R.id.radio, pplayIntent)
        views.setImageViewResource(R.id.radio,
                    android.R.drawable.ic_lock_power_off)

        var status = Notification.Builder(this).build()

          status.flags = Notification.FLAG_ONGOING_EVENT
            status.icon = R.mipmap.ic_launcher
            status.contentIntent = pplayIntent

            startForeground(543, status)
    }

    /*var lastShownNotificationId = 1

    private fun createAndShowForegroundNotification(yourService: Service, notificationId: Int) {

        val icon = BitmapFactory.decodeResource(resources, android.R.drawable.ic_media_pause)

        val playIntent = Intent(this, CollectTripDataService::class.java)
        playIntent.action = "GOOO"
        val pplayIntent = PendingIntent.getService(this,0, playIntent, 0)

        val builder = getNotificationBuilder(
            yourService,
            "com.example.your_app.notification.CHANNEL_ID_FOREGROUND", // Channel id
            NotificationManagerCompat.IMPORTANCE_LOW
        ) //Low importance prevent visual appearance for this notification channel on top
        builder.setOngoing(true)
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .setContentTitle("Estamos recolectando tus datos")
            .setContentText("Puedes finalizar el viaje desde aquí.")
            .setColor(ContextCompat.getColor(baseContext, R.color.colorToolbar))
            .setStyle( NotificationCompat.BigTextStyle().setBigContentTitle("Titulo").bigText("Desripcion"))
            .setTicker("Ticker")
            .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
            .addAction(android.R.drawable.ic_media_pause, "Play",
                pplayIntent)

        val notification = builder.build()

        yourService.startForeground(notificationId, notification)

        if (notificationId != lastShownNotificationId) {
            // Cancel previous notification
            val nm = yourService.getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager
            nm.cancel(lastShownNotificationId)
        }
        lastShownNotificationId = notificationId
    }

    fun getNotificationBuilder(context: Context, channelId: String, importance: Int): NotificationCompat.Builder {
        val builder: NotificationCompat.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            prepareChannel(context, channelId, importance)
            builder = NotificationCompat.Builder(context, channelId)
        } else {
            builder = NotificationCompat.Builder(context)
        }
        return builder
    }

    @TargetApi(26)
    private fun prepareChannel(context: Context, id: String, importance: Int) {
        val appName = "App name"
        val description = "Description xxxxxxxxx"
        val nm = context.getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager

        if (nm != null) {
            var nChannel: NotificationChannel? = nm.getNotificationChannel(id)

            if (nChannel == null) {
                nChannel = NotificationChannel(id, appName, importance)
                nChannel.description = description
                nChannel.enableLights(true)
                nChannel.lightColor = Color.BLUE
                nm.createNotificationChannel(nChannel)
            }
        }
    }*/

    override fun onDestroy() {
        super.onDestroy()
        //toast("Service destroyed.")
        Toast.makeText(applicationContext, "Service destroyed.", Toast.LENGTH_SHORT).show()
        Log.i("[INFO]", "Service destroyed.")
        mHandler.removeCallbacks(mRunnable)
    }

    // Custom method to do a task
    private fun showRandomNumber() {
        number += 1
        //toast("Random Number : $number")
        Toast.makeText(applicationContext, "Random Number : $number", Toast.LENGTH_SHORT).show()
        Log.i("[INFO]", "Random Number : $number")
        mHandler.postDelayed(mRunnable, 5000)
    }

    override fun onCreate() {
        super.onCreate()
    }
}
