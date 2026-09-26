package com.example.medialibrary.book.ui.form

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.ViewModelProvider
import com.example.medialibrary.R
import com.example.medialibrary.utils.ImageUtils
import com.example.medialibrary.utils.SharedRefreshViewModel
import com.example.medialibrary.backend.controllers.BookController
import java.io.ByteArrayOutputStream
import java.io.File
import com.example.medialibrary.backend.models.book.BookItem
import com.example.medialibrary.backend.models.book.BookSetup
import com.example.medialibrary.backend.models.book.Enums
import com.example.medialibrary.backend.models.shared.GenreObject
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.BookItemBottomSheetBinding
import com.example.medialibrary.databinding.BookFragmentFormBinding
import com.example.medialibrary.databinding.BookItemVolumeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip

class BookFormFragment : Fragment() {

    companion object {
        private const val ARG_ID = "arg_id"
        private const val ARG_IS_EDIT = "arg_is_edit"

        fun newInstance(id: Int = -1, isEdit: Boolean = false) = BookFormFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_ID, id)
                putBoolean(ARG_IS_EDIT, isEdit)
            }
        }
    }

    private val viewModel: BookFormViewModel by viewModels()
    private var _binding: BookFragmentFormBinding? = null
    private val binding get() = _binding!!

    private lateinit var itemAdapter: BookItemAdapter

    private var pendingImageTarget: String? = null // "book" or "item"
    private var pendingItemPosition: Int = -1
    private var currentSheetBinding: BookItemBottomSheetBinding? = null

    private var controller: BookController? = null

    private var tempPhotoFile: File? = null
    private var tempPhotoUri: Uri? = null


    // region Image Handling
    private val pickGalleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let { ImageUtils.handleImageUri(it, requireContext(), ::handleImageBitmap) }
        }
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempPhotoUri != null) {
            ImageUtils.handleImageUri(tempPhotoUri!!, requireContext(), ::handleImageBitmap)
        }
        tempPhotoFile?.delete()
        tempPhotoFile = null
        tempPhotoUri = null
    }

    private fun handleImageBitmap(bitmap: Bitmap) {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        ImageUtils.handleImageBitmap(bitmap, pendingImageTarget, binding, currentSheetBinding )

        if (pendingImageTarget == "book") {
            viewModel.updateCover(byteArray)
        }

    }

    private fun clearImage(target: String) {

        ImageUtils.clearImage(target, binding, currentSheetBinding,requireContext(),resources)
        if (target == "book") {
            viewModel.updateCover(null)
        }
    }

    private fun showImageOptionsDialog(target: String) {
        pendingImageTarget = target
        val options = arrayOf(
            getString(R.string.choose_from_gallery),
            getString(R.string.take_photo),
            getString(R.string.clear_image)
        )
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.select_image_source)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
                        pickGalleryLauncher.launch(intent)
                    }
                    1 -> {
                        val (file, uri) = ImageUtils.createTempPhotoUri(requireContext())
                        tempPhotoFile = file
                        tempPhotoUri = uri
                        takePhotoLauncher.launch(uri)
                    }
                    2 -> {
                        clearImage(target)
                    }
                }
            }
            .show()
    }

    //endregion

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Initialize controller
        val dbHelper = MediaLibraryDbHelper(requireContext())
        controller = BookController(dbHelper)

        _binding = BookFragmentFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookId = arguments?.getInt(ARG_ID) ?: -1
        val isEdit = arguments?.getBoolean(ARG_IS_EDIT) ?: false

        var setup = controller?.GetBookSetup()
        if(setup == null)
            setup = BookSetup()

        if (isEdit && bookId != -1) {
            viewModel.loadBook(bookId, controller, setup)
        }

        setupBookTypeRadioGroup(setup.Type)
        setupGenreSelection(setup)
        setupPublisherSelection(setup)
        setupTagSelection(setup)
        setupRecyclerView()
        setupInputListeners()

        binding.changeImage.buttonChangeCover.setOnClickListener {
            showImageOptionsDialog("book")
        }
        binding.changeImage.buttonClearCover.setOnClickListener {
            clearImage("book")
        }

        binding.buttonAddItem.setOnClickListener {
            showBookItemSheet()
        }

        binding.buttonSaveBook.setOnClickListener {
            val error = viewModel.validate()
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()

            } else {
                val saveObj = viewModel.getSaveObject()
                if(controller != null) {

                    val result = if (isEdit) {
                        controller!!.UpdateBook(saveObj)
                    } else {
                        controller!!.AddBook(saveObj)
                    }

                    if(result) {
                        // Log or process the save object
                        println("Saving book: ${saveObj.book?.Title} with ${saveObj.book.Items?.size} items")

                        ViewModelProvider(requireActivity())[SharedRefreshViewModel::class.java].incrementVersion()

                        Toast.makeText(
                            requireContext(),
                            if (isEdit) "Book Updated" else "Book Saved",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        activity?.finish()
                    }
                    else {
                        Toast.makeText(
                            requireContext(),
                            "Book Failed to Save",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            }
        }

        // Observe ViewModel
        viewModel.book.observe(viewLifecycleOwner) { book ->
            binding.editBookTitle.setText(book.Title)
            binding.editBookAuthor.setText(book.Author)
            binding.editBookArtist.setText(book.Artist)
            binding.bookCollecting.isChecked = book.Collecting ?: false
            binding.bookHasEnded.isChecked = book.HasSeriesEnded ?: false
            binding.bookCompletedCollecting.isChecked = book.HasCollectedAllItems ?: false

            // Update RadioGroup
            for (i in 0 until binding.radioGroupBookType.childCount) {
                val rb = binding.radioGroupBookType.getChildAt(i) as RadioButton
                if (rb.tag == book.Type) {
                    rb.isChecked = true
                    break
                }
            }

            if (book.Cover != null && book.Cover.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(book.Cover, 0, book.Cover.size)
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
                pub?.let { binding.publisherAutocomplete.autocomplete.setText(it.Name, false) }

                val tag = s.Tag.find { it.Id == book.Tag }
                tag?.let { binding.publisherAutocomplete.autocomplete.setText(it.Name, false) }
            }
        }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            itemAdapter.submitList(items.toList())
        }

        viewModel.selectedGenres.observe(viewLifecycleOwner) { genres ->
            updateGenreChips(genres)
        }
    }

    private fun setupBookTypeRadioGroup(types: Map<Int, String>) {
        val bookTypes = Enums.BookType.entries.toTypedArray()
        types.forEach { (key, value) ->
            if (key >= 0 && key < bookTypes.size) {
                val type = bookTypes[key]
                if (type == Enums.BookType.NoneSelected) return@forEach
                val rb = RadioButton(requireContext()).apply {
                    id = View.generateViewId()
                    text = value
                    tag = type
                }
                binding.radioGroupBookType.addView(rb)
            }
        }

        binding.radioGroupBookType.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId != -1) {
                val rb = group.findViewById<RadioButton>(checkedId)
                (rb?.tag as? Enums.BookType)?.let {
                    viewModel.updateBookType(it)
                }
            }
        }
    }

    private fun setupGenreSelection(setup: BookSetup) {
        binding.genreMultiselect.buttonSelectGenres.setOnClickListener {
            val genres = setup.Genre
            val genreNames = genres.map { it.genreName }.toTypedArray()
            val selected = viewModel.selectedGenres.value ?: mutableSetOf<GenreObject>()
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
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, publishers)
        val publisherBinding = binding.publisherAutocomplete.autocomplete
        publisherBinding.setAdapter(adapter)
        binding.publisherAutocomplete.autoCompleteLabel.setHint(R.string.publisher)
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
        binding.editBookTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { viewModel.updateTitle(s.toString()) }
        })
        binding.editBookAuthor.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { viewModel.updateAuthor(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        binding.editBookArtist.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { viewModel.updateArtist(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        binding.bookCollecting.setOnCheckedChangeListener {
            _, isChecked -> viewModel.toggleCollecting(isChecked)
        }
        binding.bookHasEnded.setOnCheckedChangeListener {
            _, isChecked -> viewModel.toggleCompleted(isChecked)
        }
        binding.bookCompletedCollecting.setOnCheckedChangeListener {
            _, isChecked -> viewModel.toggleCollectionComplete(isChecked)
        }
    }

    private fun showBookItemSheet(item: BookItem? = null, position: Int = -1) {
        val dialog = BottomSheetDialog(requireContext())
        dialog.setCancelable(false)
        val sheetBinding = BookItemBottomSheetBinding.inflate(layoutInflater)
        currentSheetBinding = sheetBinding
        pendingItemPosition = position
        dialog.setContentView(sheetBinding.root)

        sheetBinding.labelText.text = if (item == null) "Add Volume" else "Edit Volume"
        sheetBinding.cancelButton.setOnClickListener { dialog.dismiss() }


        // Setup Format dropdown
        val formats = Enums.BookFormat.entries.filter { it != Enums.BookFormat.NoneSelected }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            formats.map { it.name })
        sheetBinding.formatAutocomplete.autocomplete.setAdapter(adapter)
        sheetBinding.formatAutocomplete.autoCompleteLabel.setHint(R.string.format_label)

        // Populate if editing
        item?.let {
            sheetBinding.editSheetVolumeNumber.setText(it.VolumeNumber)
            sheetBinding.editSheetVolumeTitle.setText(it.VolumeTitle)
            sheetBinding.switchSheetOwned.isChecked = it.Owned
            sheetBinding.switchSheetRead.isChecked = it.Read
            sheetBinding.formatAutocomplete.autocomplete.setText(it.Format.name, false)
            if (it.ItemCover != null && it.ItemCover.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(it.ItemCover, 0, it.ItemCover.size)
                sheetBinding.imageItemCover.imageBookCover.setImageBitmap(bitmap)
                sheetBinding.imageItemCover.imageBookCover.imageTintList = null
                sheetBinding.imageItemCover.imageBookCover.tag = it.ItemCover
                sheetBinding.imageItemCover.buttonClearCover.visibility = View.VISIBLE
            } else {
                ImageUtils.setPlaceholderCover(sheetBinding.imageItemCover.imageBookCover, requireContext(), resources)
                sheetBinding.imageItemCover.imageBookCover.tag = null
                sheetBinding.imageItemCover.buttonClearCover.visibility = View.GONE
            }
        } ?: run {
            ImageUtils.setPlaceholderCover(sheetBinding.imageItemCover.imageBookCover, requireContext(), resources)
            sheetBinding.imageItemCover.imageBookCover.tag = null
            sheetBinding.imageItemCover.buttonClearCover.visibility = View.GONE
        }

        sheetBinding.imageItemCover.buttonChangeCover.setOnClickListener {
            showImageOptionsDialog("item")
        }
        sheetBinding.imageItemCover.buttonClearCover.setOnClickListener {
            clearImage("item")
        }

        sheetBinding.buttonSheetSave.setOnClickListener {
            val volNum = sheetBinding.editSheetVolumeNumber.text.toString()
            if (volNum.isBlank()) {
                Toast.makeText(requireContext(), "Volume number is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var format = Enums.BookFormat.NoneSelected
            if(sheetBinding.formatAutocomplete.autocomplete.text != null)
            {
                format = Enums.BookFormat.valueOf(sheetBinding.formatAutocomplete.autocomplete.text.toString())
            }

            val newItem = BookItem().apply {
                VolumeNumber = volNum
                VolumeTitle = sheetBinding.editSheetVolumeTitle.text.toString()
                Owned = sheetBinding.switchSheetOwned.isChecked
                Read = sheetBinding.switchSheetRead.isChecked
                Format = format
                ItemCover = sheetBinding.imageItemCover.imageBookCover.tag as? ByteArray
            }

            viewModel.addOrUpdateItem(newItem, position)
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

            holder.binding.textVolumeInfo.text = context.getString(
                R.string.volume_info_format,
                item.VolumeNumber,
                item.VolumeTitle ?: ""
            )

            holder.binding.textStatusInfo.text = context.getString(
                R.string.volume_status_format,
                if (item.Owned) "Yes" else "No",
                if (item.Read) "Yes" else "No"
            )

            if (item.ItemCover != null) {
                val bitmap = BitmapFactory.decodeByteArray(item.ItemCover, 0, item.ItemCover.size)
                holder.binding.imageItemCover.setImageBitmap(bitmap)
                holder.binding.imageItemCover.imageTintList = null
            } else {
                holder.binding.imageItemCover.setImageResource(R.drawable.ic_gallery_black_24dp)
                holder.binding.imageItemCover.imageTintList = ResourcesCompat.getColorStateList(resources, android.R.color.darker_gray, null)
            }

            holder.binding.buttonEditItem.setOnClickListener { onEdit(item, position) }
            holder.binding.buttonDeleteItem.setOnClickListener { onDelete(position) }
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(val binding: BookItemVolumeBinding) : RecyclerView.ViewHolder(binding.root)
    }
}