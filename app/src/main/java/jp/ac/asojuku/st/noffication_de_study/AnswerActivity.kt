package jp.ac.asojuku.st.noffication_de_study

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import jp.ac.asojuku.st.noffication_de_study.db.AnswersOpenHelper
import jp.ac.asojuku.st.noffication_de_study.db.ExamsQuestionsOpenHelper
import jp.ac.asojuku.st.noffication_de_study.db.QuestionsOpenHelper
import kotlinx.android.synthetic.main.activity_answer.*
import org.jetbrains.anko.startActivity

class AnswerActivity : AppCompatActivity() {
    lateinit var user_id: String
    lateinit var exam_data: ExamData

    var question_id = -1
    lateinit var exam_number: String
    var answer_text: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_answer)

        user_id = getSharedPreferences("user_data", MODE_PRIVATE).getString("user_id", "999999")
        exam_data = intent.getSerializableExtra("exam_data") as ExamData

        at_first()

        AA_Back_BTN.setSafeClickListener {
            finish()
        }

        AA_Next_BTN.setSafeClickListener {
            finish()
        }
    }

    fun at_first() {
        val questions = SQLiteHelper(this)
        val db = questions.readableDatabase
        val questionsDB = QuestionsOpenHelper(db)
        val examsquestionDB = ExamsQuestionsOpenHelper(db)
        when (exam_data.mac) {
            //QuestionOptionActivityから
            1 -> {
                AA_Next_BTN.setSafeClickListener {
                    startActivity<QuestionActivity>("exam_data" to exam_data)
                }
                AA_Next_BTN.visibility = View.VISIBLE
            }
            //FragmentQuestion.ktから
            2 -> {
                AA_Next_BTN.setText("統計に戻る")
                AA_Next_BTN.setSafeClickListener {
                    finish()
                }
            }
            //4択通知から
            3 -> {
                AA_Next_BTN.setText("戻る")
                AA_Next_BTN.setSafeClickListener {
                    finish()
                }
            }
            //○×から
            4 -> {
                AA_Next_BTN.setText("戻る")
                AA_Next_BTN.setSafeClickListener {
                    finish()
                }
            }
        }
        question_id = exam_data.question_current
        answer_text = questionsDB.find_comment(question_id)?.get(1)
        exam_number = examsquestionDB.find_exam_number_from_question_id(question_id).toString()
        print_answer()
    }

    fun print_answer() {
        val questions = SQLiteHelper(this)
        val db = questions.readableDatabase
        val questionsDB = QuestionsOpenHelper(db)
        val answersDB = AnswersOpenHelper(db)

        //出題年度を表示
        answer_examNumber_text.setText(exam_number)

        //正解を取得。○×問題の場合は処理を分ける
        var answerList = answersDB.find_answers(question_id)
        var answer = ""
        var sentakusi = listOf<String>()
        if (exam_data.mac != 4) {
            sentakusi = listOf("ア", "イ", "ウ", "エ")
            var answerNo = answerList!!.get(1)
            answer = sentakusi[answerNo]
        } else {
            sentakusi = listOf("○", "×")
            var answerNo = answerList!!.get(1) - 1
            answer = sentakusi[answerNo]
        }

        //改行コードの取得//
        val BR: String = System.getProperty("line.separator")
        var examNumbers: String = ""
//        for (i in 0..examNumberList!!.size-1){
//            examNumbers += examNumberList[i] + BR
//        }
        //TODO:DB側が作成されたら更新
//        examNumbers += "試験回" + BR

        //自分の解答と、正しい解答の文字列を生成。○×問題の場合は、処理を分ける
        var myAnswerInt = 0
        var myAnswerStr = ""
        var myAnswerIsCorrected = false
        if (exam_data.mac != 4) {
            myAnswerInt = exam_data.answered_list[exam_data.answered_list.size - 1] //自分の解答の番号
            myAnswerStr = sentakusi[myAnswerInt - 1]
            myAnswerIsCorrected = exam_data.isCorrect_list[exam_data.isCorrect_list.size - 1]
        } else {
            myAnswerInt = getSharedPreferences("user_data", MODE_PRIVATE).getInt("user_answer", 0)
            val answer = answersDB.find_answers(question_id)!![0]
            if (myAnswerInt == answer) {
                myAnswerIsCorrected = true
            }
            if (answer == 1) {
                myAnswerStr = "○"
            } else {
                myAnswerStr = "×"
            }
        }

        //正解か不正解かを設定
        var isCorrectStr = "不正解!!!!"
        AA_AnsweResult_Text.setTextColor(Color.RED)
        if (myAnswerIsCorrected) {
            isCorrectStr = "正解!"
            AA_AnsweResult_Text.setTextColor(Color.argb(255, 0, 128, 0))

        }
        AA_AnsweResult_Text.setText(isCorrectStr)

        var answerStr = "自分の回答：" + myAnswerStr + BR + "正解 : " + answer

        answer_examNumber_text.setText(exam_data.number)
        answer_question_correct_text.setText(answerStr)
        AA_answerComment_text.setText(questionsDB.find_comment(question_id)?.get(1))
    }
}