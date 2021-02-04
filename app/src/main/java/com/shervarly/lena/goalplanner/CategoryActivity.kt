package com.shervarly.lena.goalplanner

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.Toolbar
import android.support.v7.widget.ShareActionProvider
import android.view.*
import android.widget.*
import com.j256.ormlite.dao.Dao
import dbmodule.CategoryDTO
import dbmodule.DatabaseHelper
import kotlinx.android.synthetic.main.item_category.view.*
import java.sql.SQLException

var selectedPosition: Int = 0

class CategoryActivity : AppCompatActivity() {
    private lateinit var categoryListView: ListView
    private lateinit var purchasedCategoryListView: ListView
    private lateinit var categoryDAO: Dao<CategoryDTO, Int>
    private lateinit var databaseHelper: DatabaseHelper

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
        val categoryTitles = categoryDAO.queryForAll().filter { category -> !category.purchased }
        val purchasedCategoryTitles = categoryDAO.queryForAll().filter { category -> category.purchased }
        categoryListView.adapter = CustomAdapter(this, R.layout.item_category, categoryTitles)
        purchasedCategoryListView.adapter = PurchasedCategoriesAdapter(this, R.layout.item_purchased, purchasedCategoryTitles)
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
        purchasedProducts.forEach {
            category -> category.purchased = false
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
        setShareActionIntent("Wanna check my categories?")
        return super.onCreateOptionsMenu(menu)
    }

    private fun setShareActionIntent(message: String) {
        val intent: Intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, message)
        shareActionProvider.setShareIntent(intent)
    }

    inner class CustomAdapter(context: Context, resource: Int, private val categories: List<CategoryDTO>): ArrayAdapter<CategoryDTO>(context, resource, categories) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {

            var convertView = convertView
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false)
            }

            val categoryNameField = convertView?.findViewById(R.id.category_title) as TextView
            categoryNameField.text = categories[position].categoryTitle
            categoryNameField.tag = "categoryTitle"
            categoryNameField.isClickable = true
            @TargetApi(26)
            categoryNameField.isFocusable = true

            categoryNameField.setOnClickListener { convertView ->
                showProducts(convertView)
            }

            val deleteButton = convertView?.findViewById(R.id.delete_category) as Button
            deleteButton.setOnClickListener {view ->
                updateCategory(categories[position])
            }

            return convertView
        }
    }


    fun updateCategory(categoryDTO: CategoryDTO){
        try {
            var categoryToUpdate = databaseHelper.getCategoryById(categoryDTO.categoryId)
            categoryToUpdate.purchased = !categoryToUpdate.purchased
            categoryDAO.update(categoryToUpdate)
            categoryListView.invalidateViews()
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
            deleteButton.setOnClickListener {view ->
                removeCategory(position)
            }

            return convertView
        }
    }
}
