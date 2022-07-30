package com.abd4ll4h.navtube.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.abd4ll4h.navtube.dataBase.tables.FavVideo
import com.abd4ll4h.navtube.dataBase.tables.Label
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class FavFragmentViewModel(private val application1: Application):AndroidViewModel(application1) {
    private val repository:FavRepository = FavRepository(application1)
    val labelsList : MutableLiveData<List<Label>> = repository.getLabels().asLiveData() as MutableLiveData<List<Label>>
    private var _favList:MutableStateFlow<List<FavVideo>> = MutableStateFlow(listOf())
    val favList:StateFlow<List<FavVideo>> get() = _favList
    val checkedLabelList: ArrayList<Int> =ArrayList()


    init {
        viewModelScope.launch {
            _favList.emitAll(repository.getFav())
        }

    }

    fun addLabel(label: String) =viewModelScope.launch {
        repository.insertLabel(label)
    }


    fun updateFavList() {
        viewModelScope.launch {

             if (checkedLabelList.isNotEmpty()) _favList.emitAll(repository.getFav(checkedLabelList.toIntArray()))
            else _favList.emitAll(repository.getFav())
        }

    }

    fun insertFAv(favVideo: FavVideo) {

            viewModelScope.launch {
                repository.insertFav(favVideo)
            }

    }

    fun deleteFav(favVideo: FavVideo) {
        viewModelScope.launch {
            repository.deleteFav(favVideo)
        }
    }

    fun searchFav(text: String) {
        viewModelScope.launch {
            if (checkedLabelList.isNotEmpty()) _favList.emitAll(repository.searchFav(text,checkedLabelList.toIntArray()))
            else _favList.emitAll(repository.searchFav(text))
        }

    }


}