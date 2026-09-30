package com.example.medialibrary

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import models.shared.Enums as SharedEnums
import com.example.medialibrary.databinding.ViewDropdownBinding
import com.example.medialibrary.utils.ImageUtils
import com.example.medialibrary.utils.SharedRefreshViewModel
import java.io.ByteArrayOutputStream
import java.io.File

abstract class BaseFormFragment<VB : ViewBinding, VM : ViewModel>(
    private val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
) : Fragment() {

    companion object {
        const val ARG_ID = "arg_id"
        const val ARG_IS_EDIT = "arg_is_edit"
    }

    private var _binding: VB? = null
    protected val binding: VB
        get() = _binding ?: throw IllegalStateException("Binding is only valid between onCreateView and onDestroyView")

    protected lateinit var viewModel: VM

    protected var itemId: String = "-1"
    protected var isEdit: Boolean = false

    protected var pendingImageTarget: String? = null
    protected var pendingItemPosition: Int = -1
    protected var currentSheetBinding: ViewBinding? = null

    private var tempPhotoFile: File? = null
    private var tempPhotoUri: Uri? = null

    // Image Pickers
    protected val pickGalleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let { ImageUtils.handleImageUri(it, requireContext(), ::onImageBitmapLoaded) }
        }
    }

    protected val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempPhotoUri != null) {
            ImageUtils.handleImageUri(tempPhotoUri!!, requireContext(), ::onImageBitmapLoaded)
        }
        tempPhotoFile?.delete()
        tempPhotoFile = null
        tempPhotoUri = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingInflater(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemId = arguments?.getString(ARG_ID) ?: "-1"
        isEdit = arguments?.getBoolean(ARG_IS_EDIT) ?: false
    }

    protected open fun onImageBitmapLoaded(bitmap: Bitmap) {
        val scaledBitmap = ImageUtils.scaleBitmap(bitmap, ImageUtils.MAX_IMAGE_DIMENSION)
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, ImageUtils.COMPRESS_QUALITY, outputStream)
        val byteArray = outputStream.toByteArray()
        ImageUtils.handleImageBitmap(scaledBitmap, pendingImageTarget, binding, currentSheetBinding)
        onCoverImageUpdated(byteArray, pendingImageTarget)
    }

    protected open fun onCoverImageUpdated(byteArray: ByteArray?, target: String?) {
        // Subclasses override to update ViewModel cover
    }

    protected open fun clearImage(target: String = "main") {
        ImageUtils.clearImage(target, binding, currentSheetBinding, requireContext(), resources)
        onCoverImageUpdated(null, target)
    }

    protected open fun showImageOptionsDialog(target: String = "main") {
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

    protected fun setupCollectingPriorityDropdown(
        dropdownBinding: ViewDropdownBinding,
        onPrioritySelected: (SharedEnums.CollectingPriority) -> Unit
    ) {
        val priorities = SharedEnums.CollectingPriority.entries.toTypedArray()
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            priorities.map { it.name }
        )
        dropdownBinding.autoCompleteLabel.setHint(R.string.collecting_priority)
        dropdownBinding.autocomplete.setAdapter(adapter)
        dropdownBinding.autocomplete.setOnItemClickListener { _, _, position, _ ->
            val selectedPriority = priorities[position]
            onPrioritySelected(selectedPriority)
        }
    }

    protected fun notifyDataChanged() {
        activity?.let { act ->
            ViewModelProvider(act)[SharedRefreshViewModel::class.java].incrementVersion()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        currentSheetBinding = null
    }
}