package com.example.medialibrary.video.ui.shelf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.R
import com.example.medialibrary.video.ui.utils.SortFilterViewmodel
import com.example.medialibrary.databinding.DialogSortContentBinding
import com.example.medialibrary.databinding.FragmentVideoShelfBinding
import com.example.medialibrary.shared.FilterSummaryHelper
import com.example.medialibrary.shared.FragmentType
import com.example.medialibrary.shared.ShelfItemDecoration
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import controllers.VideoController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import models.book.BookSetup
import models.video.VideoFilter
import repository.database.MediaLibraryDbHelper

class VideoShelfFragment : BaseFragment<FragmentVideoShelfBinding, VideoShelfViewModel>(
    FragmentVideoShelfBinding::inflate
) {
    private lateinit var adapter: VideoShelfAdapter

    private var currentFilter: VideoFilter? = null
    private var videoController: VideoController = VideoController()
    private var setup: BookSetup = BookSetup()
    private lateinit var sortFilterViewModel: SortFilterViewmodel

    override fun onCreateView(inflater: LayoutInflater,
                               container: ViewGroup?,
                               savedInstanceState: Bundle?
    ): View {
        //super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[VideoShelfViewModel::class.java]
        sortFilterViewModel = ViewModelProvider(requireActivity())[SortFilterViewmodel::class.java]
        setFragmentType(FragmentType.List)

        val root = super.onCreateView(inflater, container, savedInstanceState)

        val recyclerView = binding.rvVideoshelf

        val flexboxLayoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }

        adapter = VideoShelfAdapter { book ->
            // Handle book click (e.g., open book details)
        }

        // Calculate how many books fit comfortably across the screen (~48dp per spine)
        val displayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val spanCount = (screenWidthDp / 48).toInt().coerceAtLeast(4)

        recyclerView.layoutManager = flexboxLayoutManager
        recyclerView.addItemDecoration(ShelfItemDecoration(shelfHeightPx = 28))
        recyclerView.adapter = adapter

        setDialogSort(DialogSortContentBinding.inflate(layoutInflater))

        val dbHelper = MediaLibraryDbHelper(requireContext())
        videoController = VideoController(dbHelper)

        observeSortFilterViewModel()


        // Load and sort books by sortOrder
        adapter.submitList(viewModel.items.value)
        return root
    }

    private fun observeSortFilterViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            sortFilterViewModel.currentVideoFilter.collectLatest { filter ->
                currentFilter = filter ?: VideoFilter()
                loadData(true)
            }
        }

        viewModel.items.observe(viewLifecycleOwner) { books ->
            val sortedBooks = books.orEmpty()// Replace with your sort property
            adapter.submitList(sortedBooks) {
                // Force Flexbox to recalculate row wrapping after DiffUtil commits
                binding.rvVideoshelf.requestLayout()
            }
        }
    }

    override fun onRefreshData() {
        loadData()
    }

    private fun loadData(reload: Boolean = false) {
        if (currentFilter == null) {
            currentFilter = sortFilterViewModel.getOrCreateVideoFilter()
        }
        val items = videoController.GetBookSpines(currentFilter)


        viewModel.setItems(items ?: emptyList())

        FilterSummaryHelper.bindFilterSummary(
            binding.root.findViewById(R.id.active_filter_card),
            currentFilter,
            setup,
            FragmentType.List
        ) {
            val emptyFilter = VideoFilter()
            currentFilter = emptyFilter
            sortFilterViewModel.updateVideoFilter(emptyFilter)
            //binding.searchView.setQuery("", false)
        }
    }
}