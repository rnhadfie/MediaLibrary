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
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.BaseTransformAdapter
import com.example.medialibrary.R
import com.example.medialibrary.Utils.FilterOption
import com.example.medialibrary.Utils.FilterSummaryHelper
import com.example.medialibrary.Utils.FragmentType
import com.example.medialibrary.Utils.MultiSelectFilterHelper
import com.example.medialibrary.backend.controllers.MainController
import com.example.medialibrary.backend.models.shared.*
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.MainBottomSheetBinding
import com.example.medialibrary.databinding.MainFragmentListBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class AllItemsViewFragment : BaseFragment<MainFragmentListBinding, AllItemsViewModelViewModel>(
    MainFragmentListBinding::inflate
) {

    private var currentFilter = Filter()
    private var controller: MainController = MainController()
    private var setup: MainSetup = MainSetup()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[AllItemsViewModelViewModel::class.java]
        setFragmentType(FragmentType.List)

        val root = super.onCreateView(inflater, container, savedInstanceState)

        val recyclerView = binding.recyclerviewTransform
        val adapter = BaseTransformAdapter()
        recyclerView.adapter = adapter

        val dbHelper = MediaLibraryDbHelper(requireContext())
        controller = MainController(dbHelper)

        loadData()

        setupEmptyStateObserver(
            viewModel.items,
            recyclerView,
            binding.emptyStateContainer.root,
            adapter
        )

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentFilter.Search = query
                loadData()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentFilter.Search = newText
                if (newText.isNullOrEmpty()) {
                    loadData()
                }
                return true
            }
        })

        binding.buttonFilter.setOnClickListener {
            showFilterSheet(setup, currentFilter)
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

    override fun onRefreshData() {
        loadData()
    }

    private fun loadData() {
        val items = controller.GetAllItems(currentFilter)
        setup = controller.GetSetup()
        viewModel.setItems(items ?: emptyList())

        FilterSummaryHelper.bindFilterSummary(
            binding.root.findViewById(R.id.card_active_filter),
            currentFilter,
            setup
        ) {
            currentFilter = Filter()
            binding.searchView.setQuery("", false)
            loadData()
        }
    }

    private fun showFilterSheet(setup: MainSetup, filter: Filter?) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = MainBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        val f = filter ?: Filter()
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

        val genreOptions = setup.Genre.filter { it.key != 0 }.map { FilterOption(it.key, it.value) }
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.dropdownSheetGenreBook,
            "Genres",
            genreOptions,
            f.IncludedGenres,
            f.ExcludedGenres
        )

        sheetBinding.switchSheetCompletedBook.isChecked = f.CompletedSeries ?: false
        sheetBinding.switchSheetCollectedBook.isChecked = f.Collecting ?: false
        sheetBinding.switchSheetStartedBook.isChecked = f.AnyOwned ?: false

        sheetBinding.buttonSheetFitlerBook.setOnClickListener {
            f.CompletedSeries = sheetBinding.switchSheetCompletedBook.isChecked
            f.Collecting = sheetBinding.switchSheetCollectedBook.isChecked
            f.AnyOwned = sheetBinding.switchSheetStartedBook.isChecked

            currentFilter = f
            loadData()
            dialog.dismiss()
        }

        sheetBinding.buttonSheetClearBook.setOnClickListener {
            currentFilter = Filter()
            loadData()
            dialog.dismiss()
        }

        dialog.show()
    }
}
