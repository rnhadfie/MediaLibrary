package com.example.medialibrary.book.ui.form

import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.medialibrary.utils.SharedRefreshViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import controllers.BookController
import models.book.Book
import models.book.BookItem
import models.book.BookSetup
import models.book.Enums
import models.book.Publisher
import models.music.Enums.MusicGenre
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
        setupCollectingPriorityDropdown(binding.collectingPriorityAutocomplete) { priority ->
            viewModel.updateCollectingPriority(priority)
        }
        setupRecyclerView()
        setupInputListeners()

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

    override fun onCoverImageUpdated(byteArray: ByteArray?, target: String?) {
        if (target == "book" || target == "main") {
            viewModel.updateCover(byteArray)
        }
    }

    private fun observeViewmodel(book: Book, setup: BookSetup)
    {
        binding.titleInput.setText(book.Title)
        binding.authorInput.setText(book.Author)
        binding.artistInput.setText(book.Artist)
        binding.collectingCheck.isChecked = book.Collecting ?: false
        binding.ongoingCheck.isChecked = book.Ongoing ?: false
        binding.collectedCheck.isChecked = book.HasCollectedAllItems ?: false

        val currentPriority = book.CollectingPriority ?: SharedEnums.CollectingPriority.NoPriority
        binding.collectingPriorityAutocomplete.autocomplete.setText(currentPriority.name, false)

        // Update RadioGroup
        val musicGenreId = book.Type?.ordinal ?: 0
        if(musicGenreId != 0) {
            RadioGridUtils.setSelection(
                binding.bookTypeRadio.dynamicTableLayout,
                musicGenreId
            )
        }

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

        // Update Publisher and Tag if setup is available
        setup.let { s ->
            val pub = s.Publishers.find { it.Id == book.Publisher }
            pub?.let { binding.pubAutocomplete.autocomplete.setText(it.Name, false) }

            val tag = s.Tag.find { it.Id == book.Tag }
            tag?.let { binding.tagAutocomplete.autocomplete.setText(it.Name, false) }
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
                        binding.titleInput.requestFocus()
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
        binding.titleInput.isEnabled = enabled
        binding.authorInput.isEnabled = enabled
        binding.artistInput.isEnabled = enabled
        binding.bookTypeRadio.dynamicTableLayout.children.forEach { it ->
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
    }

    private fun setupBookTypeRadioGroup(setup: BookSetup) {

        binding.bookTypeRadio.radioButtonLabel.setText(R.string.book_type_label)
        val tableLayout = binding.bookTypeRadio.dynamicTableLayout
        val musicGenre = setup.Type.filter { it.key != MusicGenre.NoneSelected.ordinal }
        RadioGridUtils.populateRadioGridFromMap(
            tableLayout = tableLayout,
            optionsMap = musicGenre,
            columnCount = 2
        ) { selectedId ->
            // This block acts as your changeListener.
            // It triggers immediately when any RadioButton in the grid is selected.
            handleRadioSelectionChange(selectedId)
        }
    }

    private fun handleRadioSelectionChange(id: Int) {
        // You can update a ViewModel, save state, or trigger network calls here
        val genre = Enums.BookType.entries.find { it.ordinal == id } ?: return
        viewModel.updateBookType(genre)
    }

    private fun setupGenreSelection(setup: BookSetup) {
        binding.genreMultiselect.buttonSelectGenres.setOnClickListener {
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
        val tagBinding = binding.tagAutocomplete.autocomplete

        tagBinding.setAdapter(adapter)
        binding.tagAutocomplete.autoCompleteLabel.setHint(R.string.tag)
        tagBinding.setOnItemClickListener { _, _, position, _ ->
            val selectedTag = adapter.getItem(position)
            selectedTag?.let { viewModel.updateTag(it) }
        }

        tagBinding.addTextChangedListener(object : TextWatcher {
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
        publishers.add(0, Publisher("", ""))
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, publishers)
        val publisherBinding = binding.pubAutocomplete.autocomplete
        publisherBinding.setAdapter(adapter)
        binding.pubAutocomplete.autoCompleteLabel.setHint(R.string.publisher)
        publisherBinding.setOnItemClickListener { _, _, position, _ ->
            val selectedPublisher = adapter.getItem(position)
            selectedPublisher?.let { viewModel.updatePublisher(it) }
        }

        publisherBinding.addTextChangedListener(object : TextWatcher {
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
        binding.titleInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateTitle(s.toString())
                binding.titleLabel.error = null
            }
        })
        binding.authorInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateAuthor(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        binding.artistInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { viewModel.updateArtist(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        binding.collectingCheck.setOnCheckedChangeListener {
            _, isChecked -> viewModel.toggleCollecting(isChecked)
        }
        binding.ongoingCheck.setOnCheckedChangeListener {
            _, isChecked -> viewModel.toggleCompleted(isChecked)
        }
        binding.collectedCheck.setOnCheckedChangeListener {
            _, isChecked -> viewModel.toggleCollectionComplete(isChecked)
        }
    }

    private fun showBookItemSheet(item: BookItem? = null, position: Int = -1) {
        val dialog = BottomSheetDialog(requireContext())
        dialog.setCancelable(false)
        val sb = BookItemBottomSheetBinding.inflate(layoutInflater)
        currentSheetBinding = sb
        pendingItemPosition = position
        dialog.setContentView(sb.root)

        sb.labelText.text = if (item == null) "Add Volume" else "Edit Volume"
        sb.cancelButton.setOnClickListener { dialog.dismiss() }


        // Setup Format dropdown
        val formats = Enums.BookFormat.entries.filter { it != Enums.BookFormat.NoneSelected }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            formats.map { it.name })
        sb.formatAutocomplete.autocomplete.setAdapter(adapter)
        sb.formatAutocomplete.autoCompleteLabel.setHint(R.string.format_label)
        sb.numberInput.keyListener = DigitsKeyListener.getInstance("0123456789,.- ")

        sb.standaloneSwitch.setOnCheckedChangeListener{
            _, isChecked ->
                if(isChecked)
                {
                    sb.numberInput.visibility = View.GONE
                    sb.titleInput.visibility = View.GONE
                    sb.numberInput.setText("")
                    sb.titleInput.setText("")
                }
                else {
                    sb.numberInput.visibility = View.VISIBLE
                    sb.titleInput.visibility = View.VISIBLE
                }
        }

        // Populate if editing
        item?.let {
            if(it.VolumeNumber == "-1")
            {
                sb.numberInput.setText("")
                sb.numberInput.visibility = View.GONE
                sb.titleInput.visibility = View.GONE
                sb.standaloneSwitch.isChecked = true
            }
            else
            {
                sb.numberInput.setText(it.VolumeNumber)
            }

            sb.titleInput.setText(it.VolumeTitle)
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

        dialog.show()
    }


    private fun addBookItem(sb: BookItemBottomSheetBinding, position: Int, dialog: BottomSheetDialog) {

        var volNum = sb.numberInput.text.toString()
        if (sb.standaloneSwitch.isChecked)
        {
            volNum = "-1"
        }


        if (volNum.isBlank() && !sb.standaloneSwitch.isChecked) {
            sb.numberLabel.error = "Volume number is required"
            Toast.makeText(requireContext(), "Volume number is required", Toast.LENGTH_SHORT).show()
            return
        }
        var format = Enums.BookFormat.NoneSelected
        if (sb.formatAutocomplete.autocomplete.text.isNullOrEmpty()) {
            format = Enums.BookFormat.valueOf(sb.formatAutocomplete.autocomplete.text.toString())
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

    override fun onDestroyView() {
        super.onDestroyView()
        currentSheetBinding = null
    }

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
            val item = items[position]
            val context = holder.binding.root.context

            if(item.VolumeNumber == "-1")
            {
                holder.binding.textVolumeInfo.text = context.getString(R.string.standalone)
            }
            else {
            holder.binding.textVolumeInfo.text = context.getString(
                R.string.volume_info_format,
                item.VolumeNumber,
                item.VolumeTitle ?: ""
            )
            }

            holder.binding.textStatusInfo.text = context.getString(
                R.string.volume_status_format,
                if (item.Owned) "Yes" else "No",
                if (item.Read) "Yes" else "No"
            )

            if (item.ItemCover != null && item.ItemCover.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(item.ItemCover, 0, item.ItemCover.size)
                holder.binding.imageItemCover.scaleType = ImageView.ScaleType.CENTER_CROP
                holder.binding.imageItemCover.setImageBitmap(bitmap)
                holder.binding.imageItemCover.imageTintList = null
            } else {
                ImageUtils.setPlaceholderCover(holder.binding.imageItemCover, context, resources)
            }

            holder.binding.buttonEditItem.setOnClickListener { onEdit(item, position) }
            holder.binding.buttonDeleteItem.setOnClickListener { onDelete(position) }
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(val binding: BookItemVolumeBinding) : RecyclerView.ViewHolder(binding.root)
    }
}