package com.example.myapp.database

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*

@Dao
interface AppDao {

    @Query("SELECT MAX(indexInResponse) + 1 FROM photo")
    fun getNextIndex() : Int

    @Query("SELECT * FROM photo WHERE searchText = :searchText")
    fun loadPhotos(searchText: String): DataSource.Factory<Int, PhotoItem>

    @Query("SELECT * FROM photo WHERE id = :id")
    fun loadPhotoById(id: String): LiveData<PhotoItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPhoto(list: List<PhotoItem>)

    @Transaction
    fun insertPhotos(list: List<PhotoItem>, text: String) {
        val start = getNextIndex()

        list.mapIndexed { index, item ->
            item.apply {
                searchText = text
                indexInResponse = start  // no need to increment the index of each record
            }
        }

        insertPhoto(list)
    }

    @Query("DELETE FROM photo")
    fun deleteFlickrPhoto()

    @Query("DELETE FROM photo WHERE searchText = :text")
    fun deleteFlickrPhoto(text: String)

    @Transaction
    fun deleteAndInsertData(list: List<PhotoItem>, text: String) {
        deleteFlickrPhoto()
        insertPhotos(list, text)
    }
}