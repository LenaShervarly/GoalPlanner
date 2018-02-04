package com.shervarly.lena.goalplanner

import android.content.Context
import android.widget.ArrayAdapter
import com.j256.ormlite.android.apptools.OpenHelperManager
import dbmodule.DatabaseHelper

/**
 * Created by elena on 21.01.2018.
 */
val TIME_FRAME = "TIME_FRAME"
val CATEGORY_TITLE = "CATEGORY_TITLE"
fun getItemAdapter(context: Context, resItem: Int, resTitle: Int, listToFill: List<Any> = ArrayList()): ArrayAdapter<Any> {
    return ArrayAdapter(context, resItem, resTitle, listToFill)
}

fun getDBHelper(context: Context): DatabaseHelper {
    return OpenHelperManager.getHelper(context, DatabaseHelper::class.java)
}
