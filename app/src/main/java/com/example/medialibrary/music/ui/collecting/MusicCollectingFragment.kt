package com.example.medialibrary.music.ui.collecting

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
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.BaseTransformAdapter
import com.example.medialibrary.R
import com.example.medialibrary.databinding.MusicBottomSheetBinding
import com.example.medialibrary.databinding.MusicFragmentCollectingBinding
import com.example.medialibrary.music.ui.utils.SharedUtils
import com.example.medialibrary.music.ui.utils.SortFilterViewmodel
import com.example.medialibrary.utils.FilterSummaryHelper
import com.example.medialibrary.utils.FragmentType
import com.example.medialibrary.utils.SharedRefreshViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import controllers.MusicController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import models.music.MusicFilter
import models.music.MusicSetup
import repository.database.MediaLibraryDbHelper

class MusicCollectingFragment : BaseFragment<MusicFragmentCollectingBinding, MusicCollectingViewModel>(
    MusicFragmentCollectingBinding::inflate
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
        viewModel = ViewModelProvider(this)[MusicCollectingViewModel::class.java]
        sortFilterViewModel = ViewModelProvider(requireActivity())[SortFilterViewmodel::class.java]
        setFragmentType(FragmentType.Collecting)

        val root: View = super.onCreateView(inflater, container, savedInstanceState)

        val recyclerView = binding.recyclerviewCds
        val adapter = BaseTransformAdapter()
        recyclerView.adapter = adapter

        val dbHelper = MediaLibraryDbHelper(requireContext())
        musicController = MusicController(dbHelper)

        observeSortFilterViewModel()
        loadData()

        setupBindings(recyclerView, adapter)

        activity?.let { act ->
            val refreshViewModel = ViewModelProvider(act)[SharedRefreshViewModel::class.java]
            var lastVersion = refreshViewModel.refreshVersion
            viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    if (refreshViewModel.refreshVersion != lastVersion) {
                        lastVersion = refreshViewModel.refreshVersion
                        loadData()
                    }
                }
            })
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

    private fun setupBindings(recyclerView: RecyclerView, adapter: BaseTransformAdapter)
    {
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

        binding.filterBtn.setOnClickListener {
            showFilterSheet(setup, currentFilter)
        }

        binding.sortBtn.setOnClickListener {
            val filter = sortFilterViewModel.getOrCreateMusicFilter()
            val sortModel = sortFilterViewModel.getOrCreateSortModel()
            com.example.medialibrary.book.ui.utils.SharedUtils.showSortDialog(requireContext(), filter, isMain = false, sortModel = sortModel) { updatedFilter ->
                sortFilterViewModel.updateMusicFilter(updatedFilter as MusicFilter)
                loadData()
            }
        }

        binding.copyListBtn.setOnClickListener {
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
    }

    override fun onRefreshData() {
        loadData()
    }

    private fun loadData() {
        if (currentFilter == null) {
            currentFilter = sortFilterViewModel.getOrCreateMusicFilter()
        }
        val effectiveFilter = MusicFilter().apply {
            Search = currentFilter?.Search
            Tag = currentFilter?.Tag ?: 0
            Genre = currentFilter?.Genre ?: 0
            MediaType = currentFilter?.MediaType
            Collecting = true
            Ongoing = currentFilter?.Ongoing
            AnyOwned = currentFilter?.AnyOwned
            Collected = currentFilter?.Collected
            IncludedTags = currentFilter?.IncludedTags ?: ArrayList()
            ExcludedTags = currentFilter?.ExcludedTags ?: ArrayList()
            IncludedGenres = currentFilter?.IncludedGenres ?: ArrayList()
            ExcludedGenres = currentFilter?.ExcludedGenres ?: ArrayList()
            SortAlphabetical = currentFilter?.SortAlphabetical
            SortPriority = currentFilter?.SortPriority
            SortItemMediaType = currentFilter?.SortItemMediaType
        }
        val items = musicController.GetListOfCds(effectiveFilter)
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
        val sheetBinding = MusicBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        val f = filter ?: MusicFilter()
        SharedUtils.filterSheetSetup(f, setup, sheetBinding)
        sheetBinding.collecting.root.visibility = View.GONE

        sheetBinding.buttonSheetFitlerMusic.setOnClickListener {
            f.Collected = sheetBinding.collected.triStateButton.tag as Boolean?

            currentFilter = f
            sortFilterViewModel.updateMusicFilter(f)
            loadData()
            dialog.dismiss()
        }

        sheetBinding.buttonSheetClearBook.setOnClickListener {
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
