package com.example.medialibrary.music.ui.display

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.medialibrary.Utils.SharedRefreshViewModel
import com.example.medialibrary.backend.controllers.MusicController
import com.example.medialibrary.backend.models.book.Enums.BookType
import com.example.medialibrary.backend.models.music.Enums.MusicGenre
import com.example.medialibrary.backend.models.music.Music
import com.example.medialibrary.backend.models.music.MusicFilter
import com.example.medialibrary.backend.models.music.MusicSetup
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.MusicBottomSheetBinding
import com.example.medialibrary.databinding.MusicFragmentDisplayBinding
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import android.graphics.Color
import com.google.android.material.bottomsheet.BottomSheetDialog

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
        viewModel = ViewModelProvider(this).get(MusicDisplayViewModel::class.java)
        _binding = MusicFragmentDisplayBinding.inflate(inflater, container, false)

        // Initialize controller
        val dbHelper = MediaLibraryDbHelper(requireContext())
        bookController = MusicController(dbHelper)

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

        return binding.root
    }

    private fun loadData() {
        val items = bookController?.GetMusics(currentFilter) ?: emptyList()

        if (items.isEmpty()) {
            binding.emptyStateContainer.visibility = View.VISIBLE
            binding.scrollViewMusicDisplay.visibility = View.GONE
        } else {
            binding.emptyStateContainer.visibility = View.GONE
            binding.scrollViewMusicDisplay.visibility = View.VISIBLE
        }

        setup = bookController?.GetMusicSetup()
        viewModel?.setMediaItems(items)
        setup?.let { setupCharts(items, it) }
    }

    private fun setupCharts(items: List<Music>, setup: MusicSetup) {
        val colors = ColorTemplate.MATERIAL_COLORS.toList()

        binding.musicTotalItemsCardText?.text = "Total Number of CDs: " + items.count().toString()

        //region Genre Pie Chart
        val pieEntries = ArrayList<PieEntry>()

        val genreList = setup.MusicGenre;

        genreList.forEach { (key, value) ->
            val total = items.filter { it.MusicGenre == MusicGenre.entries[key] }.size;
            if(total > 0) {
                pieEntries.add(PieEntry(total.toFloat(), value))
            }
        }

        val genrePieDataSet = PieDataSet(pieEntries, "Genre")
        genrePieDataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
        val genrePieData = PieData(genrePieDataSet)
        binding.musicGenrePieChart?.data = genrePieData
        binding.musicGenrePieChart?.setHoleColor(Color.TRANSPARENT)
        binding.musicGenrePieChart?.setTransparentCircleColor(Color.TRANSPARENT)
        binding.musicGenrePieChart?.setBackgroundColor(Color.TRANSPARENT)

        binding.musicGenrePieChart?.animateXY(1000, 1000)
        binding.musicGenrePieChart?.invalidate()

        //endregion

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}