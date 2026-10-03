package com.example.medialibrary.video.ui.list

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
import com.example.medialibrary.databinding.VideoBottomSheetBinding
import com.example.medialibrary.databinding.VideoFragmentListBinding
import com.example.medialibrary.utils.FilterSummaryHelper
import com.example.medialibrary.utils.FragmentType
import com.example.medialibrary.video.ui.utils.SharedUtils
import com.example.medialibrary.video.ui.utils.SortFilterViewmodel
import com.google.android.material.bottomsheet.BottomSheetDialog
import controllers.VideoController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import models.video.VideoFilter
import models.video.VideoSetup
import repository.database.MediaLibraryDbHelper

class VideoListFragment : BaseFragment<VideoFragmentListBinding, VideoListModel>(
    VideoFragmentListBinding::inflate
) {

    private var currentFilter: VideoFilter? = null
    private var videoController: VideoController = VideoController()
    private var setup: VideoSetup? = null

    private lateinit var sortFilterViewModel: SortFilterViewmodel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[VideoListModel::class.java]
        setFragmentType(FragmentType.List)

        val root = super.onCreateView(inflater, container, savedInstanceState)
        sortFilterViewModel = ViewModelProvider(requireActivity())[SortFilterViewmodel::class.java]

        val recyclerView = binding.recyclerviewTransform
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
                loadData(true)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filter = sortFilterViewModel.getOrCreateVideoFilter()
                filter.Search = newText
                sortFilterViewModel.updateVideoFilter(filter)
                loadData(true)
                return true
            }
        })

        binding.buttonFilter.setOnClickListener {
            setup?.let { s -> showFilterSheet(s, currentFilter ?: VideoFilter()) }
        }

        binding.videoItemList?.setOnClickListener {
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
                loadData(true)
            }
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

    private fun loadData(reload: Boolean = false) {
        currentFilter = sortFilterViewModel.getOrCreateVideoFilter()
        val items = videoController.GetListOfVideos(currentFilter) ?: emptyList()
        if(!reload || setup == null) {
            setup = videoController.GetVideoSetup()
        }

        viewModel.setItems(items)

        FilterSummaryHelper.bindFilterSummary(
            binding.root.findViewById(R.id.card_active_filter),
            currentFilter,
            setup,
            FragmentType.List
        ) {
            val emptyFilter = VideoFilter()
            currentFilter = emptyFilter
            sortFilterViewModel.updateVideoFilter(emptyFilter)
            binding.searchView.setQuery("", false)
            loadData(true)
        }
    }

    private fun showFilterSheet(setup: VideoSetup, filter: VideoFilter) {
        val dialog = BottomSheetDialog(requireContext())
        var sheetBinding = VideoBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        val f = filter
        sheetBinding = SharedUtils.filterSheetSetup(f, setup, sheetBinding)
        sheetBinding.collecting.root.visibility = View.VISIBLE

        sheetBinding.filterBtn.setOnClickListener {
            f.Ongoing = sheetBinding.ongoing.triStateButton.tag as Boolean?
            f.Collecting = sheetBinding.collecting.triStateButton.tag as Boolean?
            f.AnyOwned = sheetBinding.anyItemsOwned.triStateButton.tag as Boolean?
            f.Collected = sheetBinding.collected.triStateButton.tag as Boolean?
            f.Watched = sheetBinding.watched.triStateButton.tag as Boolean?
            f.Watching = sheetBinding.watching.triStateButton.tag as Boolean?

            currentFilter = f
            sortFilterViewModel.updateVideoFilter(f)
            loadData(true)
            dialog.dismiss()
        }

        sheetBinding.clearActiveFilter.setOnClickListener {
            val emptyFilter = VideoFilter()
            currentFilter = emptyFilter
            sortFilterViewModel.updateVideoFilter(emptyFilter)
            binding.searchView.setQuery("", false)
            loadData(true)
            dialog.dismiss()
        }

        dialog.show()
    }
}
