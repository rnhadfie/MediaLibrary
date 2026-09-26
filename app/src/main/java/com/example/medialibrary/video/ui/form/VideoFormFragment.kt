package com.example.medialibrary.video.ui.form

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
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.ViewModelProvider
import com.example.medialibrary.R
import com.example.medialibrary.utils.SharedRefreshViewModel
import com.example.medialibrary.backend.controllers.VideoController
import com.example.medialibrary.backend.models.video.*
import com.example.medialibrary.backend.models.shared.GenreObject
import com.example.medialibrary.backend.models.video.Enums.*
import com.example.medialibrary.backend.models.shared.Tag
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.VideoItemBottomSheetBinding
import com.example.medialibrary.databinding.VideoFragmentFormBinding
import com.example.medialibrary.databinding.BookItemVolumeBinding
import com.example.medialibrary.utils.ImageUtils
import com.example.medialibrary.video.VideoFormActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.ByteArrayOutputStream
import java.io.File

class VideoFormFragment : Fragment() {

    companion object {
        private const val ARG_ID = "arg_id"
        private const val ARG_IS_EDIT = "arg_is_edit"

        fun newInstance(id: Int = -1, isEdit: Boolean = false) = VideoFormFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_ID, id)
                putBoolean(ARG_IS_EDIT, isEdit)
            }
        }
    }

    private val viewModel: VideoFormViewModel by viewModels()
    private var _binding: VideoFragmentFormBinding? = null
    private val binding get() = _binding!!

    private lateinit var itemAdapter: VideoItemAdapter

    private var pendingImageTarget: String? = null
    private var pendingItemPosition: Int = -1
    private var currentSheetBinding: VideoItemBottomSheetBinding? = null

    private var controller: VideoController? = null

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
        controller = VideoController(dbHelper)

        _binding = VideoFragmentFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val videoId = arguments?.getInt(ARG_ID) ?: -1
        val isEdit = arguments?.getBoolean(ARG_IS_EDIT) ?: false

        var setup = controller?.GetVideoSetup()

        if(setup == null)
            setup = VideoSetup()

        if (isEdit && videoId != -1) {
            viewModel.loadVideo(videoId, controller, setup)
        }



        setupVideoTypeRadioGroup(setup.Types)
        setupVideoTagRadioGroup(setup.VideoTags)
        setupGenreSelection(setup)
        setupTagSelection(setup)
        setupRecyclerView()
        setupInputListeners()

        binding.changeImage.buttonChangeCover.setOnClickListener {
            showImageOptionsDialog("video")
        }
        binding.changeImage.buttonClearCover.setOnClickListener {
            clearImage("video")
        }

        binding.buttonAddItem.setOnClickListener {
            showVideoItemSheet()
        }

        binding.buttonSaveBook.setOnClickListener {
            val error = viewModel.validate()
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            } else {
                val saveObj = viewModel.getSaveObject()
                if(controller != null) {

                    val result = if (isEdit) {
                        controller!!.UpdateVideo(saveObj)
                    } else {
                        controller!!.AddVideo(saveObj)
                    }

                    if(result) {
                        // Log or process the save object
                        println("Saving book: ${saveObj.video?.Title} with ${saveObj.video.Items?.size} items")

                        ViewModelProvider(requireActivity())[SharedRefreshViewModel::class.java].incrementVersion()

                        Toast.makeText(
                            requireContext(),
                            if (isEdit) "Moive/TV Show Updated" else "Moive/TV Show Saved",
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
            binding.editVideoTitle.setText(book.Title)
            binding.bookCollecting.isChecked = book.Collecting ?: false
            binding.bookHasEnded.isChecked = book.HasSeriesEnded ?: false
            binding.bookCompletedCollecting.isChecked = book.HasCollectedAllItems ?: false

            // Update RadioGroup
            for (i in 0 until binding.radioGroupVideoType.childCount) {
                val rb = binding.radioGroupVideoType.getChildAt(i) as RadioButton
                if (VideoType.entries[rb.tag as Int] == book.Type) {
                    rb.isChecked = true
                    break
                }
            }

            for (i in 0 until binding.radioGroupVideoType.childCount) {
                val rb = binding.radioGroupVideoTag.getChildAt(i) as RadioButton
                if (VideoTag.entries[rb.tag as Int] == book.VideoTag) {
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


        }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            itemAdapter.submitList(items.toList())
        }

        viewModel.selectedGenres.observe(viewLifecycleOwner) { genres ->
            updateGenreChips(genres)
        }
    }

    private fun setupVideoTypeRadioGroup(types: Map<Int, String>) {
        types.forEach { (key, value) ->
            if (VideoType.entries[key] == VideoType.NoneSelected) return@forEach
            val rb = RadioButton(requireContext()).apply {
                id = View.generateViewId()
                text = value
                tag = key
            }
            binding.radioGroupVideoType.addView(rb)
            if (VideoType.entries[key] == VideoType.NoneSelected) rb.isChecked = true
        }

        binding.radioGroupVideoType.setOnCheckedChangeListener { group, checkedId ->
            val rb = group.findViewById<RadioButton>(checkedId)
            viewModel.updateVideoType(VideoType.entries[rb.tag as Int])
        }
    }

    private fun setupVideoTagRadioGroup(videoTags: Map<Int, String>) {

        videoTags.forEach { (key, value) ->
            if (VideoTag.entries[key] == VideoTag.None) return@forEach
            val rb = RadioButton(requireContext()).apply {
                id = View.generateViewId()
                text = value
                tag = key
            }
            binding.radioGroupVideoTag.addView(rb)
            if (VideoTag.entries[key] == VideoTag.None) rb.isChecked = true
        }

        binding.radioGroupVideoTag.setOnCheckedChangeListener { group, checkedId ->
            val rb = group.findViewById<RadioButton>(checkedId)
            viewModel.updateVideoTag(VideoTag.entries[rb.tag as Int])
        }
    }

    private fun setupGenreSelection(setup: VideoSetup) {
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

    private fun setupTagSelection(setup: VideoSetup) {
        val tags = setup.Tag
        val adapter = ArrayAdapter<Tag>(requireContext(), android.R.layout.simple_dropdown_item_1line, tags)
        binding.tagAutocomplete.autocomplete.setAdapter(adapter)
        binding.tagAutocomplete.autoCompleteLabel.setHint(R.string.tag)
        binding.tagAutocomplete.autocomplete.setOnItemClickListener { _, _, position, _ ->
            val selectedTag = adapter.getItem(position)
            selectedTag?.let { viewModel.updateTag(it) }
        }

        binding.tagAutocomplete.autocomplete.addTextChangedListener(object : TextWatcher {
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
        itemAdapter = VideoItemAdapter(
            onEdit = { item, pos -> showVideoItemSheet(item, pos) },
            onDelete = { pos -> viewModel.deleteItem(pos) }
        )
        binding.recyclerVideoItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }
    }

    private fun setupInputListeners() {
        binding.editVideoTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { viewModel.updateTitle(s.toString()) }
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

    private fun showVideoItemSheet(item: VideoItem? = null, position: Int = -1) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = VideoItemBottomSheetBinding.inflate(layoutInflater)
        dialog.setCancelable(false)
        currentSheetBinding = sheetBinding
        pendingItemPosition = position
        dialog.setContentView(sheetBinding.root)

        sheetBinding.labelText.text = if (item == null) "Add Set or Season" else "Edit Set or Season"
        sheetBinding.cancelButton.setOnClickListener { dialog.dismiss() }

        // Setup Format dropdown
        val formats = VideoFormat.entries.filter { it != VideoFormat.NoneSelected }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            formats.map { it.name })
        sheetBinding.formatAutocomplete.autocomplete.setAdapter(adapter)
        sheetBinding.formatAutocomplete.autoCompleteLabel.setHint(R.string.format_label)

        // Populate if editing
        item?.let {
            sheetBinding.editSheetVolumeNumber.setText(it.DiscNumber.toString())
            sheetBinding.editSheetVolumeTitle.setText(it.DiscTitle)
            sheetBinding.switchSheetOwned.isChecked = it.Owned
            sheetBinding.switchSheetWatched.isChecked = it.Watched
            sheetBinding.formatAutocomplete.autocomplete.setText(it.Format.name, false)

            if (it.ItemCover != null && it.ItemCover.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(it.ItemCover, 0, it.ItemCover.size)
                sheetBinding.imageItemCover.imageBookCover.setImageBitmap(bitmap)
                sheetBinding.imageItemCover.imageBookCover.imageTintList = null
                sheetBinding.imageItemCover.imageBookCover.tag = it.ItemCover
                sheetBinding.imageItemCover.buttonClearCover.visibility = View.VISIBLE
            } else {
                ImageUtils.setPlaceholderCover(binding.changeImage.imageBookCover, requireContext(), resources)
                sheetBinding.imageItemCover.imageBookCover.tag = null
                sheetBinding.imageItemCover.buttonClearCover.visibility = View.GONE
            }
        } ?: run {
            ImageUtils.setPlaceholderCover(binding.changeImage.imageBookCover, requireContext(), resources)
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

            var format = VideoFormat.NoneSelected
            if(sheetBinding.formatAutocomplete.autocomplete.text != null && !sheetBinding.formatAutocomplete.autocomplete.text.isEmpty())
            {
                format = VideoFormat.valueOf(sheetBinding.formatAutocomplete.autocomplete.text.toString())
            }
            val newItem = VideoItem().apply {
                DiscNumber = volNum.toIntOrNull() ?: 0
                DiscTitle = (sheetBinding.editSheetVolumeTitle.text ?: "").toString()
                Owned = sheetBinding.switchSheetOwned.isChecked
                Watched = sheetBinding.switchSheetWatched.isChecked
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

    inner class VideoItemAdapter(
        private val onEdit: (VideoItem, Int) -> Unit,
        private val onDelete: (Int) -> Unit
    ) : RecyclerView.Adapter<VideoItemAdapter.ViewHolder>() {

        private var items: List<VideoItem> = emptyList()

        fun submitList(newList: List<VideoItem>) {
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

            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, VideoFormActivity::class.java).apply {
                    putExtra("EXTRA_ID", item.Id)
                    putExtra("EXTRA_IS_EDIT", true)
                }
                context.startActivity(intent)
            }
            holder.binding.buttonEditItem.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, VideoFormActivity::class.java).apply {
                    putExtra("EXTRA_ID", item.Id)
                    putExtra("EXTRA_IS_EDIT", true)
                }
                context.startActivity(intent)
            }
            holder.binding.buttonDeleteItem.setOnClickListener {
                val dbHelper = MediaLibraryDbHelper(holder.itemView.context)
                val videoController = VideoController(dbHelper)

                MaterialAlertDialogBuilder(holder.itemView.context)
                    .setTitle("Confirm Action")
                    .setMessage("Are you sure you want to delete this Video?")
                    .setCancelable(false) // Prevents closing by tapping outside
                    .setPositiveButton("Confirm") { dialog, _ ->
                        videoController.DeleteVideo(item.Id)
                        (holder.itemView.context as? FragmentActivity)?.let { act ->
                            ViewModelProvider(act)[SharedRefreshViewModel::class.java].incrementVersion()
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

            if(item.DiscTitle == null || item.DiscTitle.isEmpty())
            {
                holder.binding.textVolumeInfo.text = item.DiscNumber.toString()
            }
            else
            {

                holder.binding.textVolumeInfo.text = getString(
                    R.string.video_item_display_text,
                    item.DiscNumber,
                    item.DiscTitle
                )
            }
            holder.binding.textStatusInfo.text = context.getString(
                R.string.movie_status_format,
                if (item.Owned) "Yes" else "No",
                if (item.Watched) "Yes" else "No"
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