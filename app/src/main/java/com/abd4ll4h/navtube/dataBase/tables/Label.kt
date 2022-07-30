package com.abd4ll4h.navtube.dataBase.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Label(
    @PrimaryKey(autoGenerate = true) val id :Int=0,
    @ColumnInfo val label :String,
    var isChecked :Boolean =false
)