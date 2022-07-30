package com.abd4ll4h.navtube.viewModel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*

import com.abd4ll4h.navtube.DataFetch.Repository
import com.abd4ll4h.navtube.DataFetch.Response
import com.abd4ll4h.navtube.DataFetch.VideoTable
import com.abd4ll4h.navtube.DataFetch.scraper.keyText
import com.abd4ll4h.navtube.dataBase.tables.FavVideo
import com.abd4ll4h.navtube.dataBase.tables.Label
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Repository(application)
    private val favRepository = FavRepository(application)
    private lateinit var videoItem: MutableLiveData<ArrayList<VideoTable>>


    init {
        viewModelScope.launch {
            getVideoItem()
        }
    }

    suspend fun getVideoItem(): LiveData<ArrayList<VideoTable>> {
        if (::videoItem.isInitialized) return videoItem
        repository.loadVidData(keyText.genrateID()).also {
            videoItem = it.data
            return videoItem
        }

    }

    suspend fun loadNewVideo() {

        val response = repository.loadVidData(keyText.genrateID())
        if (checkResponse(response))
            videoItem.postValue(videoItem.value.also { it!!.addAll(response.data.value!!) })
        else handleError(response.message)
    }

    suspend fun refreshList() {

        repository.refreshData()
        val response = repository.loadVidData(keyText.genrateID())
        if (checkResponse(response)) {
            Log.i("sdaf", "checking" + response.data.value!!.size)
            if (::videoItem.isInitialized) videoItem.postValue(response.data.value)
            else videoItem = response.data
        } else handleError(response.message)

    }

    private fun handleError(message: String?) {
        Log.i("ErrorCheck", "message: $message")
    }

    fun checkResponse(response: Response<MutableLiveData<ArrayList<VideoTable>>>): Boolean {
        return response.status == Response.Status.SUCCESS && response.data.value!!.isNotEmpty()
    }

    fun insertFAv(videoTable: FavVideo) {
        viewModelScope.launch {
             favRepository.insertFav(videoTable)
        }
    }

    fun deleteFav(videoTable: FavVideo) {
        viewModelScope.launch {
            favRepository.deleteFav(videoTable)
        }
    }

    fun getLabels(): LiveData<List<Label>> {
       return favRepository.getLabels().asLiveData()
    }

    fun updateFav(video: FavVideo) {
        viewModelScope.launch {
            favRepository.updateFav(video)
        }
    }

    fun addLabel(label: String) =viewModelScope.launch {
        favRepository.insertLabel(label)
    }
}
