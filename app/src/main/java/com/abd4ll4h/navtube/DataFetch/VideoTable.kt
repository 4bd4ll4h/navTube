package com.abd4ll4h.navtube.DataFetch


import android.util.Log
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


data class VideoTable(
    val id: String,
    val title: String,
    var isfamilyfriendly: Boolean?,

    var uploaddate: Date?,
    var duration: String?,
    var unlisted: Boolean?,
    var paid: Boolean?,
    var genre: String?,
    var interactioncount: Int?,
    var thumbnailurl: String,
    var creator: String?,
    var channelUrl: String?,
    var channelThumbnail: String? = null,
    var status: Int = 0,
    var isLiked: Boolean = false,

    var datepublished: Date?,
    var description: String? = null
) {


    class Converters() {

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

        fun dateToTimestamp(date: Date?): String? {
            return if (date == null) {
                null
            } else {
                SimpleDateFormat("""yyyy-MM-dd""", Locale.ENGLISH)
                    .format(date)
            }
        }
    }


}
