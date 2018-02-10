package com.shervarly.lena.goalplanner

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import android.support.v7.widget.ShareActionProvider
import com.j256.ormlite.dao.Dao
import dbmodule.CategoryDTO
import dbmodule.DatabaseHelper
import kotlinx.android.synthetic.main.item_category.view.*
import java.sql.SQLException

lateinit var timeFrame: String

class CategoryActivity : AppCompatActivity() {
    private lateinit var categoryListView: ListView

    private lateinit var categoryDAO: Dao<CategoryDTO, Int>
    private lateinit var databaseHelper: DatabaseHelper
    private var selectedCategory: Int = 0

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

        categoryListView = findViewById(R.id.category_list)
        categoryListView.setOnItemLongClickListener{ parent, view, position, id ->
            selectedCategory = position
            Toast.makeText(this, "Category was deleted", Toast.LENGTH_SHORT).show()
            removeGoal(view)
            true
        }

        categoryListView.setOnItemClickListener{ parent, view, position, id ->
            selectedCategory = position
            showGoals(view)
        }

        databaseHelper = getDBHelper(this)
        categoryDAO = databaseHelper.getCategoryDao()
        updateUI()
    }


    private fun showGoals(view: View) {
        val categoryTitle =  view.category_title.text
        println(categoryTitle)
        val intent = Intent(this, GoalsSettingActivity::class.java)
        intent.putExtra(CATEGORY_TITLE, categoryTitle)
        intent.putExtra(TIME_FRAME, timeFrame)
        startActivity(intent)
    }

    private fun updateUI(){
        val categoryTitles = categoryDAO.queryForAll().map { categoryDTO -> categoryDTO.categoryTitle }
        categoryListView.adapter = getItemAdapter(this, R.layout.item_category, R.id.category_title, categoryTitles)
    }

    fun addCategory(view: View){
        val categoryField: EditText = (view.parent as View).findViewById(R.id.new_Category)
        val categoryTitle = categoryField.text.toString().capitalize()
        print(categoryTitle)

        val newCategory = CategoryDTO(0, categoryTitle)
        categoryDAO.create(newCategory)
        updateUI()
        categoryField.text.clear()
    }

    fun removeGoal(view: View){
        try {
            categoryDAO.delete(categoryDAO.queryForAll()[selectedCategory])
            categoryListView.invalidateViews()
        } catch (e: SQLException){
            e.printStackTrace()
        }
        updateUI()
    }

    lateinit var shareActionProvider: ShareActionProvider

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getMenuInflater().inflate(R.menu.main_menu, menu)
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

}
