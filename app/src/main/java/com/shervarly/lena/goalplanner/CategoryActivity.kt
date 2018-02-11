package com.shervarly.lena.goalplanner

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.support.v7.widget.ShareActionProvider
import android.view.*
import android.widget.*
import com.j256.ormlite.dao.Dao
import dbmodule.CategoryDTO
import dbmodule.DatabaseHelper
import kotlinx.android.synthetic.main.item_category.view.*
import java.sql.SQLException

lateinit var timeFrame: String
var selectedPosition: Int = 0

class CategoryActivity : AppCompatActivity() {
    private lateinit var categoryListView: ListView
    private lateinit var categoryDAO: Dao<CategoryDTO, Int>
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        val toolbar: Toolbar? = findViewById(R.id.toolbar)
        if (toolbar != null)
            setSupportActionBar(toolbar)
        val actionBar: ActionBar? = getSupportActionBar()
        actionBar?.setDisplayHomeAsUpEnabled(true)
        val tempTimeFrame: String? = intent.getStringExtra(TIME_FRAME)

        if(tempTimeFrame != null)
            timeFrame = tempTimeFrame

        databaseHelper = getDBHelper(this)
        categoryDAO = databaseHelper.getCategoryDao()
        updateUI()
    }

    fun showGoals(view: View) {
        val categoryTitle =  view.category_title.text
        val intent = Intent(this, GoalsSettingActivity::class.java)
        intent.putExtra(CATEGORY_TITLE, categoryTitle)
        intent.putExtra(TIME_FRAME, timeFrame)
        startActivity(intent)
    }

    private fun updateUI(){
        categoryListView = findViewById(R.id.category_list)
        val categoryTitles = categoryDAO.queryForAll()
        categoryListView.adapter = CustomAdapter(this, R.layout.item_category, categoryTitles)
    }

    fun addCategory(view: View){
        val categoryField: EditText = (view.parent as View).findViewById(R.id.new_Category)
        val categoryTitle = categoryField.text.toString().capitalize()

        val newCategory = CategoryDTO(0, categoryTitle)
        categoryDAO.create(newCategory)
        updateUI()
        categoryField.text.clear()
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

    inner class CustomAdapter(context: Context, resource: Int, private val valluesList: List<CategoryDTO>): ArrayAdapter<CategoryDTO>(context, resource, valluesList) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {

            selectedPosition = position// - 1
            var convertView = convertView
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false)
            }

            val categoryNameField = convertView?.findViewById(R.id.category_title) as TextView
            categoryNameField.text = valluesList[position].categoryTitle
            categoryNameField.tag = "categoryTitle"
            categoryNameField.isClickable = true
            @TargetApi(26)
            categoryNameField.isFocusable = true

            categoryNameField.setOnClickListener { convertView ->
                showGoals(convertView)
            }
            val deleteButton = convertView?.findViewById(R.id.delete_category) as Button
            deleteButton.setOnClickListener {view ->
                removeCategory(position)
            }

            return convertView
        }
    }
}
