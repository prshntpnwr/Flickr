package com.example.myapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.example.myapp.R
import com.example.myapp.database.PhotoItem
import com.example.myapp.databinding.ListItemPhotoBinding
import com.example.myapp.util.AppExecutors
import com.example.myapp.util.DataBoundPagedListAdapter
import com.example.myapp.util.Status

class PictureListAdapter(
    private val dataBindingComponent: DataBindingComponent,
    appExecutors: AppExecutors,
    private val callback: ((PhotoItem?, action: Int) -> Unit)?
) : DataBoundPagedListAdapter<PhotoItem, ListItemPhotoBinding>(
    appExecutors = appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<PhotoItem>() {
        override fun areItemsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem.id == newItem.id
        }
    }
) {
    override fun retryAction() {
        callback?.invoke(null, ListFragment.RETRY)
    }

    private var netState: Status? = null
    override fun updateNetworkState(newNetworkState: Status) {
        super.updateNetworkState(newNetworkState)
        this.netState = newNetworkState
    }

    override fun createBinding(parent: ViewGroup): ListItemPhotoBinding {
        DataBindingUtil
            .inflate<ListItemPhotoBinding>(
                LayoutInflater.from(parent.context),
                R.layout.list_item_photo,
                parent,
                false,
                dataBindingComponent
            ).run { return this }
    }

    override fun bind(binding: ListItemPhotoBinding, item: PhotoItem, position: Int) {
        binding.photo = item
        binding.root.setOnClickListener {
            callback?.invoke(item, ListFragment.ITEM_CLICK)
        }
    }
}