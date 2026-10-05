package com.example.medialibrary.home.ui.collecting

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.BaseTransformAdapter
import com.example.medialibrary.R
import com.example.medialibrary.databinding.MainBottomSheetBinding
import com.example.medialibrary.databinding.MainFragmentCollectingBinding
import com.example.medialibrary.home.ui.utils.SharedUtils
import com.example.medialibrary.home.ui.utils.SharedUtils.Companion.clipboardHelper
import com.example.medialibrary.home.ui.utils.SortFilterViewmodel
import com.example.medialibrary.utils.FilterSummaryHelper
import com.example.medialibrary.utils.FragmentType
import com.google.android.material.bottomsheet.BottomSheetDialog
import controllers.MainController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import models.shared.Filter
import models.shared.MainSetup
import repository.database.MediaLibraryDbHelper

class HomeCollectingFragment : BaseFragment<MainFragmentCollectingBinding, HomeCollectingViewModel>(
    MainFragmentCollectingBinding::inflate
) {

    private var currentFilter = Filter()
    private lateinit var controller: MainController
    private var setup: MainSetup = MainSetup()
    private lateinit var sortFilterViewModel: SortFilterViewmodel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[HomeCollectingViewModel::class.java]
        sortFilterViewModel = ViewModelProvider(requireActivity())[SortFilterViewmodel::class.java]
        setFragmentType(FragmentType.Collecting)

        val root = super.onCreateView(inflater, container, savedInstanceState)

        val recyclerView = binding.recyclerviewMainCollecting
        val adapter = BaseTransformAdapter()
        recyclerView.adapter = adapter

        val dbHelper = MediaLibraryDbHelper(requireContext())
        controller = MainController(dbHelper)

        observeSortFilterViewModel()

        setupEmptyStateObserver(
            viewModel.items,
            recyclerView,
            ContextCompat.getColor(requireContext(), R.color.primary),
            binding.emptyStateContainer,
            adapter
        )
        setupBindings()

        return root
    }

    private fun setupBindings() {
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
                sortFilterViewModel.updateMainFilter(filter)
                return true
            }
        })

        binding.buttonFilter.setOnClickListener {
            showFilterSheet(setup, currentFilter)
        }

        binding.buttonSort.setOnClickListener {
            val filter = sortFilterViewModel.getOrCreateMainFilter()
            val sortModel = sortFilterViewModel.getOrCreateSortModel()
            SharedUtils.showSortDialog(requireContext(), filter, isMain = false, sortModel = sortModel) { updatedFilter ->
                sortFilterViewModel.updateMainFilter(updatedFilter)
                loadData()
            }
        }

        binding.allItemList.setOnClickListener {
            val mediaItems = viewModel.items.value
            val list = clipboardHelper(mediaItems ?: emptyList(), "Media Collection List")

            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Media Collection List", list)
            clipboard.setPrimaryClip(clipData)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
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
        setup = controller.GetSetup()
        val effectiveFilter = Filter().apply {
            Search = currentFilter.Search
            Tag = currentFilter.Tag
            Genre = currentFilter.Genre
            MediaType = currentFilter.MediaType
            Collecting = true
            Ongoing = currentFilter.Ongoing
            AnyOwned = currentFilter.AnyOwned
            Collected = currentFilter.Collected
            SortAlphabetical = currentFilter.SortAlphabetical
            SortPriority = currentFilter.SortPriority
            SortItemMediaType = currentFilter.SortItemMediaType
            IncludedTags = currentFilter.IncludedTags
            ExcludedTags = currentFilter.ExcludedTags
            IncludedGenres = currentFilter.IncludedGenres
            ExcludedGenres = currentFilter.ExcludedGenres
            IncludedMediaTypes = currentFilter.IncludedMediaTypes
            ExcludedMediaTypes = currentFilter.ExcludedMediaTypes
        }

        val items = controller.GetAllItems(effectiveFilter)
        viewModel.setItems(items ?: emptyList())

        FilterSummaryHelper.bindFilterSummary(
            binding.cardActiveFilter.root,
            effectiveFilter,
            setup,
            FragmentType.Collecting
        ) {
            currentFilter = Filter()
            sortFilterViewModel.updateMainFilter(Filter())
            binding.searchView.setQuery("", false)
        }
    }

    private fun showFilterSheet(setup: MainSetup, filter: Filter?) {
        val dialog = BottomSheetDialog(requireContext())
        var sheetBinding = MainBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        val f = filter ?: sortFilterViewModel.getOrCreateMainFilter()
        f.Collecting = true

        sheetBinding = SharedUtils.filterSheetSetup(f, setup, sheetBinding)
        sheetBinding.collecting.root.visibility = View.GONE

        sheetBinding.buttonSheetFitlerBook.setOnClickListener {
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
