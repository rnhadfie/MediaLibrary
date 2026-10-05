package com.example.medialibrary.video.ui.display

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.R
import com.example.medialibrary.databinding.VideoBottomSheetBinding
import com.example.medialibrary.databinding.VideoFragmentDisplayBinding
import com.example.medialibrary.utils.DualColumnCardHelper
import com.example.medialibrary.utils.FilterSummaryHelper
import com.example.medialibrary.utils.FragmentType
import com.example.medialibrary.utils.SafePieChartRenderer
import com.example.medialibrary.video.ui.utils.SharedUtils
import com.example.medialibrary.video.ui.utils.SortFilterViewmodel
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomsheet.BottomSheetDialog
import controllers.VideoController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import models.video.Enums
import models.video.Video
import models.video.VideoFilter
import models.video.VideoSetup
import repository.database.MediaLibraryDbHelper

class VideoDisplayFragment : BaseFragment<VideoFragmentDisplayBinding, VideoDisplayViewModel>(
    VideoFragmentDisplayBinding::inflate
) {

    private var currentFilter = VideoFilter()
    private var videoController: VideoController? = null
    private var setup: VideoSetup? = null
    private lateinit var sortFilterViewModel: SortFilterViewmodel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[VideoDisplayViewModel::class.java]
        setFragmentType(FragmentType.Display)

        val root = super.onCreateView(inflater, container, savedInstanceState)
        sortFilterViewModel = ViewModelProvider(requireActivity())[SortFilterViewmodel::class.java]

        val dbHelper = MediaLibraryDbHelper(requireContext())
        videoController = VideoController(dbHelper)

        observeSortFilterViewModel()
        loadData()
        setupBindings()

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
        val items = videoController?.GetVideos(currentFilter) ?: emptyList()

        setup = videoController?.GetVideoSetup()
        viewModel.setMediaItems(items)
        setup?.let {
            setupBindings()
            setupCharts(items, it)
        }

        FilterSummaryHelper.bindFilterSummary(
            binding.root.findViewById(R.id.card_active_filter),
            currentFilter,
            setup,
            FragmentType.Display
        ) {
            val emptyFilter = VideoFilter()
            currentFilter = emptyFilter
            sortFilterViewModel.updateVideoFilter(emptyFilter)
            loadData()
        }
    }

    private fun showFilterSheet(setup: VideoSetup, filter: VideoFilter) {
        val dialog = BottomSheetDialog(requireContext())
        var sheetBinding = VideoBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        sheetBinding = SharedUtils.filterSheetSetup(filter, setup, sheetBinding)

        sheetBinding.filterBtn.setOnClickListener {
            filter.Ongoing = sheetBinding.ongoing.triStateButton.tag as? Boolean
            filter.Collecting = sheetBinding.collecting.triStateButton.tag as? Boolean
            filter.AnyOwned = sheetBinding.anyItemsOwned.triStateButton.tag as? Boolean
            filter.Collected = sheetBinding.collected.triStateButton.tag as? Boolean

            currentFilter = filter
            sortFilterViewModel.updateVideoFilter(filter)
            loadData()
            dialog.dismiss()
        }

        sheetBinding.clearActiveFilter.setOnClickListener {
            val emptyFilter = VideoFilter()
            currentFilter = emptyFilter
            sortFilterViewModel.updateVideoFilter(emptyFilter)
            loadData()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupBindings() {
        setupEmptyStateMediaItemObserver(
            viewModel.MediaItems,
            binding.scrollView,
            ContextCompat.getColor(requireContext(), R.color.section_video),
            binding.emptyStateContainer,
            binding.emptyStateLayout,
            R.string.no_videos_found
        )

        binding.filterBtn.setOnClickListener {
            setup?.let { s -> showFilterSheet(s, currentFilter) }
        }

        binding.sortBtn.setOnClickListener {
            val filter = sortFilterViewModel.getOrCreateVideoFilter()
            val sortModel = sortFilterViewModel.getOrCreateSortModel()
            com.example.medialibrary.book.ui.utils.SharedUtils.showSortDialog(requireContext(), filter, isMain = false, sortModel = sortModel) { updatedFilter ->
                sortFilterViewModel.updateVideoFilter(updatedFilter as VideoFilter)
                loadData()
            }
        }

        binding.copyListBtn.setOnClickListener {
            val videos = viewModel.MediaItems.value
            val sortedVideos = videos?.sortedBy { it.Title }
            val videoList = buildString {
                sortedVideos?.forEach { book ->
                    appendLine(book.Title)
                }
            }
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Movies and TV Shows List", videoList)
            clipboard.setPrimaryClip(clipData)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupCharts(items: List<Video>, setup: VideoSetup) {
        val colors = ColorTemplate.MATERIAL_COLORS.toList()

        var watchedCount = 0
        var totalDvds = 0

        for (book in items) {
            watchedCount += book.Items?.count { it.Watched && it.Owned } ?: 0
            totalDvds += book.Items?.count { it.Owned } ?: 0
        }

        binding.totalSeriesText.text = getString(R.string.total_number_of_series, items.count())
        binding.totalVideoText.text = getString(R.string.total_number_of_dvds, totalDvds)

        setupWatchedPieChart(watchedCount, totalDvds)
        setupFormatPieChart(items)
        setVideoTypeBarChart(items, setup, colors)
        setCategoryBarChart(items, setup, colors)
        setupGenreBarChart(items, setup)
    }

    private fun setupWatchedPieChart(watchedCount: Int, totalDvds: Int) {
        val watchPercent = if (totalDvds > 0) ((watchedCount.toDouble() / totalDvds.toDouble()) * 100).toInt() else 0

        val watchPercentPieEntries = ArrayList<PieEntry<*>>()
        watchPercentPieEntries.add(PieEntry(watchPercent.toFloat()))
        watchPercentPieEntries.add(PieEntry(100 - watchPercent.toFloat()))

        val watchPercentPieDataSet = PieDataSet(watchPercentPieEntries, "Watched Percent")
        watchPercentPieDataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()

        val percentPieData = PieData(watchPercentPieDataSet)

        binding.watchedProgressChart.let { chart ->
            if (watchPercentPieEntries.isEmpty()) {
                chart.noDataTextColor = Color.BLACK
                chart.noDataText = "No data to display"
                chart.data = null
                chart.noDataTextColor = Color.BLACK
                chart.centerTextSize = 20f
            } else {
                chart.description.isEnabled = false
                chart.data = percentPieData
                chart.centerText = "Watched Percent: $watchPercent%"
                chart.legend.isEnabled = false
                chart.holeColor = Color.TRANSPARENT
                chart.transparentCircleColor = Color.TRANSPARENT
                chart.setBackgroundColor(Color.TRANSPARENT)
                chart.renderer = SafePieChartRenderer(
                    chart,
                    chart.animator,
                    chart.viewPortHandler
                )
                chart.animateXY(1000, 1000)
            }
            chart.invalidate()
        }
    }

    private fun setupFormatPieChart(items: List<Video>) {
        val formatPieEntries = ArrayList<PieEntry<*>>()

        var dvdCount = 0
        var digitalCount = 0
        var bluRayCount = 0
        if (items.isEmpty()) {
            binding.formatChart.visibility = View.GONE
        } else {
            binding.formatChart.visibility = View.VISIBLE
            for (video in items) {
                if (video.Items != null) {
                    for (videoItem in video.Items) {
                        when (videoItem.Format) {
                            Enums.VideoFormat.DVD -> dvdCount++
                            Enums.VideoFormat.Digital -> digitalCount++
                            Enums.VideoFormat.BluRay -> bluRayCount++
                            else -> {}
                        }
                    }
                }
            }

            val formatPieDataSet = PieDataSet(formatPieEntries, "Format %")

            if (dvdCount > 0) formatPieEntries.add(PieEntry(dvdCount.toFloat(), "DVD"))
            if (digitalCount > 0) formatPieEntries.add(PieEntry(digitalCount.toFloat(), "Digital"))
            if (bluRayCount > 0) formatPieEntries.add(PieEntry(bluRayCount.toFloat(), "Blu-Ray"))

            binding.formatChart.let { chart ->
                if (formatPieEntries.isEmpty()) {
                    chart.data = null
                    chart.noDataTextColor = Color.BLACK
                    chart.noDataText = "No format data to display"
                } else {
                    formatPieDataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
                    val formatPieData = PieData(formatPieDataSet)
                    chart.data = formatPieData
                    chart.holeColor = Color.TRANSPARENT
                    chart.transparentCircleColor = Color.TRANSPARENT
                    chart.setBackgroundColor(Color.TRANSPARENT)
                    chart.centerText = "Format"
                    chart.isUsePercentValuesEnabled = true
                    chart.description.isEnabled = false
                    chart.isDrawEntryLabelsEnabled = false

                    chart.legend.let { legend ->
                        legend.isEnabled = true
                        legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                        legend.orientation = Legend.LegendOrientation.HORIZONTAL
                        legend.isDrawInsideEnabled = false
                        legend.form = Legend.LegendForm.SQUARE
                    }
                    chart.animateXY(1000, 1000)
                    chart.renderer = SafePieChartRenderer(
                        chart,
                        chart.animator,
                        chart.viewPortHandler
                    )
                }
                chart.invalidate()
            }
        }
    }

    private fun setupGenreBarChart(items: List<Video>, setup: VideoSetup) {
        val genreList = setup.Genre
        val genreInformationMap = mutableMapOf<String, Int>()
        genreList.forEach { genre ->
            var totalBooksPerGenre = 0
            if (items.isNotEmpty()) {
                totalBooksPerGenre += items.count { it.Genre.contains(genre.genreId) }
                genreInformationMap[genre.genreName] = totalBooksPerGenre
            }
        }

        binding.genreCard.cardTitle.text = getString(R.string.total_number_of_books_per_genre)

        DualColumnCardHelper.setupDualColumnCard(
            genreInformationMap,
            binding.genreCard,
            R.string.no_genre_data_to_display,
            models.shared.Enums.MediaType.Video,
            requireContext()
        )
    }

    private fun setVideoTypeBarChart(items: List<Video>, setup: VideoSetup, colors: List<Int>) {
        val dataSets = ArrayList<IBarDataSet<*>>()
        val typeCounts = setup.Types

        var index = 1
        typeCounts.forEach { (key, value) ->
            if (Enums.VideoType.entries[key] != Enums.VideoType.NoneSelected) {
                val videoType = Enums.VideoType.entries[key]
                val videos = items.filter { it.Type == videoType }
                var count = 0
                videos.forEach {
                    if (it.Items != null) {
                        count += it.Items.count()
                    }
                }
                val set = BarDataSet(listOf(BarEntry(index.toFloat(), count.toFloat())), value)
                set.color = colors[index % colors.size]
                dataSets.add(set)
                index++
            }
        }

        val barData = BarData(dataSets)

        binding.videoTypeChart.let { chart ->
            chart.noDataText = "No data to display"

            if (dataSets.isEmpty()) {
                chart.data = null
                chart.noDataTextColor = Color.BLACK
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
                legend.form = Legend.LegendForm.SQUARE

                chart.animateY(1000)
            }
            chart.invalidate()
        }
    }

    private fun setCategoryBarChart(items: List<Video>, setup: VideoSetup, colors: List<Int>) {
        val dataSets = ArrayList<IBarDataSet<*>>()
        val tagCounts = setup.VideoTags

        var index = 1
        tagCounts.forEach { (key, value) ->
            if (Enums.VideoTag.entries[key] != Enums.VideoTag.None) {
                val videoTag = Enums.VideoTag.entries[key]
                val videos = items.filter { it.VideoTag == videoTag }
                var count = 0
                videos.forEach {
                    if (it.Items != null) {
                        count += it.Items.count()
                    }
                }
                val set = BarDataSet(listOf(BarEntry(index.toFloat(), count.toFloat())), value)
                set.color = colors[index % colors.size]
                dataSets.add(set)
                index++
            }
        }

        val barData = BarData(dataSets)

        binding.videoCategoryChart.let { chart ->
            chart.noDataText = "No Tag data to display"

            if (dataSets.isEmpty()) {
                chart.data = null
                chart.noDataTextColor = Color.BLACK
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
                legend.form = Legend.LegendForm.SQUARE

                chart.animateY(1000)
            }
            chart.invalidate()
        }
    }
}
