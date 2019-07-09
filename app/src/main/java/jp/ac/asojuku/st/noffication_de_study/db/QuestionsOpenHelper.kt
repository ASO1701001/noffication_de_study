package jp.ac.asojuku.st.noffication_de_study.db

import android.content.ContentValues
import android.database.CursorIndexOutOfBoundsException
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase

class QuestionsOpenHelper(var db: SQLiteDatabase) {
    val tableName: String = "questions";

    //問題idを渡して、
    fun find_question(question_id: Int): ArrayList<String>? {
        val query = "SELECT * FROM " + tableName + " where question_id = " + question_id
        val cursor = db.rawQuery(query, null)

        var array = ArrayList<String>()
        return try{
            cursor.moveToFirst()
            array.add(cursor.getString(0).toString())
            array.add(cursor.getString(1).toString())
            array.add(cursor.getString(2).toString())

            cursor.close()
            array
        } catch (e: CursorIndexOutOfBoundsException) {
            cursor.close()
            null
        }
    }

    fun find_comment(question_id: Int): ArrayList<String>? {
        val query = "SELECT * FROM " + tableName + " where question_id = " + question_id
        val cursor = db.rawQuery(query, null)

        var array = ArrayList<String>()
        return try{
            cursor.moveToFirst()
            array.add(cursor.getString(0).toString())
            array.add(cursor.getString(3).toString())

            cursor.close()
            array
        } catch (e: CursorIndexOutOfBoundsException) {
            cursor.close()
            null
        }
    }

    fun find_update_date(question_id: Int): ArrayList<ArrayList<Int>>? {
        val query = "SELECT * FROM " + tableName + " where question_id = " + question_id
        val cursor = db.rawQuery(query, null)

        var array = ArrayList<ArrayList<Int>>()
        return try {
            cursor.moveToFirst()
            var bufferlist: ArrayList<Int>
            for (i in 0 until cursor.count) {
                bufferlist = ArrayList()
                bufferlist.add(cursor.getInt(0))
                bufferlist.add(cursor.getInt(4))
                array.add(bufferlist)
                cursor.moveToNext();
            }
            cursor.close()
            if (array.size == 0) {
                null
            }
            array
        } catch (e: CursorIndexOutOfBoundsException) {
            cursor.close()
            null
        }
    }

    fun add_record(a: Int, b: String, c: Int, d: String, e: String, question_flag: Int) {
        val values = ContentValues()
        values.put("question_id", a)
        values.put("question", b)
        values.put("is_have_image", c)
        values.put("comment", d)
        values.put("update_date", e)
        values.put("question_flag",question_flag)

        try {
            db.insertOrThrow(tableName, null, values)
        } catch (e: SQLiteConstraintException) {
            db.update(tableName, values, "question_id = " + a, null)
        }
    }
}