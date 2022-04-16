package com.speed.car.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class History(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	@ColumnInfo(name = "name") val name: String
)
