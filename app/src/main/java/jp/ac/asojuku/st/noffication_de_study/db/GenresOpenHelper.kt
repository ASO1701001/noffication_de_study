package jp.ac.asojuku.st.noffication_de_study.db

import android.content.ContentValues
import android.database.CursorIndexOutOfBoundsException
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase

class GenresOpenHelper(var db: SQLiteDatabase) {
    val tableName: String = "genres"

    fun add_record(q_num: Int, a_num: String) {
        val values = ContentValues()
        values.put("genre_id", q_num)
        values.put("genre_name", a_num)

        try {
            db.insertOrThrow(tableName, null, values)
        } catch (e: SQLiteConstraintException) {
            db.update(tableName, values, "genre_id = $q_num", null)
        }
    }
}



