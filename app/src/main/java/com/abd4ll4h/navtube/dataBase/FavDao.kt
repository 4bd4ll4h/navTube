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

    @Query("select * from FavVideo where isFav = 1 ")
    fun  getFav(): Flow<List<FavVideo>>

    @Query("select * from  FavVideo where label IN (:labelID)")
    fun  getFavFilter(labelID: IntArray): Flow<List<FavVideo>>

    @Query("select * from FavVideo where title like '%' || :text || '%' or creator like '%' || :text || '%'")
    fun searchFavAll(text: String): Flow<List<FavVideo>>

    @Query("select * from FavVideo where label IN (:labelID) and (title like '%' || :text || '%' or creator like '%' || :text || '%') ")
    fun searchFavWithLabel(text: String, labelID: IntArray): Flow<List<FavVideo>>

    @Delete
    fun deleteAllFav(list: List<Label>)

    @Delete
    fun clearAllFav(list: List<FavVideo>)

    @Query(" delete  from Label where  id = :id ")
    fun deleteLabel(id: Int)

    @Query("delete from FavVideo where label = :labelID")
    fun clearLabel(labelID: Int)

    @Update(onConflict = REPLACE)
    fun updateLabel(label: Label)

    @Query("delete from FavVideo ")
    fun deleteAllFavWithoutLabel()
}