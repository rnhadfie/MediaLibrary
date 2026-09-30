package com.example.medialibrary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.medialibrary.databinding.DialogSortContentBinding
import com.example.medialibrary.utils.FragmentType
import com.example.medialibrary.utils.SharedRefreshViewModel
import models.shared.DisplayMediaItem
import models.shared.MediaItem

abstract class BaseFragment<VB : ViewBinding, VM : ViewModel>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : Fragment() {

    private var _binding: VB? = null
    protected val binding: VB
        get() = _binding ?: throw IllegalStateException("Binding is only valid between onCreateView and onDestroyView")

    protected lateinit var viewModel: VM

    private var _fragmentType: FragmentType? = null
    protected val fragmentType: FragmentType?
        get() = _fragmentType

    fun setFragmentType(type: FragmentType) {
        _fragmentType = type
    }

    private var _dialogView: DialogSortContentBinding? = null

    fun setDialogSort(dialog: DialogSortContentBinding) {
        _dialogView = dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingInflater(inflater, container, false)
        setupRefreshListener()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleTextView = getTextView(view)
        if (titleTextView != null) {
            TextViewCompat.setAutoSizeTextTypeWithDefaults(
                titleTextView,
                TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
            )
        }
    }

    protected open fun onRefreshData() {
        // To be overridden by subclasses to reload data when SharedRefreshViewModel updates
    }

    protected fun setupRefreshListener() {
        activity?.let { act ->
            val refreshViewModel = ViewModelProvider(act)[SharedRefreshViewModel::class.java]
            refreshViewModel.refreshVersion.observe(viewLifecycleOwner) {
                onRefreshData()
            }
        }
    }

    fun setupEmptyStateObserver(
        items: LiveData<out List<DisplayMediaItem>>,
        recyclerView: RecyclerView?,
        emptyStateContainerRoot: TextView,
        adapter: ListAdapter<DisplayMediaItem, *>
    ) {
        items.observe(viewLifecycleOwner) { itemList ->
            if (itemList.isNullOrEmpty()) {
                recyclerView?.visibility = View.GONE
                emptyStateContainerRoot.visibility = View.VISIBLE
            } else {
                recyclerView?.visibility = View.VISIBLE
                emptyStateContainerRoot.visibility = View.GONE
                adapter.submitList(itemList)
            }
        }
    }

    fun setupEmptyStateObserver(
        items: LiveData<out List<DisplayMediaItem>>,
        recyclerView: View,
        emptyStateContainerRoot: TextView
    ) {
        items.observe(viewLifecycleOwner) { itemList ->
            if (itemList.isNullOrEmpty()) {
                recyclerView.visibility = View.GONE
                emptyStateContainerRoot.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyStateContainerRoot.visibility = View.GONE
            }
        }
    }

    fun setupEmptyStateMediaItemObserver(
        items: LiveData<out List<MediaItem>>,
        recyclerView: View,
        emptyStateContainerRoot: TextView
    ) {
        items.observe(viewLifecycleOwner) { itemList ->
            if (itemList.isNullOrEmpty()) {
                recyclerView.visibility = View.GONE
                emptyStateContainerRoot.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyStateContainerRoot.visibility = View.GONE
            }
        }
    }

    private fun getTextView(view: View): TextView? {
        return when (fragmentType) {
            FragmentType.Display -> view.findViewById(R.id.nav_display)
            FragmentType.Collecting -> view.findViewById(R.id.nav_collecting)
            FragmentType.List -> view.findViewById(R.id.nav_list)
            FragmentType.Publisher -> view.findViewById(R.id.nav_publisher)
            FragmentType.Tag -> view.findViewById(R.id.nav_tag)
            else -> null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
