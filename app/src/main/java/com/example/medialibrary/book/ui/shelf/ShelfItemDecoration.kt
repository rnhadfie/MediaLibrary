package com.example.medialibrary.book.ui.shelf

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ShelfItemDecoration(
    private val shelfHeightPx: Int = 24
) : RecyclerView.ItemDecoration() {

    private val shelfPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8D6E63") // Wood tone
        style = Paint.Style.FILL
    }

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4E342E") // Darker lip/shadow
        style = Paint.Style.FILL
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        // Reserve space beneath each row for the shelf plank
        outRect.bottom = shelfHeightPx
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft.toFloat()
        val right = (parent.width - parent.paddingRight).toFloat()

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val top = child.bottom.toFloat()
            val bottom = top + shelfHeightPx

            // Draw main shelf board
            canvas.drawRect(left, top, right, bottom, shelfPaint)
            // Draw top edge highlight/shadow for depth
            canvas.drawRect(left, top, right, top + (shelfHeightPx * 0.25f), shadowPaint)
        }
    }
}