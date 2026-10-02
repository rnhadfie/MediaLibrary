package com.example.medialibrary.home.ui.display

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.R
import com.example.medialibrary.book.BookActivity
import com.example.medialibrary.book.ui.utils.SortFilterViewmodel
import com.example.medialibrary.databinding.MainFragmentDisplayBinding
import com.example.medialibrary.music.MusicActivity
import com.example.medialibrary.other.OtherActivity
import com.example.medialibrary.utils.FragmentType
import com.example.medialibrary.utils.SharedRefreshViewModel
import com.example.medialibrary.video.VideoActivity
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import controllers.MainController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import models.shared.DisplayMediaItem
import models.shared.Enums
import models.shared.Filter
import repository.database.MediaLibraryDbHelper

class HomeDisplayFragment : BaseFragment<MainFragmentDisplayBinding, HomeDisplayModel>(
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
        viewModel = ViewModelProvider(this)[HomeDisplayModel::class.java]
        sortFilterViewModel = ViewModelProvider(requireActivity())[SortFilterViewmodel::class.java]
        setFragmentType(FragmentType.Display)

        val root = super.onCreateView(inflater, container, savedInstanceState)

        val dbHelper = MediaLibraryDbHelper(requireContext())
        controller = MainController(dbHelper)

        observeSortFilterViewModel()

        setupEmptyStateObserver(
            viewModel.mediaItems,
            binding.scrollViewAllGraphs,
            ContextCompat.getColor(requireContext(), R.color.primary),
            binding.emptyStateContainer,
        )

        //region binding
        binding.buttonBook.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.section_book))
        binding.buttonBook.setOnClickListener {
            val intent = Intent(requireContext(), BookActivity::class.java)
            startActivity(intent)
        }

        binding.buttonVideo.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.section_video))
        binding.buttonVideo.setOnClickListener {
            val intent = Intent(requireContext(), VideoActivity::class.java)
            startActivity(intent)
        }

        binding.buttonMusic.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.section_music))
        binding.buttonMusic.setOnClickListener {
            val intent = Intent(requireContext(), MusicActivity::class.java)
            startActivity(intent)
        }

        binding.buttonOther.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.section_other))
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

        val dataSets = ArrayList<IBarDataSet<*>>()
        val colors = listOf(
            ContextCompat.getColor(requireContext(), R.color.section_book),
            ContextCompat.getColor(requireContext(), R.color.section_video),
            ContextCompat.getColor(requireContext(), R.color.section_music),
            ContextCompat.getColor(requireContext(), R.color.section_other)
        )

        val categories = listOf(
            Triple(0f, totalBooks.toFloat(), "Books"),
            Triple(1f, totalVideo.toFloat(), "Videos"),
            Triple(2f, totalMusic.toFloat(), "Music"),
            Triple(3f, totalOther.toFloat(), "Other")
        )

        categories.forEachIndexed { index, (x, y, label) ->
            val set = BarDataSet(listOf(BarEntry(x, y)), label)
            set.color = colors[index]
            dataSets.add(set)
        }

        val barData = BarData(dataSets)
        binding.barChart.let {chart ->
            chart.noDataText = "No data to display"
            if (dataSets.isEmpty()) {
                chart.data = null
            } else {
                chart.data = barData
                chart.description.isEnabled = false
                chart.xAxis.isEnabled = false

                val legend = chart.legend
                legend.isEnabled = true
                legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                legend.orientation = Legend.LegendOrientation.HORIZONTAL
                legend.isDrawInsideEnabled = false

                chart.animateY(1000)
            }
            chart.invalidate()
        }

    }
}
