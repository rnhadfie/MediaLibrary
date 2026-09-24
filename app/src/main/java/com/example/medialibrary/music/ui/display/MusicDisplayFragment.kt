package com.example.medialibrary.music.ui.display

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.Utils.FragmentType
import com.example.medialibrary.Utils.SafePieChartRenderer
import com.example.medialibrary.Utils.SharedRefreshViewModel
import com.example.medialibrary.backend.controllers.MusicController
import com.example.medialibrary.backend.models.music.Enums.MusicGenre
import com.example.medialibrary.backend.models.music.Music
import com.example.medialibrary.backend.models.music.MusicFilter
import com.example.medialibrary.backend.models.music.MusicSetup
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.MusicFragmentDisplayBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

class MusicDisplayFragment : BaseFragment<MusicFragmentDisplayBinding, MusicDisplayViewModel>(
    MusicFragmentDisplayBinding::inflate
) {

    private var currentFilter = MusicFilter()
    private var bookController: MusicController? = null
    private var setup: MusicSetup? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[MusicDisplayViewModel::class.java]
        setFragmentType(FragmentType.Display)

        val root = super.onCreateView(inflater, container, savedInstanceState)

        val dbHelper = MediaLibraryDbHelper(requireContext())
        bookController = MusicController(dbHelper)

        loadData()

        setupEmptyStateMediaItemObserver(
            viewModel.MediaItems,
            binding.scrollViewMusicDisplay,
            binding.emptyStateContainer.root
        )

        binding.musicItemList.setOnClickListener {
            val cds = viewModel.MediaItems.value
            val sortedCds = cds?.sortedBy { it.Title }
            val cdList = buildString {
                sortedCds?.forEach { book ->
                    appendLine(book.Title)
                }
            }
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("CD List", cdList)
            clipboard.setPrimaryClip(clipData)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
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
        val items = bookController?.GetMusics(currentFilter) ?: emptyList()
        setup = bookController?.GetMusicSetup()
        viewModel.setMediaItems(items)
        setup?.let { setupCharts(items, it) }
    }

    @SuppressLint("SetTextI18n")
    private fun setupCharts(items: List<Music>, setup: MusicSetup) {
        binding.musicTotalItemsCardText.text = "Total Number of CDs: " + items.count().toString()

        val pieEntries = ArrayList<PieEntry>()
        val genreList = setup.MusicGenre

        genreList.forEach { (key, value) ->
            val total = items.filter { it.MusicGenre == MusicGenre.entries[key] }.size
            if (total > 0 && MusicGenre.entries[key] != MusicGenre.NoneSelected) {
                pieEntries.add(PieEntry(total.toFloat(), value))
            }
        }

        val genrePieChart = binding.musicGenrePieChart

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
            genrePieChart.description.isEnabled = false
            genrePieChart.setTransparentCircleColor(Color.TRANSPARENT)
            genrePieChart.setBackgroundColor(Color.TRANSPARENT)
            genrePieChart.centerText = "Music Genre"
            genrePieChart.legend.isEnabled = false
            genrePieChart.setNoDataTextColor(Color.BLACK)
            genrePieChart.animateXY(1000, 1000)
            genrePieChart.renderer = SafePieChartRenderer(
                genrePieChart,
                genrePieChart.animator,
                genrePieChart.viewPortHandler
            )
        }
        genrePieChart.invalidate()
    }
}
