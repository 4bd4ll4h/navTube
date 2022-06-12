package com.abd4ll4h.navtube.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abd4ll4h.navtube.DataFetch.Repository
import com.abd4ll4h.navtube.DataFetch.Response
import com.abd4ll4h.navtube.DataFetch.VideoTable
import com.abd4ll4h.navtube.DataFetch.scraper.keyText
import kotlinx.coroutines.*

class MainFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Repository(application)
    private lateinit var videoItem: LiveData<Response<ArrayList<VideoTable>>>



    suspend fun getVideoItem():LiveData<Response<ArrayList<VideoTable>>>{
         repository.loadVidData(keyText.genrateID()).also {
             videoItem =it
             return videoItem }

    }
    fun loadNewVideo() {
        viewModelScope.launch(Dispatchers.IO) {
            val newItem = repository.loadVidData(keyText.genrateID())
            if (newItem.value!!.status==Response.Status.SUCCESS&& newItem.value!!.data.isNotEmpty()){
                videoItem.value!!.data.addAll(newItem.value!!.data)
            }
        }
    }
}
