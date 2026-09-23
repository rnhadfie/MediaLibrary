package com.example.medialibrary.Utils
import android.graphics.Canvas
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.renderer.PieChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler
class SafePieChartRenderer(
    chart: PieChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : PieChartRenderer(chart, animator, viewPortHandler) {

    override fun drawExtras(c: Canvas) {
        // MPAndroidChart bug: mDrawBitmap can be null if the chart layout is 0x0
        if (mDrawBitmap == null) return

        super.drawExtras(c)
    }
}