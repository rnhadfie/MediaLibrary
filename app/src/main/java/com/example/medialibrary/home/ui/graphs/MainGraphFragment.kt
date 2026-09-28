package com.example.medialibrary.home.ui.graphs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.utils.FragmentType
import com.example.medialibrary.utils.SharedRefreshViewModel
import com.example.medialibrary.backend.controllers.MainController
import com.example.medialibrary.backend.models.shared.DisplayMediaItem
import com.example.medialibrary.backend.models.shared.Enums
import com.example.medialibrary.backend.models.shared.Filter
import com.example.medialibrary.backend.models.shared.MediaItem
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.book.BookActivity
import com.example.medialibrary.databinding.MainFragmentDisplayBinding
import com.example.medialibrary.music.MusicActivity
import com.example.medialibrary.other.OtherActivity
import com.example.medialibrary.video.VideoActivity
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate

import androidx.lifecycle.lifecycleScope
import com.example.medialibrary.book.ui.utils.SortFilterViewmodel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainGraphFragment : BaseFragment<MainFragmentDisplayBinding, MainGraphViewModel>(
    MainFragmentDisplayBinding::inflate
) {

    private var currentFilter = Filter()
    private lateinit var controller: MainController
    private lateinit var sortFilterViewModel: SortFilterViewmodel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[MainGraphViewModel::class.java]
        sortFilterViewModel = ViewModelProvider(requireActivity())[SortFilterViewmodel::class.java]
        setFragmentType(FragmentType.Display)

        val root = super.onCreateView(inflater, container, savedInstanceState)

        val dbHelper = MediaLibraryDbHelper(requireContext())
        controller = MainController(dbHelper)

        observeSortFilterViewModel()

        setupEmptyStateObserver(
            viewModel.mediaItems,
            binding.mainStatsContainer,
            binding.emptyStateContainer.root
        )

        //region binding

        binding.buttonBook.setOnClickListener {
            val intent = Intent(requireContext(), BookActivity::class.java)
            startActivity(intent)
        }

        binding.buttonVideo.setOnClickListener {
            val intent = Intent(requireContext(), VideoActivity::class.java)
            startActivity(intent)
        }

        binding.buttonMusic.setOnClickListener {
            val intent = Intent(requireContext(), MusicActivity::class.java)
            startActivity(intent)
        }

        binding.buttonOther.setOnClickListener {
            val intent = Intent(requireContext(), OtherActivity::class.java)
            startActivity(intent)
        }

        //endregion

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
        viewModel.setMediaItems(items)
        setupCharts(items)
    }

    private fun setupCharts(items: List<DisplayMediaItem>) {
        var totalBooks = 0
        var totalMusic = 0
        var totalVideo = 0
        var totalOther = 0

        items.forEach {
            when(it.MediaType)
            {
                Enums.MediaType.Book -> totalBooks += it.ItemCount
                Enums.MediaType.Music -> totalMusic += it.ItemCount
                Enums.MediaType.Video -> totalVideo += it.ItemCount
                Enums.MediaType.Other -> totalOther += it.ItemCount
                else -> {}
            }
        }

        val dataSets = ArrayList<IBarDataSet>()
        val colors = ColorTemplate.MATERIAL_COLORS.toList()

        val categories = listOf(
            Triple(0f, totalBooks.toFloat(), "Books"),
            Triple(1f, totalVideo.toFloat(), "Videos"),
            Triple(2f, totalMusic.toFloat(), "Music"),
            Triple(3f, totalOther.toFloat(), "Other")
        )

        categories.forEachIndexed { index, (x, y, label) ->
            val set = BarDataSet(listOf(BarEntry(x, y)), label)
            set.color = colors[index % colors.size]
            dataSets.add(set)
        }

        val barData = BarData(dataSets)
        binding.barChart.setNoDataText("No data to display")
        if (dataSets.isEmpty()) {
            binding.barChart.data = null
        } else {
            binding.barChart.data = barData
            binding.barChart.description.isEnabled = false
            binding.barChart.xAxis.isEnabled = false

            val legend = binding.barChart.legend
            legend.isEnabled = true
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.setDrawInside(false)

            binding.barChart.animateY(1000)
        }
        binding.barChart.invalidate()
    }
}
