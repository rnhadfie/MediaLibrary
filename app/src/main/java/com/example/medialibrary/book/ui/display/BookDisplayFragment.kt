package com.example.medialibrary.book.ui.display

import android.annotation.SuppressLint
import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.R
import com.example.medialibrary.book.ui.utils.*
import com.example.medialibrary.databinding.BookBottomSheetBinding
import com.example.medialibrary.databinding.BookFragmentDisplayBinding
import com.example.medialibrary.databinding.DialogSortContentBinding
import com.example.medialibrary.databinding.ViewEmptyStateBinding
import com.example.medialibrary.utils.*
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomsheet.BottomSheetDialog
import controllers.BookController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import models.book.*
import models.book.Enums.BookFormat
import models.book.Enums.BookType
import models.shared.Enums.MediaType
import repository.database.MediaLibraryDbHelper

class BookDisplayFragment : BaseFragment<BookFragmentDisplayBinding, BookDisplayViewModel>(
    BookFragmentDisplayBinding::inflate
) {
    private var currentFilter = BookFilter()
    private var bookController: BookController? = null
    private var setup: BookSetup? = null

    //region Components
    private var emptyStateContainer: ViewEmptyStateBinding? = null
    private var filterButton: ImageButton? = null
    private var sortButton: ImageButton? = null
    private var copyListButton: ImageButton? = null
    private var readProgressChart: PieChart? = null
    private var seriesCountText: TextView? = null
    private var itemsText: TextView? = null
    private var collectedSeriesText: TextView? = null
    private var ongoingSeriesText: TextView? = null
    private var collectingText: TextView? = null
    private var formatChart: PieChart? = null
    private var bookTypeChart: BarChart? = null
    //endregion

    //region sheet Components
    private var applyFilterBtn: Button? = null
    private var clearActiveFilter: Button? = null
    private var readToggle: Button? = null
    private var readingToggle: Button? = null
    private var ownedToggle: Button? = null
    private var ongoingToggle: Button? = null
    private var collectedToggle: Button? = null
    private var collectingToggle: Button? = null
    //endregion

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
        setComponentBindings()
        setupActionBindings()

        val dbHelper = MediaLibraryDbHelper(requireContext())
        bookController = BookController(dbHelper)

        setDialogSort(DialogSortContentBinding.inflate(layoutInflater))
        observeSortFilterViewModel()

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
            setup,
            FragmentType.Display
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
        setComponentSheetBindings(sheetBinding)

        applyFilterBtn?.setOnClickListener {
            filter.Read = readToggle?.tag as Boolean?
            filter.Reading = readingToggle?.tag as Boolean?
            filter.AnyOwned = ownedToggle?.tag as Boolean?
            filter.Ongoing = ongoingToggle?.tag as Boolean?
            filter.Collecting = collectingToggle?.tag as Boolean?
            filter.Collected = collectedToggle?.tag as Boolean?

            sortFilterViewModel.updateBookFilter(filter)
            loadData()
            dialog.dismiss()
        }

        clearActiveFilter?.setOnClickListener {
            sortFilterViewModel.updateBookFilter(BookFilter())
            dialog.dismiss()
        }

        dialog.show()
    }

    //region Setup

    private fun setupActionBindings()
    {

        setupEmptyStateMediaItemObserver(
            viewModel.mediaItems,
            binding.scrollView,
            ContextCompat.getColor(requireContext(), R.color.section_book),
            emptyStateContainer!!,
            binding.emptyStateLayout,
            R.string.no_books_found
        )

        filterButton?.setOnClickListener {
            setup?.let { s -> showFilterSheet(s, currentFilter) }
        }

        sortButton?.setOnClickListener {
            val filter = sortFilterViewModel.getOrCreateBookFilter()
            val sortModel = sortFilterViewModel.getOrCreateSortModel()
            SharedUtils.showSortDialog(requireContext(), filter, isMain = false, sortModel = sortModel) { updatedFilter ->
                sortFilterViewModel.updateBookFilter(updatedFilter as BookFilter)
                loadData()
            }
        }

        copyListButton?.setOnClickListener {
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
    }

    private fun setComponentBindings()
    {
        emptyStateContainer = binding.emptyStateContainer
        filterButton = binding.filterBtn
        sortButton = binding.sortBtn
        copyListButton = binding.copyListBtn
        readProgressChart = binding.readProgressChart
        seriesCountText = binding.seriesCountText
        itemsText = binding.itemsText
        collectedSeriesText = binding.collectedSeriesText
        ongoingSeriesText = binding.ongoingSeriesText
        collectingText = binding.collectingText
        formatChart = binding.formatChart
        bookTypeChart = binding.bookTypeChart

    }

    private fun setComponentSheetBindings(sb: BookBottomSheetBinding)
    {
        readToggle = sb.read.triStateButton
        readingToggle = sb.reading.triStateButton
        ownedToggle = sb.anyItemsOwned.triStateButton
        ongoingToggle = sb.standaloneOrSeriesComplete.triStateButton
        collectedToggle = sb.collected.triStateButton
        collectingToggle = sb.collecting.triStateButton
        applyFilterBtn = sb.applyFilterBtn
        clearActiveFilter = sb.clearActiveFilter

    }

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

        seriesCountText?.text = "Total Number of Series: " + items.count().toString()
        itemsText?.text = "Total Number of Books: $totalBook"
        collectedSeriesText?.text = "Total Number Series or Standalone books Collected: $completedCount"
        ongoingSeriesText?.text = "Total Number of Ongoing Series: $updateToDate"
        collectingText?.text = "Total Number of Series or Books Currently Collecting: $collecting"

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
        readProgressChart!!.let { chart ->
            if (readPercentPieEntries.isEmpty()) {
                chart.noDataText = "No data to display"
                chart.data = null
                chart.noDataTextColor = Color.BLACK
                chart.centerTextSize = 20f
            }
            chart.description.isEnabled = false
            chart.data = readPercentPieData
            chart.centerText = "Read Percent: $readPercent%"
            chart.legend.isEnabled = false
            chart.holeColor = Color.TRANSPARENT
            chart.transparentCircleColor = Color.TRANSPARENT
            chart.setBackgroundColor(Color.TRANSPARENT)
            chart.renderer = SafePieChartRenderer(
                chart,
                chart.animator,
                chart.viewPortHandler
            )

            chart.animateXY(1000, 1000)
            chart.invalidate()
        }
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

        bookTypeChart!!.let { chart ->
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


        val title = binding.displayGenreCard.cardTitle
        title.text = getString(R.string.total_number_of_books_per_genre)

        DualColumnCardHelper.setupDualColumnCard(
            genreInformationMap,
            binding.displayGenreCard,
            R.string.no_publisher_data_to_display,
            MediaType.Book,
            requireContext())

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

        val title = binding.displayPublisherCard.cardTitle
        title.text = getString(R.string.total_number_of_books_per_publisher)

        DualColumnCardHelper.setupDualColumnCard(
            publisherInformationMap,
            binding.displayPublisherCard,
            R.string.no_genre_data_to_display,
            MediaType.Book,
            requireContext())
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

        formatChart!!.let { chart ->
            if (formatPieEntries.isEmpty()) {
                chart.data = null
                chart.noDataText = "No Format data to display"
                chart.noDataTextColor = Color.BLACK
                chart.centerTextSize = 20f
            } else {
                val formatPieDataSet = PieDataSet(formatPieEntries, "Format %")
                formatPieDataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()

                val formatPieData = PieData(formatPieDataSet)
                chart.data = formatPieData

                chart.description.isEnabled = false
                chart.description.text = ""
                chart.holeColor = Color.TRANSPARENT
                chart.transparentCircleColor = Color.TRANSPARENT
                chart.setBackgroundColor(Color.TRANSPARENT)
                chart.isUsePercentValuesEnabled = true
                chart.centerText = "Format %"
                chart.isDrawEntryLabelsEnabled = false

                chart.legend.let { legend ->
                    legend.isEnabled = true
                    legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                    legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                    legend.orientation = Legend.LegendOrientation.HORIZONTAL
                    legend.isDrawInsideEnabled = false
                    legend.form = Legend.LegendForm.SQUARE
                }

                chart.animateXY(1000, 1000)
                chart.renderer = SafePieChartRenderer(
                    chart,
                    chart.animator,
                    chart.viewPortHandler
                )
            }
            chart.invalidate()
        }

    }

    //endregion
}
