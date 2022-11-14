package com.abd4ll4h.navtube.dataBase.tables

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Entity(foreignKeys = [ForeignKey(
    entity = Label::class,
    parentColumns = ["id"],
    childColumns = ["label"],
    onDelete = CASCADE)]
)

data class FavVideo(@PrimaryKey val id: String,
                    @ColumnInfo val title: String,
                    @ColumnInfo var isfamilyfriendly: Boolean?,
                    @ColumnInfo var uploaddate: Date?,
                    @ColumnInfo var duration: String?,
                    @ColumnInfo var unlisted: Boolean?,
                    @ColumnInfo var paid: Boolean?,
                    @ColumnInfo var genre: String?,
                    @ColumnInfo var interactioncount: Int?,
                    @ColumnInfo var thumbnailurl: String,
                    @ColumnInfo var creator: String?,
                    @ColumnInfo var channelUrl: String?,
                    @ColumnInfo var channelThumbnail: String? = null,
                    @ColumnInfo var status: Int = 0,
                    @ColumnInfo var isFav: Boolean = false,
                    @ColumnInfo var datepublished: Date?,
                    @ColumnInfo var description: String? = null,
                    @ColumnInfo(index = true) var label: Int?=null
                    ) {

    object DateConverter {
        @TypeConverter
        fun toDate(dateLong: Long?): Date? {
            return dateLong?.let { Date(it) }
        }

        @TypeConverter
        fun fromDate(date: Date?): Long? {
            return date?.time
        }

        fun fromTimestamp(value: String?): Date? {
            try {
                return SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.ENGLISH
                ).parse(value)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return Date(value)
        }
    }
}