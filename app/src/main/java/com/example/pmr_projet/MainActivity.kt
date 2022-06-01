package com.example.pmr_projet

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.app.ActivityCompat
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

    }
}