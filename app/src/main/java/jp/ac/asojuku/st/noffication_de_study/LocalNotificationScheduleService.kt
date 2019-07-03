package jp.ac.asojuku.st.noffication_de_study

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.widget.RemoteViews
import jp.ac.asojuku.st.noffication_de_study.db.AnswersOpenHelper
import jp.ac.asojuku.st.noffication_de_study.db.QuestionsOpenHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LocalNotificationScheduleService : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val spGetter = context.getSharedPreferences("user_data", MODE_PRIVATE)
        // 四択問題・二択問題どちらを出題するか設定から読み込み出題
        when (spGetter.getString("way_radio_select", "two")) {
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fourQuestion(context: Context) {
        val questionId = kotlin.random.Random.nextInt(80) + 1

        val db = SQLiteHelper(context).readableDatabase
        val dbHelper = QuestionsOpenHelper(db)

        val questionText = dbHelper.find_question(questionId)
        if (questionText == null) {
            return
        }
        questionText[1].replace("&quot;".toRegex(), "\"")
        questionText[2].replace("&quot;".toRegex(), "\"")


        val mChannel = NotificationChannel("0", "問題通知", NotificationManager.IMPORTANCE_HIGH)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
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
        contentView.setTextViewText(R.id.notifiText, questionText[1])

        // 表示内容を生成
        val notification = NotificationCompat.Builder(context, "0")
            .setCustomBigContentView(contentView)
            .setContentTitle("問題です")
            .setContentIntent(piQuestion)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        // 表示
        notificationManager.notify(questionId, notification)
    }

    private fun fourQuestionRegAnswer(choice_number: Int, examData: ExamData, context: Context) {
        //自分の解答を登録
        examData.answered_list.add(choice_number)
        //正解をDBから取得
        val answers = SQLiteHelper(context)
        val db = answers.readableDatabase
        val AOH = AnswersOpenHelper(db)
        val answer = AOH.find_answers(examData.question_current)?.get(1) //正しい正解
        var isCorrected = false //正解だった場合にtrueにする
        if (choice_number == answer) {
            isCorrected = true
        }
        examData.isCorrect_list.add(isCorrected)//解いた問題が正解だったかどうかがBoolean型で入る
    }

    private fun twoQuestion(context: Context) {
        // 出題する問題を取得
        val examData = ExamData(3, "FE", "FE10901")

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
        // 後日AnswerActivityに飛ばす処理を実装
        val pendingIntent = PendingIntent.getActivity(
            context, 777, Intent(
                context,
                TitleActivity::class.java
            ).putExtra("exam_data", examData), 0
        )

        val spEditor = context.getSharedPreferences("user_data", MODE_PRIVATE).edit()
        val spGetter = context.getSharedPreferences("user_data", MODE_PRIVATE)
        val notificationId = spGetter.getInt("notification_id", 0)

        // 通知の生成
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, "default")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setChannelId("channel_question")
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
                PendingIntent.getBroadcast(context, 0, Intent(context, LocalNotificationTwoReceiver::class.java).apply {
                    action = "1"
                    putExtra("question_id", questionId)
                    putExtra("user_answer", 1)
                }, 0)
            )
            .addAction(
                R.mipmap.ic_launcher,
                "× 誤り",
                PendingIntent.getBroadcast(context, 0, Intent(context, LocalNotificationTwoReceiver::class.java).apply {
                    action = "2"
                    putExtra("question_id", questionId)
                    putExtra("user_answer", 2)
                }, 0)
            )
            .build()
        notificationManager.notify(notificationId, notification)
        spEditor.putInt("notification_id", notificationId + 1).apply()
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

            val intent = Intent(context, LocalNotificationScheduleService::class.java)
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
            val intent = Intent(context, LocalNotificationScheduleService::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }

}
