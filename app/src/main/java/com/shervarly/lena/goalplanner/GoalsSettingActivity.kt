package com.shervarly.lena.goalplanner

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
import com.j256.ormlite.android.apptools.OpenHelperManager
import com.j256.ormlite.dao.Dao
import dbmodule.DatabaseHelper
import dbmodule.GoalDTO
import java.sql.SQLException
import java.util.*
import android.widget.TextView
import android.widget.ArrayAdapter
import java.text.DateFormat

private var selectedGoalPosition: Int = 0

class GoalsSettingActivity : AppCompatActivity() {
    private lateinit var goalsListView: ListView
    private lateinit var categoryTitle: String
    private var timeCreatedField: TextView? = null
    private lateinit var timeFrame: String
    private lateinit var goalDAO: Dao<GoalDTO, Int>
    private var allGoals: List<GoalDTO> = ArrayList()
    private lateinit var databaseHelper: DatabaseHelper
    lateinit var shareActionProvider: ShareActionProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals_setting)

        categoryTitle = intent.getStringExtra(CATEGORY_TITLE)
        timeFrame = intent.getStringExtra(TIME_FRAME)
        val textForMyTitle = "\"$categoryTitle\" for the next $timeFrame"

        val toolbar: Toolbar? = findViewById(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar?.title = textForMyTitle
        }
        var actionBar: ActionBar? = getSupportActionBar()
        actionBar?.setDisplayHomeAsUpEnabled(true)

        //val titleView: TextView = findViewById(R.id.my_goals_title)
        //titleView.text = textForMyTitle
        databaseHelper = getDBHelper()
        goalDAO = databaseHelper.getGoalsDao()

        goalsListView = findViewById(R.id.goals_list)
        updateUI()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getMenuInflater().inflate(R.menu.main_menu, menu)
        val shareButton: MenuItem = menu.findItem(R.id.action_share)
        shareActionProvider = MenuItemCompat.getActionProvider(shareButton) as ShareActionProvider
        val message = StringBuilder()
        message.append("Just look! My goals for the next $timeFrame are: \n")
        goalDAO.queryForAll()
               // .filter { goalDTO ->  goalDTO.category == categoryTitle && goalDTO.timeFrame == timeFrame}
                .map { goalDTO -> goalDTO.goalName }
                .forEach{goalName -> message.append(goalName + "\n")}
        setShareActionIntent(message.toString())
        return super.onCreateOptionsMenu(menu)
    }


    private fun setShareActionIntent(message: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, message)
        shareActionProvider.setShareIntent(intent)
    }


    fun addGoal(view: View){
        val goalsTextView: EditText = (view.parent as View).findViewById(R.id.new_goal)
        val goalTitle = goalsTextView.text.toString().capitalize()

        val timeCreated = Calendar.getInstance().time
        val newGoal = GoalDTO(0, timeFrame, categoryTitle, goalTitle, timeCreated )

        goalDAO.create(newGoal)
        updateUI()
        goalsTextView.text.clear()
    }

    private fun getDBHelper(): DatabaseHelper{
        return OpenHelperManager.getHelper(this, DatabaseHelper::class.java)
    }

    /*override fun onDestroy() {
        super.onDestroy()
        if(databaseHelper!= null){
            OpenHelperManager.releaseHelper()
            databaseHelper.close()
        }
    }*/

    fun removeGoal(view: View){
        try {
            goalDAO.delete(goalDAO.queryForAll().get(selectedGoalPosition))
            goalsListView.invalidateViews()
        } catch (e: SQLException){
            e.printStackTrace()
        }
        updateUI()
    }


    private fun updateUI(){
        allGoals = goalDAO.queryForAll()
                .filter { goalDTO ->  goalDTO.category == categoryTitle && goalDTO.timeFrame == timeFrame}
        timeCreatedField = findViewById(R.id.timeCreated)
        goalsListView.adapter = CustomAdapter(this, R.layout.item_goal, allGoals)
    }


    class CustomAdapter(context: Context, resource: Int, private val valluesList: List<GoalDTO>): ArrayAdapter<GoalDTO>(context, resource, valluesList) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
           // if(position > 0)
                selectedGoalPosition = position// - 1
            var convertView = convertView
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_goal, parent, false)
            }

            val goalNameField = convertView?.findViewById(R.id.goal_title) as TextView
            val timeCreatedField = convertView?.findViewById(R.id.timeCreated) as TextView

            goalNameField.text = valluesList[position].goalName
            timeCreatedField.text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(valluesList[position].timeCreated)
            return convertView
        }

    }

}