package otus.homework.customview.view

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import otus.homework.customview.data.Item
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

class CategoryChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val points = mutableListOf<ChartItem>()
    private val rectF = RectF()
    private val paintLine = Paint().apply {
        color = Color.BLUE
        strokeWidth = 1f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    private val paintPoint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 8f
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val axisPaint = Paint().apply {
        color = Color.GRAY
        strokeWidth = 1.5f
        isAntiAlias = true
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }
    private val valueAnimator = ValueAnimator.ofInt(0, 255).apply {
        duration = 1500L
        interpolator = LinearInterpolator()
        addUpdateListener {
            textPaint.alpha = it.animatedValue as Int
            axisPaint.alpha = it.animatedValue as Int
            paintPoint.alpha = it.animatedValue as Int
            paintLine.alpha = it.animatedValue as Int
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        val resolvedWidth = when (wMode) {
            MeasureSpec.EXACTLY -> wSize
            MeasureSpec.AT_MOST -> min(desiredWidth, wSize)
            MeasureSpec.UNSPECIFIED -> desiredWidth
            else -> desiredWidth
        }

        val resolvedHeight = when (hMode) {
            MeasureSpec.EXACTLY -> hSize
            MeasureSpec.AT_MOST -> min(desiredHeight, hSize)
            MeasureSpec.UNSPECIFIED -> desiredHeight
            else -> desiredHeight
        }

        setMeasuredDimension(resolvedWidth, resolvedHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rectF.set(20.px, 20.px, w.toFloat() - 35.px, h.toFloat() - 30.px)
    }

    override fun onDraw(canvas: Canvas) {
        if (points.isEmpty()) return

        drawLabels(canvas)
        drawLineAndMarkers(canvas)
    }

    fun putValues(category: String, values: List<Item>) {
        val filteredItems =
            values.filter { it.category == category }.groupBy { it.time / 86400 }.toSortedMap()
        val minDay = filteredItems.firstKey()
        val maxDay = filteredItems.lastKey()

        val items = (minDay..maxDay).associateWith { day ->
            filteredItems[day]?.sumOf { it.amount } ?: 0
        }

        points.clear()
        for ((time, amount) in items) {
            points.add(ChartItem(time, amount))
        }
        calcPositions()
        valueAnimator.start()
    }

    private fun calcPositions() {
        val xStep = if (points.size > 1) rectF.width() / (points.size - 1).toFloat() else 0f
        val yMax = points.maxOfOrNull { it.amount } ?: return
        val yStep = rectF.height() / yMax.toFloat()

        points.forEachIndexed { index, point ->
            point.x = index * xStep + 15.px
            point.y = rectF.bottom - point.amount * yStep
        }
    }

    private fun drawLineAndMarkers(canvas: Canvas) {
        var previous: ChartItem? = null
        for (point in points) {
            if (previous != null) {
                canvas.drawLine(previous.x, previous.y, point.x, point.y, paintLine)
            }
            previous = point

            canvas.drawCircle(
                point.x,
                point.y,
                8f,
                paintPoint
            )
        }
    }

    private fun drawLabels(canvas: Canvas) {
        for (point in points) {
            canvas.drawLine(rectF.left, point.y, rectF.right, point.y, axisPaint)
            canvas.drawText(point.amount.toString(), rectF.right + 50, point.y, textPaint)

            canvas.drawLine(point.x, rectF.bottom, point.x, rectF.top, axisPaint)
            canvas.drawText(point.time.toDate(), point.x, rectF.bottom + 50, textPaint)
        }
    }

    private fun Long.toDate(): String {
        val date = Date(this * 86400 * 1000)
        val format = SimpleDateFormat("MM.dd", Locale.getDefault())
        return format.format(date)
    }

    val Int.px: Float
        get() = (this * Resources.getSystem().displayMetrics.density)
}

data class ChartItem(
    val time: Long,
    val amount: Int,
    var x: Float = 0f,
    var y: Float = 0f
)