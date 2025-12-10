package com.test.time.testchristmasgame.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.test.time.testchristmasgame.model.Ball
import kotlin.math.sqrt

class PhysicsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs)  {

    private val BOUNCE_VALUE = 1f
    private val BALL_RADIUS = 40f.toPx()
    private val CIRCLE_RADIUS = 150f.toPx()

    private val paint = Paint()
    private lateinit var ball : Ball

    private var centerX1 : Float = 0f
    private var centerY1 : Float = 0f

    private var centerX2 : Float = 0f
    private var centerY2 : Float = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        paint.color = Color.RED
        paint.style = Paint.Style.FILL

        ball = Ball(0f, 100f, BALL_RADIUS, 5f, 5f)
        Log.d("TAGTest", "width : $width - height : $height")

        centerX1 = 0f
        centerY1 = height / 2f

        centerX2 = width.toFloat()
        centerY2 = height / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paint.color = Color.GREEN
        paint.style = Paint.Style.FILL

        //half circle 1
        canvas.drawCircle(
            centerX1, centerY1,
            CIRCLE_RADIUS,
            paint
        )

        //half circle 2
        canvas.drawCircle(
            centerX2, centerY2,
            CIRCLE_RADIUS,
            paint
        )

        paint.color = Color.RED
        canvas.drawCircle(ball.x, ball.y, ball.radius, paint)
    }

    fun updatePhysics() {
        ball.x += ball.vx
        ball.y += ball.vy

        ball.vy += 0.2f // first speed value

        val dx1 = ball.x - centerX1
        val dy1 = ball.y - centerY1

        val dx2 = ball.x - centerX2
        val dy2 = ball.y - centerY2

        val distanceToCircleLeft = sqrt(dx1 * dx1 + dy1 * dy1)
        val distanceToCircleRight = sqrt(dx2 * dx2 + dy2 * dy2)

        // circle left
        if (distanceToCircleLeft < CIRCLE_RADIUS + ball.radius) {
            val overlap = CIRCLE_RADIUS + ball.radius - distanceToCircleLeft
            val nx = dx1 / distanceToCircleLeft
            val ny = dy1 / distanceToCircleLeft
            ball.x += nx * overlap
            ball.y += ny * overlap

            val dot = ball.vx * nx + ball.vy * ny
            ball.vx -= 2 * dot * nx
            ball.vy -= 2 * dot * ny

            ball.vx *= BOUNCE_VALUE
            ball.vy *= BOUNCE_VALUE
        }

        // circle right
        if (distanceToCircleRight < CIRCLE_RADIUS + ball.radius) {
            val overlap = CIRCLE_RADIUS + ball.radius - distanceToCircleRight
            val nx = dx2 / distanceToCircleRight
            val ny = dy2 / distanceToCircleRight
            ball.x += nx * overlap
            ball.y += ny * overlap

            val dot = ball.vx * nx + ball.vy * ny
            ball.vx -= 2 * dot * nx
            ball.vy -= 2 * dot * ny

            ball.vx *= BOUNCE_VALUE
            ball.vy *= BOUNCE_VALUE
        }

        //top
        if (ball.y - ball.radius < 0) {
            ball.y = ball.radius
            ball.vy = -ball.vy * BOUNCE_VALUE
        }

        //bot
        if (ball.y + ball.radius > height) {
            ball.y = height - ball.radius
            ball.vy = -ball.vy * 0.1f
        }

        //left
        if (ball.x - ball.radius < 0) {
            ball.x = ball.radius
            if(centerY1 - ball.y < 0){
                ball.vx = -ball.vx * 0.1f
            } else {
                ball.vx = -ball.vx * BOUNCE_VALUE
            }
        }

        //right
        if (ball.x + ball.radius > width) {
            ball.x = width - ball.radius
            if(centerY1 - ball.y < 0){
                ball.vx = -ball.vx * 0.1f
            } else {
                ball.vx = -ball.vx * BOUNCE_VALUE
            }
        }

        invalidate()
    }

    fun Float.toPx() : Float {
        return context.resources.displayMetrics.density * this
    }

    fun checkWin(navAction : () -> Unit) {
        if(centerY1 - ball.y < 0){
            navAction()
        }
    }
}