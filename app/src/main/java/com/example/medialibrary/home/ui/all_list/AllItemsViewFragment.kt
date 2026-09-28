package com.example.medialibrary.home.ui.all_list

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.BaseTransformAdapter
import com.example.medialibrary.R
import com.example.medialibrary.backend.controllers.MainController
import com.example.medialibrary.backend.models.shared.*
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.book.ui.utils.SharedUtils
import com.example.medialibrary.book.ui.utils.SortFilterViewmodel
import com.example.medialibrary.databinding.MainBottomSheetBinding
import com.example.medialibrary.databinding.MainFragmentListBinding
import com.example.medialibrary.utils.FilterOption
import com.example.medialibrary.utils.FilterSummaryHelper
import com.example.medialibrary.utils.FragmentType
import com.example.medialibrary.utils.MultiSelectFilterHelper
import com.example.medialibrary.utils.TriStateCheckBoxHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

        // Setup sort options in bottom sheet
        val sortModel = sortFilterViewModel.getOrCreateSortModel()
        val sortOptions = listOf("Alphabetical", "Priority", "Item Media Type")
        val sortAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sortOptions)
        sheetBinding.dropdownSheetSortBook.setAdapter(sortAdapter)
        val currentSortText = when {
            sortModel.Priority == true -> "Priority"
            sortModel.ItemMediaType == true -> "Item Media Type"
            else -> "Alphabetical"
        }
        sheetBinding.dropdownSheetSortBook.setText(currentSortText, false)
        sheetBinding.dropdownSheetSortBook.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> {
                    sortModel.Alphabetical = true
                    sortModel.Priority = false
                    sortModel.ItemMediaType = false
                }
                1 -> {
                    sortModel.Alphabetical = false
                    sortModel.Priority = true
                    sortModel.ItemMediaType = false
                }
                2 -> {
                    sortModel.Alphabetical = false
                    sortModel.Priority = false
                    sortModel.ItemMediaType = true
                }
            }
            sortFilterViewModel.updateSortModel(sortModel)
        }

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