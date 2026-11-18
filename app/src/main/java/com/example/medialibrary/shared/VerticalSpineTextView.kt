package com.example.medialibrary.shared

import android.R
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.withSave

class VerticalSpineTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    var topToBottom: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                invalidate()
            }
        }

    // Map rotated paddings so line-wrapping uses vertical padding
    // and horizontal centering uses horizontal padding
    override fun getCompoundPaddingLeft(): Int = super.getCompoundPaddingTop()
    override fun getCompoundPaddingRight(): Int = super.getCompoundPaddingBottom()
    override fun getCompoundPaddingTop(): Int = super.getCompoundPaddingRight()
    override fun getCompoundPaddingBottom(): Int = super.getCompoundPaddingLeft()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(canvas: Canvas) {
        val textLayout = layout ?: return

        paint.color = currentTextColor
        paint.drawableState = drawableState

        canvas.withSave {
            if (topToBottom) {
                translate(width.toFloat(), 0f)
                rotate(90f)
            } else {
                translate(0f, height.toFloat())
                rotate(-90f)
            }

            // Center the text block across the stretched spine width
            val availableWidth = height - compoundPaddingLeft - compoundPaddingRight
            val availableHeight = width - compoundPaddingTop - compoundPaddingBottom

            val dx = compoundPaddingLeft + ((availableWidth - textLayout.width) / 2f).coerceAtLeast(0f)
            val dy = compoundPaddingTop + ((availableHeight - textLayout.height) / 2f).coerceAtLeast(0f)

            translate(dx, dy)
            textLayout.draw(this)
        }
    }
}