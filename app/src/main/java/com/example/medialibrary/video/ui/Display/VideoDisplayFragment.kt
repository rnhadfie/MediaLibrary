package com.example.medialibrary.video.ui.Display

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
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
import android.graphics.Color


class VideoDisplayFragment : Fragment() {

    private var _binding: VideoFragmentDisplayBinding? = null
    private val binding get() = _binding!!

    private var currentFilter = VideoFilter()

    private var videoController: VideoController? = null
    private var viewModel: VideoDisplayViewModel? = null
    private var setup: VideoSetup? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(VideoDisplayViewModel::class.java)
        _binding = VideoFragmentDisplayBinding.inflate(inflater, container, false)


        // Initialize controller
        val dbHelper = MediaLibraryDbHelper(requireContext())
        videoController = VideoController(dbHelper)

        loadData()

        activity?.let { act ->
            val refreshViewModel = ViewModelProvider(act).get(SharedRefreshViewModel::class.java)
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

        return binding.root;

    }

    private fun loadData() {
        val items = videoController?.GetVideos(currentFilter) ?: emptyList()

        if (items.isEmpty()) {
            binding.emptyStateContainer.visibility = View.VISIBLE
            binding.scrollViewVideoDisplay.visibility = View.GONE
        } else {
            binding.emptyStateContainer.visibility = View.GONE
            binding.scrollViewVideoDisplay.visibility = View.VISIBLE
        }

        setup = videoController?.GetVideoSetup()
        viewModel?.setMediaItems(items)
        setup?.let { setupCharts(items, it) }
    }


    private fun setupCharts(items: List<Video>, setup: VideoSetup) {
        val colors = ColorTemplate.MATERIAL_COLORS.toList()

        //region Card Section
        var watchedCount = 0
        var totalDvds = 0

        for (book in items) {
            watchedCount = book.Items?.count { it.Watched == true } ?: 0
            totalDvds = book.Items?.count() ?: 0
        }


        val watchPercent = ((watchedCount.toDouble() / totalDvds.toDouble()) * 100).toInt()


        val readPercentPieEntries= ArrayList<PieEntry>()
        readPercentPieEntries.add(PieEntry(watchPercent.toFloat() ))
        readPercentPieEntries.add(PieEntry(100-watchPercent.toFloat()))

        val readPercentPieDataSet = PieDataSet(readPercentPieEntries, "Read Percent")

        readPercentPieDataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
        val readPercentPieData = PieData(readPercentPieDataSet)
        binding.videoWatchedProgress?.description?.isEnabled = false
        binding.videoWatchedProgress?.data = readPercentPieData
        binding.videoWatchedProgress?.centerText = "Watched Percent: " + watchPercent.toString() + "%"
        binding.videoWatchedProgress?.legend?.isEnabled = false
        binding.videoWatchedProgress?.setHoleColor(Color.TRANSPARENT)
        binding.videoWatchedProgress?.setTransparentCircleColor(Color.TRANSPARENT)
        binding.videoWatchedProgress?.setBackgroundColor(Color.TRANSPARENT)

        binding.videoWatchedProgress?.animateXY(1000, 1000)
        binding.videoWatchedProgress?.invalidate()


        binding.videoTotalSeriesCardText?.text = "Total Number of Series: " + items.count().toString()

        binding.videoTotalCardText?.text = "Total Number of Dvds & Blu-rays: " + totalDvds.toString()
        //endRegion

        //region Video Type
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
        binding.videoTypeBarChart?.let { chart ->
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
            chart.invalidate()
        }
        //endregion

        //region Genre Pie Chart
        val pieEntries = ArrayList<PieEntry>()

        val genreList = setup.Genre;

        genreList.forEach {
                genre ->
            val total = items.filter { it.Genre?.contains(genre.key) == true }.size;
            if(total > 0) {
                pieEntries.add(PieEntry(total.toFloat(), genre.value))
            }
        }

        val genrePieDataSet = PieDataSet(pieEntries, "Genre")
        genrePieDataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
        val genrePieData = PieData(genrePieDataSet)
        binding.videoGenrePieChart?.data = genrePieData
        binding.videoGenrePieChart?.setHoleColor(Color.TRANSPARENT)
        binding.videoGenrePieChart?.setTransparentCircleColor(Color.TRANSPARENT)
        binding.videoGenrePieChart?.setBackgroundColor(Color.TRANSPARENT)
        binding.videoGenrePieChart?.description?.isEnabled = false
        binding.videoGenrePieChart?.legend?.isEnabled = false


        binding.videoGenrePieChart?.animateXY(1000, 1000)
        binding.videoGenrePieChart?.invalidate()

        //endregion

        //region Video Tags
        val videoTags = setup.VideoTags
        val tagDataSets = ArrayList<IBarDataSet>()

        videoTags.forEach { index, tag ->
            if(Enums.VideoTag.entries[index] != Enums.VideoTag.None) {
                val total = items.count { it.VideoTag?.ordinal == index }.toFloat()
                val set = BarDataSet(listOf(BarEntry(index.toFloat(), total)), tag)
                set.color = colors[index % colors.size]
                tagDataSets.add(set)
            }
        }

        binding.videoTagTypeBarChart?.let { chart ->
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
            chart.invalidate()
        }
        //endregion

        //region Video Format

        val formatPieEntries = ArrayList<PieEntry>()

        var dvdCount = 0;
        var digitalCount = 0;
        var bluRayCount = 0;
        if(items.isEmpty())
        {
            binding.videoFormatPieChart.visibility = View.GONE
        }
        else {
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

            foramtPieDataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
            val formatPieData = PieData(foramtPieDataSet)
            binding.videoFormatPieChart?.data = formatPieData
            binding.videoFormatPieChart?.setHoleColor(Color.TRANSPARENT)
            binding.videoFormatPieChart?.setTransparentCircleColor(Color.TRANSPARENT)
            binding.videoFormatPieChart?.setBackgroundColor(Color.TRANSPARENT)

            binding.videoFormatPieChart?.animateXY(1000, 1000)
            binding.videoFormatPieChart?.invalidate()
        }

        //endregion
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}