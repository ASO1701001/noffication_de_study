package jp.ac.asojuku.st.noffication_de_study

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import jp.ac.asojuku.st.noffication_de_study.db.ExamsQuestionsOpenHelper
import jp.ac.asojuku.st.noffication_de_study.db.QuestionsGenresOpenHelper
import kotlinx.android.synthetic.main.activity_question_option.*
import org.jetbrains.anko.startActivity

//TODO 問題オプション画面：おおよそ完成（99%）
class QuestionOptionActivity : AppCompatActivity() {
    //TODO 定数の値はすべて仮の値
    val user_id = 12345678

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_option)

        QOA_Start_BTN.setSafeClickListener {
            // loadChoiceの実行
            val ld = loadChoice()
            val QuestionsArrayList = ld.first
            val ExamName = ld.second

            val exam_data = ExamData(1, "FE", ExamName)
            exam_data.set_list_data(QuestionsArrayList)

            startActivity<QuestionActivity>("exam_data" to exam_data)
        }
        QOA_Back_BTN.setSafeClickListener {
            finish()
        }
        QOA_Select_Exam_BTN.setSafeClickListener {
            if (QOA_Select_Exam_LL.visibility == View.VISIBLE) {
                QOA_Select_Exam_LL.visibility = View.GONE
            } else {
                QOA_Select_Exam_LL.visibility = View.VISIBLE
            }
        }
        QOA_Select_Genres_BTN.setSafeClickListener {
            if (QOA_Select_Genres_LL.visibility == View.VISIBLE) {
                QOA_Select_Genres_LL.visibility = View.GONE
            } else {
                QOA_Select_Genres_LL.visibility = View.VISIBLE
            }
        }
        QOA_Select_Amount_BTN.setSafeClickListener {
            if (QOA_Question_Amount_BOX.visibility == View.VISIBLE) {
                QOA_Question_Amount_BOX.visibility = View.GONE
            } else {
                QOA_Question_Amount_BOX.visibility = View.VISIBLE
            }
        }
        QOA_Select_Method_BTN.setSafeClickListener {
            if (QOA_Select_Method_Group.visibility == View.VISIBLE) {
                QOA_Select_Method_Group.visibility = View.GONE
            } else {
                QOA_Select_Method_Group.visibility = View.VISIBLE
            }
        }
    }

    // 選択肢を読み込む
    private fun loadChoice(): Pair<ArrayList<Int>, String> {
        // ランダム出題するかどうか
        // ランダム出題の場合、randomBooleanの中身がtrueに
        val randomBoolean = QOA_Select_Method_Random_RBTN.isChecked

        // 問題数スピナーから問題数を取得する
        val SpinnerStr: String = QOA_Question_Amount_BOX.selectedItem.toString()
        val testList = SpinnerStr.split("問")
        val SpinnerNum: Int = Integer.parseInt(testList[0])

        // 問題DBの生成
        val questions = SQLiteHelper(this)
        val db = questions.readableDatabase
        // 数値はすべて仮数

        // 出題年度ごとの問題の読み込み
        val EQOH = ExamsQuestionsOpenHelper(db)
        var TempYear: ArrayList<ArrayList<Int>>?
        val TempYear_list: ArrayList<ArrayList<ArrayList<Int>>?> = ArrayList(ArrayList(ArrayList()))

        // 出題年度が選択されなかった場合、「"empty"」のままに、年度が1つしか選ばれなかった場合、ExamNameFlgは1になる
        // 複数選択された場合、ExamNameFlgは2以外になる
        var ExamName = "empty"
        var ExamNameFlg = 0

        // 試験回選択
        // 出題IDはFEだけなので、exams_idは1?
        if (QOA_Select_Exam_Number_H31S_RBTN.isChecked) {
            TempYear = EQOH.find_all_questions(1, "FE2019S")
            TempYear_list.add(TempYear)
            ExamName = "FE2019S"
            ExamNameFlg++
        }
        if (QOA_Select_Exam_Number_H30F_RBTN.isChecked) {
            TempYear = EQOH.find_all_questions(1, "FE2018F")
            TempYear_list.add(TempYear)
            ExamName = "FE2018F"
            ExamNameFlg++
        }
        if (QOA_Select_Exam_Number_H30S_RBTN.isChecked) {
            TempYear = EQOH.find_all_questions(1, "FE2018S")
            TempYear_list.add(TempYear)
            ExamName = "FE2018S"
            ExamNameFlg++
        }
        if (QOA_Select_Exam_Number_H29F_RBTN.isChecked) {
            TempYear = EQOH.find_all_questions(1, "FE2017F")
            TempYear_list.add(TempYear)
            ExamName = "FE2017F"
            ExamNameFlg++
        }
        if (QOA_Select_Exam_Number_H29S_RBTN.isChecked) {
            TempYear = EQOH.find_all_questions(1, "FE2017S")
            TempYear_list.add(TempYear)
            ExamName = "FE2017S"
            ExamNameFlg++
        }
        if (QOA_Select_Exam_Number_H28F_RBTN.isChecked) {
            TempYear = EQOH.find_all_questions(1, "FE2016F")
            TempYear_list.add(TempYear)
            ExamName = "FE2016F"
            ExamNameFlg++
        }

        // 出題年度が選択されなかった場合(ExamData)が「""」だった場合
        if (ExamName == "empty") {
            TempYear_list.add(EQOH.find_all_questions(1, "FE2019S"))
            TempYear_list.add(EQOH.find_all_questions(1, "FE2018F"))
            TempYear_list.add(EQOH.find_all_questions(1, "FE2018S"))
            TempYear_list.add(EQOH.find_all_questions(1, "FE2017F"))
            TempYear_list.add(EQOH.find_all_questions(1, "FE2017S"))
            TempYear_list.add(EQOH.find_all_questions(1, "FE2016F"))
        }

        // ExamNameFlgが2以上の場合(年度が複数選択された場合)
        if (ExamNameFlg >= 2) {
            ExamName = "multi"
        }

        // ジャンルの読み込み
        val GOH = QuestionsGenresOpenHelper(db)
        var genre1_Questions: ArrayList<Int>? = null
        var genre2_Questions: ArrayList<Int>? = null

        // ジャンル検索
        if (QOA_Select_Genres_1.isChecked) {
            genre1_Questions = GOH.find_genre_questions(1)
        }
        if (QOA_Select_Genres_2.isChecked) {
            genre2_Questions = GOH.find_genre_questions(2)
        }

        // 取得した問題から全てのArrayListに存在するものを書き出す
        // ArrayListのTempQuestionsに出題する問題を格納する
        val TempQuestions = ArrayList<Int>()

        if (genre1_Questions != null || genre2_Questions != null) {
            if (genre1_Questions != null) {
                for (ty in TempYear_list) {
                    for (i in 0..ty!!.size) {
                        for (j in 0 until genre1_Questions.size) {
                            if (genre1_Questions[j] == ty[i][0]) {
                                TempQuestions.add(genre1_Questions[j])
                            }
                        }
                    }
                }
            }

            if (genre2_Questions != null) {
                for (ty in TempYear_list) {
                    for (i in 0 until ty!!.size) {
                        for (j in 0 until genre2_Questions.size) {
                            if (genre2_Questions[j] == ty[i][0]) {
                                TempQuestions.add(genre2_Questions[j])
                            }
                        }
                    }
                }
            }
        } else {
            for (ty in TempYear_list) {
                for (i in 0 until ty!!.size) {
                    TempQuestions.add(ty[i][0])
                }
            }
        }

        // ランダム出題するか、そうじゃないかで問題順を並び替える
        if (QOA_Select_Method_Random_RBTN.isChecked) {
            TempQuestions.shuffle()
        }

        // 問題数に応じて問題を選択する
        val QuestionsArrayList = ArrayList<Int>()
        /*
        for (tq in TempQuestions) {
            for (i in 0..SpinnerNum - 1) {
                QuestionsArrayList.add(tq)
            }
        }
        */

        for (i in 0 until SpinnerNum) {
            QuestionsArrayList.add(TempQuestions[i])
        }


        // 問題ArrayList<Int>であるQuestionsArrayとStringを返す
        return Pair(QuestionsArrayList, ExamName)
    }
}