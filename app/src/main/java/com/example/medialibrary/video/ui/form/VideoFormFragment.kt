package com.example.medialibrary.video.ui.form

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medialibrary.BaseFormFragment
import com.example.medialibrary.R
import controllers.VideoController
import models.music.Enums.MusicGenre
import models.shared.GenreObject
import models.shared.Tag
import models.video.Enums.VideoFormat
import models.video.Enums.VideoTag
import models.video.Enums.VideoType
import models.video.VideoItem
import models.video.VideoSetup
import repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.BookItemVolumeBinding
import com.example.medialibrary.databinding.VideoFragmentFormBinding
import com.example.medialibrary.databinding.VideoItemBottomSheetBinding
import com.example.medialibrary.utils.ImageUtils
import com.example.medialibrary.utils.RadioGridUtils
import com.example.medialibrary.utils.SharedRefreshViewModel
import com.example.medialibrary.video.VideoFormActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import models.shared.Enums as SharedEnums

class VideoFormFragment : BaseFormFragment<VideoFragmentFormBinding, VideoFormViewModel>(
    VideoFragmentFormBinding::inflate
) {

    companion object {
        fun newInstance(id: String = "-1", isEdit: Boolean = false) = VideoFormFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ID, id)
                putBoolean(ARG_IS_EDIT, isEdit)
            }
        }
    }

    private lateinit var itemAdapter: VideoItemAdapter
    private var controller: VideoController? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[VideoFormViewModel::class.java]

        val dbHelper = MediaLibraryDbHelper(requireContext())
        controller = VideoController(dbHelper)

        var setup = controller?.GetVideoSetup()
        if (setup == null)
            setup = VideoSetup()

        if (isEdit && itemId != "-1") {
            viewModel.loadVideo(itemId, controller, setup)
        }

        setupVideoTypeRadioGroup(setup.Types)
        setupVideoTagRadioGroup(setup.VideoTags)
        setupGenreSelection(setup)
        setupTagSelection(setup)
        setupCollectingPriorityDropdown(binding.collectingPriorityAutocomplete) { priority ->
            viewModel.updateCollectingPriority(priority)
        }
        setupRecyclerView()
        setupInputListeners()

        binding.changeImage.buttonChangeCover.setOnClickListener {
            showImageOptionsDialog("video")
        }
        binding.changeImage.buttonClearCover.setOnClickListener {
            clearImage("video")
        }
        setupRecyclerView()
        setupInputListeners()

        binding.changeImage.buttonChangeCover.setOnClickListener {
            showImageOptionsDialog("video")
        }
        binding.changeImage.buttonClearCover.setOnClickListener {
            clearImage("video")
        }

        binding.addItemBtn.setOnClickListener {
            showVideoItemSheet()
        }

        binding.saveBtn.setOnClickListener {
            val result = saveVideo(isEdit)
            if (result)
            {
                activity?.finish()
            }
        }

        // Observe ViewModel
        viewModel.video.observe(viewLifecycleOwner) { book ->
            binding.titleInput.setText(book.Title)
            binding.collectingCheck.isChecked = book.Collecting ?: false
            binding.ongoingCheck.isChecked = book.Ongoing ?: false
            binding.collectedCheck.isChecked = book.HasCollectedAllItems ?: false

            val videoCategory = book.VideoTag?.ordinal ?: 0
            if(videoCategory != 0) {
                RadioGridUtils.setSelection(
                    binding.videoCategoryRadio.dynamicTableLayout,
                    videoCategory
                )
            }

            val videoType = book.Type?.ordinal ?: 0
            if(videoCategory != 0) {
                RadioGridUtils.setSelection(
                    binding.videoTypeRadio.dynamicTableLayout,
                    videoType
                )
            }

            setup.let { s ->
                val tag = s.Tag.find { it.Id == book.Tag }
                tag?.let { binding.tagAutocomplete.autocomplete.setText(it.Name, false) }
            }

            val currentPriority = book.CollectingPriority ?: SharedEnums.CollectingPriority.NoPriority
            binding.collectingPriorityAutocomplete.autocomplete.setText(currentPriority.name, false)

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


        }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            itemAdapter.submitList(items.toList())
        }

        viewModel.selectedGenres.observe(viewLifecycleOwner) { genres ->
            updateGenreChips(genres)
        }
    }

    private fun saveVideo(isEdit: Boolean): Boolean
    {
        disableFields(false)
        val error = viewModel.validate()
        if (error.isNotEmpty()) {
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
        } else {
            val saveObj = viewModel.getSaveObject()
            if (controller != null) {

                val result = if (isEdit) {
                    controller!!.UpdateVideo(saveObj)
                } else {
                    controller!!.AddVideo(saveObj)
                }
                if (result) {
                    ViewModelProvider(requireActivity())[SharedRefreshViewModel::class.java].incrementVersion()
                    Toast.makeText(
                        requireContext(),
                        if (isEdit) "Movie/TV Show Updated" else "Movie/TV Show Saved",
                        Toast.LENGTH_SHORT
                    ).show()
                    return true
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Movie/TV Show Failed to Save",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        disableFields(true)
        return false
    }

    private fun disableFields(enabled: Boolean)
    {
        binding.titleInput.isEnabled = enabled
        binding.videoTypeRadio.dynamicTableLayout.children.forEach { it ->
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
        binding.videoCategoryRadio.dynamicTableLayout.children.forEach { it ->
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
        binding.tagAutocomplete.autoCompleteLabel.isEnabled = enabled
        binding.collectingPriorityAutocomplete.autoCompleteLabel.isEnabled = enabled
        binding.collectingPriorityAutocomplete.autocomplete.isEnabled = enabled
        binding.addItemBtn.isEnabled = enabled
        binding.collectingCheck.isEnabled = enabled
        binding.ongoingCheck.isEnabled = enabled
        binding.collectedCheck.isEnabled = enabled
        binding.saveBtn.isEnabled = enabled
    }

    override fun onCoverImageUpdated(byteArray: ByteArray?, target: String?) {
        if (target == "video" || target == "book" || target == "main") {
            viewModel.updateCover(byteArray)
        }
    }

    private fun handleRadioSelectionChange(id: Int) {
        // You can update a ViewModel, save state, or trigger network calls here
        val genre = VideoTag.entries.find { it.ordinal == id } ?: return
        viewModel.updateVideoTag(genre)
    }

    private fun setupVideoTypeRadioGroup(types: Map<Int, String>) {

        binding.videoTypeRadio.radioButtonLabel.setText(R.string.music_genre)
        val tableLayout = binding.videoTypeRadio.dynamicTableLayout
        val musicGenre = types.filter { it.key != MusicGenre.NoneSelected.ordinal }
        RadioGridUtils.populateRadioGridFromMap(
            tableLayout = tableLayout,
            optionsMap = musicGenre,
            columnCount = 2
        ) { selectedId ->
            handleVideoTypeSelectionChange(selectedId)
        }
    }

    private fun setupVideoTagRadioGroup(videoTags: Map<Int, String>) {
        binding.videoCategoryRadio.radioButtonLabel.setText(R.string.music_genre)
        val tableLayout = binding.videoCategoryRadio.dynamicTableLayout
        val musicGenre = videoTags.filter { it.key != MusicGenre.NoneSelected.ordinal }
        RadioGridUtils.populateRadioGridFromMap(
            tableLayout = tableLayout,
            optionsMap = musicGenre,
            columnCount = 2
        ) { selectedId ->
            handleRadioSelectionChange(selectedId)
        }
    }

    private fun handleVideoTypeSelectionChange(id: Int) {
        val genre = VideoType.entries.find { it.ordinal == id } ?: return
        viewModel.updateVideoType(genre)
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
        binding.titleInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.updateTitle(s.toString())
                binding.titleLabel.error = ""
            }
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

    private fun showVideoItemSheet(item: VideoItem? = null, position: Int = -1) {
        val dialog = BottomSheetDialog(requireContext())
        val sb = VideoItemBottomSheetBinding.inflate(layoutInflater)
        dialog.setCancelable(false)
        currentSheetBinding = sb
        pendingItemPosition = position
        dialog.setContentView(sb.root)

        sb.labelText.text = if (item == null) "Add Set or Season" else "Edit Set or Season"
        sb.cancelButton.setOnClickListener { dialog.dismiss() }

        // Setup Format dropdown
        val formats = VideoFormat.entries.filter { it != VideoFormat.NoneSelected }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            formats.map { it.name })
        sb.formatAutocomplete.autocomplete.setAdapter(adapter)
        sb.formatAutocomplete.autoCompleteLabel.setHint(R.string.format_label)

        // Populate if editing
        item?.let {

            sb.seasonInput.setText(it.Season.toString())
            sb.titleInput.setText(it.DiscTitle)
            sb.ownedSwitch.isChecked = it.Owned
            sb.watchedSwitch.isChecked = it.Watched
            sb.formatAutocomplete.autocomplete.setText(it.Format.name, false)
            if(it.Season == -1)
            {
                sb.standaloneSwitch.isChecked = true
                sb.seasonInput.visibility = View.GONE
                sb.titleInput.visibility = View.GONE
            }

            if (it.ItemCover != null && it.ItemCover.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(it.ItemCover, 0, it.ItemCover.size)
                sb.imageItemCover.imageBookCover.scaleType = ImageView.ScaleType.CENTER_CROP
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
            var volNum = sb.seasonInput.text.toString()

            if (volNum.isBlank() && !sb.standaloneSwitch.isChecked) {
                sb.seasonLabel.error = "Volume number is required"
                Toast.makeText(requireContext(), "Volume number is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(sb.standaloneSwitch.isChecked)
            {
                volNum = "-1"
                sb.titleInput.setText("")
            }
            saveVideoItem(sb,volNum, position)

            dialog.dismiss()
        }

        sb.standaloneSwitch.setOnCheckedChangeListener{
                _, isChecked ->
            if(isChecked)
            {
                sb.seasonInput.visibility = View.GONE
                sb.titleInput.visibility = View.GONE
                sb.seasonInput.setText("")
            }
            else {
                sb.seasonInput.visibility = View.VISIBLE
                sb.titleInput.visibility = View.VISIBLE
                sb.titleInput.setText("")
            }
        }

        dialog.show()
    }

    private fun saveVideoItem(sb:VideoItemBottomSheetBinding, volNum: String, position: Int)
    {
        var format = VideoFormat.NoneSelected
        if(sb.formatAutocomplete.autocomplete.text != null && !sb.formatAutocomplete.autocomplete.text.isEmpty())
        {
            format = VideoFormat.valueOf(sb.formatAutocomplete.autocomplete.text.toString())
        }
        val newItem = VideoItem().apply {
            Season = volNum.toIntOrNull() ?: 0
            DiscTitle = (sb.titleInput.text ?: "").toString()
            Owned = sb.ownedSwitch.isChecked
            Watched = sb.watchedSwitch.isChecked
            Format = format
            ItemCover = sb.imageItemCover.imageBookCover.tag as? ByteArray
        }

        viewModel.addOrUpdateItem(newItem, position)
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
                holder.binding.textVolumeInfo.text = item.Season.toString()
            }
            else
            {

                if(item.Season == -1)
                {
                    holder.binding.textVolumeInfo.text = getString(R.string.standalone)
                }
                else {
                    holder.binding.textVolumeInfo.text = getString(
                        R.string.video_item_display_text,
                        item.Season,
                        item.DiscTitle
                    )
                }
            }
            holder.binding.textStatusInfo.text = context.getString(
                R.string.movie_status_format,
                if (item.Owned) "Yes" else "No",
                if (item.Watched) "Yes" else "No"
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