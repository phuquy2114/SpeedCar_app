package com.speed.car.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.speed.car.data.dao.HistoryDao
import com.speed.car.model.DateConverters
import com.speed.car.model.History

@Database(
    entities = [History::class],
    version = 1
)
@TypeConverters(
    DateConverters::class
)
abstract class RoomRepository : RoomDatabase() {
    abstract fun getHistoryDao(): HistoryDao

    companion object {
        private var INSTANCE: RoomRepository? = null
        private const val DB_NAME = "db_history.db"

        fun getDatabase(context: Context): RoomRepository {
            if (INSTANCE == null) {
                synchronized(RoomRepository::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            RoomRepository::class.java,
                            DB_NAME
                        )
                            .allowMainThreadQueries()
                            .build()
                    }
                }
            }
            return INSTANCE!!
        }
    }
}
