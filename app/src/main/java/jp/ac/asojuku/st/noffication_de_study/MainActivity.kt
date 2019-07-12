package jp.ac.asojuku.st.noffication_de_study

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import jp.ac.asojuku.st.noffication_de_study.db.*
import org.jetbrains.anko.startActivity
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 通知のバージョン差対応
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 二択問題通知チャンネル
            var notificationChannel =
                NotificationChannel("channel_two_question", "二択問題", NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            notificationManager.createNotificationChannel(notificationChannel)

            // 四択問題通知チャンネル
            notificationChannel =
                NotificationChannel("channel_four_question", "四択問題", NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            notificationManager.createNotificationChannel(notificationChannel)

            // サービス通知チャンネル
            notificationChannel =
                NotificationChannel("channel_screen_question", "サービス", NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            notificationManager.createNotificationChannel(notificationChannel)
        }

        ApiGetTask {
            if (!it.isNullOrEmpty()) {
                allUpdate(JSONObject(it))
            } else {
                Toast.makeText(this, "APIの通信に失敗しました(´･ω･`)", Toast.LENGTH_SHORT).show()
            }
            startActivity<TitleActivity>()
            finish()
        }.execute("db-update.php", hashMapOf("last_update_date" to findLastUpdate()).toString())
    }

    // 最終アップデートの日付を、yyyy-MM-dd のフォーマットでStringとして返す。
    private fun findLastUpdate(): String {
        val questions = SQLiteHelper(this)
        val db = questions.readableDatabase
        val query = "SELECT update_date FROM questions ORDER BY update_date desc limit 1"
        val cursor: Cursor

        var result = "2019-05-06"
        return try {
            cursor = db.rawQuery(query, null)
            cursor.moveToFirst()
            result = cursor.getString(0).toString()
            cursor.close()
            db.close()
            result
        } catch (e: Exception) {
            db.close()
            result
        }
    }

    // 受け取った全ての値をDBに登録する。
    private fun allUpdate(callback: JSONObject): Boolean {
        var json = callback
        if (json.getString("status") != "S00") {
            return false
        }

        json = json.getJSONObject("data")
        val db = SQLiteHelper(this).writableDatabase

        val answers = AnswersOpenHelper(db)
        val answers_rate = AnswersRateOpenHelper(db)
        val exams_numbers = ExamsNumbersOpenHelper(db)
        val exams_questions = ExamsQuestionsOpenHelper(db)
        val genres = GenresOpenHelper(db)
        val image = ImageOpenHelper(db)
        val questions = QuestionsOpenHelper(db)
        val questions_genres = QuestionsGenresOpenHelper(db)
        image.setDefaultRecord()

        image.setDefaultRecoad()

        var jArray = json.getJSONArray("answer_db")
        if (jArray != {}) {
            for (i in 0 until jArray.length()) {
                answers.add_record(
                    jArray.getJSONObject(i).getInt("question_id"),
                    jArray.getJSONObject(i).getInt("answer_number")
                )
            }
        }
        jArray = json.getJSONArray("answers_rate_db")
        if (jArray != {}) {
            for (i in 0 until jArray.length()) {
                answers_rate.add_record(
                    jArray.getJSONObject(i).getInt("question_id"),
                    jArray.getJSONObject(i).getDouble("answer_rate")
                )
            }
        }
        jArray = json.getJSONArray("exams_numbers_db")
        if (jArray != {}) {
            for (i in 0 until jArray.length()) {
                exams_numbers.add_record(
                    jArray.getJSONObject(i).getInt("exam_id")
                    , jArray.getJSONObject(i).getString("exams_number")
                )
            }
        }
        jArray = json.getJSONArray("exams_questions_db")
        if (jArray != {}) {
            for (i in 0 until jArray.length()) {
                exams_questions.add_record(
                    jArray.getJSONObject(i).getInt("exam_id"),
                    jArray.getJSONObject(i).getString("exams_number"),
                    jArray.getJSONObject(i).getInt("question_id"),
                    jArray.getJSONObject(i).getInt("question_number")
                )
            }
        }
        jArray = json.getJSONArray("genres_db")
        if (jArray != {}) {
            for (i in 0 until jArray.length()) {
                genres.add_record(
                    jArray.getJSONObject(i).getInt("genre_id"),
                    jArray.getJSONObject(i).getString("genre_name")
                )
            }
        }
        jArray = json.getJSONArray("image_db")
        if (jArray != {}) {
            for (i in 0 until jArray.length()) {
                image.add_record(
                    jArray.getJSONObject(i).getInt("question_id"),
                    jArray.getJSONObject(i).getString("file_name")
                )
            }
        }
        jArray = json.getJSONArray("questions_db")
        if (jArray != {}) {
            for (i in 0 until jArray.length()) {
                questions.add_record(
                    jArray.getJSONObject(i).getInt("question_id"),
                    unEscapeHTML(jArray.getJSONObject(i).getString("question")),
                    jArray.getJSONObject(i).getInt("is_have_image"),
                    unEscapeHTML(jArray.getJSONObject(i).getString("comment")),
                    jArray.getJSONObject(i).getString("update_date"),
                    jArray.getJSONObject(i).getInt("question_flag")
                )
            }
        }
        jArray = json.getJSONArray("questions_genres_db")
        if (jArray != {}) {
            for (i in 0 until jArray.length()) {
                questions_genres.add_record(
                    jArray.getJSONObject(i).getInt("question_id"),
                    jArray.getJSONObject(i).getInt("genre_id")
                )
            }
        }
        db.close()
        return true
    }

    // 端末に登録されているトークンをAPIサーバに送信し、ユーザーIDを受け取る
    /*
    fun get_user_id(token: String): Boolean {
        var result: Boolean = true
        ApiPostTask {
            if (JSONObject(it).getString("status") != "E00") {
                val e: SharedPreferences.Editor =
                    getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE).edit()
                e.putString("user_id", JSONObject(it).getJSONObject("data").getString("user_id")).apply()

            } else {
                result = false
            }
        }.execute("add-user.php", hashMapOf("token" to token).toString())
        return result
    }
    */

    // 文字列中のHTML特殊文字を変換して、変換後の文字列を返す
    private fun unEscapeHTML(str: String): String {
        return str.replace("&quot;".toRegex(), "\"")
            .replace("&lt;".toRegex(), "<")
            .replace("&gt;".toRegex(), ">")
            .replace("&amp;".toRegex(), "&")
    }
}
