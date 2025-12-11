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
import kotlin.random.Random

class ChickenView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs)  {

    interface GameListener{
        fun onWin()
        fun onLose()
    }

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
    private var scaleCamX = 1f
    private var scaleCamY = 1f

    //view
    private var listener : GameListener? = null
    private var isJumping = false
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
        postRotate(-10f)
        postTranslate(rectLand1.width() - 15f.toPx(), rectLand1.bottom)
    }

    //land 3
    private val paintLand3 = paintLand2
    private var rectLand3 : RectF = RectF(rectLand1.right + 325f.toPx(), rectLand1.bottom, rectLand1.right + 325f.toPx() + 25f.toPx(), rectLand1.bottom + 1200f.toPx())

    //land 4
    private val paintLand4 =
        Paint().apply { style = Paint.Style.FILL; color = Color.RED; isAntiAlias = true }
    private val rectLand4 : RectF = RectF(300f.toPx(), 1600f.toPx(), 700f.toPx(), (1600f + 25f).toPx())

    //chicken
    private val paintChicken = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
    }
    private val radiusChicken = 30f.toPx()
    private var chickenX : Float = radiusChicken
    private var chickenY : Float = rectLand1.top - radiusChicken
    private var deltaX : Float = 2f.toPx()
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
        Log.d(TAG, "onSizeChanged: $visibleViewWidth $visibleViewHeight")
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

        if(deltaX > 0f) deltaX -= 0.001f.toPx()
        deltaY += 0.05f.toPx()

        //touch land 1
        if (isTouchingNoMatrix(chickenX, chickenY, rectLand1) && deltaY > 0) {
            if(isJumping){
                chickenY = rectLand1.top - radiusChicken
                deltaY -= 4f.toPx()
            } else {
                chickenY = rectLand1.top - radiusChicken
                deltaY = 0f
            }
        } else {
            deltaY += 0.05f.toPx()
        }

        //touch land 2 & 3
        if(isTouchingLand2() || isTouchingNoMatrix(chickenX, chickenY, rectLand3)) {
            deltaX = 0f
            deltaY = 0f
            listener?.onLose()
            return
        }

        //touch land 4
        if(isTouchingNoMatrix(chickenX, chickenY, rectLand4)){
            chickenY = rectLand4.top - radiusChicken
            deltaX = 0f
            deltaY = 0f
            listener?.onWin()
            return
        }

        updateCameraVision()
        invalidate()
    }

    fun updateCameraVision(){
        val targetCameraX = chickenX - ((visibleViewWidth / scaleCamX) / 2f)
        val targetCameraY = chickenY - ((visibleViewHeight / scaleCamY) / 2f)

        val lerpFactor = 0.1f
        cameraX += (targetCameraX - cameraX) * lerpFactor
        cameraY += (targetCameraY - cameraY) * lerpFactor

        cameraX = cameraX.coerceIn(0f, widthView - visibleViewWidth / scaleCamX)
        cameraY = cameraY.coerceIn(0f, heightView - visibleViewHeight / scaleCamY)
    }

    fun moveCamera(xDp: Float, yDp : Float) {
        val newCameraX = cameraX + xDp.toPx()
        val newCameraY = cameraY + yDp.toPx()
        cameraX = newCameraX.coerceIn(0f, widthView - visibleViewWidth / scaleCamX)
        cameraY = newCameraY.coerceIn(0f, heightView - visibleViewHeight / scaleCamY)
        invalidate()
    }

    private fun isTouchingLand2(): Boolean {
        val invertedMatrix = Matrix()
        val chickenCoordinates = floatArrayOf(chickenX, chickenY)

        matrixLand2.invert(invertedMatrix)
        invertedMatrix.mapPoints(chickenCoordinates)

        val chickenXConvert = chickenCoordinates[0]
        val chickenYConvert = chickenCoordinates[1]

        return isTouchingNoMatrix(chickenXConvert, chickenYConvert, rectLand2)
    }

    private fun isTouchingNoMatrix(chickenX : Float, chickenY : Float, rectLand : RectF): Boolean {
        val closestX = max(rectLand.left, min(chickenX, rectLand.right))
        val closestY = max(rectLand.top, min(chickenY, rectLand.bottom))

        val distanceX = chickenX - closestX
        val distanceY = chickenY - closestY

        val distanceSquared = distanceX.pow(2) + distanceY.pow(2)
        return distanceSquared < radiusChicken.pow(2)
    }

    private fun Float.toPx() : Float {
        return context.resources.displayMetrics.density * this
    }

    fun jump() {
        if(!isJumping){
            val deltaYdp = (3f + Random.nextInt(1,4).toFloat())
            deltaY -= deltaYdp.toPx()
            isJumping = true
            Log.d("TAG_JUMP", "jump: ${deltaYdp}")
            invalidate()
        }
    }

    fun resetGame() {
        isJumping = false
        chickenX = radiusChicken
        chickenY = rectLand1.top - radiusChicken
        deltaX = 2f.toPx()
        deltaY = 0f
        invalidate()
    }

    fun setGameListener(l : GameListener){
        this.listener = l
    }
}