package com.abd4ll4h.navtube.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*

import com.abd4ll4h.navtube.DataFetch.Repository
import com.abd4ll4h.navtube.DataFetch.ResponseWrapper
import com.abd4ll4h.navtube.DataFetch.scraper.KeyText
import com.abd4ll4h.navtube.dataBase.tables.FavVideo
import com.abd4ll4h.navtube.dataBase.tables.Label
import com.abd4ll4h.navtube.utils.ConnectionLiveData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Repository.DataRepository.getInstance(application)
    private val favRepository = FavRepository(application)
    val videoItem: StateFlow<ResponseWrapper<List<FavVideo>>> = repository.videoFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000), ResponseWrapper.loading(emptyList())
    )
    val connectionChecker = ConnectionLiveData(application.applicationContext)

    init {
        loadNewVideo()
    }


    fun loadNewVideo() {
        viewModelScope.launch {
            repository.loadVidData(KeyText.genrateID())
        }
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

    fun addLabel(label: String) = viewModelScope.launch {
        favRepository.insertLabel(label)
    }
}
