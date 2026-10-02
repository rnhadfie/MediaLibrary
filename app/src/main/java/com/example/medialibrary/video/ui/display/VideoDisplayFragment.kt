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
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.R
import com.example.medialibrary.databinding.VideoBottomSheetBinding
import com.example.medialibrary.databinding.VideoFragmentDisplayBinding
import com.example.medialibrary.music.ui.utils.SortFilterViewmodel
import com.example.medialibrary.utils.DualColumnCardHelper
import com.example.medialibrary.utils.FilterSummaryHelper
import com.example.medialibrary.utils.FragmentType
import com.example.medialibrary.utils.SafePieChartRenderer
import com.example.medialibrary.video.ui.utils.SharedUtils
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
import models.music.MusicFilter
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

        loadData()

        setupBindings()

        return root
    }

    override fun onRefreshData() {
        loadData()
    }

    private fun loadData() {
        val items = videoController?.GetVideos(currentFilter) ?: emptyList()

        setup = videoController?.GetVideoSetup()
        viewModel.setMediaItems(items)
        setup?.let { setupCharts(items, it) }

        FilterSummaryHelper.bindFilterSummary(
            binding.root.findViewById(R.id.card_active_filter),
            currentFilter,
            setup
        ) {
            currentFilter = VideoFilter()
            loadData()
        }
    }

    private fun showFilterSheet(setup: VideoSetup, filter: VideoFilter) {
        val dialog = BottomSheetDialog(requireContext())
        var sheetBinding = VideoBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        sheetBinding = SharedUtils.filterSheetSetup(filter, setup, sheetBinding)

        sheetBinding.filterBtn.setOnClickListener {
            filter.Ongoing = sheetBinding.ongoing.triStateButton.tag as Boolean?
            filter.Collecting = sheetBinding.collecting.triStateButton.tag as Boolean?
            filter.AnyOwned = sheetBinding.anyItemsOwned.triStateButton.tag as Boolean?
            filter.Collected = sheetBinding.collected.triStateButton.tag as Boolean?

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

    //region setup


    private fun setupBindings()
    {
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
            val filter = sortFilterViewModel.getOrCreateMusicFilter()
            val sortModel = sortFilterViewModel.getOrCreateSortModel()
            com.example.medialibrary.book.ui.utils.SharedUtils.showSortDialog(requireContext(), filter, isMain = false, sortModel = sortModel) { updatedFilter ->
                sortFilterViewModel.updateMusicFilter(updatedFilter as MusicFilter)
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
            watchedCount += book.Items?.count { it.Watched } ?: 0
            totalDvds += book.Items?.count() ?: 0
        }

        binding.totalSeriesText.text = getString(R.string.total_number_of_series, items.count())
        binding.totalVideoText.text = getString(R.string.total_number_of_dvds, totalDvds)

        setupWatchedPieChart(watchedCount, totalDvds)
        setupFormatPieChart(items)
        setupGenreBarChart(items, setup)
        setupVideoTagBarChart(items, setup, colors)
        setupVideoTypeBarChart(items, colors)
    }

    private fun setupWatchedPieChart(watchedCount: Int, totalDvds: Int) {
        val watchPercent = ((watchedCount.toDouble() / totalDvds.toDouble()) * 100).toInt()

        val readPercentPieEntries = ArrayList<PieEntry<*>>()
        readPercentPieEntries.add(PieEntry(watchPercent.toFloat()))
        readPercentPieEntries.add(PieEntry(100 - watchPercent.toFloat()))

        val readPercentPieDataSet = PieDataSet(readPercentPieEntries, "Read Percent")
        readPercentPieDataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
        val readPercentPieData = PieData(readPercentPieDataSet)

        binding.watchedProgressChart.let { chart ->
            if (readPercentPieEntries.isEmpty()) {
                chart.noDataTextColor = Color.BLACK
                chart.noDataText = "No data to display"
                chart.data = null
            } else {
                chart.description.isEnabled = false
                chart.data = readPercentPieData
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

            val formatPieDataSet = PieDataSet(formatPieEntries, "Format")

            if (dvdCount > 0) formatPieEntries.add(PieEntry(dvdCount.toFloat(), "DVD"))
            if (digitalCount > 0) formatPieEntries.add(PieEntry(digitalCount.toFloat(), "Digital"))
            if (bluRayCount > 0) formatPieEntries.add(PieEntry(bluRayCount.toFloat(), "Blu-Ray"))

            binding.formatChart.let {
                chart ->
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

        genreList.forEach {  genre ->
            val items = items.filter { it.Genre.contains(genre.genreId) }
            var totalBooksPerGenre = 0
            if(!items.isEmpty()) {
                for (book in items) {
                    totalBooksPerGenre += book.Items?.count() ?: 0
                }
                genreInformationMap[genre.genreName] = totalBooksPerGenre
            }
        }

        val dualColumnViewOne = binding.genreCard.dualCardColumnOne
        val dualColumnViewTwo = binding.genreCard.dualCardColumnTwo
        val title = binding.genreCard.cardTitle
        val emptyState = binding.genreCard.emptyStateContainer
        title.text = getString(R.string.total_number_of_video_per_genre)

        DualColumnCardHelper.setupDualColumnCard(
            genreInformationMap,
            dualColumnViewOne,
            dualColumnViewTwo,
            emptyState,
            R.string.no_genre_data_to_display,
            requireContext() )
    }

    private fun setupVideoTagBarChart(items: List<Video>, setup: VideoSetup, colors: List<Int>) {
        val videoTags = setup.VideoTags
        val tagDataSets = ArrayList<IBarDataSet<*>>()

        videoTags.forEach { (index, tag) ->
            if (Enums.VideoTag.entries[index] != Enums.VideoTag.None) {
                val total = items.count { it.VideoTag?.ordinal == index }.toFloat()
                val set = BarDataSet(listOf(BarEntry(index.toFloat(), total)), tag)
                set.color = colors[index % colors.size]
                tagDataSets.add(set)
            }
        }

        binding.videoCategoryChart.let { chart ->
            chart.noDataText = "No media types data to display"
            if (tagDataSets.isEmpty()) {
                chart.data = null
            } else {
                chart.data = BarData(tagDataSets)
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

    private fun setupVideoTypeBarChart(items: List<Video>, colors: List<Int>) {
        val dataSets = ArrayList<IBarDataSet<*>>()

        val typeCounts = listOf(
            Enums.VideoType.Movie to "Movies",
            Enums.VideoType.TVShow to "TV Shows",
            Enums.VideoType.Miniseries to "Mini Series",
            Enums.VideoType.WebSeries to "Web Series"
        )

        typeCounts.forEachIndexed { index, (type, label) ->
            val count = items.count { it.Type == type }.toFloat()
            val set = BarDataSet(listOf(BarEntry(index.toFloat(), count)), label)
            set.color = colors[index % colors.size]
            dataSets.add(set)
        }

        val barData = BarData(dataSets)
        binding.videoTypeChart.let { chart ->
            chart.noDataText = "No Video type data to display"
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
                legend.form = Legend.LegendForm.SQUARE

                chart.animateY(1000)
            }
            chart.invalidate()
        }
    }

    //endregion
}
