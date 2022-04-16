package com.speed.car.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.speed.car.utils.toTime
import java.io.Serializable
import java.util.*

@Entity(tableName = "history")
data class History(
    @PrimaryKey val id: Int = 0,
    @ColumnInfo(name = "exceedSpeedTime") val exceedSpeedTime: Date = Date(),
    @ColumnInfo(name = "exceedSpeedKilometers") val exceedSpeedKilometers: String = "",
    @ColumnInfo(name = "exceedSpeedKilometersAtArea") val exceedSpeedKilometersAtArea: String = "",
) : Serializable {
    val parseExceedSpeedTime: String
        get() = exceedSpeedTime.toTime("dd-MM-yyyy")
}

class DateConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
