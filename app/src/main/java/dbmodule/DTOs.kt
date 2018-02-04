package dbmodule
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.shervarly.lena.goalplanner.TimeFrame
import java.io.Serializable
import java.util.*

/**
 * Created by elena on 27.01.2018.
 */
@DatabaseTable(tableName = "Categories")
data class CategoryDTO(@DatabaseField(generatedId = true) val categoryId: Int,
                       @DatabaseField val categoryTitle: String): Serializable {
    constructor() : this(0, "") {
    }
}

@DatabaseTable(tableName = "Goals")
data class GoalDTO(@DatabaseField(generatedId = true) val goalId: Int = 0,
                   @DatabaseField val timeFrame: String,
                   @DatabaseField val category: String,
                   @DatabaseField val goalName: String,
                   @DatabaseField val timeCreated: Date): Serializable{
    constructor() : this(0, "", "", "", Calendar.getInstance().time) {
    }
}