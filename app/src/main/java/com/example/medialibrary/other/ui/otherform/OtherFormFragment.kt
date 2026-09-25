package com.example.medialibrary.other.ui.otherform

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
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
import com.example.medialibrary.utils.SharedRefreshViewModel
import com.example.medialibrary.backend.controllers.OtherController
import com.example.medialibrary.backend.models.other.OtherItem
import com.example.medialibrary.backend.models.shared.MainSetup
import com.example.medialibrary.backend.models.shared.Tag
import com.example.medialibrary.backend.repository.database.MediaLibraryDbHelper
import com.example.medialibrary.databinding.OtherItemBottomSheetBinding
import com.example.medialibrary.databinding.OtherFragmentFormBinding
import com.example.medialibrary.databinding.BookItemVolumeBinding
import com.example.medialibrary.utils.ImageUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.io.ByteArrayOutputStream
import java.io.File

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

    private var pendingImageTarget: String? = null // "other" or "item"
    private var pendingItemPosition: Int = -1
    private var currentSheetBinding: OtherItemBottomSheetBinding? = null

    private var controller: OtherController? = null

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

        if (pendingImageTarget == "other") {
            viewModel.updateCover(byteArray)
        }

    }

    private fun clearImage(target: String) {

        ImageUtils.clearImage(target, binding, currentSheetBinding,requireContext(),resources)
        if (target == "other") {
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

        binding.changeImage.buttonChangeCover.setOnClickListener {
            showImageOptionsDialog("other")
        }
        binding.changeImage.buttonClearCover.setOnClickListener {
            clearImage("other")
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

            if (other.Cover != null && other.Cover.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(other.Cover, 0, other.Cover.size)
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

    private fun setupTagSelection(setup: MainSetup) {
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
                R.string.Other_volume_status_format,
                if (item.Owned) "Yes" else "No"
            )

            if (item.ItemCover != null && item.ItemCover.isNotEmpty()) {
                val bitmap = BitmapFactory.decodeByteArray(item.ItemCover, 0, item.ItemCover.size)
                holder.binding.imageItemCover.setImageBitmap(bitmap)
                holder.binding.imageItemCover.imageTintList = null
            } else {
                ImageUtils.setPlaceholderCover(binding.changeImage.imageBookCover, requireContext(), resources)
            }

            holder.binding.buttonEditItem.setOnClickListener { onEdit(item, position) }
            holder.binding.buttonDeleteItem.setOnClickListener { onDelete(position) }
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(val binding: BookItemVolumeBinding) : RecyclerView.ViewHolder(binding.root)
    }

}