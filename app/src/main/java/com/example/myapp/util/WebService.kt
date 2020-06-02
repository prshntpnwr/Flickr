package com.example.myapp.util

import com.example.myapp.database.FlickrResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * contains all api calls
 */
interface WebService {

    @GET("rest")
    fun fetchFlickrPhoto(
        @Query("method") method: String? = "flickr.photos.search",
        @Query("api_key") apiKey: String? = "062a6c0c49e4de1d78497d13a7dbb360",
        @Query("format") format: String? = "json",
        @Query("nojsoncallback") jsonCallback: Int = 1,
        @Query("per_page") perPage: Int,
        @Query("page") page: Int,
        @Query("text") text: String? = "cosmos"
    ): Call<FlickrResponse>

}