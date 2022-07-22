package com.abd4ll4h.navtube.dataBase.tables

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Label(
    @PrimaryKey val id :Int,
    @ColumnInfo val label :String
)