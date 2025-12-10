package com.test.time.testchristmasgame

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val buttonPrediction: Button by lazy { findViewById(R.id.button1) }
    private val buttonDrawStar: Button by lazy { findViewById(R.id.button2) }
    private val buttonChickenJump: Button by lazy { findViewById(R.id.button3) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonPrediction.setOnClickListener {
            startActivity(Intent(this@MainActivity, PredictionFilterActivity::class.java))
        }

        buttonDrawStar.setOnClickListener {
            startActivity(Intent(this@MainActivity, DrawStarActivity::class.java))
        }

        buttonChickenJump.setOnClickListener {
            startActivity(Intent(this@MainActivity, ChickenJumpActivity::class.java))
        }
    }

}
