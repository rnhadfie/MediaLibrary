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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.medialibrary.BaseFragment
import com.example.medialibrary.R
import com.example.medialibrary.book.ui.utils.SharedUtils
import com.example.medialibrary.book.ui.utils.SortFilterViewmodel
import com.example.medialibrary.databinding.BookBottomSheetBinding
import com.example.medialibrary.databinding.BookFragmentDisplayBinding
import com.example.medialibrary.databinding.DialogSortContentBinding
import com.example.medialibrary.databinding.ViewTextCardBinding
import com.example.medialibrary.utils.DualColumnCardHelper
import com.example.medialibrary.utils.FilterSummaryHelper
import com.example.medialibrary.utils.FragmentType
import com.example.medialibrary.utils.SafePieChartRenderer
import com.example.medialibrary.utils.TextCardHelper
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
import models.book.Enums
import models.book.Enums.BookFormat
import models.book.Enums.BookType
import models.shared.Enums.MediaType
import repository.database.MediaLibraryDbHelper
import java.text.NumberFormat

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

        sheetBinding.applyFilterBtn.setOnClickListener {
            filter.Read = sheetBinding.read.triStateButton.tag as? Boolean
            filter.Reading = sheetBinding.reading.triStateButton.tag as? Boolean
            filter.AnyOwned = sheetBinding.anyItemsOwned.triStateButton.tag as? Boolean
            filter.Ongoing = sheetBinding.standaloneOrSeriesComplete.triStateButton.tag as? Boolean
            filter.Collecting = sheetBinding.collecting.triStateButton.tag as? Boolean
            filter.Collected = sheetBinding.collected.triStateButton.tag as? Boolean

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

    private fun setupActionBindings() {
        setupEmptyStateMediaItemObserver(
            viewModel.mediaItems,
            binding.scrollView,
            ContextCompat.getColor(requireContext(), R.color.section_book),
            binding.emptyStateContainer,
            binding.emptyStateLayout,
            R.string.no_books_found
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
    }

    @SuppressLint("SetTextI18n")
    private fun setupCharts(items: List<Book>, setup: BookSetup) {
        var readCount = 0
        var totalBook = 0

        for (book in items) {
            readCount += book.Items?.count { it.Read && it.Owned } ?: 0
            totalBook += book.Items?.count() { it.Owned } ?: 0
        }

        val completedCount = items.count { it.HasCollectedAllItems == true }
        val updateToDate = items.count { it.Ongoing }
        val collecting = items.count { it.Collecting }

        binding.seriesCountText.text = "Total Number of Series: ${items.count()}"
        binding.itemsText.text = "Total Number of Books: $totalBook"
        binding.collectedSeriesText?.text = "Total Number Series or Standalone books Collected: $completedCount"
        binding.ongoingSeriesText.text = "Total Number of Ongoing Series: $updateToDate"
        binding.collectingText.text = "Total Number of Series or Books Currently Collecting: $collecting"

        setReadPercentChart(readCount, totalBook)

        val ownedItems = items.filter { it.CurrentOwnAny }

        val colors = ColorTemplate.MATERIAL_COLORS.toList()
        setBookTypeBarChart(ownedItems, setup, colors)
        setupGenreBarChart(ownedItems, setup)
        setPublisherBarChart(ownedItems, setup)
        setBookFormatPieChart(ownedItems)
        setDemographicsPieChart(ownedItems)
    }

    private fun setReadPercentChart(readCount: Int, totalBook: Int) {
        val readPercent = if (totalBook > 0) ((readCount.toDouble() / totalBook.toDouble()) * 100).toInt() else 0
        val readPercentPieEntries = ArrayList<PieEntry<*>>()
        readPercentPieEntries.add(PieEntry(readPercent.toFloat()))
        readPercentPieEntries.add(PieEntry((100 - readPercent).toFloat()))

        val readPercentPieDataSet = PieDataSet(readPercentPieEntries, "Read Percent")
        readPercentPieDataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()

        val readPercentPieData = PieData(readPercentPieDataSet)
        binding.readProgressChart.let { chart ->
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
        var genreInformationMap = mutableMapOf<String, String>()
        val genreExtraInformationMap = mutableMapOf<String, Int>()
        genreList.forEach { genre ->
            var totalBooksPerGenre = 0
            if (items.isNotEmpty()) {
                totalBooksPerGenre += items.count { it.Genre.contains(genre.genreId) }
                genreExtraInformationMap[genre.genreName] = totalBooksPerGenre
                genreInformationMap[genre.genreName] = NumberFormat.getPercentInstance().format(totalBooksPerGenre.toDouble() / items.size)
            }
        }

        genreInformationMap = genreInformationMap.entries
            .sortedByDescending  {
                it.value.removeSuffix("%").toIntOrNull() }
            .associate { it.toPair() } as MutableMap<String, String>
        genreInformationMap.filter { it.value == "0%" }.forEach { genreInformationMap.remove(it.key) }


        val card: ViewTextCardBinding = binding.displayGenreCard
        val title = binding.displayGenreCard.cardTitle
        title.setText(R.string.total_number_of_books_per_genre)
        val emptyState = binding.displayGenreCard.emptyStateContainer


        TextCardHelper.setupTextCard(
            genreInformationMap,
            genreExtraInformationMap,
            card,
            emptyState,
            R.string.no_genre_data_to_display,
            -1,
            R.string.percent,
            R.string.number,
            models.shared.Enums.MediaType.Book,
            layoutInflater,
            requireContext()
        )
    }


    private fun setPublisherBarChart(items: List<Book>, setup: BookSetup) {
        val publishers = setup.Publishers

        var publisherInformationMap = mutableMapOf<String, String>()
        val publisherExtraInformationMap = mutableMapOf<String, Int>()
        publishers.forEach { publisher ->
            var totalBooksPerGenre = 0
            if (items.isNotEmpty()) {
                totalBooksPerGenre += items.count { it.Publisher == publisher.Id }
                publisherExtraInformationMap[publisher.Name] = totalBooksPerGenre
                publisherInformationMap[publisher.Name] = NumberFormat.getPercentInstance().format(totalBooksPerGenre.toDouble() / items.size)
            }
        }

        publisherInformationMap = publisherInformationMap.entries
            .sortedByDescending  { it.value.removeSuffix("%").toIntOrNull() }
            .associate { it.toPair() } as MutableMap<String, String>
        publisherInformationMap.filter { it.value == "0%" }.forEach { publisherInformationMap.remove(it.key) }


        val card: ViewTextCardBinding = binding.displayPublisherCard
        val title = binding.displayPublisherCard.cardTitle
        title.text = getString(R.string.total_number_of_books_per_publisher)
        val emptyState = binding.displayGenreCard.emptyStateContainer


        TextCardHelper.setupTextCard(
            publisherInformationMap,
            publisherExtraInformationMap,
            card,
            emptyState,
            R.string.no_publisher_data_to_display,
            -1,
            R.string.percent,
            R.string.number,
            models.shared.Enums.MediaType.Book,
            layoutInflater,
            requireContext()
        )




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

        binding.formatChart.let { chart ->
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

    private fun setDemographicsPieChart(items: List<Book>) {
        val demoPieEntries = ArrayList<PieEntry<*>>()

        val targetBooks = items.filter { it.Type == BookType.Manga || it.Type == BookType.LightNovel }

        val demoCounts = mutableMapOf<Enums.Demographics, Int>()

        for (book in targetBooks) {
            val demo = book.Demographics ?: Enums.Demographics.NotApplicable
            if (demo != Enums.Demographics.NotApplicable) {
                demoCounts[demo] = (demoCounts[demo] ?: 0) + 1
            }
        }

        demoCounts.forEach { (demo, count) ->
            if (count > 0) {
                val name = when (demo) {
                    Enums.Demographics.NotApplicable -> "Not Applicable"
                    Enums.Demographics.Shounen -> "Shounen"
                    Enums.Demographics.Shoujo -> "Shoujo"
                    Enums.Demographics.Josei -> "Josei"
                    Enums.Demographics.Seinen -> "Seinen"
                    Enums.Demographics.Kids -> "Kids"
                }
                demoPieEntries.add(PieEntry(count.toFloat(), name))
            }
        }

        binding.demographicsChart.let { chart ->
            if (demoPieEntries.isEmpty()) {
                chart.data = null
                chart.noDataText = "No Demographics data to display\n(Manga & Light Novel only)"
                chart.noDataTextColor = Color.BLACK
                chart.centerTextSize = 16f
            } else {
                val demoPieDataSet = PieDataSet(demoPieEntries, "Demographics %")
                demoPieDataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()

                val demoPieData = PieData(demoPieDataSet)
                chart.data = demoPieData

                chart.description.isEnabled = false
                chart.holeColor = Color.TRANSPARENT
                chart.transparentCircleColor = Color.TRANSPARENT
                chart.setBackgroundColor(Color.TRANSPARENT)
                chart.isUsePercentValuesEnabled = true
                chart.centerText = "Demographics %\n(Manga & Light Novel)"
                chart.centerTextSize = 14f
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
}
