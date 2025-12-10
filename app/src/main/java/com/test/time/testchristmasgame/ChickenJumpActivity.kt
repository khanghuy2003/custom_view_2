package com.test.time.testchristmasgame

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.test.time.testchristmasgame.databinding.ActivityChickenJumpBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChickenJumpActivity : AppCompatActivity() {

    private val binding: ActivityChickenJumpBinding by lazy {
        ActivityChickenJumpBinding.inflate(
            LayoutInflater.from(this@ChickenJumpActivity)
        )
    }

    private var job : Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        binding.chickenView.post {
            binding.up.setOnClickListener {
                binding.chickenView.moveCamera(0f, -20f)
            }
            binding.down.setOnClickListener {
                binding.chickenView.moveCamera(0f, 20f)
            }
            binding.left.setOnClickListener {
                binding.chickenView.moveCamera(-20f, 0f)
            }
            binding.right.setOnClickListener {
                binding.chickenView.moveCamera(20f, 0f)
            }

            binding.chickenView.setOnClickListener {
                binding.chickenView.jump()
            }

            job?.cancel()
            job = null
            job = lifecycleScope.launch {
                while (!this@ChickenJumpActivity.isDestroyed && !this@ChickenJumpActivity.isFinishing){
                    binding.chickenView.updateAnimation()
                    delay(16L)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        job?.cancel()
        job = null
    }

    override fun onStop() {
        super.onStop()
        job?.cancel()
        job = null
    }
}