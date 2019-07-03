package jp.ac.asojuku.st.noffication_de_study

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class LocalNotificationTwoQuestionScheduleService : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 出題する問題を取得
        val examData = ExamData(1, "FE", "FE10901")

        // DBから問題を取得
        val helper = SQLiteHelper(context)
        val db = helper.readableDatabase
        val query = "SELECT * FROM questions WHERE question_flag = ?"
        val cursor = db.rawQuery(query, arrayOf("2"))
        var question:HashMap<Int, String> = hashMapOf()
        cursor.use { c ->
            c.moveToFirst()
            val array = ArrayList<HashMap<Int, String>>()
            for (i in 0 until c.count) {
                val map: HashMap<Int, String> = hashMapOf()
                map[c.getInt(c.getColumnIndex("question_id"))] =
                    c.getString(c.getColumnIndex("question"))
                array.add(map)
                c.moveToNext()
            }
            val random = Random().nextInt(array.size - 1)
            question = array[random]
            Log.d("TEST", question.toString())
        }

        examData.set_list_data(arrayListOf(1))
        val pendingIntent = PendingIntent.getActivity(
            context, 777, Intent(
                context,
                QuestionActivity::class.java
            ).putExtra("exam_data", examData), 0
        )

        val spEditor = context.getSharedPreferences("user_data", MODE_PRIVATE).edit()
        val spGetter = context.getSharedPreferences("user_data", MODE_PRIVATE)
        val notificationId = spGetter.getInt("notification_id", 0)

        // 通知の生成
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel("channel_1", "channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notification = NotificationCompat.Builder(context, "default")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setChannelId("channel_1")
            .setContentTitle("問題")
            .setContentText(question[1])
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setColor(Color.BLUE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.mipmap.ic_launcher,
                "○ 正しい",
                PendingIntent.getBroadcast(context, 0, Intent(context, LocalNotificationReceiver::class.java).apply {
                    action = "1"
                    putExtra("question_id", question[0])
                    putExtra("answer", 1)
                }, 0)
            )
            .addAction(
                R.mipmap.ic_launcher,
                "× 誤り",
                PendingIntent.getBroadcast(context, 0, Intent(context, LocalNotificationReceiver::class.java).apply {
                    action = "2"
                    putExtra("question_id", question[0])
                    putExtra("answer", 2)
                }, 0)
            )
            .build()
        notificationManager.notify(notificationId, notification)
        spEditor.putInt("notification_id", notificationId + 1).apply()

        // 通知をAlarmManagerに登録する
        this.registerNotice(context)
    }

    fun registerNotice(context: Context) {
        // 設定情報の読み込み
        val spGetter = context.getSharedPreferences("user_data", MODE_PRIVATE)
        val startTime = spGetter.getString("NDS_Start", "09:00") as String
        val endTime = spGetter.getString("NDS_End", "21:00") as String
        val endTimeInt = Integer.valueOf(endTime.replace(":", ""))
        val startTimeList: List<String> = startTime.split(Regex(":"))

        if (spGetter.getBoolean("NDS_check", false)) {
            // 通知で出題する
            val spaceTime = spGetter.getString("NDS_Interval", "5") as String

            val intent = Intent(context, LocalNotificationTwoQuestionScheduleService::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()

            val sdf = SimpleDateFormat("HHmm", Locale.getDefault())
            val nowTime = sdf.format(calendar.time) as String

            if (endTimeInt <= nowTime.toInt()) {
                // 現在の時間が出題する最終時間を超えていたら次の日に出題
                calendar.add(Calendar.DATE, 1)
                calendar.set(Calendar.HOUR, startTimeList[0].toInt())
                calendar.set(Calendar.MINUTE, startTimeList[1].toInt())
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
            } else {
                // 現在の時間に通知間隔を加算して出題
                calendar.add(Calendar.SECOND, spaceTime.toInt())
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } else {
            // 通知をキャンセル（できてるはず）
            val intent = Intent(context, LocalNotificationTwoQuestionScheduleService::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }

}

/*
val notification = NotificationCompat.Builder(context, "default")
    .setSmallIcon(R.drawable.ic_launcher_foreground)
    .setContentTitle("問題")
    .setContentText("WAFはWeb Application Firewallの略称である")
    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
    .setColor(Color.BLUE)
    .setContentIntent(pendingIntent)
    .setAutoCancel(true)
    .addAction(
        R.mipmap.ic_launcher,
        "○ 正しい",
        PendingIntent.getBroadcast(context, 0, Intent(context, LocalNotificationReceiver::class.java).apply {
            action = "true"
            putExtra("answer", true)
        }, 0)
    )
    .addAction(
        R.mipmap.ic_launcher,
        "× 誤り",
        PendingIntent.getBroadcast(context, 0, Intent(context, LocalNotificationReceiver::class.java).apply {
            action = "false"
            putExtra("answer", false)
        }, 0)
    )
    .build()
val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
notificationManager.notify(notificationId, notification)
*/