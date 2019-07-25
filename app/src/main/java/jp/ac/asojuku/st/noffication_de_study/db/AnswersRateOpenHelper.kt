package jp.ac.asojuku.st.noffication_de_study.db

import android.content.ContentValues
import android.database.CursorIndexOutOfBoundsException
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase

class AnswersRateOpenHelper(var db: SQLiteDatabase) {
    val tableName: String = "answers_rate"

    fun find_all_rate(): ArrayList<String>? {
        val query = "SELECT * FROM $tableName"
        val cursor = db.rawQuery(query, null)

        return try {
            cursor.moveToFirst()
            val array = ArrayList<String>()

            for (i in 0 until cursor.count) {
                array.add(cursor.getString(0))
                array.add(cursor.getString(1))
                cursor.moveToNext()
            }
            cursor.close()
            if (array.size == 0) {
                return null
            }
            array
        } catch (e: CursorIndexOutOfBoundsException) {
            cursor.close()
            null
        } catch (e: IndexOutOfBoundsException) {
            cursor.close()
            null
        }
    }

    fun add_record(q_id: Int, a_num: Double) {
        val values = ContentValues()
        values.put("question_id", q_id)
        values.put("answer_rate", a_num)

        try {
            db.insertOrThrow(tableName, null, values)
        } catch (e: SQLiteConstraintException) {
            db.update(tableName, values, "question_id = $q_id", null)
        }
    }
}



