package jp.ac.asojuku.st.noffication_de_study

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    // データベースが作成された時に実行される処理
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE answers ( question_id INTEGER, answer_number INTEGER, PRIMARY KEY(question_id,answer_number) )")
        db.execSQL("CREATE TABLE answers_rate ( question_id int not null constraint answers_rate_pk primary key, answer_rate real )")
        db.execSQL("CREATE TABLE exams ( exam_id INTEGER PRIMARY KEY, exam_name TEXT )")
        db.execSQL("CREATE TABLE exams_numbers ( exam_id INTEGER, exams_number TEXT, primary key (exam_id, exams_number) )")
        db.execSQL("CREATE TABLE exams_questions ( exams_id INTEGER, exams_number TEXT, question_id INTEGER, question_number INTEGER, primary key (exams_id, exams_number, question_id, question_number) )")
        db.execSQL("CREATE TABLE genres ( genre_id INTEGER PRIMARY KEY, genre_name TEXT )")
        db.execSQL("CREATE TABLE image ( question_id INTEGER primary key, file_name TEXT )")
        db.execSQL("CREATE TABLE questions ( question_id INTEGER PRIMARY KEY, question TEXT, is_have_image BOOLEAN, comment TEXT, update_date TEXT, question_flag INTEGER)")
        db.execSQL("CREATE TABLE questions_genres ( question_id INTEGER, genre_id INTEGER, primary key (question_id, genre_id) )")
        db.execSQL("CREATE TABLE user_answers ( user_answer_id INTEGER primary key, question_id INTEGER, answer_choice INTEGER, answer_time TEXT )")
    }

    // データベースをバージョンアップした時に実行される処理
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS answers")
        db.execSQL("DROP TABLE IF EXISTS answers_rate")
        db.execSQL("DROP TABLE IF EXISTS exams")
        db.execSQL("DROP TABLE IF EXISTS exams_numbers")
        db.execSQL("DROP TABLE IF EXISTS exams_questions")
        db.execSQL("DROP TABLE IF EXISTS genres")
        db.execSQL("DROP TABLE IF EXISTS image")
        db.execSQL("DROP TABLE IF EXISTS questions")
        db.execSQL("DROP TABLE IF EXISTS questions_genres")
        db.execSQL("DROP TABLE IF EXISTS user_answers")

        onCreate(db)
    }

    companion object {
        // データベースの名前
        private const val DB_NAME = "db.db"
        // データベースのバージョン
        private const val DB_VERSION = 3
    }
}