package com.example.medialibrary.other.ui.otherform

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.ViewModelProvider
import com.example.medialibrary.utils.SharedRefreshViewModel
import com.example.medialibrary.backend.controllers.OtherController
import com.example.medialibrary.backend.models.other.OtherItem
import com.example.medialibrary.backend.models.shared.MainSetup
import com.example.medialibrary.backend.models.shared.Tag
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.OtherItemBottomSheetBinding
import com.example.medialibrary.databinding.OtherFragmentFormBinding
import com.example.medialibrary.databinding.BookItemVolumeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.ByteArrayOutputStream

class OtherFormFragment : Fragment() {

    companion object {
        private const val ARG_ID = "arg_id"
        private const val ARG_IS_EDIT = "arg_is_edit"

        fun newInstance(id: Int = -1, isEdit: Boolean = false) = OtherFormFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_ID, id)
                putBoolean(ARG_IS_EDIT, isEdit)
            }
        }
    }

    private val viewModel: OtherFormViewModel by viewModels()
    private var _binding: OtherFragmentFormBinding? = null
    private val binding get() = _binding!!

    private lateinit var itemAdapter: OtherItemAdapter

    private var pendingImageTarget: String? = null // "book" or "item"
    private var pendingItemPosition: Int = -1
    private var currentSheetBinding: OtherItemBottomSheetBinding? = null

    private var controller: OtherController? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                val inputStream = requireContext().contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
                val byteArray = outputStream.toByteArray()

                if (pendingImageTarget == "other") {
                    viewModel.updateCover(byteArray)
                    binding.imageOtherCover.setImageBitmap(bitmap)
                    binding.imageOtherCover.imageTintList = null
                } else if (pendingImageTarget == "item") {
                    currentSheetBinding?.let { sheet ->
                        sheet.imageItemCover.imageBookCover.setImageBitmap(bitmap)
                        sheet.imageItemCover.imageBookCover.imageTintList = null
                        // We store the byte array in the Tag or similar until saved
                        sheet.imageItemCover.imageBookCover.tag = byteArray
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Initialize controller
        val dbHelper = MediaLibraryDbHelper(requireContext())
        controller = OtherController(dbHelper)

        _binding = OtherFragmentFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookId = arguments?.getInt(ARG_ID) ?: -1
        val isEdit = arguments?.getBoolean(ARG_IS_EDIT) ?: false

        if (isEdit && bookId != -1) {
            viewModel.loadOtherCollection(bookId, controller)
        }

        var setup = controller?.GetSetup()

        if(setup == null)
            setup = MainSetup()

        setupTagSelection(setup)
        setupRecyclerView()
        setupInputListeners()

        binding.buttonChangeCover.setOnClickListener {
            pendingImageTarget = "other"
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
            pickImageLauncher.launch(intent)
        }

        binding.buttonAddItem.setOnClickListener {
            showOtherItemSheet()
        }

        binding.buttonSaveBook.setOnClickListener {
            val error = viewModel.validate()
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            } else {
                val saveObj = viewModel.getSaveObject()
                if(controller != null) {

                    val result = if (isEdit) {
                        controller!!.UpdateOther(saveObj)
                    } else {
                        controller!!.AddOther(saveObj)
                    }

                    if(result) {
                        ViewModelProvider(requireActivity())[SharedRefreshViewModel::class.java].incrementVersion()
                        Toast.makeText(
                            requireContext(),
                            if (isEdit) "Other Collection Updated" else "Other Collection Saved",
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
        viewModel.other.observe(viewLifecycleOwner) { other ->
            binding.editBookTitle.setText(other.Title)
            binding.otherCollecting.isChecked = other.Collecting ?: false
            binding.otherCollected.isChecked = other.HasCollectedAllItems ?: false

            if (other.Cover != null) {
                val bitmap = BitmapFactory.decodeByteArray(other.Cover, 0, other.Cover.size)
                binding.imageOtherCover.setImageBitmap(bitmap)
                binding.imageOtherCover.imageTintList = null
            }


        }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            itemAdapter.submitList(items.toList())
        }

    }



    private fun setupTagSelection(setup: MainSetup) {
        val tags = setup.Tag
        val adapter = ArrayAdapter<Tag>(requireContext(), android.R.layout.simple_dropdown_item_1line, tags)
        binding.tagAutocomplete.autocomplete.setAdapter(adapter)
        binding.tagAutocomplete.autoCompleteLabel.setHint(com.example.medialibrary.R.string.tag)
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
        binding.editBookTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { viewModel.updateTitle(s.toString()) }
        })

        binding.otherCollecting.setOnCheckedChangeListener {
                _, isChecked -> viewModel.toggleCollecting(isChecked)
        }

        binding.otherCollected.setOnCheckedChangeListener {
                _, isChecked -> viewModel.toggleCollectionComplete(isChecked)
        }
    }

    private fun showOtherItemSheet(item: OtherItem? = null, position: Int = -1) {
        val dialog = BottomSheetDialog(requireContext())
        val sheetBinding = OtherItemBottomSheetBinding.inflate(layoutInflater)
        currentSheetBinding = sheetBinding
        pendingItemPosition = position
        dialog.setContentView(sheetBinding.root)

        sheetBinding.textSheetTitle.text = if (item == null) "Add Item" else "Edit Item"



        // Populate if editing
        item?.let {
            sheetBinding.editSheetVolumeTitle.setText(it.Title)
            sheetBinding.switchSheetOwned.isChecked = it.Owned
            if (it.ItemCover != null) {
                val bitmap = BitmapFactory.decodeByteArray(it.ItemCover, 0, it.ItemCover.size)
                sheetBinding.imageItemCover.imageBookCover.setImageBitmap(bitmap)
                sheetBinding.imageItemCover.imageBookCover.imageTintList = null
                sheetBinding.imageItemCover.imageBookCover.tag = it.ItemCover
            }
        }

        sheetBinding.imageItemCover.buttonChangeCover.setOnClickListener {
            pendingImageTarget = "item"
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
            pickImageLauncher.launch(intent)
        }

        sheetBinding.buttonSheetSave.setOnClickListener {
            val volNum = sheetBinding.editSheetVolumeTitle.text.toString()
            if (volNum.isBlank()) {
                Toast.makeText(requireContext(), "Volume number is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newItem = OtherItem().apply {
                Title = sheetBinding.editSheetVolumeTitle.text.toString()
                Owned = sheetBinding.switchSheetOwned.isChecked
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
            val item = items[position]
            val context = holder.binding.root.context

            holder.binding.textVolumeInfo.text = item.Title ?: ""

            holder.binding.textStatusInfo.text = context.getString(
                com.example.medialibrary.R.string.Other_volume_status_format,
                if (item.Owned) "Yes" else "No"
            )

            if (item.ItemCover != null) {
                val bitmap = BitmapFactory.decodeByteArray(item.ItemCover, 0, item.ItemCover.size)
                holder.binding.imageItemCover.setImageBitmap(bitmap)
                holder.binding.imageItemCover.imageTintList = null
            } else {
                holder.binding.imageItemCover.setImageResource(com.example.medialibrary.R.drawable.ic_gallery_black_24dp)
                holder.binding.imageItemCover.imageTintList = ResourcesCompat.getColorStateList(resources, android.R.color.darker_gray, null)
            }

            holder.binding.buttonEditItem.setOnClickListener { onEdit(item, position) }
            holder.binding.buttonDeleteItem.setOnClickListener { onDelete(position) }
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(val binding: BookItemVolumeBinding) : RecyclerView.ViewHolder(binding.root)
    }

}