package com.abd4ll4h.navtube.viewModel

import android.app.Application
import android.content.Context
import androidx.annotation.WorkerThread
import com.abd4ll4h.navtube.DataFetch.VideoTable
import com.abd4ll4h.navtube.dataBase.AppDB
import com.abd4ll4h.navtube.dataBase.tables.FavVideo
import com.abd4ll4h.navtube.dataBase.tables.Label
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FavRepository(val context: Context) {
    private val database by lazy { AppDB.DatabaseBuilder.getInstance(context).favDao()}

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
    fun getFav(query: String?= null,labelId:IntArray?=null):Flow<List<FavVideo>>{
        return if (query!=null && labelId==null)
            database.searchFavAll(query)
        else if (query==null && labelId!=null)
            database.getFavFilter(labelId)
        else if (query!=null && labelId!=null)
            database.searchFavWithLabel(query,labelId)
        else database.getFav()
    }



    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAllFav(list: List<Label>) {
        withContext(Dispatchers.IO){
           if (list.isNotEmpty()) database.deleteAllFav(list)
            database.deleteAllFavWithoutLabel()

        }
    }

   suspend fun clearAllFav(list: List<FavVideo>) {
        withContext(Dispatchers.IO){
            database.clearAllFav(list)
        }
    }

    suspend fun deleteLabel(labelID: Int) {
        withContext(Dispatchers.IO){
            database.deleteLabel(labelID)
        }
    }

    suspend fun clearLabel(labelID: Int) {
        withContext(Dispatchers.IO){
            database.clearLabel(labelID)
        }
    }

    suspend fun updateLabel(label: Label) {
        withContext(Dispatchers.IO){
            database.updateLabel(label)
        }
    }


}