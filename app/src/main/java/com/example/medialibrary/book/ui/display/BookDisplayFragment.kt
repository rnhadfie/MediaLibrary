package com.example.medialibrary.book.ui.display

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
import androidx.lifecycle.lifecycleScope
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.R
import com.example.medialibrary.book.ui.utils.SharedUtils
import com.example.medialibrary.book.ui.utils.SortFilterViewmodel
import com.example.medialibrary.databinding.BookBottomSheetBinding
import com.example.medialibrary.databinding.BookFragmentDisplayBinding
import com.example.medialibrary.databinding.DialogSortContentBinding
import com.example.medialibrary.utils.FilterSummaryHelper
import com.example.medialibrary.utils.FragmentType
import com.example.medialibrary.utils.SafePieChartRenderer
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomsheet.BottomSheetDialog
import controllers.BookController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import models.book.Book
import models.book.BookFilter
import models.book.BookSetup
import models.book.Enums.BookFormat
import models.book.Enums.BookType
import repository.database.MediaLibraryDbHelper

class BookDisplayFragment : BaseFragment<BookFragmentDisplayBinding, BookDisplayViewModel>(
    BookFragmentDisplayBinding::inflate
) {

    private var currentFilter = BookFilter()
    private var bookController: BookController? = null
    private var setup: BookSetup? = null

    private lateinit var sortFilterViewModel: SortFilterViewmodel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[BookDisplayViewModel::class.java]
        sortFilterViewModel = ViewModelProvider(requireActivity())[SortFilterViewmodel::class.java]
        setFragmentType(FragmentType.Display)

        val root: View = super.onCreateView(inflater, container, savedInstanceState)

        val dbHelper = MediaLibraryDbHelper(requireContext())
        bookController = BookController(dbHelper)


        setDialogSort(DialogSortContentBinding.inflate(layoutInflater))

        observeSortFilterViewModel()

        setupEmptyStateMediaItemObserver(
            viewModel.mediaItems,
            binding.statsContainer,
            binding.emptyStateContainer.root,
        )

        binding.filterBtn.setOnClickListener {
            setup?.let { s -> showFilterSheet(s, currentFilter) }
        }

        binding.sortBtn?.setOnClickListener {
            val filter = sortFilterViewModel.getOrCreateBookFilter()
            val sortModel = sortFilterViewModel.getOrCreateSortModel()
            SharedUtils.showSortDialog(requireContext(), filter, isMain = false, sortModel = sortModel) { updatedFilter ->
                sortFilterViewModel.updateBookFilter(updatedFilter as BookFilter)
                loadData()
            }
        }

        binding.copyListBtn.setOnClickListener {
            val books = viewModel.mediaItems.value
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

        return root
    }

    private fun observeSortFilterViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            sortFilterViewModel.currentBookFilter.collectLatest { filter ->
                currentFilter = filter ?: BookFilter()
                loadData()
            }
        }
    }

    override fun onRefreshData() {
        loadData()
    }

    private fun loadData() {
        val items = bookController?.GetBooks(currentFilter) ?: emptyList()
        if (setup == null || (setup!!.Publishers.isEmpty() && setup!!.Tag.isEmpty())) {
            setup = bookController?.GetBookSetup()
        }
        viewModel.setMediaItems(items)
        setup?.let { setupCharts(items, it) }

        FilterSummaryHelper.bindFilterSummary(
            binding.root.findViewById(R.id.active_filter_card),
            currentFilter,
            setup
        ) {
            val emptyFilter = BookFilter()
            currentFilter = emptyFilter
            sortFilterViewModel.updateBookFilter(emptyFilter)
        }
    }

    private fun showFilterSheet(setup: BookSetup, filter: BookFilter) {
        val dialog = BottomSheetDialog(requireContext())
        var sheetBinding = BookBottomSheetBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        sheetBinding = SharedUtils.filterSheetSetup(filter, setup, sheetBinding)

        sheetBinding.applyFilterBtn.setOnClickListener {
            filter.Read = sheetBinding.read.triStateButton.tag as Boolean?
            filter.Reading = sheetBinding.reading.triStateButton.tag as Boolean?
            filter.AnyOwned = sheetBinding.anyItemsOwned.triStateButton.tag as Boolean?
            filter.Ongoing = sheetBinding.standaloneOrSeriesComplete.triStateButton.tag as Boolean?
            filter.Collecting = sheetBinding.collecting.triStateButton.tag as Boolean?
            filter.Collected = sheetBinding.collected.triStateButton.tag as Boolean?

            sortFilterViewModel.updateBookFilter(filter)
            loadData()
            dialog.dismiss()
        }

        sheetBinding.clearActiveFilter.setOnClickListener {
            sortFilterViewModel.updateBookFilter(BookFilter())
            dialog.dismiss()
        }

        dialog.show()
    }

    //region Chart Setup

    @SuppressLint("SetTextI18n")
    private fun setupCharts(items: List<Book>, setup: BookSetup) {
        var readCount = 0
        var totalBook = 0

        for (book in items) {
            readCount += book.Items?.count { it.Read } ?: 0
            totalBook += book.Items?.count() ?: 0
        }

        val completedCount = items.count { it.HasCollectedAllItems == true }
        val updateToDate = items.count { it.Ongoing }
        val collecting = items.count { it.Collecting }

        binding.seriesCountText.text = "Total Number of Series: " + items.count().toString()
        binding.itemsText.text = "Total Number of Books: $totalBook"
        binding.collectedSeriesText?.text = "Total Number Series or Standalone books Collected: $completedCount"
        binding.ongoingSeriesText.text = "Total Number of Ongoing Series: $updateToDate"
        binding.collectingText.text = "Total Number of Series or Books Currently Collecting: $collecting"



        setReadPercentChart(readCount, totalBook)

        val colors = ColorTemplate.MATERIAL_COLORS.toList()
        setBookTypeBarChart(items, setup, colors)
        setupGenreBarChart(items, setup)
        setPublisherBarChart(items, setup)
        setBookFormatPieChart(items)
    }

    private fun setReadPercentChart(readCount: Int, totalBook: Int) {
        val readPercent = ((readCount.toDouble() / totalBook.toDouble()) * 100).toInt()
        val readPercentPieEntries = ArrayList<PieEntry<*>>()
        readPercentPieEntries.add(PieEntry(readPercent.toFloat()))
        readPercentPieEntries.add(PieEntry(100 - readPercent.toFloat()))

        val readPercentPieDataSet = PieDataSet(readPercentPieEntries, "Read Percent")
        readPercentPieDataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()

        val readPercentPieData = PieData(readPercentPieDataSet)
        val bookReadProgressChart = binding.readProgressChart
        if (readPercentPieEntries.isEmpty()) {
            bookReadProgressChart.noDataText = "No data to display"
            bookReadProgressChart.data = null
            bookReadProgressChart.noDataTextColor = Color.BLACK
            bookReadProgressChart.centerTextSize = 20f
        }
        bookReadProgressChart.description.isEnabled = false
        bookReadProgressChart.data = readPercentPieData
        bookReadProgressChart.centerText = "Read Percent: $readPercent%"
        bookReadProgressChart.legend.isEnabled = false
        bookReadProgressChart.holeColor = Color.TRANSPARENT
        bookReadProgressChart.transparentCircleColor = Color.TRANSPARENT
        bookReadProgressChart.setBackgroundColor(Color.TRANSPARENT)
        bookReadProgressChart.renderer = SafePieChartRenderer(
            bookReadProgressChart,
            bookReadProgressChart.animator,
            bookReadProgressChart.viewPortHandler
        )

        bookReadProgressChart.animateXY(1000, 1000)
        bookReadProgressChart.invalidate()
    }

    private fun setBookTypeBarChart(items: List<Book>, setup: BookSetup, colors: List<Int>) {
        val dataSets = ArrayList<IBarDataSet<*>>()
        val typeCounts = setup.Type

        var index = 1
        typeCounts.forEach { (key, value) ->
            if (BookType.entries[key] != BookType.NoneSelected) {
                val bookType = BookType.entries[key]
                val books = items.filter { it.Type == bookType }
                var count = 0
                books.forEach {
                    if (it.Items != null) {
                        count += it.Items.count()
                    }
                }
                val set = BarDataSet(listOf(BarEntry(index.toFloat(), count.toFloat())), value)
                set.color = colors[index % colors.size]
                dataSets.add(set)
                index++
            }
        }

        val barData = BarData(dataSets)

        binding.bookTypeChart.let { chart ->
            chart.noDataText = "No data to display"

            if (dataSets.isEmpty()) {
                chart.data = null
                chart.noDataTextColor = Color.BLACK
            } else {
                chart.data = barData
                chart.description.isEnabled = false
                chart.xAxis.isEnabled = false

                val legend = chart.legend
                legend.isEnabled = true
                legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                legend.orientation = Legend.LegendOrientation.HORIZONTAL
                legend.isDrawInsideEnabled = false
                legend.form = Legend.LegendForm.SQUARE

                chart.animateY(1000)
            }
            chart.invalidate()
        }
    }

    private fun setupGenreBarChart(items: List<Book>, setup: BookSetup) {
        val genreList = setup.Genre
        val genreInformationMap = mutableMapOf<String, Int>()
        genreList.forEach {  genre ->
            var totalBooksPerGenre = 0
            if(!items.isEmpty()) {
                totalBooksPerGenre += items.count { it.Genre.contains(genre.genreId) }
                genreInformationMap[genre.genreName] = totalBooksPerGenre
            }
        }

        val dualColumnViewOne = binding.displayGenreCard.dualCardColumnOne
        val dualColumnViewTwo = binding.displayGenreCard.dualCardColumnTwo
        val title = binding.displayGenreCard.cardTitle
        val emptyState = binding.displayGenreCard.emptyStateContainer
        title.text = getString(R.string.total_number_of_books_per_genre)

        if(genreInformationMap.isEmpty())
        {
            dualColumnViewOne.visibility = View.GONE
            dualColumnViewTwo.visibility = View.GONE
            emptyState.root.visibility = View.VISIBLE
            emptyState.root.text = getString(R.string.no_genre_data_to_display)
        }
        else {

            dualColumnViewOne.visibility = View.VISIBLE
            dualColumnViewTwo.visibility = View.VISIBLE
            emptyState.root.visibility = View.GONE

            val genreInformationSortedMap = genreInformationMap.toList()
                .sortedByDescending { (_, value) -> value } // Sort list by the value
                .toMap()

            val halfSize = (genreInformationSortedMap.size + 1) / 2
            val chunks = genreInformationSortedMap.entries.chunked(halfSize)

            val firstHalf = chunks.getOrNull(0)?.associate { it.key to it.value } ?: emptyMap()
            val secondHalf = chunks.getOrNull(1)?.associate { it.key to it.value } ?: emptyMap()

            dualColumnViewOne.removeAllViews()
            dualColumnViewTwo.removeAllViews()

            for ((key, value) in firstHalf) {
                val textView = TextView(context)
                textView.text = getString(R.string.dual_card_text, key, value)
                textView.setPadding(8, 8, 8, 8)
                dualColumnViewOne.addView(textView)
            }
            for ((key, value) in secondHalf) {
                val textView = TextView(context)
                textView.text = getString(R.string.dual_card_text, key, value)
                textView.setPadding(8, 8, 8, 8)
                dualColumnViewTwo.addView(textView)
            }
        }
    }

    private fun setPublisherBarChart(items: List<Book>, setup: BookSetup) {
        val publishers = setup.Publishers
        val publisherInformationMap = mutableMapOf<String, Int>()

        publishers.forEach {  publisher ->
            val items = items.filter { it.Publisher == publisher.Id }
            var totalBooksPerPublisher = 0
            if(!items.isEmpty()) {
                for (book in items) {
                    totalBooksPerPublisher += book.Items?.count() ?: 0
                }
                publisherInformationMap[publisher.Name] = totalBooksPerPublisher
            }
        }

        val dualColumnViewOne = binding.displayPublisherCard.dualCardColumnOne
        val dualColumnViewTwo = binding.displayPublisherCard.dualCardColumnTwo
        val title = binding.displayPublisherCard.cardTitle
        val emptyState = binding.displayPublisherCard.emptyStateContainer
        title.text = getString(R.string.total_number_of_books_per_publisher)

        if(publisherInformationMap.isEmpty())
        {
            dualColumnViewOne.visibility = View.GONE
            dualColumnViewTwo.visibility = View.GONE
            emptyState.root.visibility = View.VISIBLE
            emptyState.root.text = getString(R.string.no_publisher_data_to_display)
        }
        else {

            dualColumnViewOne.visibility = View.VISIBLE
            dualColumnViewTwo.visibility = View.VISIBLE
            emptyState.root.visibility = View.GONE

            val publisherInformationSortedMap = publisherInformationMap.toList()
                .sortedByDescending { (_, value) -> value } // Sort list by the value
                .toMap()

            val halfSize = (publisherInformationSortedMap.size + 1) / 2
            val chunks = publisherInformationSortedMap.entries.chunked(halfSize)

            val firstHalf = chunks.getOrNull(0)?.associate { it.key to it.value } ?: emptyMap()
            val secondHalf = chunks.getOrNull(1)?.associate { it.key to it.value } ?: emptyMap()

            dualColumnViewOne.removeAllViews()
            dualColumnViewTwo.removeAllViews()

            for ((key, value) in firstHalf) {
                val textView = TextView(context)
                textView.text = getString(R.string.dual_card_text, key, value)
                textView.setPadding(8, 8, 8, 8)
                dualColumnViewOne.addView(textView)
            }
            for ((key, value) in secondHalf) {
                val textView = TextView(context)
                textView.text = getString(R.string.dual_card_text, key, value)
                textView.setPadding(8, 8, 8, 8)
                dualColumnViewTwo.addView(textView)
            }
        }
    }

    private fun setBookFormatPieChart(items: List<Book>) {
        val formatPieEntries = ArrayList<PieEntry<*>>()

        var ebookCount = 0
        var hardCoverCount = 0
        var paperBackCount = 0

        for (book in items) {
            if (book.Items != null) {
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

        if (ebookCount > 0) formatPieEntries.add(PieEntry(ebookCount.toFloat(), "E-Books"))
        if (hardCoverCount > 0) formatPieEntries.add(PieEntry(hardCoverCount.toFloat(), "Hardcover"))
        if (paperBackCount > 0) formatPieEntries.add(PieEntry(paperBackCount.toFloat(), "Paperback"))

        val bookFormatPieChart = binding.formatChart

        if (formatPieEntries.isEmpty()) {
            bookFormatPieChart.data = null
            bookFormatPieChart.noDataText = "No Format data to display"
            bookFormatPieChart.noDataTextColor = Color.BLACK
            bookFormatPieChart.centerTextSize = 20f
        } else {
            val formatPieDataSet = PieDataSet(formatPieEntries, "Format %")
            formatPieDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

            val formatPieData = PieData(formatPieDataSet)
            bookFormatPieChart.data = formatPieData

            bookFormatPieChart.description.isEnabled = false
            bookFormatPieChart.description.text = ""
            bookFormatPieChart.holeColor = Color.TRANSPARENT
            bookFormatPieChart.transparentCircleColor = Color.TRANSPARENT
            bookFormatPieChart.setBackgroundColor(Color.TRANSPARENT)
            bookFormatPieChart.isUsePercentValuesEnabled = true
            bookFormatPieChart.centerText = "Format %"
            bookFormatPieChart.legend.isEnabled = false

            bookFormatPieChart.animateXY(1000, 1000)
            bookFormatPieChart.renderer = SafePieChartRenderer(
                bookFormatPieChart,
                bookFormatPieChart.animator,
                bookFormatPieChart.viewPortHandler
            )
        }
        bookFormatPieChart.invalidate()
    }

    //endregion
}
