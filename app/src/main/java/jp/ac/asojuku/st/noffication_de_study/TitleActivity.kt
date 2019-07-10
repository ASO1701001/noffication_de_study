package jp.ac.asojuku.st.noffication_de_study

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_title.*
import org.jetbrains.anko.startActivity

class TitleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_title)

        TA_Question_BTN.setSafeClickListener {
            startActivity<QuestionOptionActivity>()
        }

        TA_Statics_BTN.setSafeClickListener {
            startActivity<StaticsActivity>()
        }

        TA_Option_BTN.setSafeClickListener {
            startActivity<OptionActivity>()
        }

    }

    override fun onResume() {
        super.onResume()

        val data = getSharedPreferences("user_data", MODE_PRIVATE)
        val userId = data.getString("user_id", "0")
    }

    //Android端末側の戻るボタンを押した時の処理を上書き
    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("終了しますか？")
            .setPositiveButton("はい") { dialog, which ->
                super.onBackPressed()
            }.show()
        return //バックキーを押した時に反応しないようにする

    }

}
