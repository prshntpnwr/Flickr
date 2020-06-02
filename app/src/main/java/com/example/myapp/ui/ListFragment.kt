package com.example.myapp.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.myapp.R
import com.example.myapp.binding.FragmentDataBindingComponent
import com.example.myapp.databinding.FragmentListBinding
import com.example.myapp.observer.ListViewModel
import com.example.myapp.util.AppExecutors
import com.example.myapp.util.Status


/**
 * A simple [Fragment] subclass.
 */
class ListFragment : Fragment() {

    lateinit var executors: AppExecutors
    private lateinit var viewModel: ListViewModel

    private var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    private lateinit var binding: FragmentListBinding
    private lateinit var adapter: PictureListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        DataBindingUtil.inflate<FragmentListBinding>(
            inflater,
            R.layout.fragment_list,
            container,
            false,
            dataBindingComponent
        )
            .also {
                it.lifecycleOwner = this
                binding = it
                executors = AppExecutors()
            }.run {
                return this.root
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as AppCompatActivity?)?.setSupportActionBar(binding.toolbar)
        PictureListAdapter(
            dataBindingComponent = dataBindingComponent,
            appExecutors = executors
        ) { photo, action ->
            when (action) {
                RETRY -> viewModel.retry()
                ITEM_CLICK -> {
                    photo?.let {
                        val act = ListFragmentDirections.actionListFragmentToDetailFragment(photo.id)
                        findNavController().navigate(act)
                    }
                }
            }
        }.also {
            adapter = it
            binding.recyclerView.adapter = it
        }

        viewModel = ViewModelProviders.of(this)
            .get(ListViewModel::class.java)
            .also {
                binding.viewModel = it
                it.setArgs(executors)
                it.posts.observe(this, Observer { list ->
                    adapter.submitList(list)
                    viewModel.shouldFetch = false
                    binding.isEmpty = list.isEmpty() && it.networkState.value == Status.SUCCESS
                })

                it.networkState.observe(this, Observer {
                    adapter.updateNetworkState(it)
                    binding.isEmpty = adapter.itemCount == 0 && it == Status.SUCCESS
                })

                it.refreshState.observe(this, Observer {  })
            }
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_search, menu)
        val searchView = androidx.appcompat.widget.SearchView((activity as AppCompatActivity?)?.supportActionBar?.themedContext ?: context)
        menu.findItem(R.id.app_bar_search).apply {
            setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW or MenuItem.SHOW_AS_ACTION_IF_ROOM)
            actionView = searchView
        }
        searchView.setOnQueryTextListener(searchListener)
    }

    private val searchListener = object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            viewModel.shouldFetch = true
            viewModel.deleteRecords()
            viewModel.search(query ?: "")
            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            return false
        }
    }

    companion object {
        const val RETRY = 0
        const val ITEM_CLICK = 1
    }
}
