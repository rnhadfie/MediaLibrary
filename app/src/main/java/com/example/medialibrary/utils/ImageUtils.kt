package com.example.medialibrary.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.viewbinding.ViewBinding
import com.example.medialibrary.R
import com.example.medialibrary.databinding.BookFragmentFormBinding
import com.example.medialibrary.databinding.BookItemBottomSheetBinding
import com.example.medialibrary.databinding.MusicFragmentFormBinding
import com.example.medialibrary.databinding.OtherFragmentFormBinding
import com.example.medialibrary.databinding.OtherItemBottomSheetBinding
import com.example.medialibrary.databinding.VideoFragmentFormBinding
import com.example.medialibrary.databinding.VideoItemBottomSheetBinding
import java.io.ByteArrayOutputStream
import java.io.File

object ImageUtils {

    const val MAX_IMAGE_DIMENSION = 800
    const val COMPRESS_QUALITY = 75

    fun createTempPhotoUri(context: Context): Pair<File, Uri> {
        val tempFile = File.createTempFile("camera_photo_", ".jpg", context.cacheDir)
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, tempFile)
        return Pair(tempFile, uri)
    }

    fun scaleBitmap(bitmap: Bitmap, maxDimension: Int = MAX_IMAGE_DIMENSION): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val targetWidth: Int
        val targetHeight: Int
        if (width >= height) {
            targetWidth = maxDimension
            targetHeight = (maxDimension / ratio).toInt().coerceAtLeast(1)
        } else {
            targetHeight = maxDimension
            targetWidth = (maxDimension * ratio).toInt().coerceAtLeast(1)
        }
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }

    fun optimizeImageBytes(
        byteArray: ByteArray?,
        maxDimension: Int = MAX_IMAGE_DIMENSION,
        quality: Int = COMPRESS_QUALITY
    ): ByteArray? {
        if (byteArray == null || byteArray.isEmpty()) return byteArray
        return try {
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size) ?: return byteArray
            val scaled = scaleBitmap(bitmap, maxDimension)
            val outputStream = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val result = outputStream.toByteArray()
            if (scaled != bitmap && !scaled.isRecycled) {
                scaled.recycle()
            }
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
            result
        } catch (e: Exception) {
            byteArray
        }
    }

    fun getCorrectlyOrientedBitmap(
        context: Context,
        uri: Uri,
        maxDimension: Int = MAX_IMAGE_DIMENSION
    ): Bitmap? {
        val inputStreamBounds = context.contentResolver.openInputStream(uri) ?: return null
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStreamBounds, null, options)
        inputStreamBounds.close()

        if (options.outWidth <= 0 || options.outHeight <= 0) return null

        var sampleSize = 1
        while (options.outWidth / sampleSize > maxDimension * 2 || options.outHeight / sampleSize > maxDimension * 2) {
            sampleSize *= 2
        }

        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val decodedBitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
        inputStream.close()

        if (decodedBitmap == null) return null

        val orientedBitmap = try {
            val exifInputStream = context.contentResolver.openInputStream(uri)
            val orientation = if (exifInputStream != null) {
                val exif = ExifInterface(exifInputStream)
                val rotation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                exifInputStream.close()
                rotation
            } else {
                ExifInterface.ORIENTATION_NORMAL
            }

            val degrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            if (degrees != 0f) {
                val matrix = Matrix()
                matrix.postRotate(degrees)
                Bitmap.createBitmap(decodedBitmap, 0, 0, decodedBitmap.width, decodedBitmap.height, matrix, true)
            } else {
                decodedBitmap
            }
        } catch (e: Exception) {
            decodedBitmap
        }

        return scaleBitmap(orientedBitmap, maxDimension)
    }

    private fun isFormTargeted(pendingImageTarget: String?): Boolean {
        return pendingImageTarget == "book" || pendingImageTarget == "video" || pendingImageTarget == "other" || pendingImageTarget == "music"
    }

    fun setPlaceholderCover(imageView: ImageView, requireContext: Context, resources: Resources) {
        imageView.setImageResource(R.drawable.ic_gallery_black_24dp)
        val typedValue = TypedValue()
        requireContext.theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true)
        imageView.imageTintList = ResourcesCompat.getColorStateList(
            resources, typedValue.resourceId, requireContext.theme
        )
    }

    fun handleImageBitmap(
        bitmap: Bitmap,
        pendingImageTarget: String?,
        binding: ViewBinding,
        currentSheetBinding: ViewBinding?,
    ) {
        val scaledBitmap = scaleBitmap(bitmap, MAX_IMAGE_DIMENSION)
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, outputStream)
        val byteArray = outputStream.toByteArray()

        if (isFormTargeted(pendingImageTarget)) {
            when (binding) {
                is BookFragmentFormBinding -> {
                    binding.changeImage.imageBookCover.setImageBitmap(scaledBitmap)
                    binding.changeImage.imageBookCover.imageTintList = null
                    binding.changeImage.buttonClearCover.visibility = View.VISIBLE
                }
                is VideoFragmentFormBinding -> {
                    binding.changeImage.imageBookCover.setImageBitmap(scaledBitmap)
                    binding.changeImage.imageBookCover.imageTintList = null
                    binding.changeImage.buttonClearCover.visibility = View.VISIBLE
                }
                is OtherFragmentFormBinding -> {
                    binding.changeImage.imageBookCover.setImageBitmap(scaledBitmap)
                    binding.changeImage.imageBookCover.imageTintList = null
                    binding.changeImage.buttonClearCover.visibility = View.VISIBLE
                }
                is MusicFragmentFormBinding -> {
                    binding.changeImage.imageBookCover.setImageBitmap(scaledBitmap)
                    binding.changeImage.imageBookCover.imageTintList = null
                    binding.changeImage.buttonClearCover.visibility = View.VISIBLE
                }
            }
        } else if (pendingImageTarget == "item") {
            currentSheetBinding?.let { sheet ->
                when (sheet) {
                    is BookItemBottomSheetBinding -> {
                        sheet.imageItemCover.imageBookCover.setImageBitmap(scaledBitmap)
                        sheet.imageItemCover.imageBookCover.imageTintList = null
                        sheet.imageItemCover.imageBookCover.tag = byteArray
                        sheet.imageItemCover.buttonClearCover.visibility = View.VISIBLE
                    }
                    is VideoItemBottomSheetBinding -> {
                        sheet.imageItemCover.imageBookCover.setImageBitmap(scaledBitmap)
                        sheet.imageItemCover.imageBookCover.imageTintList = null
                        sheet.imageItemCover.imageBookCover.tag = byteArray
                        sheet.imageItemCover.buttonClearCover.visibility = View.VISIBLE
                    }
                    is OtherItemBottomSheetBinding -> {
                        sheet.imageItemCover.imageBookCover.setImageBitmap(scaledBitmap)
                        sheet.imageItemCover.imageBookCover.imageTintList = null
                        sheet.imageItemCover.imageBookCover.tag = byteArray
                        sheet.imageItemCover.buttonClearCover.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    fun clearImage(
        target: String,
        binding: ViewBinding,
        currentSheetBinding: ViewBinding?,
        requireContext: Context,
        resources: Resources
    ) {
        if (isFormTargeted(target)) {
            when (binding) {
                is BookFragmentFormBinding -> {
                    setPlaceholderCover(
                        binding.changeImage.imageBookCover,
                        requireContext,
                        resources
                    )
                    binding.changeImage.buttonClearCover.visibility = View.GONE
                }
                is VideoFragmentFormBinding -> {
                    setPlaceholderCover(
                        binding.changeImage.imageBookCover,
                        requireContext,
                        resources
                    )
                    binding.changeImage.buttonClearCover.visibility = View.GONE
                }
                is OtherFragmentFormBinding -> {
                    setPlaceholderCover(
                        binding.changeImage.imageBookCover,
                        requireContext,
                        resources
                    )
                    binding.changeImage.buttonClearCover.visibility = View.GONE
                }
                is MusicFragmentFormBinding -> {
                    setPlaceholderCover(
                        binding.changeImage.imageBookCover,
                        requireContext,
                        resources
                    )
                    binding.changeImage.buttonClearCover.visibility = View.GONE
                }
            }
        } else if (target == "item") {
            when (currentSheetBinding) {
                is BookItemBottomSheetBinding -> {
                    setPlaceholderCover(
                        currentSheetBinding.imageItemCover.imageBookCover,
                        requireContext,
                        resources
                    )
                    currentSheetBinding.imageItemCover.imageBookCover.tag = null
                    currentSheetBinding.imageItemCover.buttonClearCover.visibility = View.GONE
                }
                is VideoItemBottomSheetBinding -> {
                    setPlaceholderCover(
                        currentSheetBinding.imageItemCover.imageBookCover,
                        requireContext,
                        resources
                    )
                    currentSheetBinding.imageItemCover.imageBookCover.tag = null
                    currentSheetBinding.imageItemCover.buttonClearCover.visibility = View.GONE
                }
                is OtherItemBottomSheetBinding -> {
                    setPlaceholderCover(
                        currentSheetBinding.imageItemCover.imageBookCover,
                        requireContext,
                        resources
                    )
                    currentSheetBinding.imageItemCover.imageBookCover.tag = null
                    currentSheetBinding.imageItemCover.buttonClearCover.visibility = View.GONE
                }
            }
        }
    }

    fun handleImageUri(uri: Uri, context: Context, handleImageBitmap: (Bitmap) -> Unit) {
        val bitmap = getCorrectlyOrientedBitmap(context, uri)
        bitmap?.let { handleImageBitmap(it) }
    }
}