package jp.ac.asojuku.st.noffication_de_study

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.support.v4.app.NotificationCompat

class LocalNotificationForegroundService : Service() {
    lateinit var mReceiver: BroadcastReceiver

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val spGetter = getSharedPreferences("user_data", MODE_PRIVATE)
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_SCREEN_ON) {
                    // 四択問題・二択問題どちらを出題するか設定から読み込み出題
                    val service = LocalNotificationScheduleService()
                    when (spGetter.getString("way_radio_select", "four")) {
                        "four" -> {
                            service.fourQuestion(context)
                        }
                        "two" -> {
                            service.twoQuestion(context)
                        }
                    }
                }
            }
        }

        registerReceiver(mReceiver, IntentFilter(Intent.ACTION_SCREEN_ON))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val notification = NotificationCompat.Builder(applicationContext, "channel_screen_question").apply {
            setSmallIcon(R.mipmap.notification_de_study_logo7)
            setChannelId("channel_screen_question")
            setContentTitle("通知de勉強")
            setContentText("サービス作動中")
            setContentIntent(
                PendingIntent.getActivity(
                    applicationContext,
                    777,
                    Intent(applicationContext, MainActivity::class.java),
                    0
                )
            )
            setWhen(System.currentTimeMillis())
            setAutoCancel(false)
        }.build()

        startForeground(1, notification)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(mReceiver)
    }

}
