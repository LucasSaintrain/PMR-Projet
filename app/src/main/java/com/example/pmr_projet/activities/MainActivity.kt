package com.example.pmr_projet.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.pmr_projet.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val voiceButton: Button = findViewById(R.id.buttonVoice)
        voiceButton.setOnClickListener{
            val intent = Intent(this, VoskActivity::class.java)
            startActivity(intent)
        }

        //val sceneviewButton: Button = findViewById(R.id.buttonSceneview)
        //sceneviewButton.setOnClickListener{
            val intent = Intent(this, ArSceneActivity::class.java)
            startActivity(intent)
        //}
    }
}