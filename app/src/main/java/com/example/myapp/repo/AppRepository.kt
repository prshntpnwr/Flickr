package com.example.myapp.repo

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.example.myapp.database.AppDao
import com.example.myapp.database.FlickrResponse
import com.example.myapp.database.PhotoItem
import com.example.myapp.util.*
import retrofit2.Call
import javax.inject.Singleton

@Singleton
class AppRepository constructor(
    private val webservice: WebService = WebServiceProvider.instance.getWebService(),
    private val executor: AppExecutors,
    private val dao: AppDao = DatabaseProvider.instance.getAppDao(AppController.instance)
) {

    fun fetchPhotoForId(id: String): LiveData<Resource<PhotoItem>> {
        return object : NetworkBoundResource<PhotoItem, PhotoItem>(executor) {
            override fun saveCallResult(item: PhotoItem) {
                TODO()
            }

            override fun shouldFetch(data: PhotoItem?) = false

            override fun loadFromDb() = dao.loadPhotoById(id = id)

            override fun createCall() = TODO()

        }.asLiveData()
    }

    companion object {
        internal var repo: AppRepository? = null

        val instance: AppRepository
            get() {
                if (repo == null) {
                    repo = AppRepository(executor = AppExecutors())
                }
                return repo!!
            }
    }

    inner class FlickrPhotoRepository : PaginationRepository<PhotoItem, FlickrResponse>(
        executors = executor,
        pagedListConfig = WebServiceProvider.instance.getPagedConfig()
    ) {
        private var text: String = "cosmos"
        private val networkPageSize: Int = 10

        @MainThread
        fun photoList(text: String): Listing<PhotoItem> {
            this.text = text
            return response()
        }

        override fun refreshAPI(): Call<FlickrResponse> {
            return webservice.fetchFlickrPhoto(
                perPage = networkPageSize,
                page = 0
            )
        }

        override fun boundaryCallback(): DataBoundBoundaryCallback<PhotoItem, FlickrResponse> {
            return FlickrPhotoBoundaryCallback(
                handleResponse = this::insertResultIntoDb,
                appExecutors = executor,
                webService = webservice,
                text = text,
                networkPageSize = networkPageSize
            )
        }

        override fun dataSourceFactory(): DataSource.Factory<Int, PhotoItem> {
            return dao.loadPhotos(searchText = text)
        }


        override fun refreshOperation(response: FlickrResponse?) {
            deleteAndInsertResultIntoDb(response)
        }


        /**
         * Insert the response into the database
         */
        private fun insertResultIntoDb(body: FlickrResponse?) {
            body?.photos?.photo?.let { it -> dao.insertPhotos(it, text) }
        }


        /**
         * Delete older records from data base
         * Insert the response into the database
         */
        private fun deleteAndInsertResultIntoDb(body: FlickrResponse?) {
            body?.photos?.photo?.let {
                dao.deleteAndInsertData(it, text)
            }
        }

        fun deleteOlderRecords() {
            executor.diskIO().execute {
                dao.deleteFlickrPhoto(text)
            }
        }


        fun deleteOlderAll() {
            executor.diskIO().execute {
                dao.deleteFlickrPhoto()
            }
        }
    }
}