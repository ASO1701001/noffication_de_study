package jp.ac.asojuku.st.noffication_de_study

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v4.app.NotificationCompat
import android.widget.Toast

class LocalNotificationTwoReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val questionId = intent.getIntExtra("question_id", 0)
        val userAnswer = intent.getIntExtra("user_answer", 0)
        if (questionId == 0 || userAnswer == 0) {
            return Toast.makeText(context, "採点できませんでした", Toast.LENGTH_LONG).show()
        }

        val helper = SQLiteHelper(context)
        val db = helper.readableDatabase
        val query = "SELECT * FROM answers WHERE question_id = ?"
        val cursor = db.rawQuery(query, arrayOf(questionId.toString()))
        var questionAnswer = 0
        cursor.use { c ->
            c.moveToFirst()
            for (i in 0 until c.count) {
                questionAnswer = c.getInt(c.getColumnIndex("answer_number"))
                c.moveToNext()
            }
        }
        if (questionAnswer == 0) {
            return Toast.makeText(context, "採点できませんでした", Toast.LENGTH_LONG).show()
        }
        val markingResult = if (userAnswer == questionAnswer) {
            "正解です！"
        } else {
            "外れです..."
        }

        val examData = ExamData(4, "FE", "FE10901")
        examData.set_list_data(arrayListOf(questionId))
        val pendingIntent = PendingIntent.getActivity(
            context, (Math.random() * 100000).toInt(), Intent(
                context,
                QuestionActivity::class.java
            ).putExtra("exam_data", examData), PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationId = intent.getIntExtra("notification_id", 0)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, "channel_two_question")
            .setSmallIcon(R.mipmap.notification_de_study_logo7)
            .setChannelId("channel_two_question")
            .setContentTitle("回答結果")
            .setContentText(markingResult)
            .setContentIntent(pendingIntent)
            .setColor(Color.BLUE)
            .setAutoCancel(true)
            .addAction(
                R.mipmap.ic_launcher,
                "もう一度解く",
                PendingIntent.getActivity(
                    context,
                    (Math.random() * 100000).toInt(),
                    Intent(context, QuestionActivity::class.java).apply {
                        val questionExamData = ExamData(4, "FE", "FE10901")
                        questionExamData.set_list_data(arrayListOf(questionId))
                        putExtra("exam_data", questionExamData)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .addAction(
                R.mipmap.ic_launcher,
                "回答画面へ",
                PendingIntent.getActivity(
                    context,
                    (Math.random() * 100000).toInt(),
                    Intent(context, AnswerActivity::class.java).apply {
                        val answerExamData = ExamData(4, "FE", "FE10901")
                        answerExamData.set_list_data(arrayListOf(questionId))
                        answerExamData.question_current = questionId
                        answerExamData.question_next = questionId
                        answerExamData.answered_list.add(userAnswer)
                        putExtra("exam_data", answerExamData)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .build()
        notificationManager.notify(notificationId, notification)
    }

}