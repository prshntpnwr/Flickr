package com.example.myapp.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import com.example.myapp.R
import com.example.myapp.databinding.ListItemLoadingBinding

/**
 * A generic RecyclerView paged adapter that uses Data Binding & DiffUtil.
 *
 * @param <T> Type of the items in the pageList
 * @param <V> The type of the ViewDataBinding
</V></T> */

abstract class DataBoundPagedListAdapter<T, V : ViewDataBinding>(
    appExecutors: AppExecutors,
    diffCallback: DiffUtil.ItemCallback<T>
) : PagedListAdapter<T, DataBoundViewHolder<V>>(
    AsyncDifferConfig.Builder<T>(diffCallback)
        .setBackgroundThreadExecutor(appExecutors.diskIO())
        .build()
) {
    private var networkState: Status? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<V> {
        val binding = when (viewType) {
            BIND_LOADING -> createLoadingBinding(parent)
            BIND_LIST -> createBinding(parent)
            else -> createLoadingBinding(parent)
        }
        return DataBoundViewHolder(binding)
    }

    private fun createLoadingBinding(parent: ViewGroup): V {
        return DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.list_item_loading,
            parent,
            false
        )
    }

    protected abstract fun createBinding(parent: ViewGroup): V

    open fun updateNetworkState(newNetworkState: Status) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()

        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    private fun hasExtraRow() = networkState != null && networkState != Status.SUCCESS

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1)
            BIND_LOADING
        else
            BIND_LIST
    }

    override fun onBindViewHolder(holder: DataBoundViewHolder<V>, position: Int) {
        val viewType = getItemViewType(position)

        when (viewType) {
            BIND_LIST -> {
                try {
                    getItem(position)?.let {
                        bind(holder.binding, it, position)
                        holder.binding.executePendingBindings()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            BIND_LOADING -> {
                (holder.binding as ListItemLoadingBinding).networkStatus = networkState
                (holder.binding as ListItemLoadingBinding).refresh.setOnClickListener {
                    retryAction()
                }
                holder.binding.executePendingBindings()
            }
        }

    }

    protected abstract fun bind(binding: V, item: T, position: Int)

    protected abstract fun retryAction()

    companion object {
        private const val BIND_LOADING = 0
        private const val BIND_LIST = 1
    }
}