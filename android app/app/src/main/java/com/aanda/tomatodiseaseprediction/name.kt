package com.aanda.tomatodiseaseprediction

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "name-table")

data class name(
    @PrimaryKey(autoGenerate = true)
    var id:Int = 0,
    @ColumnInfo(name = "userName")
    var userName:String = ""
)
