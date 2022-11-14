package com.abd4ll4h.navtube.DataFetch.scraper

import android.content.Context
import com.abd4ll4h.navtube.DataFetch.ResponseWrapper
import com.abd4ll4h.navtube.dataBase.tables.FavVideo
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.coroutines.awaitStringResult
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataFetcher(context: Context) {
    val webViewHandler = WebViewHandler(context)


    suspend fun loadData(qurey: String): ResponseWrapper<List<FavVideo>> {
        return withContext(Dispatchers.IO) {
            val token = webViewHandler.getTokenUrl()
            if (token.status == ResponseWrapper.Status.SUCCESS) {
                FuelManager.instance.baseHeaders = mapOf("User-Agent" to webViewHandler.userAgent)
                val result = Fuel.get(token.data.replace(KeyText.keyText, qurey))
                    .awaitStringResult(scope = Dispatchers.IO)

                result.fold({ success ->
                    return@withContext if (success.contains(KeyText.successJasonChecker))
                        createDataObjects(success)
                    else {

                        ResponseWrapper.error(KeyText.tokenInvalidError, emptyList())
                    }

                }, { error ->
                    return@withContext ResponseWrapper.error(error.message, emptyList())
                })
            } else {

                return@withContext ResponseWrapper.error(token.message, emptyList())
            }

        }
    }

    private fun createDataObjects(success: String): ResponseWrapper<List<FavVideo>> {
        try {
            var jasonText = success.replaceAfterLast('}', "")
            jasonText = jasonText.replaceBefore('{', "")
            val list = mutableListOf<FavVideo>()
            val jsonVal = Gson().fromJson(jasonText, JasonMap::class.java)
            if (jsonVal.results != null) {
                for (result in jsonVal.results!!) {
                    if (result.richSnippet != null) {
                        if (result.richSnippet.person != null && result.richSnippet.videoobject != null) {
                            with(result.richSnippet.videoobject) {

                                val video: FavVideo = FavVideo(
                                    id = this!!.videoid,
                                    title = name,
                                    isfamilyfriendly = isfamilyfriendly,
                                    uploaddate = FavVideo.DateConverter.fromTimestamp(uploaddate),
                                    duration = duration,
                                    unlisted = unlisted,
                                    paid = paid,
                                    genre = genre,
                                    interactioncount = interactioncount,
                                    thumbnailurl = thumbnailurl,
                                    creator = result.richSnippet.person!!.name,
                                    channelUrl = result.richSnippet.person!!.url,
                                    datepublished = FavVideo.DateConverter
                                        .fromTimestamp(datepublished),
                                    description = result.contentNoFormatting,
                                    channelThumbnail = result.richSnippet.cseThumbnail.channelThumbnail
                                )
                                list.add(video)
                            }
                        }
                    }
                }

            }
            return ResponseWrapper.success(list)

        } catch (e: Exception) {
            return ResponseWrapper.error(KeyText.jasonParsingError + e.message, emptyList())
        }
    }

    suspend fun setUpConnection(): Boolean {
        val isSuccess = webViewHandler.getTokenUrl()
        return isSuccess.status == ResponseWrapper.Status.SUCCESS
    }
}