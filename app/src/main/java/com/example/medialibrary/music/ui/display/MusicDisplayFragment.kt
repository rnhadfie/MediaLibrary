package com.example.medialibrary.music.ui.display

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
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
import android.graphics.Color
import android.widget.Toast
import com.example.medialibrary.Utils.SafePieChartRenderer

/**
 * Fragment that demonstrates a responsive layout pattern where the format of the content
 * transforms depending on the size of the screen. Specifically this Fragment shows items in
 * the [RecyclerView] using LinearLayoutManager in a small screen
 * and shows items using GridLayoutManager in a large screen.
 */
class MusicDisplayFragment : Fragment() {

    private var _binding: MusicFragmentDisplayBinding? = null
    private val binding get() = _binding!!

    private var currentFilter = MusicFilter()
    private var bookController: MusicController? = null
    private var viewModel: MusicDisplayViewModel? = null
    private var setup: MusicSetup? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[MusicDisplayViewModel::class.java]
        _binding = MusicFragmentDisplayBinding.inflate(inflater, container, false)

        // Initialize controller
        val dbHelper = MediaLibraryDbHelper(requireContext())
        bookController = MusicController(dbHelper)

        loadData()

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

        binding.musicItemList?.setOnClickListener {
            val cds = viewModel?.MediaItems?.value

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

        return binding.root
    }

    private fun loadData() {
        val items = bookController?.GetMusics(currentFilter) ?: emptyList()

        if (items.isEmpty()) {
            binding.emptyStateContainer.root.visibility = View.VISIBLE
            binding.scrollViewMusicDisplay.visibility = View.GONE
        } else {
            binding.emptyStateContainer.root.visibility = View.GONE
            binding.scrollViewMusicDisplay.visibility = View.VISIBLE
        }

        setup = bookController?.GetMusicSetup()
        viewModel?.setMediaItems(items)
        setup?.let { setupCharts(items, it) }
    }

    @SuppressLint("SetTextI18n")
    private fun setupCharts(items: List<Music>, setup: MusicSetup) {

        binding.musicTotalItemsCardText.text = "Total Number of CDs: " + items.count().toString()

        //region Genre Pie Chart
        val pieEntries = ArrayList<PieEntry>()

        val genreList = setup.MusicGenre

        genreList.forEach { (key, value) ->
            val total = items.filter { it.MusicGenre == MusicGenre.entries[key] }.size
            if(total > 0 && MusicGenre.entries[key] != MusicGenre.NoneSelected) {
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
            genrePieChart.legend.isEnabled=false
            genrePieChart.setNoDataTextColor(Color.BLACK)
            genrePieChart.animateXY(1000, 1000)
            genrePieChart.renderer = SafePieChartRenderer(
                genrePieChart,
                genrePieChart.animator,
                genrePieChart.viewPortHandler
            )
        }
        genrePieChart.invalidate()

        //endregion

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}