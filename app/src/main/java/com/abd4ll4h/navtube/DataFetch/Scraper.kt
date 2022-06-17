package com.abd4ll4h.navtube.DataFetch

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.abd4ll4h.navtube.DataFetch.scraper.JasonMap
import com.abd4ll4h.navtube.DataFetch.scraper.UrlCallBack
import com.abd4ll4h.navtube.DataFetch.scraper.WebScraper
import com.abd4ll4h.navtube.DataFetch.scraper.keyText
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.google.gson.Gson
import kotlinx.coroutines.*

typealias SuspendedValue<T> = CompletableDeferred<T>

class Scraper(context: Context) : UrlCallBack {


    public fun <T> SuspendedValue(parent: Job? = null): SuspendedValue<T> =
        CompletableDeferred(parent)

    fun <T> SuspendedValue<T>.set(t: T) = this.complete(t)
    suspend fun <T> SuspendedValue<T>.get(): T = this.await()
    private var url = CompletableDeferred<Uri>()


    private val webScraper: WebScraper by lazy { WebScraper(context, this) }

    init {

        loadUrl()
    }

    override fun getTokenUrl(link: Uri) {
        url.set(link)
    }

    fun loadUrl() {
        webScraper.loadURL(" https://cse.google.com/cse?cx=52071d28291f34345")
    }

    suspend fun loadData(query: String): Response<MutableLiveData<ArrayList<VideoTable>>> {

        return withContext(Dispatchers.IO) {
            val data = MutableLiveData<ArrayList<VideoTable>>()
            val mUrl = url.get().toString().replace(keyText.keyText, query)
            Log.i("url@check", mUrl)
            FuelManager.instance.baseHeaders = mapOf("User-Agent" to webScraper.userAgent)
            val (request, response, result) = Fuel.get(mUrl).awaitStringResponseResult()
            result.fold(
                { success ->
                    Log.i("check", success.substring(1, 30))
                    if (success.contains("\"currentPageIndex\":")) {
                        data.postValue(jasonToVideoObj(success))
                        return@withContext (Response.success(data))
                    } else return@withContext (Response.error(
                        "renew Token",
                        MutableLiveData<ArrayList<VideoTable>>()
                    ))
                },
                { error ->
                    return@withContext (Response.error(
                        error.message,
                        MutableLiveData<ArrayList<VideoTable>>()
                    ))
                })

        }
    }


    fun jasonToVideoObj(responseText: String): ArrayList<VideoTable> {
        var jasonText = responseText.replaceAfterLast('}', "")
        jasonText = jasonText.replaceBefore('{', "")
        val gson = Gson()
        val jsonVal = gson.fromJson(jasonText, JasonMap::class.java)

        if (jsonVal.results != null) {
            val list = mutableListOf<VideoTable>()
            for (result in jsonVal.results!!) {
                if (result.richSnippet.person != null && result.richSnippet.videoobject != null) {
                with(result.richSnippet.videoobject) {

                        val video: VideoTable = VideoTable(
                            videoid,
                            name,
                            isfamilyfriendly,
                            VideoTable.Converters().fromTimestamp(uploaddate),
                            duration,
                            unlisted,
                            paid,
                            genre,
                            interactioncount,
                            thumbnailurl,
                            result.richSnippet.person.name,
                            result.richSnippet.person.url,
                            datepublished = VideoTable.Converters().fromTimestamp(datepublished),
                            description = result.contentNoFormatting,
                            channelThumbnail = result.richSnippet.cseThumbnail?.let { it.channelThumbnail }
                        )
                        list.add(video)
                    }
                }


            }
            return list as ArrayList<VideoTable>
        }
        return ArrayList()
    }

    fun refresh() {
        url = CompletableDeferred<Uri>()
        webScraper.clearAll()
        webScraper.reload()
    }


}