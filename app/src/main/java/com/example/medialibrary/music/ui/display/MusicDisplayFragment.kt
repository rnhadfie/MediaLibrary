package com.example.medialibrary.music.ui.display

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import com.example.medialibrary.databinding.MusicBottomSheetBinding
import com.example.medialibrary.databinding.MusicFragmentDisplayBinding
import com.example.medialibrary.databinding.ViewTextCardBinding
import com.example.medialibrary.databinding.ViewTextCardDisplayBinding
import com.example.medialibrary.music.ui.utils.SharedUtils
import com.example.medialibrary.music.ui.utils.SortFilterViewmodel
import com.example.medialibrary.utils.FilterSummaryHelper
import com.example.medialibrary.utils.FragmentType
import com.example.medialibrary.utils.TextCardHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import controllers.MusicController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import models.music.Enums.MusicGenre
import models.music.Music
import models.music.MusicFilter
import models.music.MusicSetup
import models.shared.Enums
import repository.database.MediaLibraryDbHelper
import java.text.NumberFormat

class MusicDisplayFragment : BaseFragment<MusicFragmentDisplayBinding, MusicDisplayViewModel>(
    MusicFragmentDisplayBinding::inflate
) {

    private var currentFilter = MusicFilter()
    private var musicController: MusicController? = null
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
        musicController = MusicController(dbHelper)

        observeSortFilterViewModel()
        loadData()

        setupEmptyStateMediaItemObserver(
            viewModel.MediaItems,
            binding.scrollViewMusicDisplay,
            ContextCompat.getColor(requireContext(), R.color.section_music),
            binding.emptyStateContainer,
            binding.emptyStateLayout,
            emptyTextResId = R.string.no_cds_found,
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

    private fun observeSortFilterViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            sortFilterViewModel.currentMusicFilter.collectLatest { filter ->
                currentFilter = filter ?: MusicFilter()
                loadData()
            }
        }
    }

    override fun onRefreshData() {
        loadData()
    }

    private fun loadData() {
        val items = musicController?.GetMusics(currentFilter) ?: emptyList()
        setup = musicController?.GetMusicSetup()
        viewModel.setMediaItems(items)

        val total = items.count { it.HasCollectedAllItems }
        binding.musicTotalItemsCardText.text = buildString {
            append(getString(R.string.total_cds))
            append(": ")
            append(total)
        }

        setArtistChart(items)
        setGenreChart(items)

        FilterSummaryHelper.bindFilterSummary(
            binding.root.findViewById(R.id.card_active_filter),
            currentFilter,
            setup,
            FragmentType.Display
        ) {
            val emptyFilter = MusicFilter()
            currentFilter = emptyFilter
            sortFilterViewModel.updateMusicFilter(emptyFilter)
            loadData()
        }
    }

    private fun showFilterSheet(setup: MusicSetup, filter: MusicFilter) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = MusicBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        SharedUtils.filterSheetSetup(filter, setup, sheetBinding)

        sheetBinding.buttonSheetFitlerMusic.setOnClickListener {
            filter.Collecting = sheetBinding.collecting.triStateButton.tag as? Boolean
            filter.Collected = sheetBinding.collected.triStateButton.tag as? Boolean

            currentFilter = filter
            sortFilterViewModel.updateMusicFilter(filter)
            loadData()
            dialog.dismiss()
        }

        sheetBinding.buttonSheetClearBook.setOnClickListener {
            val emptyFilter = MusicFilter()
            currentFilter = emptyFilter
            sortFilterViewModel.updateMusicFilter(emptyFilter)
            loadData()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setArtistChart(items: List<Music>) {

        var artistInformationMap = items.groupBy { it.Artist.trim() }.mapValues {
            if(it.value.isNotEmpty()) {
                it.value.size.toString()
            }
            else
            {
                ""
            }
        }
       var sortedMap = artistInformationMap.entries
            .sortedBy { it.value }
            .associate { it.toPair() }


        sortedMap = sortedMap.filter { it.key != "" && it.value != "" }
        val card = binding.artistCard
        val title = binding.artistCard.cardTitle
        val emptyState = binding.artistCard.emptyStateContainer
        title.text = getString(R.string.artists)

        TextCardHelper.setupTextCard(
            sortedMap,
            null,
            card,
            emptyState,
            R.string.no_artist_data_to_display,
            -1,
            R.string.number,
            -1,
            Enums.MediaType.Music,
            layoutInflater,
            requireContext()
        )
    }


    private fun setGenreChart(items: List<Music>) {
        val totalItems = items.size

        var genreInformationMap = items.groupBy { it.MusicGenre.name }.mapValues {
            if(it.value.isNotEmpty() && it.key != MusicGenre.NoneSelected.name) {
                val percent = (it.value.size.toFloat() / totalItems.toFloat())
                NumberFormat.getPercentInstance().format(percent)
            }
            else
            {
                ""
            }
        }

        genreInformationMap = genreInformationMap.filter { it.value != "" && it.key == MusicGenre.NoneSelected.name }

        genreInformationMap = genreInformationMap.entries
            .sortedBy { it.value }
            .associate { it.toPair() }

        val genreExtraInfoMap = items.groupBy { it.MusicGenre.name }.mapValues {
            if(it.value.isNotEmpty()) {
              it.value.size
            }
        }


        val card: ViewTextCardBinding = binding.genreCard
        val title = binding.genreCard.cardTitle
        title.setText(R.string.music_genre)
        val emptyState = binding.genreCard.emptyStateContainer


        TextCardHelper.setupTextCard(
            genreInformationMap,
            genreExtraInfoMap,
            card,
            emptyState,
            R.string.no_genre_data_to_display,
            -1,
            R.string.percent,
            R.string.number,
            Enums.MediaType.Music,
            layoutInflater,
            requireContext()
        )
    }
}
