package com.example.myapp.observer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.ViewModel
import com.example.myapp.repo.AppRepository
import com.example.myapp.util.AppExecutors

class ListViewModel : ViewModel() {
    lateinit var executors: AppExecutors
    lateinit var repo: AppRepository
    lateinit var pageRepo: AppRepository.FlickrPhotoRepository
    var shouldFetch = true

    fun setArgs(appExecutor: AppExecutors) {
        this.executors = appExecutor
        this.repo = AppRepository(executor = appExecutor)
        this.pageRepo = repo.FlickrPhotoRepository()
    }

    private val _searchText: MutableLiveData<String> = MutableLiveData()

    val resResult = map(_searchText) { pageRepo.photoList(it) }
    val posts = switchMap(resResult) { it.pagedList }
    val networkState = switchMap(resResult) { it.networkState }
    val refreshState = switchMap(resResult) { it.refreshState }

    init {
        _searchText.value = "cosmos"
    }

    fun search(text: String) {
        if (!shouldFetch) return
        _searchText.value = text
    }

    fun deleteRecords() {
        pageRepo.deleteOlderAll()
    }

    fun refresh() {
        resResult.value?.refresh?.invoke()
    }

    fun retry() {
        val listing = resResult.value
        listing?.retry?.invoke()
    }

    fun removeOlderEntry() {
        pageRepo.deleteOlderRecords()
    }
}