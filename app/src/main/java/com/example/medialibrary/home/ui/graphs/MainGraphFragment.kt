package com.example.medialibrary.home.ui.graphs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.medialibrary.Utils.SharedRefreshViewModel
import com.example.medialibrary.backend.controllers.MainController
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

class MainGraphFragment : Fragment() {

    private var _binding: MainFragmentDisplayBinding? = null
    private val binding get() = _binding!!

    private var currentFilter = Filter()

    private lateinit var controller: MainController
    private lateinit var viewModel: MainGraphViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[MainGraphViewModel::class.java]
        _binding = MainFragmentDisplayBinding.inflate(inflater, container, false)

        // Initialize controller
        val dbHelper = MediaLibraryDbHelper(requireContext())
        controller = MainController(dbHelper)

        loadData()

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

        return binding.root
    }

    private fun loadData() {
        val items = controller.GetMediaItems(currentFilter)

        if (items.isEmpty()) {
            binding.emptyStateContainer.visibility = View.VISIBLE
            binding.mainStatsContainer.visibility = View.GONE
        } else {
            binding.emptyStateContainer.visibility = View.GONE
            binding.mainStatsContainer.visibility = View.VISIBLE
        }

        viewModel.setMediaItems(items)
        setupCharts(items)

    }

    private fun setupCharts(items: List<MediaItem>) {
        // Bar Chart Data
        val totalBooks = items.count { it.MediaType == Enums.MediaType.Book }
        val totalVideo = items.count { it.MediaType == Enums.MediaType.Video }
        val totalMusic = items.count { it.MediaType == Enums.MediaType.Music }
        val totalOther = items.count { it.MediaType == Enums.MediaType.Other }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
