package com.abd4ll4h.navtube.DataFetch.scraper

import android.util.Log
import java.util.*
import kotlin.random.Random

object KeyText {
    const val firstTime: String="FIRST_LUNCH"
    const val tokenInvalidError: String="Error token got Expired"
    const val successJasonChecker: String="\"currentPageIndex\":"
    const val jasonParsingError: String = "Error making class objects :"
    const val tokenErrorMassage: String="Failed to get Token"
    const val keyText="WhereAmI"
    const val urlPrefix="https://youtube.com/watch?v="
    val urlsList= listOf("https://cse.google.com/cse?cx=52071d28291f34345")

    val alphList = mutableListOf<String>().also { list ->
        Log.i("time@check", Date().toString())
        list.addAll(CharRange('0', '9').toMutableList().map { it.toString() })
        list.addAll(CharRange('a', 'z').toMutableList().map { it -> it.toString() })
        list.addAll(CharRange('A', 'Z').toMutableList().map { it.toString() })
        list.addAll(listOf("-", "_"))
        Log.i("time@check", "Done :"+Date().toString())

    }

    fun  getBaseUrl(last:String?):String{
        return if (last!=null){
            urlsList[(urlsList.indexOf(last)+1)% urlsList.size]
        }else urlsList[0]
    }

    fun genrateID(n: Int = 4): String {

        var id = ""
        for (i in 1..n) {
            id += alphList.random(Random(Date().time))
        }
        return "allinurl:$id"
    }
}
