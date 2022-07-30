package com.abd4ll4h.navtube.dataBase

import androidx.room.*
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.OnConflictStrategy.REPLACE
import com.abd4ll4h.navtube.dataBase.tables.FavVideo
import com.abd4ll4h.navtube.dataBase.tables.Label
import kotlinx.coroutines.flow.Flow


@Dao
interface FavDao {

    @Insert(onConflict = IGNORE)
    suspend fun insertLabel(label: Label):Long

    @Query("select * from Label")
    fun  getLabels(): Flow<List<Label>>

    @Insert(onConflict = REPLACE)
    suspend fun insertFav(videoTable: FavVideo):Long
    @Delete
    fun deleteFav(videoTable: FavVideo)

    @Update(onConflict = REPLACE)
    fun updateFav(video: FavVideo)

    @Query("select * from FavVideo")
    fun  getFav(): Flow<List<FavVideo>>

    @Query("select * from  FavVideo where label IN (:labelID)")
    fun  getFavFilter(labelID: IntArray): Flow<List<FavVideo>>

    @Query("select * from FavVideo where title  like '%' || :text || '%'")
    fun searchFavAll(text: String): Flow<List<FavVideo>>

    @Query("select * from FavVideo where label IN (:labelID) and (title like '%' || :text || '%' or creator like '%' || :text || '%') ")
    fun searchFavWithLabel(text: String, labelID: IntArray): Flow<List<FavVideo>>
}