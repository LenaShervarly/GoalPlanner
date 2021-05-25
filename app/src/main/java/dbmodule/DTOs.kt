package dbmodule
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.io.Serializable


@DatabaseTable(tableName = "Categories")
data class CategoryDTO(@DatabaseField(generatedId = true) val categoryId: Int,
                       @DatabaseField var categoryTitle: String,
                       @DatabaseField var purchased: Boolean,
                       @DatabaseField var order: Int): Serializable {
    constructor() : this(0, "", false, 0)
}

@DatabaseTable(tableName = "Products")
data class ProductDTO(@DatabaseField(generatedId = true) val productId: Int = 0,
                      @DatabaseField val category: String,
                      @DatabaseField val productName: String,
                      @DatabaseField var purchased: Boolean,
                      @DatabaseField var order: Int): Serializable{
    constructor() : this(0,  "", "", false, 0)
}