package jp.ac.asojuku.st.noffication_de_study.db

import android.content.ContentValues
import android.database.CursorIndexOutOfBoundsException
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import jp.ac.asojuku.st.noffication_de_study.R

class ImageOpenHelper(var db: SQLiteDatabase) {
    val tableName: String = "image"

    fun find_image(question_id: Int): Int? {
        val query = "SELECT * FROM $tableName where question_id = $question_id"
        val cursor = db.rawQuery(query, null)

        return try {
            cursor.moveToFirst()
            var result: Int? = null
            for (i in 0 until cursor.count) {
                result = cursor.getInt(1)
            }
            cursor.close()
            result
        } catch (e: CursorIndexOutOfBoundsException) {
            cursor.close()
            null
        }
    }

    fun add_record(q_id: Int, a_num: String) {
        val values = ContentValues()
        values.put("question_id", q_id)
        values.put("file_name", a_num)

        try {
            db.insertOrThrow(tableName, null, values)
        } catch (e: SQLiteConstraintException) {
            db.update(tableName, values, "question_id = $q_id", null)
        }
    }

    fun setDefaultRecord() {
        val imageAddress = hashMapOf<Int, Int>(
            3 to R.drawable.image_3,
            5 to R.drawable.image_5,
            7 to R.drawable.image_7,
            10 to R.drawable.image_10,
            14 to R.drawable.image_14,
            15 to R.drawable.image_15,
            22 to R.drawable.image_22,
            27 to R.drawable.image_27,
            28 to R.drawable.image_28,
            29 to R.drawable.image_29,
            34 to R.drawable.image_34,
            53 to R.drawable.image_53,
            54 to R.drawable.image_54,
            64 to R.drawable.image_64,
            65 to R.drawable.image_65,
            77 to R.drawable.image_77,
            78 to R.drawable.image_78,
            82 to R.drawable.image_82,
            84 to R.drawable.image_84,
            89 to R.drawable.image_89,
            95 to R.drawable.image_95,
            96 to R.drawable.image_96,
            102 to R.drawable.image_102,
            106 to R.drawable.image_106,
            108 to R.drawable.image_108,
            119 to R.drawable.image_119,
            124 to R.drawable.image_124,
            125 to R.drawable.image_125,
            132 to R.drawable.image_132,
            133 to R.drawable.image_133,
            146 to R.drawable.image_146,
            149 to R.drawable.image_149,
            155 to R.drawable.image_155,
            157 to R.drawable.image_157,
            162 to R.drawable.image_162,
            164 to R.drawable.image_164,
            165 to R.drawable.image_165,
            166 to R.drawable.image_166,
            169 to R.drawable.image_169,
            176 to R.drawable.image_176,
            183 to R.drawable.image_183,
            184 to R.drawable.image_184,
            188 to R.drawable.image_188,
            190 to R.drawable.image_190,
            204 to R.drawable.image_204,
            209 to R.drawable.image_209,
            211 to R.drawable.image_211,
            212 to R.drawable.image_212,
            214 to R.drawable.image_214,
            227 to R.drawable.image_227,
            238 to R.drawable.image_238,
            240 to R.drawable.image_240
        )
        for (i in imageAddress) {
            val values = ContentValues()
            values.put("question_id", i.key)
            values.put("file_name", i.value)

            try {
                db.insertOrThrow(tableName, null, values)
            } catch (e: SQLiteConstraintException) {
                db.update(tableName, values, "question_id = " + i.key, null)
            }
        }
    }
}



