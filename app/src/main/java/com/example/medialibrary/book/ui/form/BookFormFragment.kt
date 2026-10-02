package com.example.medialibrary.book.ui.form

import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medialibrary.BaseFormFragment
import com.example.medialibrary.R
import com.example.medialibrary.databinding.BookFragmentFormBinding
import com.example.medialibrary.databinding.BookItemBottomSheetBinding
import com.example.medialibrary.databinding.BookItemVolumeBinding
import com.example.medialibrary.databinding.ViewAutocompleteDropdownBinding
import com.example.medialibrary.databinding.ViewChangeImageBinding
import com.example.medialibrary.databinding.ViewDropdownBinding
import com.example.medialibrary.databinding.ViewGenreMultiselectBinding
import com.example.medialibrary.databinding.ViewRadioButtonBinding
import com.example.medialibrary.utils.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import controllers.BookController
import models.book.*
import models.shared.*

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

    //region component bindings

    private var titleInput: TextView? = null
    private var authorInput: TextView? = null
    private var artistInput: TextView? = null
    private var collectingCheck: CheckBox? = null
    private var ongoingCheck: CheckBox? = null
    private var collectedCheck: CheckBox? = null
    private var priorityAutocomplete: ViewDropdownBinding? = null
    private var genreMultiselect: ViewGenreMultiselectBinding? = null
    private var pubAutocomplete: ViewAutocompleteDropdownBinding? = null
    private var tagAutocomplete: ViewAutocompleteDropdownBinding? = null
    private var bookTypeRadio: ViewRadioButtonBinding? = null
    private var addItemBtn: Button? = null
    private var saveBtn: Button? = null
    private var changeImage: ViewChangeImageBinding? = null

    //endregion

    //region item component bindings
    private var itemCover: ViewChangeImageBinding? = null
    private var volumeNumber: TextView? = null
    private var volumeTitle: TextView? = null
    private var standaloneSwitch: SwitchMaterial? = null
    private var ownedSwitch: SwitchMaterial? = null
    private var readSwitch: SwitchMaterial? = null
    private var formatAutocomplete: ViewDropdownBinding? = null
    private var numberLabel: TextInputLayout? = null
    //endregion

    //region main form

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[BookFormViewModel::class.java]

        val dbHelper = MediaLibraryDbHelper(requireContext())
        controller = BookController(dbHelper)

        setupComponentBindings()

        var setup = controller?.GetBookSetup()
        if (setup == null)
            setup = BookSetup()

        if (isEdit && itemId != "-1") {
            viewModel.loadBook(itemId, controller, setup)
        }

        setupBookTypeRadioGroup(setup)
        setupGenreSelection(setup)
        setupPublisherSelection(setup)
        setupTagSelection(setup)
        setupCollectingPriorityDropdown(priorityAutocomplete!!) { priority ->
            viewModel.updateCollectingPriority(priority)
        }

        setupRecyclerView()
        setupInputListeners()

        changeImage!!.buttonChangeCover.setOnClickListener {
            showImageOptionsDialog("book")
        }
        changeImage!!.buttonClearCover.setOnClickListener {
            clearImage("book")
        }

        addItemBtn?.setOnClickListener {
            showBookItemSheet()
        }

        saveBtn?.setOnClickListener {
            val success = saveAction(isEdit)
            if (success) {
                activity?.finish()
            }
        }

        // Observe ViewModel
        viewModel.book.observe(viewLifecycleOwner) { book ->
            observeViewmodel(book, setup)
        }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            itemAdapter.submitList(items.toList())
        }

        viewModel.selectedGenres.observe(viewLifecycleOwner) { genres ->
            updateGenreChips(genres)
        }
    }

    private fun setupComponentBindings() {
        titleInput = binding.titleInput
        authorInput = binding.authorInput
        artistInput = binding.artistInput
        collectingCheck = binding.collectingCheck
        ongoingCheck = binding.ongoingCheck
        collectedCheck = binding.collectedCheck
        priorityAutocomplete = binding.collectingPriorityAutocomplete
        genreMultiselect = binding.genreMultiselect
        pubAutocomplete = binding.pubAutocomplete
        tagAutocomplete = binding.tagAutocomplete
        bookTypeRadio = binding.bookTypeRadio
        addItemBtn = binding.addItemBtn
        saveBtn = binding.saveBtn
        changeImage = binding.changeImage

    }

    override fun onCoverImageUpdated(byteArray: ByteArray?, target: String?) {
        if (target == "book" || target == "main") {
            viewModel.updateCover(byteArray)
        }
    }

    private fun observeViewmodel(book: Book, setup: BookSetup)
    {
        titleInput?.text = book.Title
        authorInput?.text = book.Author
        artistInput?.text = book.Artist
        collectingCheck?.isChecked = book.Collecting ?: false
        ongoingCheck?.isChecked = book.Ongoing ?: false
        collectedCheck?.isChecked = book.HasCollectedAllItems ?: false

        val currentPriority = book.CollectingPriority ?: SharedEnums.CollectingPriority.NoPriority
        priorityAutocomplete?.autocomplete?.setText(currentPriority.name, false)

        // Update RadioGroup
        val musicGenreId = book.Type?.ordinal ?: 0
        if(musicGenreId != 0) {
            RadioGridUtils.setSelection(
                bookTypeRadio!!.dynamicTableLayout,
                musicGenreId
            )
        }

        if (book.Cover != null && book.Cover.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeByteArray(book.Cover, 0, book.Cover.size)
            changeImage!!.imageBookCover.scaleType = ImageView.ScaleType.CENTER_CROP
            changeImage!!.imageBookCover.setImageBitmap(bitmap)
            changeImage!!.imageBookCover.imageTintList = null
            changeImage!!.buttonClearCover.visibility = View.VISIBLE
        } else {
            ImageUtils.setPlaceholderCover(changeImage!!.imageBookCover, requireContext(), resources)
            changeImage!!.buttonClearCover.visibility = View.GONE
        }

        // Update Publisher and Tag if setup is available
        setup.let { s ->
            val pub = s.Publishers.find { it.Id == book.Publisher }
            pub?.let { pubAutocomplete?.autocomplete?.setText(it.Name, false) }

            val tag = s.Tag.find { it.Id == book.Tag }
            tag?.let { tagAutocomplete?.autocomplete?.setText(it.Name, false) }
        }
    }

    private fun saveAction(isEdit: Boolean): Boolean
    {
        disableFields(false)
        val error = viewModel.validateFields()
        if(error.isEmpty()) {
            val saveObj = viewModel.getSaveObject()
            if (controller != null) {

                val result = if (isEdit) {
                    controller!!.UpdateBook(saveObj)
                } else {
                    controller!!.AddBook(saveObj)
                }

                if (result) {
                    ViewModelProvider(requireActivity())[SharedRefreshViewModel::class.java].incrementVersion()
                    Toast.makeText(
                        requireContext(),
                        if (isEdit) "Book Updated" else "Book Saved",
                        Toast.LENGTH_SHORT
                    ).show()
                    return true

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Book Failed to Save",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        }
        else {
            error.forEach { (string, string1) ->
                when (string) {
                    "title" -> {
                        binding.titleLabel.error = string1
                        titleInput?.requestFocus()
                        Toast.makeText(requireContext(), string1, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(requireContext(), string1, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            disableFields(true)
        }
        return false
    }

    private fun disableFields(enabled: Boolean)
    {
        titleInput?.isEnabled = enabled
        authorInput?.isEnabled = enabled
        artistInput?.isEnabled = enabled
        bookTypeRadio?.dynamicTableLayout?.children?.forEach { it ->
            if(it is ViewGroup)
            {
                it.children.forEach {
                    if(it is RadioButton)
                    {
                        it.isEnabled = enabled
                    }
                }
            }
        }
        changeImage?.buttonChangeCover?.isEnabled = enabled
        changeImage?.buttonClearCover?.isEnabled = enabled
        changeImage?.imageBookCover?.isEnabled = enabled
        genreMultiselect?.buttonSelectGenres?.isEnabled = enabled
        pubAutocomplete?.autoCompleteLabel?.isEnabled = enabled
        pubAutocomplete?.autocomplete?.isEnabled = enabled
        tagAutocomplete?.autoCompleteLabel?.isEnabled = enabled
        tagAutocomplete?.autocomplete?.isEnabled = enabled
        priorityAutocomplete?.autoCompleteLabel?.isEnabled = enabled
        priorityAutocomplete?.autocomplete?.isEnabled = enabled
        addItemBtn?.isEnabled = enabled
        collectingCheck?.isEnabled = enabled
        ongoingCheck?.isEnabled = enabled
        collectedCheck?.isEnabled = enabled
        saveBtn?.isEnabled = enabled
    }

    //region setup

    private fun setupBookTypeRadioGroup(setup: BookSetup) {

        bookTypeRadio?.radioButtonLabel?.setText(R.string.book_type_label)
        val tableLayout = bookTypeRadio?.dynamicTableLayout
        val type = setup.Type.filter { it.key != Enums.BookType.NoneSelected.ordinal }
        RadioGridUtils.populateRadioGridFromMap(
            tableLayout = tableLayout!!,
            optionsMap = type,
            columnCount = 2
        ) { selectedId ->
            handleRadioSelectionChange(selectedId)
        }
    }

    private fun handleRadioSelectionChange(id: Int) {
        // You can update a ViewModel, save state, or trigger network calls here
        val genre = Enums.BookType.entries.find { it.ordinal == id } ?: return
        viewModel.updateBookType(genre)
    }

    private fun setupGenreSelection(setup: BookSetup) {
        genreMultiselect?.buttonSelectGenres?.setOnClickListener {
            val genres = setup.Genre
            val genreNames = genres.map { it.genreName }.toTypedArray()
            val selected = viewModel.selectedGenres.value ?: mutableSetOf()
            val checkedItems = genres.map {
                selected.contains(it)
            }.toBooleanArray()

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
        tags.add(0, Tag("", ""))
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tags)
        val tagBinding = tagAutocomplete?.autocomplete

        tagBinding?.setAdapter(adapter)
        tagAutocomplete?.autoCompleteLabel?.setHint(R.string.tag)
        tagBinding?.setOnItemClickListener { _, _, position, _ ->
            val selectedTag = adapter.getItem(position)
            selectedTag?.let { viewModel.updateTag(it) }
        }

        tagBinding?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                val existing = tags.find { it.Name == text }
                if (existing == null) {
                    viewModel.updateTagName(text)
                } else {
                    viewModel.updateTag(existing)
                }
            }
        })
    }

    private fun setupPublisherSelection(setup: BookSetup) {
        val publishers = setup.Publishers
        if (!publishers.isEmpty() && publishers[0].Id != "")
        {
            publishers.add(0, Publisher("", ""))
        }
       
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, publishers)
        val publisherBinding = pubAutocomplete?.autocomplete
        publisherBinding?.setAdapter(adapter)
        pubAutocomplete?.autoCompleteLabel?.setHint(R.string.publisher)
        publisherBinding?.setOnItemClickListener { _, _, position, _ ->
            val selectedPublisher = adapter.getItem(position)
            selectedPublisher?.let { viewModel.updatePublisher(it) }
        }

        publisherBinding?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                val existing = publishers.find { it.Name == text }
                if (existing == null) {
                    viewModel.updatePublisherName(text)
                } else {
                    viewModel.updatePublisher(existing)
                }
            }
        })
    }

    private fun updateGenreChips(genres: Set<GenreObject>) {
        genreMultiselect?.chipGroupGenres?.removeAllViews()
        genres.forEach { genre ->
            val chip = Chip(requireContext()).apply {
                text = genre.genreName
                isCloseIconVisible = true
                setOnCloseIconClickListener { viewModel.toggleGenre(genre) }
            }
            genreMultiselect?.chipGroupGenres?.addView(chip)
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
        titleInput?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateTitle(s.toString())
                binding.titleLabel.error = null
            }
        })
        authorInput?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateAuthor(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        artistInput?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { viewModel.updateArtist(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        collectingCheck?.setOnCheckedChangeListener {
            _, isChecked -> viewModel.toggleCollecting(isChecked)
        }
        ongoingCheck?.setOnCheckedChangeListener {
            _, isChecked -> viewModel.toggleCompleted(isChecked)
        }
        collectedCheck?.setOnCheckedChangeListener {
            _, isChecked -> viewModel.toggleCollectionComplete(isChecked)
        }
    }

    //endregion

    //endregion


    //region item sheet

    private fun showBookItemSheet(item: BookItem? = null, position: Int = -1) {
        val dialog = BottomSheetDialog(requireContext())
        dialog.setCancelable(false)
        val sb = BookItemBottomSheetBinding.inflate(layoutInflater)
        currentSheetBinding = sb
        pendingItemPosition = position
        dialog.setContentView(sb.root)

        setupItemComponentBindings(sb)

        sb.labelText.text = if (item == null) "Add Volume" else "Edit Volume"

        setupBindings(sb, dialog, item, position)
        dialog.show()
    }

    private fun setupItemComponentBindings(sb: BookItemBottomSheetBinding) {
        itemCover = sb.imageItemCover
        volumeNumber = sb.numberInput
        volumeTitle = sb.titleInput
        standaloneSwitch = sb.standaloneSwitch
        ownedSwitch = sb.ownedSwitch
        readSwitch = sb.readSwitch
        formatAutocomplete = sb.formatAutocomplete
        numberLabel = sb.numberLabel
    }

    private fun setupBindings(sb: BookItemBottomSheetBinding, dialog: BottomSheetDialog, item: BookItem?, position: Int)
    {
        sb.cancelButton.setOnClickListener { dialog.dismiss() }

        // Setup Format dropdown
        val formats = Enums.BookFormat.entries.filter { it != Enums.BookFormat.NoneSelected }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            formats.map { it.name })
        formatAutocomplete!!.autocomplete.setAdapter(adapter)
        formatAutocomplete!!.autoCompleteLabel.setHint(R.string.format_label)
        volumeNumber?.keyListener = DigitsKeyListener.getInstance("0123456789,.- ")

        standaloneSwitch?.setOnCheckedChangeListener{
                _, isChecked ->
            if(isChecked)
            {
                volumeNumber?.visibility = View.GONE
                volumeTitle?.visibility = View.GONE
                volumeNumber?.text = ""
                volumeTitle?.text = ""
            }
            else {
                volumeNumber?.visibility = View.VISIBLE
                volumeTitle?.visibility = View.VISIBLE
            }
        }

        // Populate if editing
        item?.let {
            if(it.VolumeNumber == "-1")
            {
                volumeNumber?.text = ""
                volumeNumber?.visibility = View.GONE
                volumeTitle?.visibility = View.GONE
                standaloneSwitch?.isChecked = true
            }
            else
            {
                volumeNumber?.text = it.VolumeNumber
            }

            volumeTitle?.text = it.VolumeTitle
            ownedSwitch?.isChecked = it.Owned
            readSwitch?.isChecked = it.Read
            formatAutocomplete!!.autocomplete.setText(it.Format.name, false)
            if (it.ItemCover != null && it.ItemCover.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(it.ItemCover, 0, it.ItemCover.size)
                itemCover!!.imageBookCover.setImageBitmap(bitmap)
                itemCover!!.imageBookCover.imageTintList = null
                itemCover!!.imageBookCover.tag = it.ItemCover
                itemCover!!.buttonClearCover.visibility = View.VISIBLE
            } else {
                ImageUtils.setPlaceholderCover(itemCover!!.imageBookCover, requireContext(), resources)
                itemCover!!.imageBookCover.tag = null
                itemCover!!.buttonClearCover.visibility = View.GONE
            }
        } ?: run {
            ImageUtils.setPlaceholderCover(itemCover!!.imageBookCover, requireContext(), resources)
            itemCover!!.imageBookCover.tag = null
            itemCover!!.buttonClearCover.visibility = View.GONE
        }

        itemCover!!.buttonChangeCover.setOnClickListener {
            showImageOptionsDialog("item")
        }
        itemCover!!.buttonClearCover.setOnClickListener {
            clearImage("item")
        }

        sb.saveItemBtn.setOnClickListener {
            addBookItem(position, dialog)
        }
    }

    private fun addBookItem(position: Int, dialog: BottomSheetDialog) {

        var volNum = volumeNumber?.text.toString()
        if (standaloneSwitch?.isChecked == true)
        {
            volNum = "-1"
        }


        if (volNum.isBlank() && !standaloneSwitch!!.isChecked) {
            numberLabel?.error = "Volume number is required"
            Toast.makeText(requireContext(), "Volume number is required", Toast.LENGTH_SHORT).show()
            return
        }
        var format = Enums.BookFormat.NoneSelected
        if (formatAutocomplete!!.autocomplete.text.isNullOrEmpty()) {
            format = Enums.BookFormat.valueOf(formatAutocomplete!!.autocomplete.text.toString())
        }

        val newItem = BookItem().apply {
            VolumeNumber = volNum
            VolumeTitle = volumeTitle?.text.toString()
            Owned = ownedSwitch!!.isChecked
            Read = readSwitch!!.isChecked
            Format = format
            ItemCover = itemCover!!.imageBookCover.tag as? ByteArray
        }

        viewModel.addOrUpdateItem(newItem, position)
        dialog.dismiss()
    }

    //endregion

    override fun onDestroyView() {
        super.onDestroyView()
        currentSheetBinding = null
    }

    inner class BookItemAdapter(
        private val onEdit: (BookItem, Int) -> Unit,
        private val onDelete: (Int) -> Unit
    ) : RecyclerView.Adapter<BookItemAdapter.ViewHolder>() {

        //region component bindings

        private var changeImage: ImageView? = null
        private var infoText: TextView? = null
        private var statusText: TextView? = null
        private var editBtn: ImageButton? = null
        private var deleteBtn: ImageButton? = null

        //endregion

        private var items: List<BookItem> = emptyList()

        private fun setupComponentBindings(holder: ViewHolder){
            changeImage = holder.binding.imageItemCover
            infoText = holder.binding.textVolumeInfo
            statusText = holder.binding.textStatusInfo
            editBtn = holder.binding.buttonEditItem
            deleteBtn = holder.binding.buttonDeleteItem
        }

        fun submitList(newList: List<BookItem>) {
            items = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = BookItemVolumeBinding.inflate(LayoutInflater.from(parent.context), parent, false)

            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val context = holder.binding.root.context
            setupComponentBindings(holder)

            if(item.VolumeNumber == "-1")
            {
                infoText?.text = context.getString(R.string.standalone)
            }
            else {
                infoText?.text = context.getString(
                R.string.volume_info_format,
                item.VolumeNumber,
                item.VolumeTitle ?: ""
            )
            }

            statusText?.text = context.getString(
                R.string.volume_status_format,
                if (item.Owned) "Yes" else "No",
                if (item.Read) "Yes" else "No"
            )

            if (item.ItemCover != null && item.ItemCover.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(item.ItemCover, 0, item.ItemCover.size)
                changeImage!!.scaleType = ImageView.ScaleType.CENTER_CROP
                changeImage!!.setImageBitmap(bitmap)
                changeImage!!.imageTintList = null
            } else {
                ImageUtils.setPlaceholderCover( changeImage!!, context, resources)
            }

            editBtn?.setOnClickListener { onEdit(item, position) }
           deleteBtn?.setOnClickListener { onDelete(position) }
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(val binding: BookItemVolumeBinding) : RecyclerView.ViewHolder(binding.root)
    }
}