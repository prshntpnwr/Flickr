package com.example.myapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.myapp.R
import com.example.myapp.binding.FragmentDataBindingComponent
import com.example.myapp.databinding.DetailFragmentBinding
import com.example.myapp.observer.DetailViewModel
import com.example.myapp.util.AppExecutors
import com.example.myapp.util.toBitmap

class DetailFragment : Fragment() {

    lateinit var executors: AppExecutors
    private lateinit var viewModel: DetailViewModel
    private lateinit var binding: DetailFragmentBinding
    private var photoId = "0"

    private var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(PHOTO_ID, photoId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        DataBindingUtil.inflate<DetailFragmentBinding>(inflater,
            R.layout.detail_fragment,
            container,
            false,
            dataBindingComponent)
            .also {
                executors = AppExecutors()
                it.lifecycleOwner = this
                binding = it
                setupToolbar()
            }.run {
                return this.root
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        photoId = savedInstanceState?.getString(PHOTO_ID) ?: DetailFragmentArgs.fromBundle(arguments).photoId

        ViewModelProviders.of(this)
            .get(DetailViewModel::class.java)
            .also {
                binding.viewModel = it
                viewModel = it
                it.init(appExecutor = executors)
                it.setResId(resId = photoId)
                it.result.observe(this, Observer {
                })
            }
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity?)?.setSupportActionBar(binding.detailToolbar)
        (activity as AppCompatActivity?)?.supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.detailToolbar.let {toolbar ->
            toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.avd_arrow_back)
            toolbar.setNavigationOnClickListener {
                requireActivity().onBackPressed()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.menu_detail, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.action_share)
            sharePhoto()
        return super.onOptionsItemSelected(item)
    }

    private fun sharePhoto() {
        val bitmapDrawable =  binding.image.drawable.toBitmap()
        val bitmapPath = MediaStore.Images.Media.insertImage(context?.contentResolver, bitmapDrawable,"some title", null)
        val bitmapUri = Uri.parse(bitmapPath);
        val shareIntent= Intent(Intent.ACTION_SEND);
        shareIntent.type = "image/jpeg";
        shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey Checkout this! \n ${viewModel.result.value?.data?.title}")
        startActivity(Intent.createChooser(shareIntent,"Share Image"))
    }

    companion object {
        const val PHOTO_ID = "photo_id"
    }
}
