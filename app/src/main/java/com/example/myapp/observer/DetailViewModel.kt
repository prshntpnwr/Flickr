package com.example.myapp.observer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.myapp.database.PhotoItem
import com.example.myapp.repo.AppRepository
import com.example.myapp.util.AbsentedLiveData
import com.example.myapp.util.AppExecutors
import com.example.myapp.util.Resource

class DetailViewModel : ViewModel() {
    lateinit var executors: AppExecutors
    lateinit var repo: AppRepository

    fun init(appExecutor: AppExecutors) {
        this.executors = appExecutor
        this.repo = AppRepository(executor = appExecutor)
    }

    private val _resId: MutableLiveData<String> = MutableLiveData()
    val resId: LiveData<String>
        get() = _resId

    fun setResId(resId: String) {
        _resId.value = resId
    }

    val result: LiveData<Resource<PhotoItem>> = Transformations
        .switchMap(_resId) {
            when(it) {
                null -> AbsentedLiveData.create()
                else -> repo.fetchPhotoForId(it)
            }
        }
}
