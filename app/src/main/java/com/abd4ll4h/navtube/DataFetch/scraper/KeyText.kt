package com.abd4ll4h.navtube.DataFetch.scraper

import android.util.Log
import java.util.*

object keyText {
    const val keyText="WhereAmI"
    const val urlPrefix="https://youtube.com/watch?v="

    val alphList = mutableListOf<String>().also { list ->
        Log.i("time@check", Date().toString())
        list.addAll(CharRange('0', '9').toMutableList().map { it.toString() })
        list.addAll(CharRange('a', 'z').toMutableList().map { it -> it.toString() })
        list.addAll(CharRange('A', 'Z').toMutableList().map { it.toString() })
        list.addAll(listOf("-", "_"))
        Log.i("time@check", "Done :"+Date().toString())

    }

    fun genrateID(n: Int = 4): String {

        var id = ""
        for (i in 1..n) {
            id += alphList.random()
        }
        return id
    }
}
