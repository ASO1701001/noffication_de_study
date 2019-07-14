package jp.ac.asojuku.st.noffication_de_study.db

import android.content.ContentValues
import android.database.CursorIndexOutOfBoundsException
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase

class UserAnswersOpenHelper(var db: SQLiteDatabase) {
    val tableName: String = "user_answers";

    fun find_all_user_answers(): ArrayList<ArrayList<String>>? {
        val query = "SELECT * FROM $tableName"
        val cursor = db.rawQuery(query, null)

        return try {
            cursor.moveToFirst()

            val array = ArrayList<ArrayList<String>>()
            var bufferlist: ArrayList<String>
            for (i in 0 until cursor.count) {
                bufferlist = ArrayList()
                bufferlist.add(cursor.getString(0).toString())
                bufferlist.add(cursor.getString(1).toString())
                bufferlist.add(cursor.getString(2).toString())
                bufferlist.add(cursor.getString(3).toString())
                array.add(bufferlist)
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
        }
    }

    fun find_user_answers(question_id: Int): ArrayList<ArrayList<String>>? {
        val query = "SELECT * FROM $tableName where question_id = $question_id"
        val cursor = db.rawQuery(query, null)

        return try {
            cursor.moveToFirst()

            val array = ArrayList<ArrayList<String>>()
            var bufferlist: ArrayList<String>
            for (i in 0 until cursor.count) {
                bufferlist = ArrayList()
                bufferlist.add(cursor.getString(0).toString())
                bufferlist.add(cursor.getString(1).toString())
                bufferlist.add(cursor.getString(2).toString())
                bufferlist.add(cursor.getString(3).toString())
                array.add(bufferlist)
                cursor.moveToNext()
            }
            cursor.close()
            if (array.size == 0) {
                return null
            }
            return array
        } catch (e: CursorIndexOutOfBoundsException) {
            cursor.close()
            null
        }
    }

    fun add_record(a: Int, b: Int, c: Int, d: Int, db: SQLiteDatabase) {
        val values = ContentValues()
        values.put("user_answer_id", a)
        values.put("question_id", b)
        values.put("answer_choice", c)
        values.put("answer_time", d)

        try {
            db.insertOrThrow(tableName, null, values)
        } catch (e: SQLiteConstraintException) {
            db.update(tableName, values, "user_answer_id = $a", null)
        }
    }
}



