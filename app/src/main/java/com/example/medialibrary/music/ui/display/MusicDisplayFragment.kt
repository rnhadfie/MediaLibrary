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
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.R
import com.example.medialibrary.databinding.MusicBottomSheetBinding
import com.example.medialibrary.databinding.MusicFragmentDisplayBinding
import com.example.medialibrary.music.ui.utils.SharedUtils
import com.example.medialibrary.music.ui.utils.SortFilterViewmodel
import com.example.medialibrary.utils.FilterSummaryHelper
import com.example.medialibrary.utils.FragmentType
import com.example.medialibrary.utils.SafePieChartRenderer
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomsheet.BottomSheetDialog
import controllers.MusicController
import models.music.Enums.MusicGenre
import models.music.Music
import models.music.MusicFilter
import models.music.MusicSetup
import repository.database.MediaLibraryDbHelper

class MusicDisplayFragment : BaseFragment<MusicFragmentDisplayBinding, MusicDisplayViewModel>(
    MusicFragmentDisplayBinding::inflate
) {

    private var currentFilter = MusicFilter()
    private var bookController: MusicController? = null
    private var setup: MusicSetup? = null

    private lateinit var sortFilterViewModel: SortFilterViewmodel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[MusicDisplayViewModel::class.java]
        sortFilterViewModel = ViewModelProvider(requireActivity())[SortFilterViewmodel::class.java]
        setFragmentType(FragmentType.Display)

        val root = super.onCreateView(inflater, container, savedInstanceState)

        val dbHelper = MediaLibraryDbHelper(requireContext())
        bookController = MusicController(dbHelper)

        loadData()


        setupEmptyStateMediaItemObserver(
            viewModel.MediaItems,
            binding.musicStatsContainer,
            binding.emptyStateContainer.root
        )

        binding.buttonFilter?.setOnClickListener {
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

        FilterSummaryHelper.bindFilterSummary(
            binding.root.findViewById(R.id.card_active_filter),
            currentFilter,
            setup
        ) {
            currentFilter = MusicFilter()
            loadData()
        }
    }

    private fun showFilterSheet(setup: MusicSetup, filter: MusicFilter) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = MusicBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        SharedUtils.filterSheetSetup(filter, setup, sheetBinding)

        sheetBinding.buttonSheetFitlerMusic.setOnClickListener {
            filter.Collecting = sheetBinding.collecting.triStateButton.tag as Boolean?
            filter.Collected = sheetBinding.collected.triStateButton.tag as Boolean?

            currentFilter = filter
            loadData()
            dialog.dismiss()
        }



        sheetBinding.buttonSheetClearBook.setOnClickListener {
            currentFilter = MusicFilter()
            loadData()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setArtistChart(items: List<Music>) {
       val artistInformationMap = items.groupBy { it.Artist.trim() }.mapValues { it.value.size }


        val dualColumnViewOne = binding.artistCard?.dualCardColumnOne
        val dualColumnViewTwo = binding.artistCard?.dualCardColumnTwo
        val title = binding.artistCard?.cardTitle
        val emptyState = binding.artistCard?.emptyStateContainer
        title?.text = getString(R.string.number_of_cds_by_artist)

        if(artistInformationMap.isEmpty())
        {
            dualColumnViewOne?.visibility = View.GONE
            dualColumnViewTwo?.visibility = View.GONE
            emptyState?.root?.visibility = View.VISIBLE
            emptyState?.root?.text = getString(R.string.no_artist_data_to_display)
        }
        else {

            dualColumnViewOne?.visibility = View.VISIBLE
            dualColumnViewTwo?.visibility = View.VISIBLE
            emptyState?.root?.visibility = View.GONE

            val publisherInformationSortedMap = artistInformationMap.toList()
                .sortedByDescending { (_, value) -> value } // Sort list by the value
                .toMap()

            val halfSize = (publisherInformationSortedMap.size + 1) / 2
            val chunks = publisherInformationSortedMap.entries.chunked(halfSize)

            val firstHalf = chunks.getOrNull(0)?.associate { it.key to it.value } ?: emptyMap()
            val secondHalf = chunks.getOrNull(1)?.associate { it.key to it.value } ?: emptyMap()

            dualColumnViewOne?.removeAllViews()
            dualColumnViewTwo?.removeAllViews()

            for ((key, value) in firstHalf) {
                val textView = TextView(context)
                textView.text = getString(R.string.dual_card_text, key, value)
                textView.setPadding(8, 8, 8, 8)
                dualColumnViewOne?.addView(textView)
            }
            for ((key, value) in secondHalf) {
                val textView = TextView(context)
                textView.text = getString(R.string.dual_card_text, key, value)
                textView.setPadding(8, 8, 8, 8)
                dualColumnViewTwo?.addView(textView)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupCharts(items: List<Music>, setup: MusicSetup) {
        binding.musicTotalItemsCardText.text = "Total Number of CDs: " + items.count().toString()

        val pieEntries = ArrayList<PieEntry<*>>()
        val genreList = setup.MusicGenre

        genreList.forEach { (key, value) ->
            val total = items.filter { it.MusicGenre == MusicGenre.entries[key] }.size
            if (total > 0 && MusicGenre.entries[key] != MusicGenre.NoneSelected) {
                pieEntries.add(PieEntry(total.toFloat(), value))
            }
        }

        val genrePieChart = binding.musicGenrePieChart

        if (pieEntries.isEmpty()) {
            genrePieChart.noDataText = "No Genre data to display"
            genrePieChart.data = null
            genrePieChart.noDataTextColor = Color.BLACK
            genrePieChart.centerTextSize = 20f
        } else {
            val genrePieDataSet = PieDataSet(pieEntries, "Genre")
            genrePieDataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
            val genrePieData = PieData(genrePieDataSet)
            genrePieChart.data = genrePieData
            genrePieChart.holeColor = Color.TRANSPARENT
            genrePieChart.description.isEnabled = false
            genrePieChart.transparentCircleColor = Color.TRANSPARENT
            genrePieChart.setBackgroundColor(Color.TRANSPARENT)
            genrePieChart.centerText = "Music Genre"
            genrePieChart.legend.isEnabled = false
            genrePieChart.noDataTextColor = Color.BLACK
            genrePieChart.animateXY(1000, 1000)
            genrePieChart.renderer = SafePieChartRenderer(
                genrePieChart,
                genrePieChart.animator,
                genrePieChart.viewPortHandler
            )
        }
        genrePieChart.invalidate()

        setArtistChart(items)
    }
}
