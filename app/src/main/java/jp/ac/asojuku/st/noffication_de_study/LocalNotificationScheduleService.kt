package jp.ac.asojuku.st.noffication_de_study

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Color
import android.support.v4.app.NotificationCompat
import android.widget.RemoteViews
import jp.ac.asojuku.st.noffication_de_study.db.AnswersOpenHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LocalNotificationScheduleService : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val spGetter = context.getSharedPreferences("user_data", MODE_PRIVATE)
        // 四択問題・二択問題どちらを出題するか設定から読み込み出題
        when (spGetter.getString("way_radio_select", "four")) {
            "four" -> {
                this.fourQuestion(context)
            }
            "two" -> {
                this.twoQuestion(context)
            }
        }

        // 通知をAlarmManagerに登録する
        this.registerNotice(context)
    }

    // 四択問題の通知の生成
    fun fourQuestion(context: Context) {
        val helper = SQLiteHelper(context)
        val db = helper.readableDatabase

        val query = "SELECT * FROM questions WHERE is_have_image = 0 AND question_flag = 1"
        val cursor = db.rawQuery(query, null)

        var question: HashMap<Int, String> = hashMapOf()
        cursor.use { c ->
            c.moveToFirst()
            val array = ArrayList<HashMap<Int, String>>()
            for (i in 0 until c.count) {
                val map: HashMap<Int, String> = hashMapOf()
                if (c.getString(c.getColumnIndex("question")).count() < 300) {
                    map[c.getInt(c.getColumnIndex("question_id"))] =
                        c.getString(c.getColumnIndex("question"))
                    array.add(map)
                }
                c.moveToNext()
            }
            val random = Random().nextInt(array.size - 1)
            question = array[random]
            if (question.isNullOrEmpty()) return
        }

        val questionId = question.keys.first()
        val questionText = question[question.keys.first()]

        if (questionText == null) {
            return
        }

        val contentView = RemoteViews(context.packageName, R.layout.notifi_layout)

        val intent1 = Intent(context, AnswerActivity::class.java)
        val intent2 = Intent(context, AnswerActivity::class.java)
        val intent3 = Intent(context, AnswerActivity::class.java)
        val intent4 = Intent(context, AnswerActivity::class.java)
        val intentQuestion = Intent(context, QuestionActivity::class.java)

        // 通知ボタンタップ時に渡す値
        intent1.putExtra("question_id", questionId)
        intent2.putExtra("question_id", questionId)
        intent3.putExtra("question_id", questionId)
        intent4.putExtra("question_id", questionId)

        // ExamDataを生成＆整形
        val data1 = ExamData(3, "", "")
        data1.set_list_data(arrayListOf(questionId))
        val data2 = ExamData(3, "", "")
        data2.set_list_data(arrayListOf(questionId))
        val data3 = ExamData(3, "", "")
        data3.set_list_data(arrayListOf(questionId))
        val data4 = ExamData(3, "", "")
        data4.set_list_data(arrayListOf(questionId))
        val data5 = ExamData(3, "", "")
        data5.set_list_data(arrayListOf(questionId))
        data1.question_current = questionId
        data2.question_current = questionId
        data3.question_current = questionId
        data4.question_current = questionId
        data5.question_current = questionId
        data1.question_next = questionId
        data2.question_next = questionId
        data3.question_next = questionId
        data4.question_next = questionId
        data5.question_next = questionId
        fourQuestionRegAnswer(0, data1, context)
        fourQuestionRegAnswer(1, data2, context)
        fourQuestionRegAnswer(2, data3, context)
        fourQuestionRegAnswer(3, data4, context)

        // 生成したExamDataをインテントに登録
        intent1.putExtra("exam_data", data1)
        intent2.putExtra("exam_data", data2)
        intent3.putExtra("exam_data", data3)
        intent4.putExtra("exam_data", data4)
        intentQuestion.putExtra("exam_data", data5)

        // 各intentをPendingIntentに変換
        val pi1 = PendingIntent.getActivity(context, 1983418741, intent1, PendingIntent.FLAG_UPDATE_CURRENT)
        contentView.setOnClickPendingIntent(R.id.notifyButton, pi1)
        val pi2 = PendingIntent.getActivity(context, 1983418742, intent2, PendingIntent.FLAG_UPDATE_CURRENT)
        contentView.setOnClickPendingIntent(R.id.notifyButton2, pi2)
        val pi3 = PendingIntent.getActivity(context, 1983418743, intent3, PendingIntent.FLAG_UPDATE_CURRENT)
        contentView.setOnClickPendingIntent(R.id.notifyButton3, pi3)
        val pi4 = PendingIntent.getActivity(context, 1983418744, intent4, PendingIntent.FLAG_UPDATE_CURRENT)
        contentView.setOnClickPendingIntent(R.id.notifyButton4, pi4)
        val piQuestion =
            PendingIntent.getActivity(context, 1983418745, intentQuestion, PendingIntent.FLAG_CANCEL_CURRENT)

        // 表示するテキスト
        contentView.setTextViewText(R.id.notifiText, questionText)

        contentView.setTextColor(R.id.notifiText, Color.DKGRAY)
        contentView.setTextColor(R.id.notifyButton, Color.WHITE)
        contentView.setTextColor(R.id.notifyButton2, Color.WHITE)
        contentView.setTextColor(R.id.notifyButton3, Color.WHITE)
        contentView.setTextColor(R.id.notifyButton4, Color.WHITE)

        // 表示内容を生成
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, "channel_four_question")
            .setChannelId("channel_four_question")
            .setCustomBigContentView(contentView)
            .setContentTitle("問題です")
            .setContentIntent(piQuestion)
            .setSmallIcon(R.mipmap.notification_de_study_logo8)
            .setAutoCancel(true)
            .build()

        // 表示
        notificationManager.notify(999, notification)
    }

    // 四択問題の正誤判定
    private fun fourQuestionRegAnswer(choice_number: Int, examData: ExamData, context: Context) {
        // 自分の解答を登録
        examData.answered_list.add(choice_number)
        //正解をDBから取得
        val answers = SQLiteHelper(context)
        val db = answers.readableDatabase
        val AOH = AnswersOpenHelper(db)
        val answer = AOH.find_answers(examData.question_current)?.get(1) // 正しい正解
        var isCorrected = false // 正解だった場合にtrueにする
        if (choice_number == answer) {
            isCorrected = true
        }
        examData.isCorrect_list.add(isCorrected)// 解いた問題が正解だったかどうかがBoolean型で入る
    }

    // 二択問題の通知の生成
    fun twoQuestion(context: Context) {
        // 出題する問題を取得
        val examData = ExamData(4, "FE", "FE10901")

        // DBから問題を取得
        val helper = SQLiteHelper(context)
        val db = helper.readableDatabase
        val query = "SELECT * FROM questions WHERE question_flag = ?"
        val cursor = db.rawQuery(query, arrayOf("2"))
        var question: HashMap<Int, String> = hashMapOf()
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
            if (question.isNullOrEmpty()) return
        }

        val questionId = question.keys.first()
        val questionContent = question[question.keys.first()]

        examData.set_list_data(arrayListOf(questionId))
        val pendingIntent = PendingIntent.getActivity(
            context, (Math.random() * 100000).toInt(), Intent(
                context,
                QuestionActivity::class.java
            ).putExtra("exam_data", examData), PendingIntent.FLAG_UPDATE_CURRENT
        )

        val spEditor = context.getSharedPreferences("user_data", MODE_PRIVATE).edit()
        val spGetter = context.getSharedPreferences("user_data", MODE_PRIVATE)
        val notificationId = spGetter.getInt("notification_id", 0)

        // 通知の生成
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, "channel_two_question")
            .setSmallIcon(R.mipmap.notification_de_study_logo8)
            .setChannelId("channel_two_question")
            .setContentTitle("問題")
            .setContentText(questionContent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setColor(Color.BLUE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.mipmap.ic_launcher,
                "○ 正しい",
                PendingIntent.getBroadcast(
                    context,
                    (Math.random() * 100000).toInt(),
                    Intent(context, LocalNotificationTwoReceiver::class.java).apply {
                        action = "1"
                        putExtra("question_id", questionId)
                        putExtra("user_answer", 1)
                        putExtra("notification_id", notificationId)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .addAction(
                R.mipmap.ic_launcher,
                "× 誤り",
                PendingIntent.getBroadcast(
                    context,
                    (Math.random() * 100000).toInt(),
                    Intent(context, LocalNotificationTwoReceiver::class.java).apply {
                        action = "2"
                        putExtra("question_id", questionId)
                        putExtra("user_answer", 2)
                        putExtra("notification_id", notificationId)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .build()
        notificationManager.notify(notificationId, notification)
        spEditor.putInt("notification_id", notificationId + 1).apply()
    }

    // 通知de勉強モード
    fun registerNotice(context: Context) {
        // 設定情報の読み込み
        val spGetter = context.getSharedPreferences("user_data", MODE_PRIVATE)
        val startTime = spGetter.getString("NDS_Start", "09:00") as String
        val endTime = spGetter.getString("NDS_End", "21:00") as String
        val endTimeList = endTime.split(Regex(":"))
        val startTimeList = startTime.split(Regex(":"))

        if (spGetter.getBoolean("NDS_check", false)) {
            // 通知で出題する
            val spaceTime = spGetter.getString("NDS_Interval", "5") as String

            val intent = Intent(context, LocalNotificationScheduleService::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()

            val hourOfDay = SimpleDateFormat("HH", Locale.getDefault()).format(calendar.time).toInt()
            val minuteOfDay = SimpleDateFormat("mm", Locale.getDefault()).format(calendar.time).toInt()

            if (hourOfDay < Integer.parseInt(startTimeList[0]) ||
                hourOfDay == Integer.parseInt(startTimeList[0]) && minuteOfDay <= Integer.parseInt(startTimeList[1]) ||
                hourOfDay > Integer.parseInt(endTimeList[0]) ||
                hourOfDay == Integer.parseInt(endTimeList[0]) && minuteOfDay >= Integer.parseInt(endTimeList[1])
            ) {
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
            // 通知をキャンセル
            val intent = Intent(context, LocalNotificationScheduleService::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }

}
