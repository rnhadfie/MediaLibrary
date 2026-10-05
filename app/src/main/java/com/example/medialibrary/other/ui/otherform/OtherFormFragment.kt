package com.example.medialibrary.other.ui.otherform

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
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
        fun newInstance(id: String = "-1", isEdit: Boolean = false) = OtherFormFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_ID, id)
                putBoolean(ARG_IS_EDIT, isEdit)
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

        if (isEdit && itemId != "-1") {
            viewModel.loadOtherCollection(itemId, controller)
        }

        val setup = controller?.GetSetup() ?: MainSetup()

        setupTagSelection(setup)
        setupCollectingPriorityDropdown(binding.collectingPriorityAutocomplete) { priority ->
            viewModel.updateCollectingPriority(priority)
        }
        setupRecyclerView()
        setupInputListeners()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.changeImage.buttonChangeCover.setOnClickListener {
            showImageOptionsDialog("other")
        }
        binding.changeImage.buttonClearCover.setOnClickListener {
            clearImage("other")
        }
        binding.buttonAddItem.setOnClickListener {
            showOtherItemSheet()
        }
        binding.buttonSaveOther.setOnClickListener {
            handleSaveAction()
        }
    }

    private fun observeViewModel() {
        viewModel.other.observe(viewLifecycleOwner) { other ->
            binding.editOtherTitle.setText(other.Title)
            binding.otherCollecting.isChecked = other.Collecting ?: false
            binding.otherCompletedCollecting.isChecked = other.HasCollectedAllItems ?: false

            val currentPriority = other.CollectingPriority ?: SharedEnums.CollectingPriority.None
            binding.collectingPriorityAutocomplete.autocomplete.setText(currentPriority.name, false)

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
    }

    private fun handleSaveAction() {
        val error = viewModel.validateFields()
        if (error.isNotEmpty()) {
            if (error.containsKey("general")) {
                Toast.makeText(requireContext(), error["general"], Toast.LENGTH_SHORT).show()
            }
            if (error.containsKey("title")) {
                binding.editOtherTitleLabel.error = error["title"]
                Toast.makeText(requireContext(), error["title"], Toast.LENGTH_SHORT).show()
            }
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
                    activity?.finish()
                } else {
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

        binding.otherCollecting.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleCollecting(isChecked)
        }

        binding.otherCompletedCollecting.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleCollectionComplete(isChecked)
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

        sheetBinding.imageItemCover.buttonChangeCover.setOnClickListener {
            showImageOptionsDialog("item")
        }
        sheetBinding.imageItemCover.buttonClearCover.setOnClickListener {
            clearImage("item")
        }

        sheetBinding.buttonSheetSave.setOnClickListener {
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
                } else {
                    ImageUtils.setPlaceholderCover(binding.imageItemCover, context, resources)
                }

                binding.buttonEditItem.setOnClickListener { onEdit(item, position) }
                binding.buttonDeleteItem.setOnClickListener { onDelete(position) }
            }
        }
    }
}
