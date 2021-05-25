package dbmodule

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil


/**
 * Created by elena on 28.01.2018.
 */

fun main(args: Array<String>) {
    // Provide the name of .txt file which you have already created and kept in res/raw directory
    OrmLiteConfigUtil.writeConfigFile("ormlite_config.txt")
}