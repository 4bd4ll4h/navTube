package com.abd4ll4h.navtube.DataFetch

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(context: Context) {
    private val scraper:Scraper =Scraper(context)

    suspend fun loadVidData(query:String): Response<MutableLiveData<ArrayList<VideoTable>>> {
        return withContext(Dispatchers.IO){
            scraper.loadData(query)
        }
    }

    fun refreshData() {
        scraper.refresh()
    }
}