package com.example.medialibrary.shared

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.medialibrary.R
import models.shared.Enums.MediaType

object CoverFrameUtils {

    /**
     * Applies the styled book, DVD, or CD appearance to an ImageView based on MediaType and cover bytes.
     */
    fun applyCoverFrame(
        imageView: ImageView,
        coverBytes: ByteArray?,
        mediaType: MediaType,
        context: Context
    ) {
        val targetWidth: Int
        val targetHeight: Int

        when (mediaType) {
            MediaType.Music -> {
                targetWidth = 180
                targetHeight = 180
                val lp = imageView.layoutParams
                if (lp != null) {
                    val cdSize = (64 * context.resources.displayMetrics.density).toInt()
                    if (lp.width != cdSize || lp.height != cdSize) {
                        lp.width = cdSize
                        lp.height = cdSize
                        imageView.layoutParams = lp
                    }
                }
            }
            MediaType.Video -> {
                targetWidth = 160
                targetHeight = 220
                val lp = imageView.layoutParams
                if (lp != null) {
                    val width = (60 * context.resources.displayMetrics.density).toInt()
                    val height = (84 * context.resources.displayMetrics.density).toInt()
                    if (lp.width != width || lp.height != height) {
                        lp.width = width
                        lp.height = height
                        imageView.layoutParams = lp
                    }
                }
            }
            else -> { // Book / Other / Default
                targetWidth = 160
                targetHeight = 210
                val lp = imageView.layoutParams
                if (lp != null) {
                    val width = (60 * context.resources.displayMetrics.density).toInt()
                    val height = (80 * context.resources.displayMetrics.density).toInt()
                    if (lp.width != width || lp.height != height) {
                        lp.width = width
                        lp.height = height
                        imageView.layoutParams = lp
                    }
                }
            }
        }

        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.imageTintList = null

        val sourceBitmap: Bitmap? = if (coverBytes != null && coverBytes.isNotEmpty()) {
            try {
                BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.size)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

        val framedBitmap = when (mediaType) {
            MediaType.Book -> createBookCoverBitmap(context, sourceBitmap, targetWidth, targetHeight)
            MediaType.Video -> createDvdCoverBitmap(context, sourceBitmap, targetWidth, targetHeight)
            MediaType.Music -> createCdCoverBitmap(context, sourceBitmap, targetWidth)
            else -> createGenericCoverBitmap(context, sourceBitmap, targetWidth, targetHeight)
        }

        imageView.setImageBitmap(framedBitmap)
    }

    private fun createBookCoverBitmap(
        context: Context,
        source: Bitmap?,
        width: Int,
        height: Int
    ): Bitmap {
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())

        if (source != null) {
            val path = Path().apply {
                addRoundRect(
                    rect,
                    floatArrayOf(
                        0f, 0f,         // Top-left (sharp spine edge)
                        14f, 14f,       // Top-right (rounded)
                        14f, 14f,       // Bottom-right (rounded)
                        0f, 0f          // Bottom-left (sharp spine edge)
                    ),
                    Path.Direction.CW
                )
            }
            canvas.clipPath(path)

            val srcRect = Rect(0, 0, source.width, source.height)
            canvas.drawBitmap(source, srcRect, rect, paint)
        } else {
            val bookColor = ThemeUtils.getThemeColor(context, androidx.appcompat.R.attr.colorPrimary)
            paint.color = bookColor
            canvas.drawRoundRect(rect, 8f, 8f, paint)

            drawVectorIcon(context, canvas, R.drawable.ic_book_small, width / 2f, height / 2f, (width * 0.45f).toInt(), Color.WHITE)
        }

        // --- BOOK SPINE & PAGE STACK OVERLAY ---
        // 1. Spine Shadow Crease (Left Edge)
        val spineWidth = width * 0.12f
        val spineGradient = LinearGradient(
            0f, 0f, spineWidth, 0f,
            intArrayOf(0x88000000.toInt(), 0x33000000.toInt(), 0x00000000),
            floatArrayOf(0f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = spineGradient
        canvas.drawRect(0f, 0f, spineWidth, height.toFloat(), paint)
        paint.shader = null

        // 2. Spine Fold Line (Vertical highlight)
        paint.color = 0x55FFFFFF.toInt()
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(spineWidth, 0f, spineWidth, height.toFloat(), paint)

        // 3. Right Page Stack Edge
        paint.style = Paint.Style.FILL
        paint.color = 0xFFE2E8F0.toInt()
        canvas.drawRect(width - 4f, 2f, width.toFloat(), height - 2f, paint)

        // 4. Outer Book Border
        paint.style = Paint.Style.STROKE
        paint.color = 0x33000000.toInt()
        paint.strokeWidth = 2f
        canvas.drawRect(rect, paint)

        return output
    }

    private fun createDvdCoverBitmap(
        context: Context,
        source: Bitmap?,
        width: Int,
        height: Int
    ): Bitmap {
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val headerHeight = (height * 0.16f).toInt()
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())

        // 1. Draw Plastic DVD Case Base
        paint.color = 0xFF0F172A.toInt()
        canvas.drawRoundRect(rect, 8f, 8f, paint)

        // 2. Draw Cover Artwork below header
        val artworkRect = RectF(2f, headerHeight.toFloat(), width - 2f, height - 2f)
        if (source != null) {
            canvas.save()
            canvas.clipRect(artworkRect)
            val srcRect = Rect(0, 0, source.width, source.height)
            canvas.drawBitmap(source, srcRect, artworkRect, paint)
            canvas.restore()
        } else {
            val videoColor = ThemeUtils.getThemeColor(context, androidx.appcompat.R.attr.colorPrimary)
            paint.color = videoColor
            canvas.drawRect(artworkRect, paint)

            drawVectorIcon(context, canvas, R.drawable.ic_movie, width / 2f, (height + headerHeight) / 2f, (width * 0.4f).toInt(), Color.WHITE)
        }

        // --- DVD CASE HEADER BANNER ---
        paint.color = 0xFF1E293B.toInt()
        canvas.drawRect(0f, 0f, width.toFloat(), headerHeight.toFloat(), paint)

        paint.color = Color.WHITE
        paint.textSize = headerHeight * 0.55f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        val fontMetrics = paint.fontMetrics
        val textY = (headerHeight / 2f) - ((fontMetrics.descent + fontMetrics.ascent) / 2f)
        canvas.drawText("DVD", width / 2f, textY, paint)

        paint.color = 0xAA000000.toInt()
        paint.strokeWidth = 3f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(0f, headerHeight.toFloat(), width.toFloat(), headerHeight.toFloat(), paint)

        paint.color = 0x44FFFFFF.toInt()
        paint.strokeWidth = 2f
        canvas.drawRoundRect(rect, 8f, 8f, paint)

        return output
    }

    private fun createCdCoverBitmap(
        context: Context,
        source: Bitmap?,
        size: Int
    ): Bitmap {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val center = size / 2f
        val radius = size / 2f - 2f

        // 1. Draw Square Jewel Case Base
        paint.color = 0xFF1E293B.toInt()
        canvas.drawRoundRect(0f, 0f, size.toFloat(), size.toFloat(), 12f, 12f, paint)

        // 2. Draw Circular CD Artwork
        canvas.save()
        val cdPath = Path().apply {
            addCircle(center, center, radius, Path.Direction.CW)
        }
        canvas.clipPath(cdPath)

        if (source != null) {
            val srcRect = Rect(0, 0, source.width, source.height)
            val destRect = RectF(0f, 0f, size.toFloat(), size.toFloat())
            canvas.drawBitmap(source, srcRect, destRect, paint)
        } else {
            val cdColor = ThemeUtils.getThemeColor(context, androidx.appcompat.R.attr.colorPrimary)
            paint.color = cdColor
            canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), paint)

            drawVectorIcon(context, canvas, R.drawable.ic_music, center, center, (size * 0.4f).toInt(), Color.WHITE)
        }
        canvas.restore()

        // --- CD TRACK GROOVE RINGS & CENTER SPINDLE HOLE ---
        paint.style = Paint.Style.STROKE
        paint.color = 0x66FFFFFF.toInt()
        paint.strokeWidth = 2f
        canvas.drawCircle(center, center, radius, paint)

        canvas.drawCircle(center, center, radius * 0.65f, paint)

        // Center Spindle Clear Ring
        paint.style = Paint.Style.FILL
        paint.color = 0xCCFFFFFF.toInt()
        canvas.drawCircle(center, center, radius * 0.28f, paint)

        // Center Spindle Hole
        paint.color = 0xFF0F172A.toInt()
        canvas.drawCircle(center, center, radius * 0.16f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = 0xAA000000.toInt()
        paint.strokeWidth = 2f
        canvas.drawCircle(center, center, radius * 0.28f, paint)

        // Jewel Case Left Plastic Spine
        paint.style = Paint.Style.FILL
        paint.color = 0x33FFFFFF.toInt()
        canvas.drawRect(0f, 0f, size * 0.08f, size.toFloat(), paint)

        return output
    }

    private fun createGenericCoverBitmap(
        context: Context,
        source: Bitmap?,
        width: Int,
        height: Int
    ): Bitmap {
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        if (source != null) {
            val srcRect = Rect(0, 0, source.width, source.height)
            canvas.drawBitmap(source, srcRect, rect, paint)
        } else {
            val color = ThemeUtils.getThemeColor(context, androidx.appcompat.R.attr.colorPrimary)
            paint.color = color
            canvas.drawRoundRect(rect, 8f, 8f, paint)
            drawVectorIcon(context, canvas, R.drawable.other_item_small, width / 2f, height / 2f, (width * 0.4f).toInt(), Color.WHITE)
        }
        return output
    }

    private fun drawVectorIcon(
        context: Context,
        canvas: Canvas,
        drawableResId: Int,
        centerX: Float,
        centerY: Float,
        size: Int,
        tintColor: Int
    ) {
        val drawable = ContextCompat.getDrawable(context, drawableResId) ?: return
        val half = size / 2
        drawable.setBounds(
            (centerX - half).toInt(),
            (centerY - half).toInt(),
            (centerX + half).toInt(),
            (centerY + half).toInt()
        )
        drawable.setTint(tintColor)
        drawable.draw(canvas)
    }
}
