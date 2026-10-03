package com.example.medialibrary.video.ui.collecting

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
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.BaseTransformAdapter
import com.example.medialibrary.R
import com.example.medialibrary.databinding.VideoBottomSheetBinding
import com.example.medialibrary.databinding.VideoFragmentCollectingBinding
import com.example.medialibrary.utils.FragmentType
import com.example.medialibrary.utils.SharedRefreshViewModel
import com.example.medialibrary.video.ui.utils.SharedUtils
import com.example.medialibrary.video.ui.utils.SortFilterViewmodel
import com.google.android.material.bottomsheet.BottomSheetDialog
import controllers.VideoController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import models.video.VideoFilter
import models.video.VideoSetup
import repository.database.MediaLibraryDbHelper

class CollectingFragment : BaseFragment<VideoFragmentCollectingBinding, CollectingViewModel>(
    VideoFragmentCollectingBinding::inflate
) {

    private var currentFilter: VideoFilter? = null
    private var videoController: VideoController = VideoController()
    private var setup: VideoSetup = VideoSetup()

    private lateinit var sortFilterViewModel: SortFilterViewmodel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[CollectingViewModel::class.java]
        sortFilterViewModel = ViewModelProvider(requireActivity())[SortFilterViewmodel::class.java]
        setFragmentType(FragmentType.Collecting)

        val root: View = super.onCreateView(inflater, container, savedInstanceState)

        val recyclerView = binding.recyclerviewVideos
        val adapter = BaseTransformAdapter()
        recyclerView.adapter = adapter

        val dbHelper = MediaLibraryDbHelper(requireContext())
        videoController = VideoController(dbHelper)

        observeSortFilterViewModel()
        loadData()

        setupEmptyStateObserver(
            viewModel.items,
            recyclerView,
            ContextCompat.getColor(requireContext(), R.color.section_video),
            binding.emptyStateContainer,
            adapter
        )

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val filter = sortFilterViewModel.getOrCreateVideoFilter()
                filter.Search = query
                sortFilterViewModel.updateVideoFilter(filter)
                loadData()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filter = sortFilterViewModel.getOrCreateVideoFilter()
                filter.Search = newText
                sortFilterViewModel.updateVideoFilter(filter)
                loadData()
                return true
            }
        })

        binding.filterBtn.setOnClickListener {
            showFilterSheet(setup, currentFilter ?: VideoFilter())
        }

        binding.copyListBtn.setOnClickListener {
            val videos = viewModel.items.value
            val sortedVideos = videos?.sortedBy { it.Title }
            val videoList = buildString {
                sortedVideos?.forEach { video ->
                    appendLine(video.Title)
                }
            }
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Movie & TV Show List", videoList)
            clipboard.setPrimaryClip(clipData)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        binding.sortBtn.setOnClickListener {
            val filter = sortFilterViewModel.getOrCreateVideoFilter()
            val sortModel = sortFilterViewModel.getOrCreateSortModel()
            com.example.medialibrary.book.ui.utils.SharedUtils.showSortDialog(requireContext(), filter, isMain = false, sortModel = sortModel) { updatedFilter ->
                sortFilterViewModel.updateVideoFilter(updatedFilter as VideoFilter)
                loadData()
            }
        }

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
            sortFilterViewModel.currentVideoFilter.collectLatest { filter ->
                currentFilter = filter ?: VideoFilter()
                loadData()
            }
        }
    }

    override fun onRefreshData() {
        loadData()
    }

    private fun loadData() {
        if (currentFilter == null) {
            currentFilter = sortFilterViewModel.getOrCreateVideoFilter()
        }
        val effectiveFilter = VideoFilter().apply {
            Search = currentFilter?.Search
            Tag = currentFilter?.Tag ?: 0
            Genre = currentFilter?.Genre ?: 0
            MediaType = currentFilter?.MediaType
            Collecting = true
            Ongoing = currentFilter?.Ongoing
            AnyOwned = currentFilter?.AnyOwned
            Collected = currentFilter?.Collected
            Watched = currentFilter?.Watched
            Watching = currentFilter?.Watching
            Type = currentFilter?.Type
            VideoTag = currentFilter?.VideoTag
            IncludedVideoTags = currentFilter?.IncludedVideoTags ?: ArrayList()
            ExcludedVideoTags = currentFilter?.ExcludedVideoTags ?: ArrayList()
            IncludedTypes = currentFilter?.IncludedTypes ?: ArrayList()
            ExcludedTypes = currentFilter?.ExcludedTypes ?: ArrayList()
            IncludedTags = currentFilter?.IncludedTags ?: ArrayList()
            ExcludedTags = currentFilter?.ExcludedTags ?: ArrayList()
            IncludedGenres = currentFilter?.IncludedGenres ?: ArrayList()
            ExcludedGenres = currentFilter?.ExcludedGenres ?: ArrayList()
            SortAlphabetical = currentFilter?.SortAlphabetical
            SortPriority = currentFilter?.SortPriority
            SortItemMediaType = currentFilter?.SortItemMediaType
        }
        val items = videoController.GetListOfVideos(effectiveFilter)
        setup = videoController.GetVideoSetup()
        viewModel.setItems(items ?: emptyList())
    }

    private fun showFilterSheet(setup: VideoSetup, filter: VideoFilter) {
        val dialog = BottomSheetDialog(requireContext())
        var sheetBinding = VideoBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        val f = filter
        sheetBinding = SharedUtils.filterSheetSetup(f, setup, sheetBinding)
        sheetBinding.collecting.root.visibility = View.GONE

        sheetBinding.filterBtn.setOnClickListener {
            f.Ongoing = sheetBinding.ongoing.triStateButton.tag as Boolean?
            f.AnyOwned = sheetBinding.anyItemsOwned.triStateButton.tag as Boolean?
            f.Collected = sheetBinding.collected.triStateButton.tag as Boolean?
            f.Watched = sheetBinding.watched.triStateButton.tag as Boolean?
            f.Watching = sheetBinding.watching.triStateButton.tag as Boolean?

            currentFilter = f
            sortFilterViewModel.updateVideoFilter(f)
            loadData()
            dialog.dismiss()
        }

        sheetBinding.clearActiveFilter.setOnClickListener {
            val emptyFilter = VideoFilter()
            currentFilter = emptyFilter
            sortFilterViewModel.updateVideoFilter(emptyFilter)
            binding.searchView.setQuery("", false)
            loadData()
            dialog.dismiss()
        }

        dialog.show()
    }
}
