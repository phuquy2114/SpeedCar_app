package com.speed.car.domain.usecase

import com.speed.car.data.RoomRepository
import com.speed.car.model.History
import kotlinx.coroutines.flow.Flow

class HistoryUseCase(private val db: RoomRepository) {
    fun loadHistory(): Flow<List<History>> {
        return db.getHistoryDao().loadHistory()
    }

    suspend fun insertHistory(history: History) {
        db.getHistoryDao().insertHistory(history)
    }
}