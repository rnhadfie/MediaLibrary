package com.example.medialibrary.music.ui.form

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medialibrary.BaseFormFragment
import com.example.medialibrary.R
import com.example.medialibrary.databinding.BookItemVolumeBinding
import com.example.medialibrary.databinding.MusicFragmentFormBinding
import com.example.medialibrary.databinding.MusicItemBottomSheetBinding
import com.example.medialibrary.utils.ImageUtils
import com.example.medialibrary.utils.RadioGridUtils
import com.example.medialibrary.utils.YearPickerHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import controllers.MusicController
import models.music.Enums.MusicGenre
import models.music.MusicItem
import models.music.MusicSetup
import models.shared.Tag
import repository.database.MediaLibraryDbHelper
import kotlin.sequences.forEach
import models.shared.Enums as SharedEnums

class MusicFormFragment : BaseFormFragment<MusicFragmentFormBinding, MusicFormViewModel>(
    MusicFragmentFormBinding::inflate
) {

    companion object {
        fun newInstance(id: String = "-1", isEdit: Boolean = false, isView: Boolean = false) = MusicFormFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ID, id)
                putBoolean(ARG_IS_EDIT, isEdit)
                putBoolean(ARG_IS_VIEW, isView)
            }
        }
    }

    private lateinit var itemAdapter: MusicItemAdapter
    private var controller: MusicController? = null
    private var setup: MusicSetup = MusicSetup()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[MusicFormViewModel::class.java]

        val dbHelper = MediaLibraryDbHelper(requireContext())
        controller = MusicController(dbHelper)

        setup = controller?.GetMusicSetup() ?: MusicSetup()

        if ((isEdit || isView) && itemId != "-1") {
            viewModel.loadCd(itemId, controller)
        }

        setupRecyclerView()
        setBindings(setup)
        observeViewModel()
    }

    private fun setupRecyclerView() {
        itemAdapter = MusicItemAdapter(
            onEdit = { position, item -> showMusicItemSheet(item, position) },
            onDelete = { position -> viewModel.deleteItem(position) }
        )
        binding.recyclerviewMusicItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.music.observe(viewLifecycleOwner) { music ->
            binding.editMusicArtist.setText(music.Artist)

            val currentPriority = music.CollectingPriority ?: SharedEnums.CollectingPriority.None
            RadioGridUtils.setSelection(
                binding.collectingPriorityRadioGroup.dynamicTableLayout,
                currentPriority.ordinal
            )
        }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            itemAdapter.submitList(items.toList())
        }

        if (isView) {
            disableForm(false)
        }
    }

    private fun setBindings(setup: MusicSetup) {
        setupTagSelection(setup)

        setupCollectingPriorityRadioGroup(binding.collectingPriorityRadioGroup) { priority ->
            viewModel.updateCollectingPriority(priority)
        }
        setupInputListeners()

        binding.buttonAddItem.setOnClickListener {
            showMusicItemSheet()
        }

        if (isView) {
            binding.buttonSaveMusic.setText(R.string.close)
            binding.buttonSaveMusic.setOnClickListener { activity?.finish() }
        } else {
            binding.buttonSaveMusic.setOnClickListener {
                handleSaveAction()
            }
        }
    }

    private fun handleSaveAction() {
        disableForm(false)
        val error = viewModel.validate()
        if (error.isNotEmpty()) {
            if (error.containsKey("general")) {
                Toast.makeText(requireContext(), error["general"], Toast.LENGTH_SHORT).show()
            }
            if (error.containsKey("artist")) {
                binding.editMusicArtistLabel.error = error["artist"]
                Toast.makeText(requireContext(), error["artist"], Toast.LENGTH_SHORT).show()
            }
            disableForm(true)
        } else {
            val saveObj = viewModel.getSaveObject()
            controller?.let { ctrl ->
                val result = if (isEdit) {
                    ctrl.UpdateMusic(saveObj)
                } else {
                    ctrl.AddMusic(saveObj)
                }

                if (result) {
                    notifyDataChanged()
                    Toast.makeText(
                        requireContext(),
                        if (isEdit) "Music Saved" else "Music Saved",
                        Toast.LENGTH_SHORT
                    ).show()
                    activity?.finish()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Music Failed to Save",
                        Toast.LENGTH_SHORT
                    ).show()
                    disableForm(true)
                }
            } ?: disableForm(true)
        }
    }

    private var currentSheetCover: ByteArray? = null

    private fun showMusicItemSheet(item: MusicItem? = null, position: Int = -1) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = MusicItemBottomSheetBinding.inflate(layoutInflater)
        currentSheetBinding = sheetBinding
        dialog.setContentView(sheetBinding.root)
        dialog.setOnDismissListener { currentSheetBinding = null }

        currentSheetCover = item?.Cover
        var selectedGenre = item?.MusicGenre ?: MusicGenre.NoneSelected

        sheetBinding.labelText.text = if (item == null) "Add Item" else "Edit Item"
        sheetBinding.editSheetMusicTitle.setText(item?.Title ?: "")
        sheetBinding.editSheetMusicYear.setText(item?.Year?.toString() ?: "")
        sheetBinding.editSheetMusicYear.setOnClickListener {
            if (!isView) {
                val currentYear = sheetBinding.editSheetMusicYear.text.toString().toIntOrNull()
                YearPickerHelper.showYearPickerDialog(requireContext(), currentYear, title = "Select CD Release Year") { year ->
                    sheetBinding.editSheetMusicYear.setText(year.toString())
                }
            }
        }
        sheetBinding.editSheetMusicYearLabel.setEndIconOnClickListener {
            if (!isView) {
                val currentYear = sheetBinding.editSheetMusicYear.text.toString().toIntOrNull()
                YearPickerHelper.showYearPickerDialog(requireContext(), currentYear, title = "Select CD Release Year") { year ->
                    sheetBinding.editSheetMusicYear.setText(year.toString())
                }
            }
        }
        sheetBinding.switchSheetCollecting.isChecked = item?.Collecting ?: false
        sheetBinding.switchSheetOwned.isChecked = item?.Owned ?: false

        if (currentSheetCover != null && currentSheetCover!!.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeByteArray(currentSheetCover, 0, currentSheetCover!!.size)
            sheetBinding.imageItemCover.imageBookCover.setImageBitmap(bitmap)
            sheetBinding.imageItemCover.buttonClearCover.visibility = View.VISIBLE
        } else {
            ImageUtils.setPlaceholderCover(sheetBinding.imageItemCover.imageBookCover, requireContext(), resources)
            sheetBinding.imageItemCover.buttonClearCover.visibility = View.GONE
        }

        sheetBinding.imageItemCover.imageBookCover.setOnClickListener {
            val coverBytes = currentSheetCover ?: item?.Cover
            if (coverBytes != null && coverBytes.isNotEmpty()) {
                ImageUtils.showImagePreviewDialog(requireContext(), coverBytes, item?.Title ?: "CD Cover")
            }
        }
        sheetBinding.imageItemCover.buttonChangeCover.setOnClickListener {
            showImageOptionsDialog("sheet_cover")
        }
        sheetBinding.imageItemCover.buttonClearCover.setOnClickListener {
            currentSheetCover = null
            ImageUtils.setPlaceholderCover(sheetBinding.imageItemCover.imageBookCover, requireContext(), resources)
            sheetBinding.imageItemCover.buttonClearCover.visibility = View.GONE
        }

        sheetBinding.cancelButton.setOnClickListener { dialog.dismiss() }

        // Setup genre grid
        sheetBinding.musicGenreRadio.radioButtonLabel.setText(R.string.music_genre)
        val musicGenres = setup.MusicGenre.filter { it.key != MusicGenre.NoneSelected.ordinal }
        RadioGridUtils.populateRadioGridFromMap(
            tableLayout = sheetBinding.musicGenreRadio.dynamicTableLayout,
            optionsMap = musicGenres,
            columnCount = 2
        ) { selectedId ->
            selectedGenre = MusicGenre.entries.find { it.ordinal == selectedId } ?: MusicGenre.NoneSelected
        }

        if (selectedGenre != MusicGenre.NoneSelected) {
            RadioGridUtils.setSelection(
                sheetBinding.musicGenreRadio.dynamicTableLayout,
                selectedGenre.ordinal
            )
        }

        sheetBinding.buttonSheetSave.setOnClickListener {
            if (isView)
            {
                dialog.dismiss()
                return@setOnClickListener
            }
            val title = sheetBinding.editSheetMusicTitle.text.toString().trim()
            if (title.isBlank()) {
                sheetBinding.editSheetMusicTitleLabel.error = "Title is required"
                return@setOnClickListener
            }

            val yearText = sheetBinding.editSheetMusicYear.text.toString().trim()
            val year = yearText.toIntOrNull() ?: 0

            val newItem = (item ?: MusicItem()).apply {
                Title = title
                Year = year
                MusicGenre = selectedGenre
                Collecting = sheetBinding.switchSheetCollecting.isChecked
                Owned = sheetBinding.switchSheetOwned.isChecked
                Cover = currentSheetCover
            }

            viewModel.addOrUpdateItem(newItem, position)
            dialog.dismiss()
        }

        if(isView)
        {
            sheetBinding.buttonSheetSave.setText(R.string.close)
            sheetBinding.editSheetMusicTitle.isEnabled = false
            sheetBinding.editSheetMusicYear.isEnabled = false
            sheetBinding.switchSheetCollecting.isEnabled = false
            sheetBinding.switchSheetOwned.isEnabled = false
            sheetBinding.imageItemCover.buttonChangeCover.visibility = View.GONE
            sheetBinding.imageItemCover.buttonClearCover.visibility = View.GONE
            sheetBinding.imageItemCover.imageBookCover.isEnabled = false
            sheetBinding.musicGenreRadio.dynamicTableLayout.children.forEach { child ->
                if (child is ViewGroup) {
                    child.children.forEach { subChild ->
                        if (subChild is RadioButton) {
                            subChild.isEnabled = false
                        }
                    }
                }
            }
        }

        dialog.show()
    }

    override fun onCoverImageUpdated(byteArray: ByteArray?, target: String?) {
        currentSheetCover = byteArray
    }

    private fun setupTagSelection(setup: MusicSetup) {
        val tags = setup.Tags
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

    private fun setupInputListeners() {
        binding.editMusicArtist.doAfterTextChanged { s ->
            viewModel.updateArtist(s.toString())
            binding.editMusicArtistLabel.error = null
        }
    }

    private fun disableForm(enabled: Boolean) {
        binding.editMusicArtist.isEnabled = enabled
        binding.tagAutocomplete.autocomplete.isEnabled = enabled
        binding.buttonAddItem.isEnabled = enabled && !isView
        binding.buttonAddItem.visibility = if (isView) View.GONE else View.VISIBLE
        binding.tagAutocomplete.autoCompleteLabel.isEnabled = enabled
        if (!isView) {
            binding.buttonSaveMusic.isEnabled = enabled
        }
        binding.collectingPriorityRadioGroup.dynamicTableLayout.children.forEach { child ->
            if (child is ViewGroup) {
                child.children.forEach { subChild ->
                    if (subChild is RadioButton) {
                        subChild.isEnabled = enabled
                    }
                }
            }
        }
    }

    inner class MusicItemAdapter(
        private val onEdit: (Int, MusicItem) -> Unit,
        private val onDelete: (Int) -> Unit
    ) : RecyclerView.Adapter<MusicItemAdapter.ViewHolder>() {
            private var items: List<MusicItem> = emptyList()

            fun submitList(newList: List<MusicItem>) {
                items = newList
                notifyDataSetChanged()
            }

            override fun getItemCount() = items.size

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val binding = BookItemVolumeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolder(binding)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                holder.bind(items[position], position)
            }

            inner class ViewHolder(val binding: BookItemVolumeBinding)
                : RecyclerView.ViewHolder(binding.root)
            {
                fun bind(item: MusicItem, position: Int) {
                    val context = binding.root.context
                    binding.textVolumeInfo.text = item.Title
                    val collectingStr = if (item.Collecting == true) "Collecting" else "Not Collecting"
                    val ownedStr = if (item.Owned == true) "Owned" else "Not Owned"
                    "Year: ${item.Year ?: 0} | Genre: ${item.MusicGenre?.name ?: "None"} | $collectingStr | $ownedStr".also { binding.textStatusInfo.text = it }

                    if (item.Cover != null && item.Cover.isNotEmpty()) {
                        val bitmap = BitmapFactory.decodeByteArray(item.Cover, 0, item.Cover.size)
                       binding.imageItemCover.scaleType = ImageView.ScaleType.CENTER_CROP
                        binding.imageItemCover.setImageBitmap(bitmap)
                        binding.imageItemCover.imageTintList = null
                        binding.imageItemCover.setOnClickListener {
                            ImageUtils.showImagePreviewDialog(itemView.context, item.Cover, item.Title)
                        }
                    } else {
                        ImageUtils.setPlaceholderCover(binding.imageItemCover, itemView.context, itemView.resources)
                    }

                    if (isView) {
                        binding.buttonEditItem.setImageResource(android.R.drawable.ic_menu_view)
                        binding.buttonDeleteItem.visibility = View.GONE
                    } else {
                        binding.buttonEditItem.setImageResource(android.R.drawable.ic_menu_edit)
                        binding.buttonEditItem.visibility = View.VISIBLE
                        binding.buttonDeleteItem.visibility = View.VISIBLE
                    }
                    binding.buttonEditItem.setOnClickListener { onEdit(position, item) }
                    binding.buttonDeleteItem.setOnClickListener { onDelete(position) }
                }
            }
        }





}
