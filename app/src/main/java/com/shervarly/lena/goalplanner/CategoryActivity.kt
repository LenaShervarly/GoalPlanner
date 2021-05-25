package com.shervarly.lena.goalplanner

import android.content.Context
import android.content.Intent
import android.database.SQLException
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.LayoutInflater
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ShareActionProvider
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.j256.ormlite.dao.Dao
import dbmodule.CategoryDTO
import dbmodule.DatabaseHelper
import kotlinx.android.synthetic.main.item_category.view.*
import java.util.*


@RequiresApi(Build.VERSION_CODES.N)
class CategoryActivity : AppCompatActivity() {
    private lateinit var purchasedCategoryListView: ListView
    private lateinit var categoryDAO: Dao<CategoryDTO, Int>
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var touchHelper: ItemTouchHelper
    private var inEditMode = false
    private lateinit var shareActionProvider: ShareActionProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        val toolbar: Toolbar? = findViewById(R.id.toolbar)
        if (toolbar != null)
            setSupportActionBar(toolbar)

        databaseHelper = getDBHelper(this)
        categoryDAO = databaseHelper.getCategoryDao()
        updateUI()
    }

    fun showProducts(view: View) {
        val categoryTitle =  view.category_title.text
        val intent = Intent(this, ProductsSettingActivity::class.java)
        intent.putExtra(CATEGORY_TITLE, categoryTitle)
        startActivity(intent)
    }


    private fun updateUI(){
        val categoryTitles = categoryDAO.queryForEq("purchased", false).sortedBy { cat -> cat.order }
        val purchasedCategoryTitles = categoryDAO.queryForEq("purchased", true)
        purchasedCategoryListView = findViewById(R.id.purchased_categories_list)
        purchasedCategoryListView.adapter =
            PurchasedCategoriesAdapter(this, R.layout.item_purchased, purchasedCategoryTitles)

        val categoryRecyclerView: RecyclerView = findViewById(R.id.recycler_view)
        val adapter = CategoryViewAdapter( categoryTitles)
        val callback: ItemTouchHelper.Callback = ItemMoveCallbackListener(adapter)
        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(categoryRecyclerView)
        categoryRecyclerView.adapter = adapter
        categoryRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    fun addCategory(view: View){
        val categoryField: EditText = (view.parent as View).findViewById(R.id.new_Category)
        val categoryTitle = categoryField.text.toString().capitalize(Locale.ROOT)

        val newCategory = CategoryDTO(0, categoryTitle, false,
            categoryDAO.queryForEq("purchased", false).size)
        categoryDAO.create(newCategory)
        updateUI()
        categoryField.text.clear()
    }

    fun resetCategory(view: View) {
        val purchasedProducts = categoryDAO.queryForAll()
        purchasedProducts.forEach { category -> category.purchased = false
            categoryDAO.update(category)
        }
        updateUI()
    }

    fun removeCategory(category: CategoryDTO){
        try {
            categoryDAO.delete(category)
        } catch (e: SQLException){
            e.printStackTrace()
        }
        updateUI()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val shareButton: MenuItem = menu.findItem(R.id.action_share)
        shareActionProvider = MenuItemCompat.getActionProvider(shareButton) as ShareActionProvider
        val productList = StringBuilder("")
        categoryDAO.queryForEq("purchased", false).forEach {
            productList.append(">>> " + it.categoryTitle + " <<< \n")
            val allProducts = databaseHelper.getProductsByCategoryAndPurchasedStatus(it.categoryTitle, false)
            for (product in allProducts) {
                productList.append(product.productName + "\n")
            }
        }
        setShareActionIntent(productList.toString())
        return super.onCreateOptionsMenu(menu)
    }

    private fun setShareActionIntent(message: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, message)
        shareActionProvider.setShareIntent(intent)
    }

    fun updatePurchasedStatus(categoryDTO: CategoryDTO){
        try {
            val categoryToUpdate = databaseHelper.getCategoryById(categoryDTO.categoryId)
            categoryToUpdate.purchased = !categoryToUpdate.purchased
            categoryDAO.update(categoryToUpdate)
            purchasedCategoryListView.invalidateViews()
        } catch (e: SQLException){
            e.printStackTrace()
        }
        updateUI()
    }

    inner class PurchasedCategoriesAdapter(context: Context, resource: Int, private val purchasedCategories: List<CategoryDTO>):
            ArrayAdapter<CategoryDTO>(context, resource, purchasedCategories) {

        override fun getView(position: Int, viewParam: View?, parent: ViewGroup): View {

            var convertView = viewParam
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_purchased, parent, false)
            }

            val categoryNameField = convertView?.findViewById(R.id.purchased_title) as TextView
            categoryNameField.text = purchasedCategories[position].categoryTitle
            categoryNameField.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

            categoryNameField.setOnClickListener {
                updatePurchasedStatus(purchasedCategories[position])
            }

            val deleteButton = convertView.findViewById(R.id.delete_item) as Button
            deleteButton.setOnClickListener {
                val categoryToRemove = purchasedCategories[position]
                removeCategory(categoryToRemove)
            }

            return convertView
        }
    }


    inner class CategoryViewAdapter(private val categories: List<CategoryDTO>): IAdapter,
        RecyclerView.Adapter<CategoryViewAdapter.CategoryViewHolder>(), ItemMoveCallbackListener.Listener {
        inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            private val categoryNameField = itemView.findViewById(R.id.category_title) as TextView
            private val deleteButton = itemView.findViewById(R.id.delete_category) as Button
            private val editButton = itemView.findViewById(R.id.edit_category) as Button
            private val editCategoryField = itemView.findViewById(R.id.edit_Category_field) as EditText

            fun bind(category: CategoryDTO) {
                categoryNameField.text = category.categoryTitle
                categoryNameField.tag = "categoryTitle"
                categoryNameField.isClickable = true
                categoryNameField.setOnClickListener { view ->
                    showProducts(view)
                }
                categoryNameField.setOnDragListener{ _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        touchHelper.startDrag(this)
                    }
                    return@setOnDragListener true
                }

                deleteButton.setOnClickListener {
                    updatePurchasedStatus(category)
                }

                editButton.setOnClickListener {
                    if (!inEditMode) {
                        editCategoryField.visibility = View.VISIBLE
                        editCategoryField.hint = category.categoryTitle
                        categoryNameField.visibility = View.INVISIBLE
                    } else {
                        category.categoryTitle = editCategoryField.text.toString().capitalize(Locale.ROOT)
                        categoryDAO.update(category)
                        editCategoryField.visibility = View.INVISIBLE
                        categoryNameField.visibility = View.VISIBLE
                        updateUI()
                    }
                    inEditMode = !inEditMode
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewAdapter.CategoryViewHolder {
            val view =  LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
            return CategoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: CategoryViewAdapter.CategoryViewHolder, position: Int) {
            val category = categories[position]
            holder.bind(category)
        }

        override fun getItemCount() = categories.size

        override fun onRowMoved(fromPosition: Int, toPosition: Int) {
            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(categories, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(categories, i, i - 1)
                }
            }
            val currentOrder = categories[fromPosition].order
            categories[fromPosition].order = categories[toPosition].order
            categories[toPosition].order = currentOrder
            categoryDAO.update(categories[fromPosition])
            categoryDAO.update(categories[toPosition])
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onRowSelected(itemViewHolder: CategoryViewHolder) {
        }

        override fun onRowClear(itemViewHolder: CategoryViewHolder) {
        }

    }
}
