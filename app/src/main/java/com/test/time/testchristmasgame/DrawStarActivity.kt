package com.test.time.testchristmasgame

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.test.time.testchristmasgame.databinding.ActivityDrawStarBinding

class DrawStarActivity : AppCompatActivity() {

    private val binding: ActivityDrawStarBinding by lazy {
        ActivityDrawStarBinding.inflate(
            LayoutInflater.from(this@DrawStarActivity)
        )
    }

    private var tapCount = 0
    private val maxTapCount = 5
    private val animators = mutableListOf<ObjectAnimator>()
    private var wingStarAnimator: ObjectAnimator? = null
    private var isPlaying = false
    private val slowDuration = 5000L
    private val wingStarViews = mutableListOf<View>()
    private val tapAngles = mutableListOf<Float>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.buttonStart.setOnClickListener {
            resetPlayActivity()
            startGame()
        }
    }

    override fun onResume() {
        super.onResume()
        resetPlayActivity()
    }

    private fun startGame() {
        if(isPlaying) return
        isPlaying = true
        tapCount = 0
        tapAngles.clear()
        val overlay = binding.overlayContainer
        for (i in overlay.childCount - 1 downTo 0) {
            val child = overlay.getChildAt(i)
            if (child.tag == "wing_star_child") {
                overlay.removeViewAt(i)
            }
        }
        wingStarViews.clear()

        clearAllStars()
        binding.imgStar.visibility = View.VISIBLE
        binding.imgWingStar.setImageResource(R.drawable.img_wing_star)
        binding.imgWingStar.bringToFront()
        startRotate(binding.imgWingStar, slowDuration)
        setupTouchListener()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        val overlay = binding.overlayContainer
        overlay.setOnTouchListener { _, event ->
            if (!isPlaying) return@setOnTouchListener false

            if (event.action == MotionEvent.ACTION_DOWN) {
                if (tapCount < maxTapCount) {
                    tapCount++
                    tapAngles.add(binding.imgWingStar.rotation)
                    addStarAtImgWingStar(overlay)
                    updateWingStarSpeed()
                    if (tapCount >= maxTapCount) {
                        stopGame()
                    }
                }
                return@setOnTouchListener true
            }
            false
        }
    }

    private fun stopGame() {
        isPlaying = false
        animators.forEach { it.cancel() }
        animators.clear()
        wingStarAnimator = null
        binding.imgStar.visibility = View.INVISIBLE
        binding.imgWingStar.setImageResource(R.drawable.img_wing_star_no_border)
        binding.overlayContainer.setOnTouchListener(null)

        if (tapCount >= maxTapCount && tapAngles.size >= maxTapCount) {
            val percent = calculateStarMatchPercent()
            binding.tvDesc.visibility = View.VISIBLE
            binding.tvDesc.text = "${"%.0f".format(percent)}%"

            playFinishAnimationAllStars()
        }
    }

    private fun resetPlayActivity() {
        isPlaying = false
        tapCount = 0
        tapAngles.clear()

        // Reset UI
        binding.tvDesc.visibility = View.VISIBLE
        binding.tvDesc.text = "record to play"

        animators.forEach {
            try {
                it.cancel()
            } catch (_: Exception) {}
        }
        animators.clear()
        wingStarAnimator = null

        val overlay = binding.overlayContainer
        for (i in overlay.childCount - 1 downTo 0) {
            val child = overlay.getChildAt(i)
            if (child.tag == "wing_star_child") {
                overlay.removeViewAt(i)
            }
        }
        wingStarViews.clear()

        binding.overlayContainer.setOnTouchListener(null)

        binding.imgPineTree.apply {
            translationY = 0f
            translationX = 0f
        }

        binding.imgWingStar.apply {
            clearAnimation()
            rotation = 0f
            scaleX = 1f
            scaleY = 1f
            translationX = 0f
            translationY = 0f
            setImageResource(R.drawable.img_wing_star)
        }

        binding.overlayContainer.post {
            val starView = binding.imgStar
            if (starView.width > 0 && starView.height > 0) {
                binding.imgWingStar.x = starView.x
                binding.imgWingStar.y = starView.y
            } else {
                binding.overlayContainer.requestLayout()
                binding.overlayContainer.post {
                    binding.imgWingStar.x = binding.imgStar.x
                    binding.imgWingStar.y = binding.imgStar.y
                }
            }
        }

        binding.imgStar.visibility = View.VISIBLE
    }

    private fun playFinishAnimationAllStars() {
        val pineTree = binding.imgPineTree

        val stars = mutableListOf<View>().apply {
            add(binding.imgWingStar)
            addAll(wingStarViews)
        }

        if (stars.isEmpty()) return

        pineTree.bringToFront()
        stars.forEach { it.bringToFront() }

        pineTree.post {
            var sumCenterX = 0f
            var sumCenterY = 0f
            stars.forEach { star ->
                sumCenterX += star.x + star.width / 2f
                sumCenterY += star.y + star.height / 2f
            }
            val groupCenterX = sumCenterX / stars.size
            val groupCenterY = sumCenterY / stars.size

            val treeX = pineTree.x
            val treeY = pineTree.y
            val treeWidth = pineTree.width

            val treeTopCenterX = treeX + treeWidth / 2f
            val treeTopCenterY = treeY - stars.first().height * 0.30f

            val deltaX = treeTopCenterX - groupCenterX
            val deltaY = treeTopCenterY - groupCenterY

            val allAnimators = mutableListOf<Animator>()

            val moveTreeUp = ObjectAnimator.ofFloat(
                pineTree,
                View.TRANSLATION_Y,
                pineTree.translationY,
                pineTree.translationY - pineTree.height * 0.45f
            ).apply {
                duration = 1500
                interpolator = AccelerateDecelerateInterpolator()
            }
            allAnimators.add(moveTreeUp)

            stars.forEach { star ->
                val startX = star.x
                val startY = star.y
                val targetX = startX + deltaX
                val targetY = startY + deltaY

                val moveX = ObjectAnimator.ofFloat(star, View.X, startX, targetX)
                val moveY = ObjectAnimator.ofFloat(star, View.Y, startY, targetY)
                val scaleX = ObjectAnimator.ofFloat(star, View.SCALE_X, star.scaleX, 0.3f)
                val scaleY = ObjectAnimator.ofFloat(star, View.SCALE_Y, star.scaleY, 0.3f)

                listOf(moveX, moveY, scaleX, scaleY).forEach {
                    it.duration = 1500
                    it.interpolator = AccelerateDecelerateInterpolator()
                }

                allAnimators.addAll(listOf(moveX, moveY, scaleX, scaleY))
            }

            AnimatorSet().apply {
                playTogether(allAnimators)
                start()
            }
        }
    }

    private fun calculateStarMatchPercent(): Float {
        if (tapAngles.size < maxTapCount) return 0f

        var scoreSum = 0f

        tapAngles.forEach { angle ->
            val norm = ((angle % 360f) + 360f) % 360f

            val nearestStep = (Math.round(norm / 72f) * 72f) % 360
            val diff = angleDiff(norm, nearestStep.toFloat())

            val normalized = (diff / 36f).coerceAtMost(1f)
            val score = 1f - normalized
            scoreSum += score
        }

        val avgScore = scoreSum / tapAngles.size
        return avgScore * 100f
    }

    private fun angleDiff(a: Float, b: Float): Float {
        var d = (a - b) % 360f
        if (d < 0) d += 360f
        return if (d > 180f) 360f - d else d
    }

    private fun startRotate(view: View, duration: Long) {
        val startRotation = view.rotation
        val animator = ObjectAnimator.ofFloat(view, View.ROTATION, startRotation, startRotation + 360f).apply {
            this.duration = duration
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
        animators.add(animator)
        if (view == binding.imgWingStar) {
            wingStarAnimator = animator
        }
    }

    private fun updateWingStarSpeed() {
        val desiredDuration = when(tapCount){
            0 -> 4000L
            1 -> 3400L
            2 -> 2800L
            3 -> 2200L
            4 -> 1600L
            else -> 1000L
        }
        val currentDuration = wingStarAnimator?.duration ?: -1L
        if (currentDuration == desiredDuration) return

        wingStarAnimator?.cancel()
        wingStarAnimator?.let { animators.remove(it) }

        startRotate(binding.imgWingStar, desiredDuration)
    }

    private fun addStarAtImgWingStar(container: ConstraintLayout) {
        val imageView = View.inflate(this, R.layout.item_wing_star, null) as View

        imageView.visibility = View.INVISIBLE

        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        val wingStarIndex = container.indexOfChild(binding.imgWingStar)
        val insertIndex = if (wingStarIndex >= 0) wingStarIndex + 1 else 0
        container.addView(imageView, insertIndex, params)

        imageView.tag = "wing_star_child"
        wingStarViews.add(imageView)

        binding.imgWingStar.bringToFront()

        imageView.post {
            val base = binding.imgWingStar

            imageView.x = base.x + base.width / 2f - imageView.width / 2f
            imageView.y = base.y + base.height / 2f - imageView.height / 2f

            imageView.rotation = base.rotation

            imageView.visibility = View.VISIBLE

            binding.imgWingStar.bringToFront()
        }
    }

    private fun clearAllStars() {
        animators.forEach { it.cancel() }
        animators.clear()
        wingStarAnimator = null
    }
}