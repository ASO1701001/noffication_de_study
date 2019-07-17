package jp.ac.asojuku.st.noffication_de_study

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import jp.ac.asojuku.st.noffication_de_study.db.AnswersOpenHelper
import jp.ac.asojuku.st.noffication_de_study.db.ImageOpenHelper
import jp.ac.asojuku.st.noffication_de_study.db.QuestionsOpenHelper
import jp.ac.asojuku.st.noffication_de_study.db.UserAnswersOpenHelper
import kotlinx.android.synthetic.main.activity_question.*
import org.jetbrains.anko.startActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class QuestionActivity : AppCompatActivity() {
    lateinit var examData: ExamData

    var isTouched = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)
        examData = intent.getSerializableExtra("exam_data") as ExamData
        QA_Dialog_Background.visibility = View.GONE

        choiceNextQuestion() // 次の問題を読み込んでおく

        if (examData.question_current == 9999) { // 表示する問題がないとき終了する
            printResult()
        } else {
            printQuestion() //問題文の表示

            when (examData.mac) {
                // QuestionOptionActivityから
                1 -> {
                    QA_TwoAnswers.visibility = View.GONE
                    QA_Answers.visibility = View.VISIBLE
                    QA_Answer_0.setSafeClickListener { choiceAnswer(0) }
                    QA_Answer_1.setSafeClickListener { choiceAnswer(1) }
                    QA_Answer_2.setSafeClickListener { choiceAnswer(2) }
                    QA_Answer_3.setSafeClickListener { choiceAnswer(3) }
                    QA_End_BTN.setSafeClickListener { pushEndButton() }
                    QA_Skip_BTN.setSafeClickListener { skipQuestion() }
                    QA_Next_BTN.setSafeClickListener {
                        finish()
                        startActivity<QuestionActivity>("exam_data" to examData)
                    }
                }
                // FragmentQuestionから
                2 -> {
                    QA_TwoAnswers.visibility = View.GONE
                    QA_Answers.visibility = View.VISIBLE
                    QA_Skip_BTN.visibility = View.GONE
                    QA_Answer_0.setSafeClickListener { choiceAnswer(0) }
                    QA_Answer_1.setSafeClickListener { choiceAnswer(1) }
                    QA_Answer_2.setSafeClickListener { choiceAnswer(2) }
                    QA_Answer_3.setSafeClickListener { choiceAnswer(3) }
                    QA_End_BTN.setSafeClickListener { finish() }
                    QA_Next_BTN.text = "戻る"
                    QA_Next_BTN.setSafeClickListener { finish() }

                }
                // 4択通知から
                3 -> {
                    QA_Answers.visibility = View.VISIBLE
                    QA_TwoAnswers.visibility = View.GONE
                    QA_Skip_BTN.visibility = View.GONE
                    QA_Answer_0.setSafeClickListener { choiceAnswer(0) }
                    QA_Answer_1.setSafeClickListener { choiceAnswer(1) }
                    QA_Answer_2.setSafeClickListener { choiceAnswer(2) }
                    QA_Answer_3.setSafeClickListener { choiceAnswer(3) }
                    QA_End_BTN.setSafeClickListener { finish() }
                    QA_Next_BTN.text = "戻る"
                    QA_Next_BTN.setSafeClickListener { finish() }

                }
                // ◯×通知から
                4 -> {
                    QA_Answers.visibility = View.GONE
                    QA_Skip_BTN.visibility = View.GONE
                    QA_TwoAnswers.visibility = View.VISIBLE
                    QA_maru_BTN.setSafeClickListener {
                        QA_TwoAnswers.visibility = View.INVISIBLE
                        choiceAnswer(1)
                    }
                    QA_batu_BTN.setSafeClickListener {
                        QA_TwoAnswers.visibility = View.INVISIBLE
                        choiceAnswer(2)
                    }
                    QA_End_BTN.setSafeClickListener { finish() }
                    QA_Next_BTN.text = "戻る"
                    QA_Next_BTN.setSafeClickListener { finish() }
                }
                else -> {
                    QA_Answers.visibility = View.GONE
                    QA_Skip_BTN.visibility = View.GONE
                    QA_TwoAnswers.visibility = View.GONE
                }
            }
        }

    }

    // 次の問題設定
    private fun choiceNextQuestion() {
        if (examData.question_current == 0) {
            examData.question_current = examData.question_list[0]
            try {
                examData.question_next = examData.question_list[1]
            } catch (e: IndexOutOfBoundsException) {
                examData.question_next = 9999
            }
        } else {
            examData.question_current = examData.question_next

            try {
                // 次の問題IDを取得
                examData.question_next = examData.question_list[examData.isCorrect_list.size + 1]

            } catch (e: IndexOutOfBoundsException) {
                // 次の問題が存在しない場合は次に9999を設定する
                examData.question_next = 9999
            }
        }
    }

    // 問題表示
    private fun printQuestion() {
        val questions = SQLiteHelper(this)
        val db = questions.readableDatabase
        val QOH = QuestionsOpenHelper(db)
        val question_arr: ArrayList<String>? = QOH.find_question(examData.question_current)
        val question_str: String
        if (question_arr == null) {
            question_str = "問題文がありません"
        } else {
            question_str = question_arr[1]
            if (question_arr[2] == "1") {
                val IOH = ImageOpenHelper(db)
                val imageAddress = IOH.find_image(examData.question_current)
                question_image.setImageDrawable(this.getDrawable(imageAddress!!))
            }
        }

        QA_Question_Text.text = question_str

    }

    // 解答選択
    private fun choiceAnswer(choice_number: Int) {
        if (isTouched) {
            return
        }
        isTouched = true
        // 登録処理
        regAnswer(choice_number)

        // 画面遷移
        // Toastを表示する処理
        if (examData.isCorrect_list[examData.isCorrect_list.size - 1]) {
            // 正解のとき
            Toast.makeText(this, "正解です！", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "不正解です！", Toast.LENGTH_SHORT).show()
        }
        answered()
    }

    // スキップ
    private fun skipQuestion() {
        if (isTouched) {
            return
        }
        finish()
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        // DBにスキップしたとして999を登録して次の問題画面に画面遷移
        regAnswer(9999)
        startActivity<QuestionActivity>("exam_data" to examData)
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    // 解答登録
    private fun regAnswer(choice_number: Int) {
        // 自分の解答を登録
        examData.answered_list.add(choice_number)
        // 正解をDBから取得
        val answers = SQLiteHelper(this)
        val db = answers.readableDatabase
        val AOH = AnswersOpenHelper(db)
        val answer = AOH.find_answers(examData.question_current)?.get(1) // 正しい正解
        var isCorrected = false // 正解だった場合にtrueにする
        if (choice_number == answer) {
            isCorrected = true
        }
        examData.isCorrect_list.add(isCorrected) // 解いた問題が正解だったかどうかがBoolean型で入る

        // 回答をDBに登録
        val uaHelper = UserAnswersOpenHelper(db)
        val userAnswerId = uaHelper.getNewId()
        uaHelper.addRecord(
            userAnswerId,
            examData.question_current,
            choice_number,
            (System.currentTimeMillis() / 1000).toString(),
            db
        )

        val data = getSharedPreferences("user_data", MODE_PRIVATE)
        val userId = data.getString("user_id", "0") as String
        if (userId != "0") {
            val date = Date()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormat.format(date)

            ApiPostTask {

            }.execute(
                "add-answer.php",
                hashMapOf(
                    "user_id" to userId,
                    "question_id" to examData.question_current,
                    "answer_choice" to choice_number,
                    "answer_time" to formattedDate
                ).toString()
            )
        }
    }

    // 結果表示
    private fun printResult() {
        // 解いた問題数を取得
        val BR: String? = System.getProperty("line.separator") // 改行用コードを取得
        val answerCount = examData.isCorrect_list.size.toDouble()
        val skipCount = examData.answered_list.count { it == 9999 }

        // 正解数を取得
        val correctedCount = examData.isCorrect_list.filter { it }.count().toDouble()
        // 正答率を計算
        val answerRate = correctedCount / answerCount * 100.00
        val answerRateStr: String = String.format("%4.1f", answerRate)

        // メッセージを用意する
        val popupMsg1 = "回答数:" + String.format("%3d", answerCount.toInt()) + "問" + BR
        val popupMsg2 = "正答率:$answerRateStr%$BR"
        var popupMsg3 = BR
        if (skipCount > 0) {
            popupMsg3 = "スキップ数:$skipCount$BR"
        }

        QA_Dialog_Background.visibility = View.VISIBLE

        val popupMsgs = popupMsg1 + popupMsg2 + popupMsg3

        // ポップアップ用のビルダー
        val builder = AlertDialog.Builder(this)
        if (answerRate < 100) { // ミスがある場合、間違った問題を解くボタンを表示させる
            builder.setMessage(popupMsgs)
                .setCancelable(false)// 範囲外タップによるキャンセルを不可にする
                .setNeutralButton("間違った問題を解く") { _, _ ->
                    // 間違った問題のリストを用意して問題解答画面に遷移する処理
                    val tempQuestionList: ArrayList<Int> = ArrayList()
                    for (question_id in examData.question_list) {
                        tempQuestionList.add(question_id)
                    }
                    examData.question_list.clear() // 問題リストの初期化
                    examData.answered_list.clear() // 解答リストの初期化
                    examData.question_current = 0
                    examData.question_next = 0
                    for (i in 0 until examData.isCorrect_list.size) {
                        val isCollected = examData.isCorrect_list[i]
                        if (!isCollected) {
                            examData.question_list.add(tempQuestionList[i]) // 間違ったquestion_idを詰め込んでいく
                        }
                    }
                    examData.isCorrect_list.clear()
                    startActivity<QuestionActivity>("exam_data" to examData)
                    finish()

                }
                .setPositiveButton("終了") { _, _ ->
                    // タイトル画面に戻る処理
                    examData.question_list.clear() // 問題リストの初期化
                    examData.answered_list.clear() // 解答リストの初期化
                    startActivity(
                        Intent(
                            this, MainActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
                .show()
        } else {
            builder.setMessage("正答率:100%!!!")
                .setCancelable(false)// 範囲外タップによるキャンセルを不可にする
                .setNeutralButton("おめでとう！！") { _, _ ->
                    //タイトル画面に戻る処理
                    examData.question_list.clear() //問題リストの初期化
                    examData.answered_list.clear() //解答リストの初期化
                    startActivity(
                        Intent(
                            this, MainActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }.show()
        }
    }

    private fun pushEndButton() {
        if (isTouched) { // 他のボタンが押されてない時だけ処理する
            return
        }
        if (examData.isCorrect_list.size == 0) { // 1問も解いていない場合はオプション画面に戻る
            finish()
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("終了しますか？")
                .setPositiveButton("はい") { _, _ ->
                    printResult()
                }
                .setNegativeButton("がんばる") { _, _ ->
                    // 何もしない
                }.show()
        }
        return
    }

    // Android端末側の戻るボタンを押した時の処理を上書き
    override fun onBackPressed() {
        pushEndButton()
        return // 無理やりリターンされることでActivityがDestroyされることを防ぐ
        // 以下は必ず処理されない。この方法がどうなのかは微妙
        super.onBackPressed()
    }


    private fun answered() {
        val mHandler = Handler()
        // スレッドを生成
        val thread = Thread(Runnable {
            mHandler.post {
                // UI関連の処理はThreadでは行えないのでHandlerを用いる
                QA_Skip_BTN.setSafeClickListener {
                    finish()
                    startActivity<QuestionActivity>("exam_data" to examData)
                }
                QA_to_Answer_BTN.setSafeClickListener {
                    startActivity<AnswerActivity>("exam_data" to examData)
                }
                QA_to_Answer_BTN.visibility = View.VISIBLE // 解説へボタンを表示
                QA_Answers.visibility = View.INVISIBLE // 選択肢を非表示に
                QA_Next_BTN.visibility = View.VISIBLE // 次へボタンを表示
            }
            Thread.sleep(100)
            isTouched = false
        })
        thread.start()
    }
}