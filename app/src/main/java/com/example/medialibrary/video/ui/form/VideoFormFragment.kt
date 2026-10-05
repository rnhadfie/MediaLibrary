package com.example.medialibrary.video.ui.form

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
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
import com.example.medialibrary.databinding.BookItemVolumeBinding
import com.example.medialibrary.databinding.VideoFragmentFormBinding
import com.example.medialibrary.databinding.VideoItemBottomSheetBinding
import com.example.medialibrary.utils.ImageUtils
import com.example.medialibrary.utils.RadioGridUtils
import com.example.medialibrary.video.VideoFormActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
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

        val setup = controller?.GetVideoSetup() ?: VideoSetup()

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
        setupClickListeners()

        val formats = VideoFormat.entries.filter { it != VideoFormat.NoneSelected }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            formats.map { it.name }
        )
        binding.formatAutocomplete.autocomplete.setAdapter(adapter)
        binding.formatAutocomplete.autoCompleteLabel.setHint(R.string.format_label)

        observeViewModel(setup)
    }

    private fun setupClickListeners() {
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
            if (result) {
                activity?.finish()
            }
        }


    }

    private fun observeViewModel(setup: VideoSetup) {
        viewModel.video.observe(viewLifecycleOwner) { video ->
            binding.titleInput.setText(video.Title)
            binding.collectingCheck.isChecked = video.Collecting ?: false
            binding.ongoingCheck.isChecked = video.Ongoing ?: false
            binding.collectedCheck.isChecked = video.HasCollectedAllItems ?: false

            val videoCategory = video.VideoTag?.ordinal ?: 0
            if (videoCategory != 0) {
                RadioGridUtils.setSelection(
                    binding.videoCategoryRadio.dynamicTableLayout,
                    videoCategory
                )
            }

            val videoType = video.Type?.ordinal ?: 0
            if (videoType != 0) {
                RadioGridUtils.setSelection(
                    binding.videoTypeRadio.dynamicTableLayout,
                    videoType
                )
            }

            val tag = setup.Tag.find { it.Id == video.Tag }
            tag?.let { binding.tagAutocomplete.autocomplete.setText(it.Name, false) }

            val currentPriority = video.CollectingPriority ?: SharedEnums.CollectingPriority.None
            binding.collectingPriorityAutocomplete.autocomplete.setText(currentPriority.name, false)

            if (video.Cover != null && video.Cover.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(video.Cover, 0, video.Cover.size)
                binding.changeImage.imageBookCover.scaleType = ImageView.ScaleType.CENTER_CROP
                binding.changeImage.imageBookCover.setImageBitmap(bitmap)
                binding.changeImage.imageBookCover.imageTintList = null
                binding.changeImage.buttonClearCover.visibility = View.VISIBLE
            } else {
                ImageUtils.setPlaceholderCover(binding.changeImage.imageBookCover, requireContext(), resources)
                binding.changeImage.buttonClearCover.visibility = View.GONE
            }

            if (video.Items != null && video.Items.size == 1 && video.Items[0].Season == -1) {
                binding.standaloneSwitch.isChecked = true
                binding.watchedSwitch.isChecked = video.Items[0].Watched
                binding.ownedSwitch.isChecked = video.Items[0].Owned
                binding.formatAutocomplete.autocomplete.setText(video.Items[0].Format.name, false)
                binding.standaloneSection.visibility = View.VISIBLE
                binding.seriesSection.visibility = View.GONE
            }
            else
            {
                binding.standaloneSection.visibility = View.GONE
                binding.seriesSection.visibility = View.VISIBLE
            }
        }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            itemAdapter.submitList(items.toList())
        }

        viewModel.selectedGenres.observe(viewLifecycleOwner) { genres ->
            updateGenreChips(genres)
        }
    }

    private fun saveVideo(isEdit: Boolean): Boolean {
        disableFields(false)
        val error = viewModel.validate()
        if (error.isNotEmpty()) {
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
            return false
        }

        if (binding.standaloneSwitch.isChecked) {
            createStandAloneVideoItem()
        }
        val saveObj = viewModel.getSaveObject()
        controller?.let { ctrl ->
            val result = if (isEdit) {
                ctrl.UpdateVideo(saveObj)
            } else {
                ctrl.AddVideo(saveObj)
            }
            if (result) {
                notifyDataChanged()
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
        disableFields(true)
        return false
    }

    private fun createStandAloneVideoItem() {
        val formatText = binding.formatAutocomplete.autocomplete.text.toString()
        val format = try {
            VideoFormat.valueOf(formatText)
        } catch (_: Exception) {
            VideoFormat.NoneSelected
        }

        val item = VideoItem().apply {
            Watched = binding.watchedSwitch.isChecked
            Owned = binding.ownedSwitch.isChecked
            Season = -1
            Format = format
        }
        viewModel.clearItems()
        viewModel.addOrUpdateItem(item, 0)
    }
    private fun disableFields(enabled: Boolean) {
        binding.titleInput.isEnabled = enabled
        binding.videoTypeRadio.dynamicTableLayout.children.forEach { child ->
            if (child is ViewGroup) {
                child.children.forEach { subChild ->
                    if (subChild is RadioButton) {
                        subChild.isEnabled = enabled
                    }
                }
            }
        }
        binding.videoCategoryRadio.dynamicTableLayout.children.forEach { child ->
            if (child is ViewGroup) {
                child.children.forEach { subChild ->
                    if (subChild is RadioButton) {
                        subChild.isEnabled = enabled
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
        binding.standaloneSwitch.isEnabled = enabled
        binding.formatAutocomplete.autocomplete.isEnabled = enabled
        binding.watchedSwitch.isEnabled = enabled
        binding.ownedSwitch.isEnabled = enabled
    }

    override fun onCoverImageUpdated(byteArray: ByteArray?, target: String?) {
        if (target == "video" || target == "book" || target == "main") {
            viewModel.updateCover(byteArray)
        }
    }

    private fun handleRadioSelectionChange(id: Int) {
        val genre = VideoTag.entries.find { it.ordinal == id } ?: return
        viewModel.updateVideoTag(genre)
    }

    private fun setupVideoTypeRadioGroup(types: Map<Int, String>) {
        binding.videoTypeRadio.radioButtonLabel.setText(R.string.video_type)
        val tableLayout = binding.videoTypeRadio.dynamicTableLayout
        val videoType = types.filter { it.key != VideoType.NoneSelected.ordinal }
        RadioGridUtils.populateRadioGridFromMap(
            tableLayout = tableLayout,
            optionsMap = videoType,
            columnCount = 2
        ) { selectedId ->
            handleVideoTypeSelectionChange(selectedId)
        }
    }

    private fun setupVideoTagRadioGroup(videoTags: Map<Int, String>) {
        binding.videoCategoryRadio.radioButtonLabel.setText(R.string.video_tag)
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

    private fun setupTagSelection(setup: VideoSetup) {
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

        tagAutoComplete.doAfterTextChanged { s ->
            val text = s.toString()
            val existing = tags.find { it.Name == text }
            if (existing == null) {
                viewModel.updateTagName(text)
            } else {
                viewModel.updateTag(existing)
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
        binding.titleInput.doAfterTextChanged { s ->
            viewModel.updateTitle(s.toString())
            binding.titleLabel.error = ""
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

        val formats = VideoFormat.entries.filter { it != VideoFormat.NoneSelected }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            formats.map { it.name }
        )
        sb.formatAutocomplete.autocomplete.setAdapter(adapter)
        sb.formatAutocomplete.autoCompleteLabel.setHint(R.string.format_label)

        item?.let {
            sb.seasonInput.setText(it.Season.toString())
            sb.titleInput.setText(it.DiscTitle)
            sb.ownedSwitch.isChecked = it.Owned
            sb.watchedSwitch.isChecked = it.Watched

            sb.formatAutocomplete.autocomplete.setText(it.Format.name, false)

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

            if (volNum.isBlank()) {
                sb.seasonLabel.error = "Volume number is required"
                Toast.makeText(requireContext(), "Volume number is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveVideoItem(sb, volNum, position)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun saveVideoItem(sb: VideoItemBottomSheetBinding, volNum: String, position: Int) {
        val formatText = sb.formatAutocomplete.autocomplete.text.toString()
        val format = try {
            VideoFormat.valueOf(formatText)
        } catch (_: Exception) {
            VideoFormat.NoneSelected
        }

        val newItem = VideoItem().apply {
            Season = volNum.toIntOrNull() ?: 0
            DiscTitle = sb.titleInput.text?.toString() ?: ""
            Owned = sb.ownedSwitch.isChecked
            Watched = sb.watchedSwitch.isChecked
            Format = format
            ItemCover = sb.imageItemCover.imageBookCover.tag as? ByteArray
        }

        viewModel.addOrUpdateItem(newItem, position)
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
            holder.bind(items[position], position)
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(val binding: BookItemVolumeBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: VideoItem, position: Int) {
                val context = binding.root.context

                itemView.setOnClickListener {
                    val intent = Intent(context, VideoFormActivity::class.java).apply {
                        putExtra("EXTRA_ID", item.Id)
                        putExtra("EXTRA_IS_EDIT", true)
                    }
                    context.startActivity(intent)
                }

                if (item.Season != -1 && item.DiscTitle.isNullOrEmpty()) {
                    binding.textVolumeInfo.text = item.Season.toString()
                } else {
                    if (item.Season == -1) {
                        binding.textVolumeInfo.text = context.getString(R.string.standalone)
                    } else {
                        binding.textVolumeInfo.text = context.getString(
                            R.string.video_item_display_text,
                            item.Season,
                            item.DiscTitle
                        )
                    }
                }

                binding.textStatusInfo.text = context.getString(
                    R.string.movie_status_format,
                    if (item.Owned) "Yes" else "No",
                    if (item.Watched) "Yes" else "No"
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
