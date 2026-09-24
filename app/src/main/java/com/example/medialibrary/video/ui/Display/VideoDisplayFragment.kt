package com.example.medialibrary.video.ui.Display

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.R
import com.example.medialibrary.Utils.FragmentType
import com.example.medialibrary.Utils.SafePieChartRenderer
import com.example.medialibrary.Utils.SharedRefreshViewModel
import com.example.medialibrary.backend.controllers.VideoController
import com.example.medialibrary.backend.models.video.Enums
import com.example.medialibrary.backend.models.video.Video
import com.example.medialibrary.backend.models.video.VideoFilter
import com.example.medialibrary.backend.models.video.VideoSetup
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.VideoFragmentDisplayBinding
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate

class VideoDisplayFragment : BaseFragment<VideoFragmentDisplayBinding, VideoDisplayViewModel>(
    VideoFragmentDisplayBinding::inflate
) {

    private var currentFilter = VideoFilter()
    private var videoController: VideoController? = null
    private var setup: VideoSetup? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[VideoDisplayViewModel::class.java]
        setFragmentType(FragmentType.Display)

        val root = super.onCreateView(inflater, container, savedInstanceState)

        val dbHelper = MediaLibraryDbHelper(requireContext())
        videoController = VideoController(dbHelper)

        loadData()

        setupEmptyStateMediaItemObserver(
            viewModel.MediaItems,
            binding.videoStatContainer,
            binding.emptyStateContainer.root
        )

        //region binding

        binding.videoItemList.setOnClickListener {
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

        binding.dropdownType.setOnItemClickListener { parent, _, position, _ ->
            var selectedItem = parent.getItemAtPosition(position).toString()
            selectedItem = selectedItem.replace(" ", "")
            val typeEnum = Enums.VideoType.entries.find { it.name == selectedItem }

            currentFilter.Type = typeEnum
            reloadData()
        }

        binding.dropdownVideoTag.setOnItemClickListener { parent, _, position, _ ->
            var selectedItem = parent.getItemAtPosition(position).toString()
            selectedItem = selectedItem.replace(" ", "")
            val typeEnum = Enums.VideoTag.entries.find { it.name == selectedItem }

            currentFilter.VideoTag = typeEnum
            reloadData()
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

    override fun onRefreshData() {
        loadData()
    }

    private fun reloadData() {
        val items = videoController?.GetVideos(currentFilter) ?: emptyList()
        viewModel.setMediaItems(items)
        setupCharts(items, setup!!)
    }

    private fun loadData() {
        val items = videoController?.GetVideos(currentFilter) ?: emptyList()

        setup = videoController?.GetVideoSetup()
        viewModel.setMediaItems(items)
        setup?.let { setupCharts(items, it) }

        val types = setup?.Types ?: emptyMap()
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types.values.toList())
        binding.dropdownType.setAdapter(typeAdapter)

        val defaultTypeText = types[currentFilter.Type?.ordinal ?: Enums.VideoType.NoneSelected.ordinal]
            ?: types[Enums.VideoType.NoneSelected.ordinal]
            ?: ""
        binding.dropdownType.setText(defaultTypeText, false)

        val tags = setup?.VideoTags ?: emptyMap()
        val tagAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tags.values.toList())
        binding.dropdownVideoTag.setAdapter(tagAdapter)

        val defaultTagText = tags[currentFilter.VideoTag?.ordinal ?: Enums.VideoTag.None.ordinal]
            ?: tags[Enums.VideoTag.None.ordinal]
            ?: ""
        binding.dropdownVideoTag.setText(defaultTagText, false)
    }

    private fun setupCharts(items: List<Video>, setup: VideoSetup) {
        val colors = ColorTemplate.MATERIAL_COLORS.toList()

        var watchedCount = 0
        var totalDvds = 0

        for (book in items) {
            watchedCount = book.Items?.count { it.Watched == true } ?: 0
            totalDvds = book.Items?.count() ?: 0
        }

        binding.videoTotalSeriesCardText.text = getString(R.string.total_number_of_series, items.count())
        binding.videoTotalCardText.text = getString(R.string.total_number_of_dvds, totalDvds)

        setupWatchedPieChart(watchedCount, totalDvds)
        setupFormatPieChart(items)
        setupGenrePieChart(items, setup)
        setupVideoTagBarChart(items, setup, colors)
        setupVideoTypeBarChart(items, colors)
    }

    private fun setupWatchedPieChart(watchedCount: Int, totalDvds: Int) {
        val watchPercent = ((watchedCount.toDouble() / totalDvds.toDouble()) * 100).toInt()

        val readPercentPieEntries = ArrayList<PieEntry>()
        readPercentPieEntries.add(PieEntry(watchPercent.toFloat()))
        readPercentPieEntries.add(PieEntry(100 - watchPercent.toFloat()))

        val readPercentPieDataSet = PieDataSet(readPercentPieEntries, "Read Percent")
        readPercentPieDataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
        val readPercentPieData = PieData(readPercentPieDataSet)

        val watchedPieChart = binding.videoWatchedProgress

        if (readPercentPieEntries.isEmpty()) {
            watchedPieChart.setNoDataTextColor(Color.BLACK)
            watchedPieChart.setNoDataText("No data to display")
            watchedPieChart.data = null
        } else {
            watchedPieChart.description?.isEnabled = false
            watchedPieChart.data = readPercentPieData
            watchedPieChart.centerText = "Watched Percent: $watchPercent%"
            watchedPieChart.legend?.isEnabled = false
            watchedPieChart.setHoleColor(Color.TRANSPARENT)
            watchedPieChart.setTransparentCircleColor(Color.TRANSPARENT)
            watchedPieChart.setBackgroundColor(Color.TRANSPARENT)
            watchedPieChart.renderer = SafePieChartRenderer(
                watchedPieChart,
                watchedPieChart.animator,
                watchedPieChart.viewPortHandler
            )
            watchedPieChart.animateXY(1000, 1000)
        }
        watchedPieChart.invalidate()
    }

    private fun setupFormatPieChart(items: List<Video>) {
        val formatPieEntries = ArrayList<PieEntry>()

        var dvdCount = 0
        var digitalCount = 0
        var bluRayCount = 0
        if (items.isEmpty()) {
            binding.videoFormatPieChart.visibility = View.GONE
        } else {
            binding.videoFormatPieChart.visibility = View.VISIBLE
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

            val foramtPieDataSet = PieDataSet(formatPieEntries, "Format")

            if (dvdCount > 0) formatPieEntries.add(PieEntry(dvdCount.toFloat(), "DVD"))
            if (digitalCount > 0) formatPieEntries.add(PieEntry(digitalCount.toFloat(), "Digital"))
            if (bluRayCount > 0) formatPieEntries.add(PieEntry(bluRayCount.toFloat(), "Blu-Ray"))

            val formatPieChart = binding.videoFormatPieChart

            if (formatPieEntries.isEmpty()) {
                formatPieChart.data = null
                formatPieChart.setNoDataTextColor(Color.BLACK)
                formatPieChart.setNoDataText("No format data to display")
            } else {
                foramtPieDataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
                val formatPieData = PieData(foramtPieDataSet)
                formatPieChart.data = formatPieData
                formatPieChart.setHoleColor(Color.TRANSPARENT)
                formatPieChart.setTransparentCircleColor(Color.TRANSPARENT)
                formatPieChart.setBackgroundColor(Color.TRANSPARENT)
                formatPieChart.centerText = "Format"
                formatPieChart.legend.isEnabled = false
                formatPieChart.animateXY(1000, 1000)
                formatPieChart.renderer = SafePieChartRenderer(
                    formatPieChart,
                    formatPieChart.animator,
                    formatPieChart.viewPortHandler
                )
            }

            formatPieChart.invalidate()
        }
    }

    private fun setupGenrePieChart(items: List<Video>, setup: VideoSetup) {
        val pieEntries = ArrayList<PieEntry>()
        val genreList = setup.Genre

        genreList.forEach { genre ->
            val total = items.filter { it.Genre?.contains(genre.key) == true }.size
            if (total > 0) {
                pieEntries.add(PieEntry(total.toFloat(), genre.value))
            }
        }

        val genrePieChart = binding.videoGenrePieChart
        if (pieEntries.isEmpty()) {
            genrePieChart.setNoDataText("No Genre data to display")
            genrePieChart.data = null
            genrePieChart.setNoDataTextColor(Color.BLACK)
            genrePieChart.setCenterTextSize(20f)
        } else {
            val genrePieDataSet = PieDataSet(pieEntries, "Genre")
            genrePieDataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
            val genrePieData = PieData(genrePieDataSet)
            genrePieChart.data = genrePieData
            genrePieChart.setHoleColor(Color.TRANSPARENT)
            genrePieChart.setTransparentCircleColor(Color.TRANSPARENT)
            genrePieChart.setBackgroundColor(Color.TRANSPARENT)
            genrePieChart.description?.isEnabled = false
            genrePieChart.legend?.isEnabled = false
            genrePieChart.setUsePercentValues(true)
            genrePieChart.centerText = "Genre"

            genrePieChart.renderer = SafePieChartRenderer(
                genrePieChart,
                genrePieChart.animator,
                genrePieChart.viewPortHandler
            )

            genrePieChart.animateXY(1000, 1000)
        }
        genrePieChart.invalidate()
    }

    private fun setupVideoTagBarChart(items: List<Video>, setup: VideoSetup, colors: List<Int>) {
        val videoTags = setup.VideoTags
        val tagDataSets = ArrayList<IBarDataSet>()

        videoTags.forEach { (index, tag) ->
            if (Enums.VideoTag.entries[index] != Enums.VideoTag.None) {
                val total = items.count { it.VideoTag?.ordinal == index }.toFloat()
                val set = BarDataSet(listOf(BarEntry(index.toFloat(), total)), tag)
                set.color = colors[index % colors.size]
                tagDataSets.add(set)
            }
        }

        binding.videoTagTypeBarChart.let { chart ->
            chart.setNoDataText("No media types data to display")
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
                legend.setDrawInside(false)

                chart.animateY(1000)
            }
            chart.invalidate()
        }
    }

    private fun setupVideoTypeBarChart(items: List<Video>, colors: List<Int>) {
        val dataSets = ArrayList<IBarDataSet>()

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
        binding.videoTypeBarChart.let { chart ->
            chart.setNoDataText("No Video type data to display")
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
                legend.setDrawInside(false)
                legend.form = Legend.LegendForm.SQUARE

                chart.animateY(1000)
            }
            chart.invalidate()
        }
    }
}
