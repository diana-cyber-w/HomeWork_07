package otus.homework.customview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import otus.homework.customview.data.CategoryItem
import otus.homework.customview.data.Item
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.min

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val rectF = RectF()
    private val items = mutableListOf<CategoryItem>()
    private val colors = listOf(
        0xFFe53935.toInt(),
        0xFF8e24aa.toInt(),
        0xFF5e35b1.toInt(),
        0xFF3949ab.toInt(),
        0xFF1e88e5.toInt(),
        0xFF00acc1.toInt(),
        0xFF00897b.toInt(),
        0xFF43a047.toInt(),
        0xFFc0ca33.toInt(),
        0xFFffb300.toInt()
    )
    private val sweepAngles = mutableListOf<Float>()
    private val startAngles = mutableListOf<Float>()
    private val paints = mutableListOf<Paint>()
    private val circlePaint = Paint().apply {
        color = Color.WHITE
    }
    private var circleRadius = 0f
    private var categoryClickListener: OnCategoryClickListener? = null

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
        rectF.set(0f, 0f, w.toFloat(), h.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        if (items.isEmpty()) return

        items.forEachIndexed { index, _ ->
            canvas.drawArc(rectF, startAngles[index], sweepAngles[index], true, paints[index])
        }

        circleRadius = min(height, width) / 2f - 80f
        if (circleRadius > 0) {
            canvas.drawCircle(width / 2f, height / 2f, circleRadius, circlePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            val centerX = (rectF.left + rectF.right) / 2
            val centerY = (rectF.top + rectF.bottom) / 2

            val dx = event.x - centerX
            val dy = event.y - centerY
            val distance = hypot(dx.toDouble(), dy.toDouble()).toFloat()
            val radius = (rectF.right - rectF.left) / 2

            if (distance < radius && distance > circleRadius) {
                val touchAngle = Math.toDegrees(atan2(dy, dx).toDouble()).let {
                    if (it < 0)
                        360 + it
                    else it
                }

                items.forEachIndexed { index, angle ->
                    if (touchAngle > startAngles[index] && touchAngle < startAngles[index] + sweepAngles[index]) {
                        categoryClickListener?.onCategoryClick(items[index].category)
                        return true
                    }
                }
            }
        }
        return true
    }

    fun setOnCategoryClickListener(listener: OnCategoryClickListener) {
        categoryClickListener = listener
    }

    fun setValues(values: List<Item>) {
        items.clear()
        val groups = values
            .groupBy { it.category }
            .mapValues { (_, item) ->
                item.sumOf { it.amount }
            }
            .map { (category, amount) ->
                CategoryItem(amount, category)
            }
        items.addAll(groups)

        calculateAngles()
        postInvalidate()
    }

    private fun calculateAngles() {
        sweepAngles.clear()
        startAngles.clear()
        paints.clear()

        val totalAmount = items.sumOf { it.amount }.toLong()
        var startAngle = 0f
        var sweepAngle = 0f

        items.forEachIndexed { index, item ->
            if (index <= 9) { // берем только 10 категорий, т.к. задано всего 10 цветов
                sweepAngle = (item.amount.toDouble() * 360 / totalAmount).toFloat()
                sweepAngles.add(sweepAngle)
                startAngles.add(startAngle)
                startAngle += sweepAngle.toFloat()
                paints.add(Paint().apply {
                    color = colors[index]
                })
            }
        }
    }
}