package jp.ac.asojuku.st.noffication_de_study.db

import android.content.ContentValues
import android.database.CursorIndexOutOfBoundsException
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase

class ExamsNumbersOpenHelper(var db: SQLiteDatabase) {
    val tableName: String = "exams_numbers";

    fun add_record(q_id: Int, a_num: String) {
        val values = ContentValues()
        values.put("exam_id", q_id)
        values.put("exams_number", a_num)

        try {
            db.insertOrThrow(tableName, null, values)
        } catch (e: SQLiteConstraintException) {
            db.update(tableName, values, "exam_id = $q_id and exams_number = '$a_num'", null)
        }
    }
}



