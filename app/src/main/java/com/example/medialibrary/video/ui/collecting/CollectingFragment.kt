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
                if (currentFilter == null) currentFilter = VideoFilter()
                currentFilter?.Search = query
                loadData()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (currentFilter == null) currentFilter = VideoFilter()
                currentFilter?.Search = newText
                loadData()
                return true
            }
        })

        if (currentFilter == null) {
            currentFilter = VideoFilter()
        }

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

    override fun onRefreshData() {
        loadData()
    }

    private fun loadData() {
        if (currentFilter == null) {
            currentFilter = sortFilterViewModel.getOrCreateVideoFilter()
        }
        currentFilter?.Collecting = true
        val items = videoController.GetListOfVideos(currentFilter)
        setup = videoController.GetVideoSetup()
        viewModel.setItems(items ?: emptyList())
    }

    private fun showFilterSheet(setup: VideoSetup, filter: VideoFilter) {
        val dialog = BottomSheetDialog(requireContext())
        var sheetBinding = VideoBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        sheetBinding = SharedUtils.filterSheetSetup(filter, setup, sheetBinding)

        sheetBinding.collecting.root.visibility = View.GONE


        sheetBinding.filterBtn.setOnClickListener {
            filter.Ongoing = sheetBinding.ongoing.triStateButton.tag as Boolean?
            filter.Collecting = true
            filter.AnyOwned = sheetBinding.anyItemsOwned.triStateButton.tag as Boolean?
            filter.Collected = sheetBinding.collected.triStateButton.tag as Boolean?
            filter.Watched = sheetBinding.watched.triStateButton.tag as Boolean?
            filter.Watching = sheetBinding.watching.triStateButton.tag as Boolean?
            loadData()
            dialog.dismiss()
        }

        sheetBinding.clearActiveFilter.setOnClickListener {
            currentFilter = VideoFilter()
            loadData()
            dialog.dismiss()
        }

        dialog.show()
    }
}
