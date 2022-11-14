package com.abd4ll4h.navtube.DataFetch

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.abd4ll4h.navtube.DataFetch.scraper.DataFetcher
import com.abd4ll4h.navtube.dataBase.tables.FavVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

class Repository(context: Context) {
    private val scraper:DataFetcher= DataFetcher(context)
    private val  _videoFlow:MutableSharedFlow<ResponseWrapper<List<FavVideo>>> =MutableSharedFlow(1,5,BufferOverflow.DROP_OLDEST)
    val videoFlow=_videoFlow.asSharedFlow()
    private var wasErorrResulte=false

    object   DataRepository{
        var INSTANCE:Repository?=null
        fun getInstance(context: Context):Repository{
            if (INSTANCE == null) {
                synchronized(Repository::class) {
                    INSTANCE= Repository(context)
                      }
            }
            return INSTANCE!!
        }
    }
    suspend fun loadVidData(query:String)  {
         withContext(Dispatchers.IO){
             if (wasErorrResulte) refreshToken()
              val result= scraper.loadData(query)
                 wasErorrResulte = result.status==ResponseWrapper.Status.ERROR
                 _videoFlow.emit(result)

        }
    }

    private suspend fun refreshToken() {
        scraper.webViewHandler.resetWebView()
    }

    suspend fun setUpConnection(context: Context?): Boolean {
     return scraper.setUpConnection()
    }
}