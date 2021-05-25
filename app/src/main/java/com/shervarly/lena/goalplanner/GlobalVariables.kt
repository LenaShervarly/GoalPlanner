package com.shervarly.lena.goalplanner

import android.content.Context
import com.j256.ormlite.android.apptools.OpenHelperManager
import dbmodule.DatabaseHelper

/**
 * Created by elena on 21.01.2018.
 */

const val CATEGORY_TITLE = "CATEGORY_TITLE"

fun getDBHelper(context: Context): DatabaseHelper {
    return OpenHelperManager.getHelper(context, DatabaseHelper::class.java)
}
