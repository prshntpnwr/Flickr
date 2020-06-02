package com.example.myapp.util

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class PaginationRepository<T, R>(
    private val executors: AppExecutors,
    private val pagedListConfig: PagedList.Config
) {

    @MainThread
    protected fun response(): Listing<T> {
        val boundaryCallback = boundaryCallback()
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refresh()
        }
        val dataSourceFactory = dataSourceFactory()
        val livePagedList = LivePagedListBuilder<Int, T>(dataSourceFactory, pagedListConfig)
                .setBoundaryCallback(boundaryCallback)
                .setFetchExecutor(executors.diskIO()).build()

        return Listing(
            pagedList = livePagedList,
            networkState = boundaryCallback.networkState,
            retry = {
                boundaryCallback.helper.retryAllFailed()
            },
            refresh = {
                refreshTrigger.value = null
            },
            refreshState = refreshState
        )
    }

    @MainThread
    private fun refresh(): LiveData<Status> {
        val networkState = MutableLiveData<Status>()
        networkState.value = Status.LOADING
        refreshAPI().enqueue(createWebserviceCallback(networkState))
        return networkState
    }

    protected abstract fun refreshAPI(): Call<R>


    protected abstract fun boundaryCallback(): DataBoundBoundaryCallback<T, R>

    protected abstract fun dataSourceFactory(): DataSource.Factory<Int, T>

    protected abstract fun refreshOperation(response: R?)

    private fun createWebserviceCallback(networkState: MutableLiveData<Status>)
            : Callback<R> {
        return object : Callback<R> {
            override fun onFailure(call: Call<R>, t: Throwable) {
                // retrofit calls this on main thread so safe to call set value
                networkState.value = Status.ERROR
            }

            override fun onResponse(
                call: Call<R>,
                response: Response<R>
            ) {
                executors.diskIO().execute {
                    refreshOperation(response.body())
                    // since we are in bg thread now, post the result.
                    networkState.postValue(Status.SUCCESS)
                }
            }
        }
    }

}