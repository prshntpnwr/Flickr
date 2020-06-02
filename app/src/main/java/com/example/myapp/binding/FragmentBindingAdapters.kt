package com.example.myapp.binding

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import javax.inject.Inject

/**
 * Binding adapters that work with a fragment instance.
 */
class FragmentBindingAdapters @Inject constructor(val fragment: Fragment)  {

    @BindingAdapter("viewVisible")
    fun showHide(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }

    @BindingAdapter("imageUrl")
    fun bindImage(imageView: ImageView, url: String?) {
        if (url.isNullOrEmpty()) return
        val circularProgressDrawable = CircularProgressDrawable(fragment.requireContext())
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        Glide.with(fragment.context).load(url).placeholder(circularProgressDrawable).into(imageView)
    }

}