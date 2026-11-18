package com.example.medialibrary.music.ui.shelf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.R
import com.example.medialibrary.music.ui.utils.SortFilterViewmodel
import com.example.medialibrary.databinding.DialogSortContentBinding
import com.example.medialibrary.databinding.FragmentMusicShelfBinding
import com.example.medialibrary.shared.FilterSummaryHelper
import com.example.medialibrary.shared.FragmentType
import com.example.medialibrary.shared.ShelfItemDecoration
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import controllers.MusicController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import models.music.MusicFilter
import models.music.MusicSetup
import repository.database.MediaLibraryDbHelper

class MusicShelfFragment : BaseFragment<FragmentMusicShelfBinding, MusicShelfViewModel>(
    FragmentMusicShelfBinding::inflate
) {
    private lateinit var adapter: MusicShelfAdapter

    private var currentFilter: MusicFilter? = null
    private var musicController: MusicController = MusicController()
    private var setup: MusicSetup = MusicSetup()
    private lateinit var sortFilterViewModel: SortFilterViewmodel

    override fun onCreateView(inflater: LayoutInflater,
                               container: ViewGroup?,
                               savedInstanceState: Bundle?
    ): View {
        //super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[MusicShelfViewModel::class.java]
        sortFilterViewModel = ViewModelProvider(requireActivity())[SortFilterViewmodel::class.java]
        setFragmentType(FragmentType.List)

        val root = super.onCreateView(inflater, container, savedInstanceState)

        val recyclerView = binding.rvMusicshelf

        val flexboxLayoutManager = FlexboxLayoutManager(requireContext()).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
        }

        adapter = MusicShelfAdapter { book ->
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
        musicController = MusicController(dbHelper)

        observeSortFilterViewModel()


        // Load and sort books by sortOrder
        adapter.submitList(viewModel.items.value)
        return root
    }

    private fun observeSortFilterViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            sortFilterViewModel.currentMusicFilter.collectLatest { filter ->
                currentFilter = filter ?: MusicFilter()
                loadData()
            }
        }

        viewModel.items.observe(viewLifecycleOwner) { books ->
            val sortedBooks = books.orEmpty()// Replace with your sort property
            adapter.submitList(sortedBooks) {
                // Force Flexbox to recalculate row wrapping after DiffUtil commits
                //binding.rvMusicshelf.requestLayout()
            }
        }
    }

    override fun onRefreshData() {
        loadData()
    }

    private fun loadData() {
        if (currentFilter == null) {
            currentFilter = sortFilterViewModel.getOrCreateMusicFilter()
        }
        val items = musicController.GetMusicSpines(currentFilter)


        viewModel.setItems(items ?: emptyList())

        FilterSummaryHelper.bindFilterSummary(
            binding.root.findViewById(R.id.active_filter_card),
            currentFilter,
            setup,
            FragmentType.List
        ) {
            val emptyFilter = MusicFilter()
            currentFilter = emptyFilter
            sortFilterViewModel.updateMusicFilter(emptyFilter)
            //binding.searchView.setQuery("", false)
        }
    }
}