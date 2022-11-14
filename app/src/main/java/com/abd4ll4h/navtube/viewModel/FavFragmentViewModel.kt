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
    val labelsList  = repository.getLabels().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
        listOf())
    val checkedLabelList: MutableStateFlow<ArrayList<Int>?> = MutableStateFlow(null)
     val searchQuery: MutableStateFlow<String?> = MutableStateFlow(null)
    val favList:StateFlow<List<FavVideo>>   = checkedLabelList.combine(labelsList){
       a,b->  a }.combine(searchQuery){ list, query ->  list }.flatMapLatest {
        repository.getFav(searchQuery.value, it?.toIntArray())
    }.buffer().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf())





    fun addLabel(label: String, id: Int) =viewModelScope.launch {
        if (id ==0) repository.insertLabel(label)
        else repository.updateLabel(Label(id,label))
    }


//    fun updateFavList() {
//        viewModelScope.launch {
//
//             if (checkedLabelList.isNotEmpty()) _favList.emitAll(repository.getFav(checkedLabelList.toIntArray()))
//            else _favList.emitAll(repository.getFav())
//        }
//
//    }

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

//    fun searchFav(text: String) {
//        viewModelScope.launch {
//            if (checkedLabelList.isNotEmpty()) _favList.emitAll(repository.searchFav(text,checkedLabelList.toIntArray()))
//            else _favList.emitAll(repository.searchFav(text))
//        }
//
//    }

    fun deleteAllFav() = viewModelScope.launch {
                repository.deleteAllFav(labelsList.value)
        }



    fun clearAllFav() = viewModelScope.launch {
            repository.clearAllFav(favList.value) }


    fun deleteLabel() {
        viewModelScope.launch {
            repository.deleteLabel(checkedLabelList.value!![0])
            checkedLabelList.value =null
        }
    }

    fun clearLabel() {
        viewModelScope.launch{
            repository.clearLabel(checkedLabelList.value!![0])
        }
    }

    fun updateFavVideo(video: FavVideo) {
        viewModelScope.launch {
            repository.updateFav(video)
        }
    }


}