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
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.BaseTransformAdapter
import com.example.medialibrary.R
import com.example.medialibrary.databinding.MusicBottomSheetBinding
import com.example.medialibrary.databinding.MusicFragmentListBinding
import com.example.medialibrary.music.ui.utils.SharedUtils
import com.example.medialibrary.music.ui.utils.SortFilterViewmodel
import com.example.medialibrary.utils.FilterSummaryHelper
import com.example.medialibrary.utils.FragmentType
import com.google.android.material.bottomsheet.BottomSheetDialog
import controllers.MusicController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import models.music.MusicFilter
import models.music.MusicSetup
import repository.database.MediaLibraryDbHelper

class MusicListFragment : BaseFragment<MusicFragmentListBinding, MusicListViewModel>(
    MusicFragmentListBinding::inflate
) {

    private var currentFilter: MusicFilter? = null
    private var musicController: MusicController = MusicController()
    private var setup: MusicSetup = MusicSetup()

    private lateinit var sortFilterViewModel: SortFilterViewmodel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[MusicListViewModel::class.java]
        sortFilterViewModel = ViewModelProvider(requireActivity())[SortFilterViewmodel::class.java]
        setFragmentType(FragmentType.List)

        val root = super.onCreateView(inflater, container, savedInstanceState)

        val recyclerView = binding.recyclerviewCds
        val adapter = BaseTransformAdapter()
        recyclerView.adapter = adapter

        val dbHelper = MediaLibraryDbHelper(requireContext())
        musicController = MusicController(dbHelper)

        observeSortFilterViewModel()
        loadData()

        setupEmptyStateObserver(
            viewModel.items,
            recyclerView,
            ContextCompat.getColor(requireContext(), R.color.section_music),
            binding.emptyStateContainer,
            adapter
        )

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val filter = sortFilterViewModel.getOrCreateMusicFilter()
                filter.Search = query
                sortFilterViewModel.updateMusicFilter(filter)
                loadData()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filter = sortFilterViewModel.getOrCreateMusicFilter()
                filter.Search = newText
                sortFilterViewModel.updateMusicFilter(filter)
                loadData()
                return true
            }
        })

        binding.buttonFilter.setOnClickListener {
            showFilterSheet(setup, currentFilter)
        }

        binding.buttonSort.setOnClickListener {
            val filter = sortFilterViewModel.getOrCreateMusicFilter()
            val sortModel = sortFilterViewModel.getOrCreateSortModel()
            com.example.medialibrary.book.ui.utils.SharedUtils.showSortDialog(requireContext(), filter, isMain = false, sortModel = sortModel) { updatedFilter ->
                sortFilterViewModel.updateMusicFilter(updatedFilter as MusicFilter)
                loadData()
            }
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

    private fun observeSortFilterViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            sortFilterViewModel.currentMusicFilter.collectLatest { filter ->
                currentFilter = filter ?: MusicFilter()
                loadData()
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
        val items = musicController.GetListOfCds(currentFilter)
        setup = musicController.GetMusicSetup()
        viewModel.setItems(items ?: emptyList())

        FilterSummaryHelper.bindFilterSummary(
            binding.root.findViewById(R.id.card_active_filter),
            currentFilter,
            setup,
            FragmentType.List
        ) {
            val emptyFilter = MusicFilter()
            currentFilter = emptyFilter
            sortFilterViewModel.updateMusicFilter(emptyFilter)
            binding.searchView.setQuery("", false)
            loadData()
        }
    }

    private fun showFilterSheet(setup: MusicSetup, filter: MusicFilter?) {
        val dialog = BottomSheetDialog(requireContext())
        val sb = MusicBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sb.root)

        val f = filter ?: sortFilterViewModel.getOrCreateMusicFilter()
        SharedUtils.filterSheetSetup(f, setup, sb)
        sb.collecting.root.visibility = View.VISIBLE

        sb.buttonSheetFitlerMusic.setOnClickListener {
            f.Collecting = sb.collecting.triStateButton.tag as Boolean?
            f.Collected = sb.collected.triStateButton.tag as Boolean?

            currentFilter = f
            sortFilterViewModel.updateMusicFilter(f)
            loadData()
            dialog.dismiss()
        }

        sb.buttonSheetClearBook.setOnClickListener {
            val emptyFilter = MusicFilter()
            currentFilter = emptyFilter
            sortFilterViewModel.updateMusicFilter(emptyFilter)
            binding.searchView.setQuery("", false)
            loadData()
            dialog.dismiss()
        }

        dialog.show()
    }
}
