package com.test.time.testchristmasgame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.test.time.testchristmasgame.view.PhysicsView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PredictionFilterActivity : AppCompatActivity() {

    private val physicsView : PhysicsView by lazy { findViewById(R.id.physicsView) }
    private var job : Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prediction_filter)

        job?.cancel()
        job = lifecycleScope.launch {
            while (true){
                physicsView.post {
                    physicsView.updatePhysics()
                }
                delay(16L)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        job?.cancel()
        job = null
    }
}