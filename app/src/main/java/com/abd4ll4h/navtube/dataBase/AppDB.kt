package com.abd4ll4h.navtube.dataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.abd4ll4h.navtube.dataBase.tables.FavVideo
import com.abd4ll4h.navtube.dataBase.tables.Label

@Database(entities = [FavVideo::class,Label::class], version = 1)
@TypeConverters(FavVideo.DateConverter::class)
abstract class AppDB: RoomDatabase() {

    abstract fun favDao():FavDao

    object DatabaseBuilder {
        private var INSTANCE: AppDB? = null
        fun getInstance(context: Context): AppDB {
            if (INSTANCE == null) {
                synchronized(AppDB::class) {
                    INSTANCE = buildRoomDB(context)
                }
            }
            return INSTANCE!!
        }
        private fun buildRoomDB(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDB::class.java,
                "NavTube-dataBase"
            ).build()
    }
}