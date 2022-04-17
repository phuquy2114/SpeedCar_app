package com.speed.car.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.speed.car.model.History
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
	@Insert
	suspend fun insertHistory(history: History)

	@Query("SELECT * FROM history")
	fun loadHistory(): Flow<List<History>>
}
