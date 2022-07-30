package com.abd4ll4h.navtube.viewModel

import android.app.Application
import androidx.annotation.WorkerThread
import com.abd4ll4h.navtube.DataFetch.VideoTable
import com.abd4ll4h.navtube.dataBase.AppDB
import com.abd4ll4h.navtube.dataBase.tables.FavVideo
import com.abd4ll4h.navtube.dataBase.tables.Label
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FavRepository(val application: Application) {
    private val database by lazy { AppDB.DatabaseBuilder.getInstance(application).favDao()}

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertLabel(label: String):Long= withContext(Dispatchers.IO){database.insertLabel(Label(label = label))}

    fun getLabels(): Flow<List<Label>> = database.getLabels()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertFav(videoTable: FavVideo):Long = withContext(Dispatchers.IO){
        database.insertFav(videoTable)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteFav(videoTable: FavVideo) {
        withContext(Dispatchers.IO){
            database.deleteFav(videoTable)
        }
    }
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateFav(video: FavVideo) {
        withContext(Dispatchers.IO){
            database.updateFav(video)
        }
    }
    fun getFav(id:IntArray?=null):Flow<List<FavVideo>>{
        return if (id==null) database.getFav()
        else database.getFavFilter(id)
    }

    fun searchFav(text: String, checkedLabelList: IntArray?=null): Flow<List<FavVideo>> {
        return  if(checkedLabelList==null) database.searchFavAll(text)
        else database.searchFavWithLabel(text,checkedLabelList)
    }


}