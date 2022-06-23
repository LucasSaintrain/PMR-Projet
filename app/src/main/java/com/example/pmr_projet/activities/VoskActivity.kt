// Copyright 2019 Alpha Cephei Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.example.pmr_projet.activities

import android.Manifest
import android.app.Activity
import org.vosk.android.SpeechService
import org.vosk.android.SpeechStreamService
import android.widget.TextView
import android.os.Bundle
import com.example.pmr_projet.R
import android.widget.ToggleButton
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.media.AudioManager
import androidx.core.app.ActivityCompat
import org.vosk.android.StorageService
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import com.google.gson.Gson
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import java.io.IOException
import java.lang.Exception
import java.lang.IllegalStateException

class VoskActivity : Activity(), RecognitionListener {
    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var speechStreamService: SpeechStreamService? = null
    private var resultView: TextView? = null
    private lateinit var audioManager: AudioManager
    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_vosk)

        audioManager = applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager

        // Setup layout
        resultView = findViewById(R.id.result_text)
        setUiState(STATE_START)
        findViewById<View>(R.id.recognize_file).setOnClickListener { recognizeFile() }
        findViewById<View>(R.id.recognize_mic).setOnClickListener { recognizeMicrophone() }
        (findViewById<View>(R.id.pause) as ToggleButton).setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            pause(
                isChecked
            )
        }
//        LibVosk.setLogLevel(LogLevel.INFO)

        // Check if user has given permission to record audio, init the model after permission is granted
        val permissionCheck =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_REQUEST_RECORD_AUDIO
            )
        } else {
            initModel()
        }

    }

    private fun initModel() {
        StorageService.unpack(this, "vosk-model-small-fr-0.22", "model",
            { model: Model? ->
                this.model = model
                setUiState(STATE_READY)
            }
        ) { exception: IOException -> setErrorState("Failed to unpack the model" + exception.message) }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
                initModel()
            } else {
                finish()
            }
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (speechService != null) {
            speechService!!.stop()
            speechService!!.shutdown()
        }
        if (speechStreamService != null) {
            speechStreamService!!.stop()
        }
    }

    override fun onResult(hypothesis: String) {
        resultView!!.append(
            """
    $hypothesis
    
    """.trimIndent()
        )

        val result = Gson().fromJson(hypothesis, ResultClass::class.java)
        if (result.text.contains("volume", ignoreCase = true)){
            if (result.text.contains("higher", ignoreCase = true)){
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
            }
            if (result.text.contains("lower", ignoreCase = true)) {
                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
            }
        }




    }

    override fun onFinalResult(hypothesis: String) {
        resultView!!.append(
            """
    $hypothesis
    
    """.trimIndent()
        )
        setUiState(STATE_DONE)
        if (speechStreamService != null) {
            speechStreamService = null
        }

    }

    override fun onPartialResult(hypothesis: String) {
        resultView!!.append(
            """
    $hypothesis
    
    """.trimIndent()
        )
    }

    override fun onError(e: Exception) {
        setErrorState(e.message)
    }

    override fun onTimeout() {
        setUiState(STATE_DONE)
    }

    private fun setUiState(state: Int) {
        when (state) {
            STATE_START -> {
                resultView!!.setText(R.string.preparing)
                resultView!!.movementMethod = ScrollingMovementMethod()
                findViewById<View>(R.id.recognize_file).isEnabled = false
                findViewById<View>(R.id.recognize_mic).isEnabled = false
                findViewById<View>(R.id.pause).isEnabled = false
            }
            STATE_READY -> {
                resultView!!.setText(R.string.ready)
                (findViewById<View>(R.id.recognize_mic) as Button).setText(R.string.recognize_microphone)
                findViewById<View>(R.id.recognize_file).isEnabled = true
                findViewById<View>(R.id.recognize_mic).isEnabled = true
                findViewById<View>(R.id.pause).isEnabled = false
            }
            STATE_DONE -> {
                (findViewById<View>(R.id.recognize_file) as Button).setText(R.string.recognize_file)
                (findViewById<View>(R.id.recognize_mic) as Button).setText(R.string.recognize_microphone)
                findViewById<View>(R.id.recognize_file).isEnabled = true
                findViewById<View>(R.id.recognize_mic).isEnabled = true
                findViewById<View>(R.id.pause).isEnabled = false
            }
            STATE_FILE -> {
                (findViewById<View>(R.id.recognize_file) as Button).setText(R.string.stop_file)
                resultView!!.text = getString(R.string.starting)
                findViewById<View>(R.id.recognize_mic).isEnabled = false
                findViewById<View>(R.id.recognize_file).isEnabled = true
                findViewById<View>(R.id.pause).isEnabled = false
            }
            STATE_MIC -> {
                (findViewById<View>(R.id.recognize_mic) as Button).setText(R.string.stop_microphone)
                resultView!!.text = getString(R.string.say_something)
                findViewById<View>(R.id.recognize_file).isEnabled = false
                findViewById<View>(R.id.recognize_mic).isEnabled = true
                findViewById<View>(R.id.pause).isEnabled = true
            }
            else -> throw IllegalStateException("Unexpected value: $state")
        }
    }

    private fun setErrorState(message: String?) {
        resultView!!.text = message
        (findViewById<View>(R.id.recognize_mic) as Button).setText(R.string.recognize_microphone)
        findViewById<View>(R.id.recognize_file).isEnabled = false
        findViewById<View>(R.id.recognize_mic).isEnabled = false
    }

    private fun recognizeFile() {
        if (speechStreamService != null) {
            setUiState(STATE_DONE)
            speechStreamService!!.stop()
            speechStreamService = null
        } else {
            setUiState(STATE_FILE)
            try {
                val rec = Recognizer(
                    model, 16000f, "[\"one zero zero zero one\", " +
                            "\"oh zero one two three four five six seven eight nine\", \"[unk]\"]"
                )
                val ais = assets.open(
                    "10001-90210-01803.wav"
                )
                if (ais.skip(44) != 44L) throw IOException("File too short")
                speechStreamService = SpeechStreamService(rec, ais, 16000F)
                speechStreamService!!.start(this)
            } catch (e: IOException) {
                setErrorState(e.message)
            }
        }
    }

    private fun recognizeMicrophone() {
        if (speechService != null) {
            setUiState(STATE_DONE)
            speechService!!.stop()
            speechService = null
        } else {
            setUiState(STATE_MIC)
            try {
                val rec = Recognizer(model, 16000.0f)
                speechService = SpeechService(rec, 16000.0f)
                speechService!!.startListening(this)
            } catch (e: IOException) {
                setErrorState(e.message)
            }
        }
    }

    private fun pause(checked: Boolean) {
        if (speechService != null) {
            speechService!!.setPause(checked)
        }
    }

    companion object {
        private const val STATE_START = 0
        private const val STATE_READY = 1
        private const val STATE_DONE = 2
        private const val STATE_FILE = 3
        private const val STATE_MIC = 4

        /* Used to handle permission request */
        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 1
    }

    class ResultClass (val text: String){
    }

}

