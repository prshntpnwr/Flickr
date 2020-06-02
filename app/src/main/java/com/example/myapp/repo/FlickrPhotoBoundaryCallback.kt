package com.example.myapp.repo

import android.util.Log
import com.example.myapp.database.FlickrResponse
import com.example.myapp.database.PhotoItem
import com.example.myapp.util.AppExecutors
import com.example.myapp.util.DataBoundBoundaryCallback
import com.example.myapp.util.WebService
import retrofit2.Call

class FlickrPhotoBoundaryCallback (
    private val handleResponse: (FlickrResponse?) -> Unit,
    appExecutors: AppExecutors,
    private val webService: WebService,
    private val networkPageSize: Int,
    private val text: String? = ""
) : DataBoundBoundaryCallback<PhotoItem, FlickrResponse>(
    appExecutors = appExecutors
) {

    override fun handleAPIResponse(response: FlickrResponse?) {
        handleResponse(response)
    }

    override fun itemAtEndLoaded(item: PhotoItem): Call<FlickrResponse> {
        Log.d(Thread.currentThread().name, "itemAtEndLoaded ${item.indexInResponse}")
        return webService.fetchFlickrPhoto(
            perPage = networkPageSize,
            page = item.indexInResponse,
            text = text
        )
    }

    override fun zeroItemLoaded(): Call<FlickrResponse> {
        Log.d(Thread.currentThread().name, "zeroItemLoaded")
        return webService.fetchFlickrPhoto(
            perPage = networkPageSize,
            page = 0,
            text = text
        )
    }
}