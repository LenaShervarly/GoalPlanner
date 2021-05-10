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
import androidx.recyclerview.widget.RecyclerView
import com.j256.ormlite.dao.Dao
import dbmodule.CategoryDTO
import dbmodule.DatabaseHelper
import kotlinx.android.synthetic.main.item_category.view.*
import java.lang.StringBuilder
import java.util.*


@RequiresApi(Build.VERSION_CODES.N)
class CategoryActivity : AppCompatActivity() {
    private lateinit var categoryListView: ListView
    private lateinit var purchasedCategoryListView: ListView
    private lateinit var categoryDAO: Dao<CategoryDTO, Int>
    private lateinit var databaseHelper: DatabaseHelper
    lateinit var touchHelper: ItemTouchHelper

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
        categoryListView = findViewById(R.id.category_list)
        purchasedCategoryListView = findViewById(R.id.purchased_categories_list)
        val categoryTitles = categoryDAO.queryForEq("purchased", false);
        val purchasedCategoryTitles = categoryDAO.queryForEq("purchased", true);
        purchasedCategoryListView.adapter = PurchasedCategoriesAdapter(this, R.layout.item_purchased, purchasedCategoryTitles)

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        val adapter = RecyclerViewAdapter( categoryTitles)
        val callback: ItemTouchHelper.Callback = ItemMoveCallbackListener(adapter)
        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recyclerView)
        recyclerView.adapter = adapter
    }

    fun addCategory(view: View){
        val categoryField: EditText = (view.parent as View).findViewById(R.id.new_Category)
        val categoryTitle = categoryField.text.toString().capitalize()

        val newCategory = CategoryDTO(0, categoryTitle, false)
        categoryDAO.create(newCategory)
        updateUI()
        categoryField.text.clear()
    }

    fun resetCategory(view: View) {
        var purchasedProducts = categoryDAO.queryForAll()
        purchasedProducts.forEach { category -> category.purchased = false
            categoryDAO.update(category)
        }
        updateUI()
    }

    fun removeCategory(position: Int){
        try {
            categoryDAO.delete(categoryDAO.queryForAll()[position])
            categoryListView.invalidateViews()
        } catch (e: SQLException){
            e.printStackTrace()
        }
        updateUI()
    }

    lateinit var shareActionProvider: ShareActionProvider

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val shareButton: MenuItem = menu.findItem(R.id.action_share)
        shareActionProvider = MenuItemCompat.getActionProvider(shareButton) as ShareActionProvider
        val productList = StringBuilder("")
        categoryDAO.queryForEq("purchased", false).forEach {
            productList.append(">>> " + it.categoryTitle + " <<< \n")
            var allProducts = databaseHelper.getProductsByCategoryAndPurchasedStatus(it.categoryTitle, false)
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

    fun updateCategory(categoryDTO: CategoryDTO){
        try {
            var categoryToUpdate = databaseHelper.getCategoryById(categoryDTO.categoryId)
            categoryToUpdate.purchased = !categoryToUpdate.purchased
            categoryDAO.update(categoryToUpdate)
            //categoryListView.invalidateViews()
            purchasedCategoryListView.invalidateViews()
        } catch (e: SQLException){
            e.printStackTrace()
        }
        updateUI()
    }

    inner class PurchasedCategoriesAdapter(context: Context, resource: Int, private val purchasedCategories: List<CategoryDTO>): ArrayAdapter<CategoryDTO>(context, resource, purchasedCategories) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {

            var convertView = convertView
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_purchased, parent, false)
            }

            val categoryNameField = convertView?.findViewById(R.id.purchased_title) as TextView
            categoryNameField.text = purchasedCategories[position].categoryTitle
            categoryNameField.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

            categoryNameField.setOnClickListener { convertView ->
                updateCategory(purchasedCategories[position])
            }

            val deleteButton = convertView?.findViewById(R.id.delete_item) as Button
            deleteButton.setOnClickListener { view ->
                removeCategory(position)
            }

            return convertView
        }
    }


    inner class RecyclerViewAdapter(private val categories: List<CategoryDTO>):
        RecyclerView.Adapter<RecyclerViewAdapter.CategoryViewHolder>(), ItemMoveCallbackListener.Listener {
        inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            private val categoryNameField = itemView.findViewById(R.id.category_title) as TextView
            private val deleteButton = itemView.findViewById(R.id.delete_category) as Button

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
                    updateCategory(category)
                }
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewAdapter.CategoryViewHolder {
            val view =  LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
            return CategoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: RecyclerViewAdapter.CategoryViewHolder, position: Int) {
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
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onRowSelected(itemViewHolder: CategoryViewHolder) {
        }

        override fun onRowClear(itemViewHolder: CategoryViewHolder) {
        }

    }
}
