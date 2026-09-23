package com.example.medialibrary.book.ui.display

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.example.medialibrary.Utils.SharedRefreshViewModel
import com.example.medialibrary.backend.models.book.*
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
import android.widget.Toast
import com.example.medialibrary.Utils.SafePieChartRenderer



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
        viewModel = ViewModelProvider(this)[BookDisplayViewModel::class.java]
        _binding = BookFragmentDisplayBinding.inflate(inflater, container, false)

        // Initialize controller
        val dbHelper = MediaLibraryDbHelper(requireContext())
        bookController = BookController(dbHelper)

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


        binding.dropdownSheetType?.setOnItemClickListener { parent, _, position, _ ->
            var selectedItem = parent.getItemAtPosition(position).toString()
            selectedItem = selectedItem.replace(" ", "")
            val typeEnum = BookType.entries.find { it.name == selectedItem }

            currentFilter.Type = typeEnum
            reloadData()
        }

        binding.bookItemList?.setOnClickListener {
            val books = viewModel?.mediaItems?.value

            val sortedBooks = books?.sortedBy { it.Title }
            val bookList = buildString {
                sortedBooks?.forEach { book ->
                    appendLine(book.Title)
                }
            }
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Book List", bookList)
            clipboard.setPrimaryClip(clipData)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val types = setup?.Type ?: emptyMap()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, types.values.toList())

        binding.dropdownSheetType?.setAdapter(adapter)
        adapter.notifyDataSetChanged()

        val defaultText = types[currentFilter.Type?.ordinal ?: BookType.NoneSelected.ordinal]
            ?: types[BookType.NoneSelected.ordinal]
            ?: ""
        binding.dropdownSheetType?.setText(defaultText, false)
    }

    private fun loadData() {
        val items = bookController?.GetBooks(currentFilter) ?: emptyList()

        if (items.isEmpty()) {
            binding.emptyStateContainer.visibility = View.VISIBLE
            binding.bookStatsContainer?.visibility = View.GONE
        } else {
            binding.emptyStateContainer.visibility = View.GONE
            binding.bookStatsContainer?.visibility = View.VISIBLE
        }

        setup = bookController?.GetBookSetup()
        viewModel?.setMediaItems(items)
        setup?.let { setupCharts(items, it) }


    }

    private fun reloadData()
    {
        val items = bookController?.GetBooks(currentFilter) ?: emptyList()

        if (items.isEmpty()) {
            binding.emptyStateContainer.visibility = View.VISIBLE
            binding.bookStatsContainer?.visibility = View.GONE
        } else {
            binding.emptyStateContainer.visibility = View.GONE
            binding.bookStatsContainer?.visibility = View.VISIBLE
        }

        viewModel?.setMediaItems(items)

        setupCharts(items, setup!!)
    }

    @SuppressLint("SetTextI18n")
    private fun setupCharts(items: List<Book>, setup: BookSetup) {

        var readCount = 0
        var totalBook = 0

        for (book in items) {
            readCount += book.Items?.count { it.Read } ?: 0
            totalBook += book.Items?.count() ?: 0
        }

        val completedCount = items.count { it.HasCollectedAllItems == true && it.HasSeriesEnded == true }
        val updateToDate = items.count { it.HasSeriesEnded == false && it.HasCollectedAllItems == false }

        binding.bookTotalSeriesCardText.text = "Total Number of Series: " + items.count().toString()
        binding.bookTotalCardText.text = "Total Number of Books: $totalBook"
        binding.bookCompletedCardText.text = "Total Number of Completed Series: $completedCount"
        binding.bookOngoingCardText?.text = "Total Number of Ongoing Series: $updateToDate"

        setReadPercentChart(readCount, totalBook)


        val colors = ColorTemplate.MATERIAL_COLORS.toList()

        setBookTypeBarChart(items, setup, colors)

        setBookGenrePieChart(items, setup)

        setPublisherBarChart(items, setup, colors)

        setBookFormatPieChart(items)
    }

    private fun setReadPercentChart(readCount: Int, totalBook: Int)
    {

        //region Get Data

        val readPercent = ((readCount.toDouble() / totalBook.toDouble()) * 100).toInt()
        val readPercentPieEntries= ArrayList<PieEntry>()
        readPercentPieEntries.add(PieEntry(readPercent.toFloat() ))
        readPercentPieEntries.add(PieEntry(100-readPercent.toFloat()))

        val readPercentPieDataSet = PieDataSet(readPercentPieEntries, "Read Percent")
        readPercentPieDataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()

        //endregion

        val readPercentPieData = PieData(readPercentPieDataSet)

        val bookReadProgressChart = binding.bookReadProgress
        if(readPercentPieEntries.isEmpty())
        {
            bookReadProgressChart.setNoDataText("No data to display")
            bookReadProgressChart.data = null
            bookReadProgressChart.setNoDataTextColor(Color.BLACK)
            bookReadProgressChart.setCenterTextSize(20f)
        }
        bookReadProgressChart.description?.isEnabled = false
        bookReadProgressChart.data = readPercentPieData
        bookReadProgressChart.centerText = "Read Percent: $readPercent%"
        bookReadProgressChart.legend?.isEnabled = false
        bookReadProgressChart.setHoleColor(Color.TRANSPARENT)
        bookReadProgressChart.setTransparentCircleColor(Color.TRANSPARENT)
        bookReadProgressChart.setBackgroundColor(Color.TRANSPARENT)
        bookReadProgressChart.renderer = SafePieChartRenderer(
            bookReadProgressChart,
            bookReadProgressChart.animator,
            bookReadProgressChart.viewPortHandler
        )


        bookReadProgressChart.animateXY(1000, 1000)
        bookReadProgressChart.invalidate()
    }

    private fun setBookTypeBarChart(items: List<Book>, setup: BookSetup, colors: List<Int>)
    {
        val dataSets = ArrayList<IBarDataSet>()
        val typeCounts = setup.Type

        var index = 1
        typeCounts.forEach { (key, value) ->
            if(BookType.entries[key] != BookType.NoneSelected)
            {
                val bookType = BookType.entries[key]
                val count = items.count { it.Type == bookType }.toFloat()
                val set = BarDataSet(listOf(BarEntry(index.toFloat(), count)), value)
                set.color = colors[index % colors.size]
                dataSets.add(set)
                index++
            }
        }

        val barData = BarData(dataSets)

        binding.bookTypeBarChart.let { chart ->
            chart.setNoDataText("No data to display")
            if (dataSets.isEmpty()) {
                chart.data = null
                chart.setNoDataTextColor(Color.BLACK)
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

    private fun setBookGenrePieChart(items: List<Book>, setup: BookSetup)
    {
        val pieEntries = ArrayList<PieEntry>()

        val genreList = setup.Genre
        genreList.forEach {
                genre ->
            val total = items.filter { it.Genre?.contains(genre.key) == true }.size
            if(total > 0) {
                pieEntries.add(PieEntry(total.toFloat(), genre.value))
            }
        }

        val genrePieChart = binding.bookGenrePieChart
        if (pieEntries.isEmpty()) {
            genrePieChart.data = null
            genrePieChart.setNoDataTextColor(Color.BLACK)
            genrePieChart.setNoDataText("No Genre data to display")
            genrePieChart.setCenterTextSize(20f)

        } else {
            val genrePieDataSet = PieDataSet(pieEntries, "Genre")
            genrePieDataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
            val genrePieData = PieData(genrePieDataSet)
            genrePieChart.data = genrePieData
            genrePieChart.description?.isEnabled = false
            genrePieChart.setHoleColor(Color.TRANSPARENT)
            genrePieChart.setTransparentCircleColor(Color.TRANSPARENT)
            genrePieChart.setBackgroundColor(Color.TRANSPARENT)
            genrePieChart.setUsePercentValues(true)
            genrePieChart.centerText = "Genre"
            genrePieChart.legend.isEnabled=false

            genrePieChart.animateXY(1000, 1000)
            genrePieChart.renderer = SafePieChartRenderer(
                genrePieChart,
                genrePieChart.animator,
                genrePieChart.viewPortHandler
            )
        }
        genrePieChart.invalidate()
    }

    private fun setPublisherBarChart(items: List<Book>, setup: BookSetup, colors: List<Int>)
    {
        val publishers = setup.Publishers
        val pubDataSets = ArrayList<IBarDataSet>()

        publishers.forEachIndexed { index, publisher ->
            val total = items.count { it.Publisher == publisher.Id }.toFloat()
            if(total > 0) {
                val set = BarDataSet(listOf(BarEntry(index.toFloat(), total)), publisher.Name)
                set.color = colors[index % colors.size]
                pubDataSets.add(set)
            }
        }

        binding.bookPublisherBarChart.let { chart ->

            if (pubDataSets.isEmpty()) {
                chart.setNoDataText("No Publisher data to display")
                chart.setNoDataTextColor(Color.BLACK)
                chart.data = null
            } else {
                chart.data = BarData(pubDataSets)
                chart.description.isEnabled = false
                chart.xAxis.isEnabled = false


                val legend = chart.legend
                legend.isEnabled = true
                legend.verticalAlignment = Legend.LegendVerticalAlignment.CENTER
                legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                legend.orientation = Legend.LegendOrientation.VERTICAL

                legend.setDrawInside(false)

                chart.animateY(1000)
            }
            chart.invalidate()
        }
    }

    private fun setBookFormatPieChart(items: List<Book>)
    {
        // region Format Data

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

        if (ebookCount > 0) {
            formatPieEntries.add(PieEntry(ebookCount.toFloat(), "E-Books"))
        }
        if (hardCoverCount > 0) {
            formatPieEntries.add(PieEntry(hardCoverCount.toFloat(), "Hardcover"))
        }
        if (paperBackCount > 0) {
            formatPieEntries.add(PieEntry(paperBackCount.toFloat(), "Paperback"))
        }

        //endregion

        val bookFormatPieChart = binding.bookFormatPieChart


        if (formatPieEntries.isEmpty()) {
            bookFormatPieChart.data = null
            bookFormatPieChart.setNoDataText("No Format data to display")
            bookFormatPieChart.setNoDataTextColor(Color.BLACK)
            bookFormatPieChart.setCenterTextSize(20f)
        } else {
            val formatPieDataSet = PieDataSet(formatPieEntries, "Format")
            formatPieDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

            val formatPieData = PieData(formatPieDataSet)
            bookFormatPieChart.data = formatPieData

            bookFormatPieChart.description.isEnabled = false
            bookFormatPieChart.description.text = ""
            bookFormatPieChart.setHoleColor(Color.TRANSPARENT)
            bookFormatPieChart.setTransparentCircleColor(Color.TRANSPARENT)
            bookFormatPieChart.setBackgroundColor(Color.TRANSPARENT)
            bookFormatPieChart.setUsePercentValues(true)
            bookFormatPieChart.centerText = "Format"
            bookFormatPieChart.legend.isEnabled=false



            bookFormatPieChart.animateXY(1000, 1000)
            bookFormatPieChart.renderer = SafePieChartRenderer(
                bookFormatPieChart,
                bookFormatPieChart.animator,
                bookFormatPieChart.viewPortHandler
            )
        }
        bookFormatPieChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
