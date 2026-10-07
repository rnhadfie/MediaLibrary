package com.example.medialibrary.book.ui.form

import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medialibrary.BaseFormFragment
import com.example.medialibrary.R
import com.example.medialibrary.databinding.BookFragmentFormBinding
import com.example.medialibrary.databinding.BookItemBottomSheetBinding
import com.example.medialibrary.databinding.BookItemVolumeBinding
import com.example.medialibrary.utils.ImageUtils
import com.example.medialibrary.utils.RadioGridUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import controllers.BookController
import models.book.Book
import models.book.BookItem
import models.book.BookSetup
import models.book.Enums
import models.book.Publisher
import models.shared.GenreObject
import models.shared.Tag
import repository.database.MediaLibraryDbHelper
import models.shared.Enums as SharedEnums

class BookFormFragment : BaseFormFragment<BookFragmentFormBinding, BookFormViewModel>(
    BookFragmentFormBinding::inflate
) {

    companion object {
        fun newInstance(id: String = "-1", isEdit: Boolean = false) = BookFormFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ID, id)
                putBoolean(ARG_IS_EDIT, isEdit)
            }
        }
    }

    private lateinit var itemAdapter: BookItemAdapter
    private var controller: BookController? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[BookFormViewModel::class.java]

        val dbHelper = MediaLibraryDbHelper(requireContext())
        controller = BookController(dbHelper)

        val setup = controller?.GetBookSetup() ?: BookSetup()

        if (isEdit && itemId != "-1") {
            viewModel.loadBook(itemId, controller, setup)
        }

        setupBookTypeRadioGroup(setup)
        setupDemographicsRadioGroup(setup)
        setupGenreSelection(setup)
        setupPublisherSelection(setup)
        setupTagSelection(setup)
        setupCollectingPriorityDropdown(binding.collectingPriorityAutocomplete) { priority ->
            viewModel.updateCollectingPriority(priority)
        }

        setupRecyclerView()
        setupInputListeners()
        setupClickListeners()
        observeViewModel(setup)
    }

    private fun setupClickListeners() {
        binding.changeImage.buttonChangeCover.setOnClickListener {
            showImageOptionsDialog("book")
        }
        binding.changeImage.buttonClearCover.setOnClickListener {
            clearImage("book")
        }
        binding.addItemBtn.setOnClickListener {
            showBookItemSheet()
        }
        binding.saveBtn.setOnClickListener {
            val success = saveAction(isEdit)
            if (success) {
                activity?.finish()
            }
        }
    }

    private fun observeViewModel(setup: BookSetup) {
        viewModel.book.observe(viewLifecycleOwner) { book ->
            observeBookData(book, setup)
        }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            itemAdapter.submitList(items.toList())
        }

        viewModel.selectedGenres.observe(viewLifecycleOwner) { genres ->
            updateGenreChips(genres)
        }
    }

    override fun onCoverImageUpdated(byteArray: ByteArray?, target: String?) {
        if (target == "book" || target == "main") {
            viewModel.updateCover(byteArray)
        }
    }

    private fun observeBookData(book: Book, setup: BookSetup) {
        binding.titleInput.setText(book.Title)
        binding.authorInput.setText(book.Author)
        binding.artistInput.setText(book.Artist)
        binding.collectingCheck.isChecked = book.Collecting ?: false
        binding.ongoingCheck.isChecked = book.Ongoing ?: false
        binding.collectedCheck.isChecked = book.HasCollectedAllItems ?: false

        val currentPriority = book.CollectingPriority ?: SharedEnums.CollectingPriority.None
        binding.collectingPriorityAutocomplete.autocomplete.setText(currentPriority.name, false)

        val bookTypeId = book.Type?.ordinal ?: 0
        if (bookTypeId != 0) {
            RadioGridUtils.setSelection(
                binding.bookTypeRadio.dynamicTableLayout,
                bookTypeId
            )
        }

        val bookType = book.Type ?: Enums.BookType.NoneSelected
        updateDemographicsVisibility(bookType)
        val demoId = book.Demographics?.ordinal ?: Enums.Demographics.NotApplicable.ordinal
        RadioGridUtils.setSelection(
            binding.demographicsRadio.dynamicTableLayout,
            demoId
        )

        if (book.Cover != null && book.Cover.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeByteArray(book.Cover, 0, book.Cover.size)
            binding.changeImage.imageBookCover.scaleType = ImageView.ScaleType.CENTER_CROP
            binding.changeImage.imageBookCover.setImageBitmap(bitmap)
            binding.changeImage.imageBookCover.imageTintList = null
            binding.changeImage.buttonClearCover.visibility = View.VISIBLE
        } else {
            ImageUtils.setPlaceholderCover(binding.changeImage.imageBookCover, requireContext(), resources)
            binding.changeImage.buttonClearCover.visibility = View.GONE
        }

        val pub = setup.Publishers.find { it.Id == book.Publisher }
        pub?.let { binding.pubAutocomplete.autocomplete.setText(it.Name, false) }

        val tag = setup.Tag.find { it.Id == book.Tag }
        tag?.let { binding.tagAutocomplete.autocomplete.setText(it.Name, false) }

        if (book.Items != null && book.Items.size == 1 && book.Items[0].VolumeNumber == "-1") {
            binding.standaloneSwitch.isChecked = true
            binding.readSwitch.isChecked = book.Items[0].Read
            binding.ownedSwitch.isChecked = book.Items[0].Owned
            binding.formatAutocomplete.autocomplete.setText(book.Items[0].Format.name, false)
            binding.standaloneSection.visibility = View.VISIBLE
            binding.seriesSection.visibility = View.GONE
        }
        else
        {
            binding.standaloneSection.visibility = View.GONE
            binding.seriesSection.visibility = View.VISIBLE
        }


    }

    private fun saveAction(isEdit: Boolean): Boolean {
        disableFields(false)
        val error = viewModel.validateFields()
        if (error.isEmpty()) {
            if (binding.standaloneSwitch.isChecked) {
                createStandAloneBookItem()
            }
            val saveObj = viewModel.getSaveObject()
            controller?.let { ctrl ->
                val result = if (isEdit) {
                    ctrl.UpdateBook(saveObj)
                } else {
                    ctrl.AddBook(saveObj)
                }

                if (result) {
                    notifyDataChanged()
                    Toast.makeText(
                        requireContext(),
                        if (isEdit) "Book Updated" else "Book Saved",
                        Toast.LENGTH_SHORT
                    ).show()
                    disableFields(true)
                    return true
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Book Failed to Save",
                        Toast.LENGTH_SHORT
                    ).show()
                    disableFields(true)
                    return false
                }
            }
        } else {
            error.forEach { (key, message) ->
                when (key) {
                    "title" -> {
                        binding.titleLabel.error = message
                        binding.titleInput.requestFocus()
                    }
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
            disableFields(true)
        }
        disableFields(true)
        return false
    }

    private fun createStandAloneBookItem() {
        val formatText = binding.formatAutocomplete.autocomplete.text.toString()
        val format = try {
            Enums.BookFormat.valueOf(formatText)
        } catch (_: Exception) {
            Enums.BookFormat.NoneSelected
        }

        val item = BookItem().apply {
            Read = binding.readSwitch.isChecked
            Owned = binding.ownedSwitch.isChecked
            VolumeNumber = "-1"
            Format = format
        }
        viewModel.clearItems()
        viewModel.addOrUpdateItem(item, 0)
    }

    private fun disableFields(enabled: Boolean) {
        binding.titleInput.isEnabled = enabled
        binding.authorInput.isEnabled = enabled
        binding.artistInput.isEnabled = enabled
        binding.bookTypeRadio.dynamicTableLayout.children.forEach { child ->
            if (child is ViewGroup) {
                child.children.forEach { subChild ->
                    if (subChild is RadioButton) {
                        subChild.isEnabled = enabled
                    }
                }
            }
        }
        binding.demographicsRadio.dynamicTableLayout.children.forEach { child ->
            if (child is ViewGroup) {
                child.children.forEach { subChild ->
                    if (subChild is RadioButton) {
                        subChild.isEnabled = enabled
                    }
                }
            }
        }
        binding.changeImage.buttonChangeCover.isEnabled = enabled
        binding.changeImage.buttonClearCover.isEnabled = enabled
        binding.changeImage.imageBookCover.isEnabled = enabled
        binding.genreMultiselect.buttonSelectGenres.isEnabled = enabled
        binding.pubAutocomplete.autoCompleteLabel.isEnabled = enabled
        binding.pubAutocomplete.autocomplete.isEnabled = enabled
        binding.tagAutocomplete.autoCompleteLabel.isEnabled = enabled
        binding.tagAutocomplete.autocomplete.isEnabled = enabled
        binding.collectingPriorityAutocomplete.autoCompleteLabel.isEnabled = enabled
        binding.collectingPriorityAutocomplete.autocomplete.isEnabled = enabled
        binding.addItemBtn.isEnabled = enabled
        binding.collectingCheck.isEnabled = enabled
        binding.ongoingCheck.isEnabled = enabled
        binding.collectedCheck.isEnabled = enabled
        binding.saveBtn.isEnabled = enabled
        binding.standaloneSwitch.isEnabled = enabled
        binding.formatAutocomplete.autocomplete.isEnabled = enabled
        binding.readSwitch.isEnabled = enabled
        binding.ownedSwitch.isEnabled = enabled
    }

    //region setup

    private fun setupBookTypeRadioGroup(setup: BookSetup) {
        binding.bookTypeRadio.radioButtonLabel.setText(R.string.book_type_label)
        val tableLayout = binding.bookTypeRadio.dynamicTableLayout
        val type = setup.Type.filter { it.key != Enums.BookType.NoneSelected.ordinal }
        RadioGridUtils.populateRadioGridFromMap(
            tableLayout = tableLayout,
            optionsMap = type,
            columnCount = 2
        ) { selectedId ->
            handleRadioSelectionChange(selectedId)
        }
    }

    private fun handleRadioSelectionChange(id: Int) {
        val bookType = Enums.BookType.entries.find { it.ordinal == id } ?: return
        viewModel.updateBookType(bookType)
        updateDemographicsVisibility(bookType)
    }

    private fun setupDemographicsRadioGroup(setup: BookSetup) {
        binding.demographicsRadio.radioButtonLabel.setText(R.string.demographics_label)
        val tableLayout = binding.demographicsRadio.dynamicTableLayout
        RadioGridUtils.populateRadioGridFromMap(
            tableLayout = tableLayout,
            optionsMap = setup.Demographics,
            columnCount = 2,
            selectedId = Enums.Demographics.NotApplicable.ordinal
        ) { selectedId ->
            handleDemographicsSelectionChange(selectedId)
        }
    }

    private fun handleDemographicsSelectionChange(id: Int) {
        val demo = Enums.Demographics.entries.find { it.ordinal == id } ?: Enums.Demographics.NotApplicable
        viewModel.updateDemographics(demo)
    }

    private fun updateDemographicsVisibility(bookType: Enums.BookType) {
        if (bookType == Enums.BookType.Manga || bookType == Enums.BookType.LightNovel) {
            binding.demographicsRadio.root.visibility = View.VISIBLE
        } else {
            binding.demographicsRadio.root.visibility = View.GONE
            viewModel.updateDemographics(Enums.Demographics.NotApplicable)
            RadioGridUtils.setSelection(
                binding.demographicsRadio.dynamicTableLayout,
                Enums.Demographics.NotApplicable.ordinal
            )
        }
    }

    private fun setupGenreSelection(setup: BookSetup) {
        binding.genreMultiselect.buttonSelectGenres.setOnClickListener {
            val genres = setup.Genre
            val genreNames = genres.map { it.genreName }.toTypedArray()
            val selected = viewModel.selectedGenres.value ?: mutableSetOf()
            val checkedItems = genres.map { selected.contains(it) }.toBooleanArray()

            AlertDialog.Builder(requireContext())
                .setTitle("Select Genres")
                .setMultiChoiceItems(genreNames, checkedItems) { _, which, _ ->
                    val selectedText = genreNames[which]
                    val selectedGenre = genres.find { it.genreName == selectedText }
                    if (selectedGenre != null) {
                        viewModel.toggleGenre(selectedGenre)
                    }
                }
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun setupTagSelection(setup: BookSetup) {
        val tags = setup.Tag
        if (tags.isEmpty() || tags[0].Id != "") {
            tags.add(0, Tag("", ""))
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tags)
        val tagAutoComplete = binding.tagAutocomplete.autocomplete

        tagAutoComplete.setAdapter(adapter)
        binding.tagAutocomplete.autoCompleteLabel.setHint(R.string.tag)
        tagAutoComplete.setOnItemClickListener { _, _, position, _ ->
            val selectedTag = adapter.getItem(position)
            selectedTag?.let { viewModel.updateTag(it) }
        }

        tagAutoComplete.doAfterTextChanged { text ->
            val str = text.toString()
            val existing = tags.find { it.Name == str }
            if (existing == null) {
                viewModel.updateTagName(str)
            } else {
                viewModel.updateTag(existing)
            }
        }
    }

    private fun setupPublisherSelection(setup: BookSetup) {
        val publishers = setup.Publishers
        if (publishers.isEmpty() || publishers[0].Id != "") {
            publishers.add(0, Publisher("", ""))
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, publishers)
        val publisherAutoComplete = binding.pubAutocomplete.autocomplete

        publisherAutoComplete.setAdapter(adapter)
        binding.pubAutocomplete.autoCompleteLabel.setHint(R.string.publisher)
        publisherAutoComplete.setOnItemClickListener { _, _, position, _ ->
            val selectedPublisher = adapter.getItem(position)
            selectedPublisher?.let { viewModel.updatePublisher(it) }
        }

        publisherAutoComplete.doAfterTextChanged { text ->
            val str = text.toString()
            val existing = publishers.find { it.Name == str }
            if (existing == null) {
                viewModel.updatePublisherName(str)
            } else {
                viewModel.updatePublisher(existing)
            }
        }
    }

    private fun updateGenreChips(genres: Set<GenreObject>) {
        binding.genreMultiselect.chipGroupGenres.removeAllViews()
        genres.forEach { genre ->
            val chip = Chip(requireContext()).apply {
                text = genre.genreName
                isCloseIconVisible = true
                setOnCloseIconClickListener { viewModel.toggleGenre(genre) }
            }
            binding.genreMultiselect.chipGroupGenres.addView(chip)
        }
    }

    private fun setupRecyclerView() {
        itemAdapter = BookItemAdapter(
            onEdit = { item, pos -> showBookItemSheet(item, pos) },
            onDelete = { pos -> viewModel.deleteItem(pos) }
        )
        binding.recyclerBookItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }
    }

    private fun setupInputListeners() {
        binding.titleInput.doAfterTextChanged { s ->
            viewModel.updateTitle(s.toString())
            binding.titleLabel.error = null
        }
        binding.authorInput.doAfterTextChanged { s ->
            viewModel.updateAuthor(s.toString())
        }
        binding.artistInput.doAfterTextChanged { s ->
            viewModel.updateArtist(s.toString())
        }
        binding.collectingCheck.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleCollecting(isChecked)
        }
        binding.ongoingCheck.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleCompleted(isChecked)
        }
        binding.collectedCheck.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleCollectionComplete(isChecked)
        }

        binding.standaloneSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.standaloneSection.visibility = View.VISIBLE
                binding.seriesSection.visibility = View.GONE
            } else {
                binding.standaloneSection.visibility = View.GONE
                binding.seriesSection.visibility = View.VISIBLE
            }
        }

        val formats = Enums.BookFormat.entries.filter { it != Enums.BookFormat.NoneSelected }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            formats.map { it.name }
        )
        binding.formatAutocomplete.autocomplete.setAdapter(adapter)
        binding.formatAutocomplete.autoCompleteLabel.setHint(R.string.format_label)
    }

    //endregion

    //region item sheet

    private fun showBookItemSheet(item: BookItem? = null, position: Int = -1) {
        val dialog = BottomSheetDialog(requireContext())
        dialog.setCancelable(false)
        val sb = BookItemBottomSheetBinding.inflate(layoutInflater)
        currentSheetBinding = sb
        pendingItemPosition = position
        dialog.setContentView(sb.root)

        sb.labelText.text = if (item == null) "Add Volume" else "Edit Volume"
        setupItemSheetBindings(sb, dialog, item, position)
        dialog.show()
    }

    private fun setupItemSheetBindings(
        sb: BookItemBottomSheetBinding,
        dialog: BottomSheetDialog,
        item: BookItem?,
        position: Int
    ) {
        sb.cancelButton.setOnClickListener { dialog.dismiss() }

        val formats = Enums.BookFormat.entries.filter { it != Enums.BookFormat.NoneSelected }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            formats.map { it.name }
        )
        sb.formatAutocomplete.autocomplete.setAdapter(adapter)
        sb.formatAutocomplete.autoCompleteLabel.setHint(R.string.format_label)
        sb.numberInput.keyListener = DigitsKeyListener.getInstance("0123456789,.- ")

        item?.let {
            sb.titleInput.setText(it.VolumeTitle)
            sb.numberInput.setText(it.VolumeNumber)
            sb.ownedSwitch.isChecked = it.Owned
            sb.readSwitch.isChecked = it.Read
            sb.formatAutocomplete.autocomplete.setText(it.Format.name, false)
            if (it.ItemCover != null && it.ItemCover.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(it.ItemCover, 0, it.ItemCover.size)
                sb.imageItemCover.imageBookCover.setImageBitmap(bitmap)
                sb.imageItemCover.imageBookCover.imageTintList = null
                sb.imageItemCover.imageBookCover.tag = it.ItemCover
                sb.imageItemCover.buttonClearCover.visibility = View.VISIBLE
            } else {
                ImageUtils.setPlaceholderCover(sb.imageItemCover.imageBookCover, requireContext(), resources)
                sb.imageItemCover.imageBookCover.tag = null
                sb.imageItemCover.buttonClearCover.visibility = View.GONE
            }
        } ?: run {
            ImageUtils.setPlaceholderCover(sb.imageItemCover.imageBookCover, requireContext(), resources)
            sb.imageItemCover.imageBookCover.tag = null
            sb.imageItemCover.buttonClearCover.visibility = View.GONE
        }

        sb.imageItemCover.buttonChangeCover.setOnClickListener {
            showImageOptionsDialog("item")
        }
        sb.imageItemCover.buttonClearCover.setOnClickListener {
            clearImage("item")
        }

        sb.saveItemBtn.setOnClickListener {
            addBookItem(sb, position, dialog)
        }
    }

    private fun addBookItem(sb: BookItemBottomSheetBinding, position: Int, dialog: BottomSheetDialog) {
        var volNum = sb.numberInput.text.toString()
        if (binding.standaloneSwitch.isChecked) {
            volNum = "-1"
        }

        if (volNum.isBlank() && !binding.standaloneSwitch.isChecked) {
            sb.numberLabel.error = "Volume number is required"
            Toast.makeText(requireContext(), "Volume number is required", Toast.LENGTH_SHORT).show()
            return
        }

        val formatText = sb.formatAutocomplete.autocomplete.text.toString()
        val format = try {
            Enums.BookFormat.valueOf(formatText)
        } catch (_: Exception) {
            Enums.BookFormat.NoneSelected
        }

        val newItem = BookItem().apply {
            VolumeNumber = volNum
            VolumeTitle = sb.titleInput.text.toString()
            Owned = sb.ownedSwitch.isChecked
            Read = sb.readSwitch.isChecked
            Format = format
            ItemCover = sb.imageItemCover.imageBookCover.tag as? ByteArray
        }

        viewModel.addOrUpdateItem(newItem, position)
        dialog.dismiss()
    }

    //endregion

    inner class BookItemAdapter(
        private val onEdit: (BookItem, Int) -> Unit,
        private val onDelete: (Int) -> Unit
    ) : RecyclerView.Adapter<BookItemAdapter.ViewHolder>() {

        private var items: List<BookItem> = emptyList()

        fun submitList(newList: List<BookItem>) {
            items = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = BookItemVolumeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position], position)
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(val binding: BookItemVolumeBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: BookItem, position: Int) {
                val context = binding.root.context

                if (item.VolumeNumber == "-1") {
                    binding.textVolumeInfo.text = context.getString(R.string.standalone)
                } else {
                    binding.textVolumeInfo.text = context.getString(
                        R.string.volume_info_format,
                        item.VolumeNumber,
                        item.VolumeTitle ?: ""
                    )
                }

                binding.textStatusInfo.text = context.getString(
                    R.string.volume_status_format,
                    if (item.Owned) "Yes" else "No",
                    if (item.Read) "Yes" else "No"
                )

                if (item.ItemCover != null && item.ItemCover.isNotEmpty()) {
                    val bitmap = BitmapFactory.decodeByteArray(item.ItemCover, 0, item.ItemCover.size)
                    binding.imageItemCover.scaleType = ImageView.ScaleType.CENTER_CROP
                    binding.imageItemCover.setImageBitmap(bitmap)
                    binding.imageItemCover.imageTintList = null
                } else {
                    ImageUtils.setPlaceholderCover(binding.imageItemCover, context, resources)
                }

                binding.buttonEditItem.setOnClickListener { onEdit(item, position) }
                binding.buttonDeleteItem.setOnClickListener { onDelete(position) }
            }
        }
    }
}
