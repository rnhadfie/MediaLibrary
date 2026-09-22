package com.example.medialibrary.book.ui.display

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.medialibrary.Utils.SharedRefreshViewModel
import com.example.medialibrary.backend.models.book.Book
import com.example.medialibrary.backend.models.book.BookFilter
import com.example.medialibrary.backend.models.book.BookSetup
import com.example.medialibrary.backend.controllers.BookController
import com.example.medialibrary.backend.models.book.Enums.*
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.BookFragmentDisplayBinding
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import android.graphics.Color
import android.widget.ArrayAdapter
import com.example.medialibrary.backend.models.book.Enums


class BookDisplayFragment : Fragment() {

    private var _binding: BookFragmentDisplayBinding? = null
    private val binding get() = _binding!!

    private var currentFilter = BookFilter()
    private var bookController: BookController? = null
    private var viewModel: BookDisplayViewModel? = null
    private var setup: BookSetup? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(BookDisplayViewModel::class.java)
        _binding = BookFragmentDisplayBinding.inflate(inflater, container, false)


        // Initialize controller
        val dbHelper = MediaLibraryDbHelper(requireContext())
        bookController = BookController(dbHelper)

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

        binding.dropdownSheetType?.setOnClickListener {
            val selected =  binding.dropdownSheetType?.listSelection ?:0

            var typeEnum = Enums.BookType.entries[selected];

            currentFilter.Type = typeEnum;
            reloadData()
        }

        return binding.root
    }

    private fun loadData() {
        val items = bookController?.GetBooks(currentFilter) ?: emptyList()

        if (items.isEmpty()) {
            binding.emptyStateContainer.visibility = View.VISIBLE
            binding.scrollViewBookDisplay.visibility = View.GONE
        } else {
            binding.emptyStateContainer.visibility = View.GONE
            binding.scrollViewBookDisplay.visibility = View.VISIBLE
        }

        setup = bookController?.GetBookSetup()
        viewModel?.setMediaItems(items)
        setup?.let { setupCharts(items, it) }

        val types = setup?.Type ?: emptyMap()
        binding.dropdownSheetType?.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types.values.toList())
        )
    }

    private fun reloadData()
    {
        val items = bookController?.GetBooks(currentFilter) ?: emptyList()

        if (items.isEmpty()) {
            binding.emptyStateContainer.visibility = View.VISIBLE
            binding.scrollViewBookDisplay.visibility = View.GONE
        } else {
            binding.emptyStateContainer.visibility = View.GONE
            binding.scrollViewBookDisplay.visibility = View.VISIBLE
        }

        viewModel?.setMediaItems(items)
    }

    private fun setupCharts(items: List<Book>, setup: BookSetup) {
        //region Card Section
        var readCount = 0
        var totalBook = 0
        var completedCount = 0;
        var updateToDate = 0;

        for (book in items) {
            readCount += book.Items?.count { it.Read == true } ?: 0
            totalBook += book.Items?.count() ?: 0
        }

        completedCount = items.count { it.HasCollectedAllItems == true && it.HasSeriesEnded == true }
        updateToDate = items.count { it.HasSeriesEnded == false && it.HasCollectedAllItems == false }

        val readPercent = ((readCount.toDouble() / totalBook.toDouble()) * 100).toInt()

        val readPercentPieEntries= ArrayList<PieEntry>()
        readPercentPieEntries.add(PieEntry(readPercent.toFloat() ))
        readPercentPieEntries.add(PieEntry(100-readPercent.toFloat()))

        val readPercentPieDataSet = PieDataSet(readPercentPieEntries, "Read Percent")

        readPercentPieDataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
        val readPercentPieData = PieData(readPercentPieDataSet)
        binding.bookReadProgress?.description?.isEnabled = false
        binding.bookReadProgress?.data = readPercentPieData
        binding.bookReadProgress?.centerText = "Read Percent: " + readPercent.toString() + "%"
        binding.bookReadProgress?.legend?.isEnabled = false
        binding.bookReadProgress?.setHoleColor(Color.TRANSPARENT)
        binding.bookReadProgress?.setTransparentCircleColor(Color.TRANSPARENT)
        binding.bookReadProgress?.setBackgroundColor(Color.TRANSPARENT)

        binding.bookReadProgress?.animateXY(1000, 1000)
        binding.bookReadProgress?.invalidate()


        binding.bookTotalSeriesCardText?.text = "Total Number of Series: " + items.count().toString()

        binding.bookTotalCardText?.text = "Total Number of Books: " + totalBook.toString()

        binding.bookCompletedCardText?.text = "Total Number of Completed Series: " + completedCount.toString()

        binding.bookOngoingCardText?.text = "Total Number of Ongoing Series: " + updateToDate.toString()

        //endregion

        val colors = ColorTemplate.MATERIAL_COLORS.toList()

        //region Book Type
        val dataSets = ArrayList<IBarDataSet>()

        val typeCounts = setup.Type

        var index = 1;
        typeCounts.forEach { (key, value) ->
            if(BookType.entries[key] != BookType.NoneSelected)
            {
                var bookType = BookType.entries[key];
                val count = items.count { it.Type == bookType }.toFloat()
                val set = BarDataSet(listOf(BarEntry(index.toFloat(), count)), value)
                set.color = colors[index % colors.size]
                dataSets.add(set);
                index++;
            }
        }

        val barData = BarData(dataSets)
        binding.bookTypeBarChart?.let { chart ->
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

        val genreList = setup.Genre

        genreList.forEach {
                genre ->
            val total = items.filter { it.Genre?.contains(genre.key) == true }.size
            if(total > 0) {
                pieEntries.add(PieEntry(total.toFloat(), genre.value))
            }
        }

        val genrePieDataSet = PieDataSet(pieEntries, "Genre")
        genrePieDataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
        val genrePieData = PieData(genrePieDataSet)
        binding.bookGenrePieChart?.data = genrePieData
        binding.bookGenrePieChart?.description?.isEnabled = false
        binding.bookGenrePieChart?.setHoleColor(Color.TRANSPARENT)
        binding.bookGenrePieChart?.setTransparentCircleColor(Color.TRANSPARENT)
        binding.bookGenrePieChart?.setBackgroundColor(Color.TRANSPARENT)

        binding.bookGenrePieChart?.animateXY(1000, 1000)
        binding.bookGenrePieChart?.invalidate()

        //endregion

        //region Publishers
        val publishers = setup.Publishers
        val pubDataSets = ArrayList<IBarDataSet>()

        publishers.forEachIndexed { index, publisher ->
            val total = items.count { it.Publisher == publisher.Id }.toFloat()
            val set = BarDataSet(listOf(BarEntry(index.toFloat(), total)), publisher.Name)
            set.color = colors[index % colors.size]
            pubDataSets.add(set)
        }

        binding.bookPublisherBarChart?.let { chart ->
            chart.data = BarData(pubDataSets)
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

        //region Book Format

        val formatPieEntries = ArrayList<PieEntry>()

        var ebookCount = 0
        var hardCoverCount = 0
        var paperBackCount = 0

        for (book in items) {
            if(book.Items != null) {
                for (bookItem in book.Items) {
                    when (bookItem.Format) {
                        BookFormat.EBook -> ebookCount++
                        BookFormat.Hardcover -> hardCoverCount++
                        BookFormat.Paperback -> paperBackCount++
                        else -> {}
                    }
                }
            }
        }

        val formatPieDataSet = PieDataSet(formatPieEntries, "Format")

        if (ebookCount > 0) {
            formatPieEntries.add(PieEntry(ebookCount.toFloat(), "E-Books"))
        }
        if (hardCoverCount > 0) {
            formatPieEntries.add(PieEntry(hardCoverCount.toFloat(), "Hardcover"))
        }
        if (paperBackCount > 0) {
            formatPieEntries.add(PieEntry(paperBackCount.toFloat(), "Paperback"))
        }

        formatPieDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
        val formatPieData = PieData(formatPieDataSet)
        binding.bookFormatPieChart?.data = formatPieData
        binding.bookFormatPieChart?.description?.isEnabled = false
        binding.bookFormatPieChart?.setHoleColor(Color.TRANSPARENT)
        binding.bookFormatPieChart?.setTransparentCircleColor(Color.TRANSPARENT)
        binding.bookFormatPieChart?.setBackgroundColor(Color.TRANSPARENT)

        binding.bookFormatPieChart?.animateXY(1000, 1000)
        binding.bookFormatPieChart?.invalidate()

        //endregion
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
