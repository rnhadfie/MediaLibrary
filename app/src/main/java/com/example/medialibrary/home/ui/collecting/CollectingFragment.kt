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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.BaseTransformAdapter
import com.example.medialibrary.R
import com.example.medialibrary.backend.controllers.MainController
import com.example.medialibrary.backend.models.shared.Enums
import com.example.medialibrary.backend.models.shared.Filter
import com.example.medialibrary.backend.models.shared.MainSetup
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.book.ui.utils.SharedUtils
import com.example.medialibrary.book.ui.utils.SortFilterViewmodel
import com.example.medialibrary.databinding.MainBottomSheetBinding
import com.example.medialibrary.databinding.MainFragmentCollectingBinding
import com.example.medialibrary.utils.FilterOption
import com.example.medialibrary.utils.FilterSummaryHelper
import com.example.medialibrary.utils.FragmentType
import com.example.medialibrary.utils.MultiSelectFilterHelper
import com.example.medialibrary.utils.TriStateCheckBoxHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CollectingFragment : BaseFragment<MainFragmentCollectingBinding, CollectingViewModel>(
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
        viewModel = ViewModelProvider(this)[CollectingViewModel::class.java]
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
            binding.emptyStateContainer.root,
            adapter
        )

        binding.searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

        binding.buttonFilter?.setOnClickListener {
            showFilterSheet(setup, currentFilter)
        }

        binding.buttonSort?.setOnClickListener {
            val filter = sortFilterViewModel.getOrCreateMainFilter()
            val sortModel = sortFilterViewModel.getOrCreateSortModel()
            SharedUtils.showSortDialog(requireContext(), filter, isMain = true, sortModel = sortModel) { updatedFilter ->
                sortFilterViewModel.updateMainFilter(updatedFilter)
            }
        }

        binding.allItemList?.setOnClickListener {
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

        binding.cardActiveFilter.root.let {
            FilterSummaryHelper.bindFilterSummary(
                it,
                effectiveFilter,
                setup
            ) {
                currentFilter = Filter()
                sortFilterViewModel.updateMainFilter(Filter())
                binding.searchView.setQuery("", false)
            }
        }
    }

    private fun showFilterSheet(setup: MainSetup, filter: Filter?) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = MainBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        val f = filter ?: sortFilterViewModel.getOrCreateMainFilter()
        val tags = setup.Tag

        val typeOptions = setup.MediaType.filter { it.key != 0 }.map { FilterOption(Enums.MediaType.entries[it.key], it.value) }
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.dropdownSheetTypeBook,
            "Media Types",
            typeOptions,
            f.IncludedMediaTypes,
            f.ExcludedMediaTypes
        )

        val tagOptions = tags.map { FilterOption(it.Id, it.Name) }
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.dropdownSheetTagBook,
            "Tags",
            tagOptions,
            f.IncludedTags,
            f.ExcludedTags
        )

        val genreOptions = setup.Genre.filter { it.genreId != 0 }.map { FilterOption(it.genreId, it.genreName) }
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.dropdownSheetGenreBook,
            "Genres",
            genreOptions,
            f.IncludedGenres,
            f.ExcludedGenres
        )

        TriStateCheckBoxHelper.setupTriStateCheckBox(
            sheetBinding.standaloneOrSeriesComplete.root,
            R.string.Ongoing,
            f.Ongoing
        ) { f.Ongoing = it }

        TriStateCheckBoxHelper.setupTriStateCheckBox(
            sheetBinding.collected.root,
            R.string.completely_collected,
            f.Collected
        ) { f.Collected = it }

        TriStateCheckBoxHelper.setupTriStateCheckBox(
            sheetBinding.collecting.root,
            R.string.collecting,
            f.Collecting
        ) { f.Collecting = it }

        TriStateCheckBoxHelper.setupTriStateCheckBox(
            sheetBinding.anyItemsOwned.root,
            R.string.started_collecting,
            f.AnyOwned
        ) { f.AnyOwned = it }

        sheetBinding.buttonSheetFitlerBook.setOnClickListener {
            sortFilterViewModel.updateMainFilter(f)
            dialog.dismiss()
        }

        sheetBinding.buttonSheetClearBook.setOnClickListener {
            sortFilterViewModel.updateMainFilter(Filter())
            dialog.dismiss()
        }

        dialog.show()
    }
}