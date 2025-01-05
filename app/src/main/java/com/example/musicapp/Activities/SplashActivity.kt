package com.example.musicapp.Activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import com.example.musicapp.R

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    lateinit var text:TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        text = findViewById(R.id.text)
        text.visibility = TextView.GONE

        Handler(Looper.getMainLooper()).postDelayed({
            text.visibility = TextView.VISIBLE
        }, 1000)


            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this@SplashActivity, MusicListActivity::class.java)
                startActivity(intent)
                finish()
            }, 1700)


    }
}