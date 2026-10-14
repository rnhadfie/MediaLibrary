package com.example.medialibrary.other.ui.otherform

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
import com.example.medialibrary.databinding.OtherFragmentFormBinding
import com.example.medialibrary.databinding.OtherItemBottomSheetBinding
import com.example.medialibrary.utils.ImageUtils
import com.example.medialibrary.utils.RadioGridUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import controllers.OtherController
import models.other.OtherItem
import models.shared.MainSetup
import models.shared.Tag
import repository.database.MediaLibraryDbHelper
import models.shared.Enums as SharedEnums

class OtherFormFragment : BaseFormFragment<OtherFragmentFormBinding, OtherFormViewModel>(
    OtherFragmentFormBinding::inflate
) {

    companion object {
        fun newInstance(id: String = "-1", isEdit: Boolean = false, isView: Boolean = false) = OtherFormFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ID, id)
                putBoolean(ARG_IS_EDIT, isEdit)
                putBoolean(ARG_IS_VIEW, isView)
            }
        }
    }

    private lateinit var itemAdapter: OtherItemAdapter
    private var controller: OtherController? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[OtherFormViewModel::class.java]

        val dbHelper = MediaLibraryDbHelper(requireContext())
        controller = OtherController(dbHelper)

        if ((isEdit || isView)  && itemId != "-1") {
            viewModel.loadOtherCollection(itemId, controller)
        }

        val setup = controller?.GetSetup() ?: MainSetup()

        setupTagSelection(setup)

        setupCollectingPriorityRadioGroup(binding.collectingPriorityRadioGroup) { priority ->
            viewModel.updateCollectingPriority(priority)
        }

        setupRecyclerView()
        setupInputListeners()
        setupCollectingStatusRadioGroup()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.changeImage.imageBookCover.setOnClickListener {
            val cover = viewModel.other.value?.Cover
            if (cover != null && cover.isNotEmpty()) {
                ImageUtils.showImagePreviewDialog(requireContext(), cover, viewModel.other.value?.Title ?: "Collection Cover")
            }
        }
        binding.changeImage.buttonChangeCover.setOnClickListener {
            showImageOptionsDialog("other")
        }
        binding.changeImage.buttonClearCover.setOnClickListener {
            clearImage("other")
        }
        binding.buttonAddItem.setOnClickListener {
            showOtherItemSheet()
        }
        if (isView) {
            binding.buttonSaveOther.text = "Close"
            binding.buttonSaveOther.setOnClickListener { activity?.finish() }
        } else {
            binding.buttonSaveOther.setOnClickListener {
                handleSaveAction()
            }
        }
    }

    private fun setupCollectingStatusRadioGroup() {
        binding.collectingStatusRadio.radioButtonLabel.setText(R.string.collecting_status)
        val optionsMap = mapOf(
            0 to "No Collecting",
            1 to "Collecting",
            2 to "Collected"
        )
        RadioGridUtils.populateRadioGridFromMap(
            tableLayout = binding.collectingStatusRadio.dynamicTableLayout,
            optionsMap = optionsMap,
            columnCount = 3,
            selectedId = 0
        ) { selectedId ->
            when (selectedId) {
                0 -> {
                    viewModel.toggleCollecting(false)
                    viewModel.toggleCollectionComplete(false)
                }
                1 -> {
                    viewModel.toggleCollecting(true)
                    viewModel.toggleCollectionComplete(false)
                }
                2 -> {
                    viewModel.toggleCollecting(false)
                    viewModel.toggleCollectionComplete(true)
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.other.observe(viewLifecycleOwner) { other ->
            binding.editOtherTitle.setText(other.Title)

            val statusId = when {
                other.Collecting == true -> 1
                other.HasCollectedAllItems == true -> 2
                else -> 0
            }
            RadioGridUtils.setSelection(
                binding.collectingStatusRadio.dynamicTableLayout,
                statusId
            )

            val currentPriority = other.CollectingPriority ?: SharedEnums.CollectingPriority.None
            RadioGridUtils.setSelection(
                binding.collectingPriorityRadioGroup.dynamicTableLayout,
                currentPriority.ordinal
            )
            if (other.Cover != null && other.Cover.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(other.Cover, 0, other.Cover.size)
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

        if (isView) {
            disableForm(false)
        }
    }

    private fun handleSaveAction() {
        disableForm(false)
        val error = viewModel.validateFields()

        if (error.isNotEmpty()) {
            if (error.containsKey("general")) {
                Toast.makeText(requireContext(), error["general"], Toast.LENGTH_SHORT).show()
            }
            if (error.containsKey("title")) {
                binding.editOtherTitleLabel.error = error["title"]
                Toast.makeText(requireContext(), error["title"], Toast.LENGTH_SHORT).show()
            }
            disableForm(true)
        } else {
            val saveObj = viewModel.getSaveObject()
            controller?.let { ctrl ->
                val result = if (isEdit) {
                    ctrl.UpdateOther(saveObj)
                } else {
                    ctrl.AddOther(saveObj)
                }

                if (result) {
                    notifyDataChanged()
                    Toast.makeText(
                        requireContext(),
                        if (isEdit) "Other Collection Updated" else "Other Collection Saved",
                        Toast.LENGTH_SHORT
                    ).show()
                    disableForm(true)
                    activity?.finish()
                } else {
                    disableForm(true)
                    Toast.makeText(
                        requireContext(),
                        "Failed to Save Collection",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onCoverImageUpdated(byteArray: ByteArray?, target: String?) {
        if (target == "other" || target == "main") {
            viewModel.updateCover(byteArray)
        }
    }


    private fun disableForm(enabled: Boolean) {
        binding.editOtherTitle.isEnabled = enabled
        binding.collectingStatusRadio.dynamicTableLayout.children.forEach { child ->
            if (child is ViewGroup) {
                child.children.forEach { subChild ->
                    if (subChild is RadioButton) {
                        subChild.isEnabled = enabled
                    }
                }
            }
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
        binding.changeImage.buttonChangeCover.isEnabled = enabled
        binding.changeImage.buttonClearCover.isEnabled = enabled
        binding.tagAutocomplete.autoCompleteLabel.isEnabled = enabled
        binding.tagAutocomplete.autocomplete.isEnabled = enabled
        binding.buttonAddItem.isEnabled = enabled
        if(!isView) {
            binding.buttonSaveOther.isEnabled = enabled
        }
    }

    private fun setupTagSelection(setup: MainSetup) {
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

    private fun setupRecyclerView() {
        itemAdapter = OtherItemAdapter(
            onEdit = { item, pos -> showOtherItemSheet(item, pos) },
            onDelete = { pos -> viewModel.deleteItem(pos) }
        )
        binding.recyclerOtherItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAdapter
        }
    }

    private fun setupInputListeners() {
        binding.editOtherTitle.doAfterTextChanged { s ->
            viewModel.updateTitle(s.toString())
            binding.editOtherTitleLabel.error = null
        }

    }

    private fun showOtherItemSheet(item: OtherItem? = null, position: Int = -1) {
        val dialog = BottomSheetDialog(requireContext())
        dialog.setCancelable(false)
        val sheetBinding = OtherItemBottomSheetBinding.inflate(layoutInflater)
        currentSheetBinding = sheetBinding
        pendingItemPosition = position
        dialog.setContentView(sheetBinding.root)

        sheetBinding.labelText.text = if (item == null) "Add Item" else "Edit Item"
        sheetBinding.cancelButton.setOnClickListener { dialog.dismiss() }

        item?.let {
            sheetBinding.editSheetVolumeTitle.setText(it.Title)
            sheetBinding.switchSheetOwned.isChecked = it.Owned
            if (it.ItemCover != null && it.ItemCover.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(it.ItemCover, 0, it.ItemCover.size)
                sheetBinding.imageItemCover.imageBookCover.scaleType = ImageView.ScaleType.CENTER_CROP
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

        sheetBinding.imageItemCover.imageBookCover.setOnClickListener {
            val coverBytes = sheetBinding.imageItemCover.imageBookCover.tag as? ByteArray ?: item?.ItemCover
            if (coverBytes != null && coverBytes.isNotEmpty()) {
                ImageUtils.showImagePreviewDialog(requireContext(), coverBytes, item?.Title ?: "Item Cover")
            }
        }
        sheetBinding.imageItemCover.buttonChangeCover.setOnClickListener {
            showImageOptionsDialog("item")
        }
        sheetBinding.imageItemCover.buttonClearCover.setOnClickListener {
            clearImage("item")
        }

        sheetBinding.buttonSheetSave.setOnClickListener {
            if(isView)
            {
                dialog.dismiss()
                return@setOnClickListener
            }
            val title = sheetBinding.editSheetVolumeTitle.text.toString()
            if (title.isBlank()) {
                sheetBinding.editSheetVolumeTitleLabel.error = "Title is required"
                Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newItem = OtherItem().apply {
                Title = title
                Owned = sheetBinding.switchSheetOwned.isChecked
                ItemCover = sheetBinding.imageItemCover.imageBookCover.tag as? ByteArray
            }

            viewModel.addOrUpdateItem(newItem, position)
            dialog.dismiss()
        }

        if(isView)
        {
            sheetBinding.editSheetVolumeTitle.isEnabled = false
            sheetBinding.switchSheetOwned.isEnabled = false
            sheetBinding.imageItemCover.buttonChangeCover.isEnabled = false;
            sheetBinding.imageItemCover.buttonClearCover.isEnabled = false;
            sheetBinding.buttonSheetSave.setText(R.string.close)
        }

        dialog.show()
    }

    inner class OtherItemAdapter(
        private val onEdit: (OtherItem, Int) -> Unit,
        private val onDelete: (Int) -> Unit
    ) : RecyclerView.Adapter<OtherItemAdapter.ViewHolder>() {

        private var items: List<OtherItem> = emptyList()

        fun submitList(newList: List<OtherItem>) {
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
            fun bind(item: OtherItem, position: Int) {
                val context = binding.root.context

                binding.textVolumeInfo.text = item.Title ?: ""

                binding.textStatusInfo.text = context.getString(
                    R.string.Other_volume_status_format,
                    if (item.Owned) "Yes" else "No"
                )

                if (item.ItemCover != null && item.ItemCover.isNotEmpty()) {
                    val bitmap = BitmapFactory.decodeByteArray(item.ItemCover, 0, item.ItemCover.size)
                    binding.imageItemCover.scaleType = ImageView.ScaleType.CENTER_CROP
                    binding.imageItemCover.setImageBitmap(bitmap)
                    binding.imageItemCover.imageTintList = null
                    binding.imageItemCover.setOnClickListener {
                        ImageUtils.showImagePreviewDialog(context, item.ItemCover, item.Title)
                    }
                } else {
                    ImageUtils.setPlaceholderCover(binding.imageItemCover, context, resources)
                }

                if (isView) {
                    binding.buttonEditItem.setImageResource(android.R.drawable.ic_menu_view)
                    binding.buttonDeleteItem.visibility = View.GONE
                } else {
                    binding.buttonEditItem.setImageResource(android.R.drawable.ic_menu_edit)
                    binding.buttonEditItem.visibility = View.VISIBLE
                    binding.buttonDeleteItem.visibility = View.VISIBLE
                }
                binding.buttonEditItem.setOnClickListener { onEdit(item, position) }
                binding.buttonDeleteItem.setOnClickListener { onDelete(position) }
            }
        }
    }
}
