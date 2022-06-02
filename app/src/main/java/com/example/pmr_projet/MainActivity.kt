package com.example.pmr_projet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val voiceButton: Button = findViewById(R.id.buttonVoice)
        voiceButton.setOnClickListener{
            val intent = Intent(this, VoskActivity::class.java)
            startActivity(intent)
        }

        val sceneviewButton: Button = findViewById(R.id.buttonSceneview)
        sceneviewButton.setOnClickListener{
            val intent = Intent(this, ArSceneActivity::class.java)
            startActivity(intent)
        }
    }
}