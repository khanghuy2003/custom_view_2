package com.test.time.testchristmasgame.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.graphics.withMatrix
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class ChickenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs)  {

    companion object{
        private const val TAG = "TAGChickenView"
    }

    //test
    private val paintTest = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 5f.toPx()
        isAntiAlias = true
    }

    //camera vision
    private var cameraX = 0f
    private var cameraY = 0f
    private var scaleCamX = 0.3f
    private var scaleCamY = 0.3f

    //view
    private var widthView = 0f
    private var heightView = 0f
    private var visibleViewWidth = 0f
    private var visibleViewHeight = 0f

    //land 1
    private val paintLand1 =
        Paint().apply { style = Paint.Style.FILL; color = Color.RED; isAntiAlias = true }
    private var rectLand1: RectF = RectF(0f, 250f.toPx(), 300f.toPx(), 270f.toPx())

    //land 2
    private val paintLand2 =
        Paint().apply { style = Paint.Style.FILL; color = Color.BLUE; isAntiAlias = true }
    private var rectLand2: RectF = RectF(0f, 0f, 25f.toPx(), 1200f.toPx())
    private var matrixLand2 = Matrix().apply {
        postRotate(-15f)
        postTranslate(rectLand1.width(), rectLand1.bottom)
    }

    //land 3
    private val paintLand3 = paintLand2
    private var rectLand3 : RectF = RectF(rectLand1.right + 250f.toPx(), rectLand1.bottom, rectLand1.right + 250f.toPx() + 25f.toPx(), rectLand1.bottom + 1200f.toPx())

    //land 4
    private val paintLand4 =
        Paint().apply { style = Paint.Style.FILL; color = Color.RED; isAntiAlias = true }
    private val rectLand4 : RectF = RectF(300f.toPx(), 1600f.toPx(), 700f.toPx(), (1600f + 25f).toPx())

    //chicken
    private val paintChicken = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
    }
    private val radiusChicken = 20f.toPx()
    private var chickenX : Float = radiusChicken
    private var chickenY : Float = rectLand1.top - radiusChicken
    private var deltaX : Float = 10f
    private var deltaY : Float = 0f


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        widthView = 1000f.toPx()
        heightView = 2000f.toPx()
        setMeasuredDimension(widthView.toInt(), heightView.toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        visibleViewWidth = w.toFloat()
        visibleViewHeight = h.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // vision camera
        canvas.translate(-cameraX, -cameraY)
        canvas.scale(scaleCamX, scaleCamY)

        //border view test
        canvas.drawRect(RectF(0f, 0f, widthView, heightView), paintTest)

        //draw land 1
        canvas.drawRect(rectLand1, paintLand1)

        //draw land 2
        canvas.withMatrix(matrixLand2) { drawRect(rectLand2, paintLand2) }

        //draw land 3
        canvas.drawRect(rectLand3, paintLand3)

        //draw land 4
        canvas.drawRect(rectLand4, paintLand4)

        //chicken
        canvas.drawCircle(chickenX, chickenY, radiusChicken, paintChicken)

    }

    fun updateAnimation() {
        chickenX += deltaX
        chickenY += deltaY

        if(deltaX > 0f) deltaX -= 0.03f
        deltaY += 0.2f

        //touch land 1
//        Log.d(TAG, "isTouchLand1: ${isTouchingLand1()}")
        if (isTouchingLand1() && deltaY > 0) {
            chickenY = rectLand1.top - radiusChicken
            deltaY = 0f
        } else {
            deltaY += 0.2f
        }

        Log.d(TAG, "isTouchingLand2 : ${isTouchingLand2()}")
        if(isTouchingLand2()) {
        }

        invalidate()
    }

    fun moveCamera(xDp: Float, yDp : Float) {
        val newCameraX = cameraX + xDp.toPx()
        val newCameraY = cameraY + yDp.toPx()
        cameraX = newCameraX.coerceIn(0f, widthView - visibleViewWidth)
        cameraY = newCameraY.coerceIn(0f, heightView - visibleViewHeight)
        invalidate()
    }

    private fun isTouchingLand1(): Boolean {
        val closestX = max(rectLand1.left, min(chickenX, rectLand1.right))
        val closestY = max(rectLand1.top, min(chickenY, rectLand1.bottom))

        val distanceX = chickenX - closestX
        val distanceY = chickenY - closestY

        val distanceSquared = distanceX.pow(2) + distanceY.pow(2)
        return distanceSquared < radiusChicken.pow(2)
    }

    private fun isTouchingLand2(): Boolean {
        val invertedMatrix = Matrix()

        if (!matrixLand2.invert(invertedMatrix)) {
            return false
        }

        val chickenCoordinates = floatArrayOf(chickenX, chickenY)

        invertedMatrix.mapPoints(chickenCoordinates)

        val chickenXConvert = chickenCoordinates[0]
        val chickenYConvert = chickenCoordinates[1]

        val closestX = max(rectLand2.left, min(chickenXConvert, rectLand2.right))
        val closestY = max(rectLand2.top, min(chickenYConvert, rectLand2.bottom))

        val distanceX = chickenXConvert - closestX
        val distanceY = chickenYConvert - closestY

        val distanceSquared = distanceX.pow(2) + distanceY.pow(2)
        return distanceSquared < radiusChicken.pow(2)
    }

    private fun Float.toPx() : Float {
        return context.resources.displayMetrics.density * this
    }

    fun jump() {
        deltaY -= 15f
        invalidate()
    }
}