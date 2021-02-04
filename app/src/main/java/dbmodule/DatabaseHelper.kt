package dbmodule

import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils



/**
 * Created by elena on 27.01.2018.
 */
var DATABASE_NAME: String = "GoalPlanner"
val DATABASE_VERSION: Int = 1

class DatabaseHelper(context: Context) : OrmLiteSqliteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?, connectionSource: ConnectionSource?) {
        try {
            TableUtils.createTable<CategoryDTO>(connectionSource, CategoryDTO::class.java)
            TableUtils.createTable<ProductDTO>(connectionSource, ProductDTO::class.java)
        } catch (e: SQLException) {
            Log.e(DatabaseHelper::class.java.name, "Unable to create databases", e)
        }
    }
    override fun onUpgrade(db: SQLiteDatabase?, connectionSource: ConnectionSource?, oldVersion: Int, newVersion: Int) {
        try {
            TableUtils.dropTable<CategoryDTO, Any>(connectionSource, CategoryDTO::class.java, true)
            TableUtils.dropTable<ProductDTO, Any>(connectionSource, ProductDTO::class.java, true)
            onCreate(db, connectionSource)
        } catch (e: SQLException) {
            Log.e(DatabaseHelper::class.java.name, "Unable to upgrade database from version $oldVersion to new $newVersion", e)
        }
    }

    @Throws(SQLException::class)
    fun getCategoryDao(): Dao<CategoryDTO, Int> {
        return getDao(CategoryDTO::class.java)
    }

    @Throws(SQLException::class)
    fun getProductsDao(): Dao<ProductDTO, Int> {
        return getDao(ProductDTO::class.java)
    }

    fun getProductsByCategoryAndPurchasedStatus(category: String, purchasedStatus: Boolean): List<ProductDTO> {
        return getProductsDao().filter { productDTO -> productDTO.category == category &&  productDTO.purchased == purchasedStatus }
    }

    fun getProductsById(id: Int): ProductDTO {
        return getProductsDao().filter { productDTO -> productDTO.productId == id }.first()
    }

    fun getCategoryById(id: Int): CategoryDTO {
        return getCategoryDao().filter { category -> category.categoryId == id }.first()
    }
}