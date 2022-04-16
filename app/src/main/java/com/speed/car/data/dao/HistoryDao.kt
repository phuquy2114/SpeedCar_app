package com.speed.car.data.dao

import androidx.room.Dao
import androidx.room.Insert
import com.speed.car.model.History

@Dao
interface HistoryDao {
	@Insert
	suspend fun insertHistory(history: History)
}
