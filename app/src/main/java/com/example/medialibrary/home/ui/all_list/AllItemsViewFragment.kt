package com.example.medialibrary.home.ui.all_list

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.BaseTransformAdapter
import com.example.medialibrary.R
import com.example.medialibrary.book.ui.utils.SharedUtils
import com.example.medialibrary.databinding.MainBottomSheetBinding
import com.example.medialibrary.databinding.MainFragmentListBinding
import com.example.medialibrary.home.ui.utils.SharedUtils.Companion.filterSheetSetup
import com.example.medialibrary.home.ui.utils.SortFilterViewmodel
import com.example.medialibrary.utils.FilterSummaryHelper
import com.example.medialibrary.utils.FragmentType
import com.google.android.material.bottomsheet.BottomSheetDialog
import controllers.MainController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import models.book.BookFilter
import models.shared.Enums
import models.shared.Filter
import models.shared.MainSetup
import repository.database.MediaLibraryDbHelper

class AllItemsViewFragment : BaseFragment<MainFragmentListBinding, AllItemsViewModelViewModel>(
    MainFragmentListBinding::inflate
) {

    private var currentFilter = Filter()
    private var controller: MainController = MainController()
    private var setup: MainSetup = MainSetup()
    private lateinit var sortFilterViewModel: SortFilterViewmodel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[AllItemsViewModelViewModel::class.java]
        sortFilterViewModel = ViewModelProvider(requireActivity())[SortFilterViewmodel::class.java]
        setFragmentType(FragmentType.List)

        val root = super.onCreateView(inflater, container, savedInstanceState)

        val recyclerView = binding.recyclerviewTransform
        val adapter = BaseTransformAdapter()
        recyclerView.adapter = adapter

        val dbHelper = MediaLibraryDbHelper(requireContext())
        controller = MainController(dbHelper)

        observeSortFilterViewModel()

        setupEmptyStateObserver(
            viewModel.items,
            recyclerView,
            binding.emptyStateContainer.root,
            adapter
        )

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val filter = sortFilterViewModel.getOrCreateMainFilter()
                filter.Search = query
                sortFilterViewModel.updateMainFilter(filter)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filter = sortFilterViewModel.getOrCreateMainFilter()
                filter.Search = newText
                if (newText.isNullOrEmpty()) {
                    sortFilterViewModel.updateMainFilter(filter)
                }
                return true
            }
        })

        binding.buttonFilter.setOnClickListener {
            showFilterSheet(setup, currentFilter)
        }

        binding.buttonSort.setOnClickListener {
            val filter = sortFilterViewModel.getOrCreateItemFilter()
            val sortModel = sortFilterViewModel.getOrCreateSortModel()
            SharedUtils.showSortDialog(requireContext(), filter, isMain = false, sortModel = sortModel) { updatedFilter ->
                sortFilterViewModel.updateMainFilter(updatedFilter as BookFilter)
                loadData()
            }
        }

        binding.allItemList.setOnClickListener {
            val mediaItems = viewModel.items.value
            val sortedBooks = mediaItems?.filter { it.MediaType == Enums.MediaType.Book }?.sortedBy { it.Title }
            val sortedVideos = mediaItems?.filter { it.MediaType == Enums.MediaType.Video }?.sortedBy { it.Title }
            val sortedMusics = mediaItems?.filter { it.MediaType == Enums.MediaType.Music }?.sortedBy { it.Title }
            val sortedOthers = mediaItems?.filter { it.MediaType == Enums.MediaType.Other }?.sortedBy { it.Title }
            val list = buildString {
                appendLine("Books:")
                sortedBooks?.forEach { book ->
                    appendLine(book.Title)
                }
                appendLine()
                appendLine("Videos:")
                sortedVideos?.forEach { video ->
                    appendLine(video.Title)
                }
                appendLine()
                appendLine("Music Collection:")
                sortedMusics?.forEach { music ->
                    appendLine(music.Title)
                }
                appendLine()
                appendLine("Other Collection:")
                sortedOthers?.forEach { other ->
                    appendLine(other.Title)
                }
            }

            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Media Collection List", list)
            clipboard.setPrimaryClip(clipData)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        return root
    }

    private fun observeSortFilterViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            sortFilterViewModel.currentMainFilter.collectLatest { filter ->
                currentFilter = filter ?: Filter()
                loadData()
            }
        }
    }

    override fun onRefreshData() {
        loadData()
    }

    private fun loadData() {
        val items = controller.GetAllItems(currentFilter)
        if (setup.Tag.isEmpty() && setup.Genre.isEmpty()) {
            setup = controller.GetSetup()
        }
        viewModel.setItems(items ?: emptyList())

        FilterSummaryHelper.bindFilterSummary(
            binding.root.findViewById(R.id.card_active_filter),
            currentFilter,
            setup
        ) {
            val emptyFilter = Filter()
            currentFilter = emptyFilter
            sortFilterViewModel.updateMainFilter(emptyFilter)
            binding.searchView.setQuery("", false)
        }
    }

    private fun showFilterSheet(setup: MainSetup, filter: Filter?) {
        val dialog = BottomSheetDialog(requireContext())
        var sheetBinding = MainBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        val f = filter ?: sortFilterViewModel.getOrCreateMainFilter()
        sheetBinding = filterSheetSetup(f, setup, sheetBinding)

        sheetBinding.buttonSheetFitlerBook.setOnClickListener {
            f.AnyOwned = sheetBinding.anyItemsOwned.triStateButton.tag as Boolean?
            f.Ongoing = sheetBinding.standaloneOrSeriesComplete.triStateButton.tag as Boolean?
            f.Collecting = true
            f.Collected = sheetBinding.collected.triStateButton.tag as Boolean?
            sortFilterViewModel.updateMainFilter(f)
            loadData()
            dialog.dismiss()
        }

        sheetBinding.buttonSheetClearBook.setOnClickListener {
            sortFilterViewModel.updateMainFilter(Filter())
            dialog.dismiss()
        }

        dialog.show()
    }
}