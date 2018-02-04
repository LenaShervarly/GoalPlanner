package dbmodule

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

/**
 * Created by elena on 27.01.2018.
 */
abstract class TableBase(val tableName: String) {

   /* lateinit var database: SQLiteDatabase
    val Goal_ID = "ID"

    fun addValues(contentValues: ContentValues){
        openDataBase()
        database.insertWithOnConflict(tableName, null, contentValues, SQLiteDatabase.CONFLICT_REPLACE)
        closeDataBase()
    }

    fun editRow(contentValues: ContentValues, whereClause: String) {
        openDataBase()
        database.update(tableName, contentValues, whereClause, null)
        closeDataBase()
    }

    fun deleteRows(whereClause: String) {
        openDataBase()
        database.delete(tableName, whereClause, null)
        closeDataBase()
    }

    private fun openDataBase(){
        database = DatabaseHelper.getDatabase()
    }

    private fun closeDataBase(){
        database.close()
    } */
}