package com.example.medialibrary.music.ui.list

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
import com.example.medialibrary.backend.controllers.MusicController
import com.example.medialibrary.backend.models.music.MusicFilter
import com.example.medialibrary.backend.models.music.MusicSetup
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.MusicBottomSheetBinding
import com.example.medialibrary.databinding.MusicFragmentListBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class MusicListFragment : BaseFragment<MusicFragmentListBinding, MusicListViewModel>(
    MusicFragmentListBinding::inflate
) {

    private var currentFilter: MusicFilter? = null
    private var musicController: MusicController = MusicController()
    private var setup: MusicSetup = MusicSetup()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[MusicListViewModel::class.java]
        setFragmentType(FragmentType.List)

        val root = super.onCreateView(inflater, container, savedInstanceState)

        val recyclerView = binding.recyclerviewCds
        val adapter = BaseTransformAdapter()
        recyclerView.adapter = adapter

        val dbHelper = MediaLibraryDbHelper(requireContext())
        musicController = MusicController(dbHelper)

        loadData()

        setupEmptyStateObserver(
            viewModel.items,
            recyclerView,
            binding.emptyStateContainer.root,
            adapter
        )

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (currentFilter == null) currentFilter = MusicFilter()
                currentFilter?.Search = query
                loadData()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (currentFilter == null) currentFilter = MusicFilter()
                currentFilter?.Search = newText
                if (newText.isNullOrEmpty()) {
                    loadData()
                }
                return true
            }
        })

        binding.buttonFilter.setOnClickListener {
            showFilterSheet(setup, currentFilter)
        }

        binding.musicItemList.setOnClickListener {
            val cds = viewModel.items.value
            val sortedCds = cds?.sortedBy { it.Title }
            val cdList = buildString {
                sortedCds?.forEach { book ->
                    appendLine(book.Title)
                }
            }
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Cd List", cdList)
            clipboard.setPrimaryClip(clipData)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        return root
    }

    override fun onRefreshData() {
        loadData()
    }

    private fun loadData() {
        if (currentFilter == null) {
            currentFilter = MusicFilter()
        }
        val items = musicController.GetListOfBooks(currentFilter)
        setup = musicController.GetMusicSetup()
        viewModel.setItems(items ?: emptyList())

        FilterSummaryHelper.bindFilterSummary(
            binding.root.findViewById(R.id.card_active_filter),
            currentFilter,
            setup
        ) {
            currentFilter = MusicFilter()
            binding.searchView.setQuery("", false)
            loadData()
        }
    }

    private fun showFilterSheet(setup: MusicSetup, filter: MusicFilter?) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = MusicBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        val f = filter ?: MusicFilter()
        val tags = setup.Tags

        val tagOptions = tags.map { FilterOption(it.Id, it.Name) }
        sheetBinding.tagAutocomplete.autoCompleteLabel.setText(R.string.tag)
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.tagAutocomplete.autocomplete,
            "Tags",
            tagOptions,
            f.IncludedTags,
            f.ExcludedTags
        )

        val genreOptions = setup.MusicGenre.filter { it.key != 0 }.map { FilterOption(it.key, it.value) }
        sheetBinding.musicGenreAutocomplete.autoCompleteLabel.setText(R.string.tag)
        MultiSelectFilterHelper.setupTriStateDropdown(
            sheetBinding.musicGenreAutocomplete.autocomplete,
            "Music Genres",
            genreOptions,
            f.IncludedMusicGenres,
            f.ExcludedMusicGenres
        )

        sheetBinding.switchSheetCollectedBook.isChecked = f.Collecting ?: false
        sheetBinding.switchSheetStartedBook.isChecked = f.AnyOwned ?: false

        sheetBinding.buttonSheetFitlerMusic.setOnClickListener {
            f.Collecting = sheetBinding.switchSheetCollectedBook.isChecked
            f.AnyOwned = sheetBinding.switchSheetStartedBook.isChecked

            currentFilter = f
            loadData()
            dialog.dismiss()
        }

        sheetBinding.buttonSheetClearBook.setOnClickListener {
            currentFilter = MusicFilter()
            loadData()
            dialog.dismiss()
        }

        dialog.show()
    }
}
