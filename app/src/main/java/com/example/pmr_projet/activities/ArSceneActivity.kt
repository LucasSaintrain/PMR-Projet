package com.example.pmr_projet.activities

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import com.example.pmr_projet.ar_scene.ArSceneviewFragment
import com.example.pmr_projet.R
import com.google.gson.Gson
import io.github.sceneview.utils.doOnApplyWindowInsets
import io.github.sceneview.utils.setFullScreen
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.SpeechStreamService
import org.vosk.android.StorageService
import java.io.IOException
import java.lang.IllegalStateException
import kotlin.Exception

class ArSceneActivity : AppCompatActivity(R.layout.activity_ar_scene) , RecognitionListener {
    lateinit var sceneviewFragment: ArSceneviewFragment
    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var speechStreamService: SpeechStreamService? = null
//    private var resultView: TextView? = null
    private lateinit var audioManager: AudioManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFullScreen(
            findViewById(R.id.rootView),
            fullScreen = true,
            hideSystemBars = false,
            fitsSystemWindows = false
        )

        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar)?.apply {
            doOnApplyWindowInsets { systemBarsInsets ->
                (layoutParams as ViewGroup.MarginLayoutParams).topMargin = systemBarsInsets.top
            }
            title = ""
        })

        try{
            audioManager = applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager
        }
        catch (e: Exception){
            print("Exception $e")
        }

        // Setup layout
//        resultView = findViewById(R.id.result_text)
//        setUiState(ArSceneActivity.STATE_START)
//        LibVosk.setLogLevel(LogLevel.INFO)

        // Check if user has given permission to record audio, init the model after permission is granted
        val permissionCheck =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                ArSceneActivity.PERMISSIONS_REQUEST_RECORD_AUDIO
            )
        } else {
            initModel()
        }

        recognizeMicrophone()


        sceneviewFragment = supportFragmentManager.findFragmentById(R.id.containerFragment) as ArSceneviewFragment
    }

    private fun initModel() {
        StorageService.unpack(this, "model-en-us", "model",
            { model: Model? ->
                this.model = model
                print ("TESTE")
//                setUiState(STATE_READY)
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


        val result = Gson().fromJson(hypothesis, ResultClass::class.java)
        if (result.text.contains("volume", ignoreCase = true)){
            if (result.text.contains("higher", ignoreCase = true)){
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
            }
            if (result.text.contains("lower", ignoreCase = true)) {
                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
            }
            if (result.text.contains(("cat_moves"), ignoreCase = true)) {
                sceneviewFragment.activeSceneNodes["living room"]?.invokeAction("action")
            }
        }

    }

    override fun onFinalResult(hypothesis: String) {
//        setUiState(STATE_DONE)
        if (speechStreamService != null) {
            speechStreamService = null
        }

    }

    override fun onPartialResult(hypothesis: String) {

    }

    override fun onError(e: Exception) {
        setErrorState(e.message)
    }

    override fun onTimeout() {
//        setUiState(STATE_DONE)
    }

//    private fun setUiState(state: Int) {
//        when (state) {
//            STATE_START -> {
//                resultView!!.setText(R.string.preparing)
//                resultView!!.movementMethod = ScrollingMovementMethod()
//                findViewById<View>(R.id.recognize_file).isEnabled = false
//                findViewById<View>(R.id.recognize_mic).isEnabled = false
//                findViewById<View>(R.id.pause).isEnabled = false
//            }
//            STATE_READY -> {
//                resultView!!.setText(R.string.ready)
//                (findViewById<View>(R.id.recognize_mic) as Button).setText(R.string.recognize_microphone)
//                findViewById<View>(R.id.recognize_file).isEnabled = true
//                findViewById<View>(R.id.recognize_mic).isEnabled = true
//                findViewById<View>(R.id.pause).isEnabled = false
//            }
//            STATE_DONE -> {
//                (findViewById<View>(R.id.recognize_file) as Button).setText(R.string.recognize_file)
//                (findViewById<View>(R.id.recognize_mic) as Button).setText(R.string.recognize_microphone)
//                findViewById<View>(R.id.recognize_file).isEnabled = true
//                findViewById<View>(R.id.recognize_mic).isEnabled = true
//                findViewById<View>(R.id.pause).isEnabled = false
//            }
//            STATE_FILE -> {
//                (findViewById<View>(R.id.recognize_file) as Button).setText(R.string.stop_file)
//                resultView!!.text = getString(R.string.starting)
//                findViewById<View>(R.id.recognize_mic).isEnabled = false
//                findViewById<View>(R.id.recognize_file).isEnabled = true
//                findViewById<View>(R.id.pause).isEnabled = false
//            }
//            STATE_MIC -> {
//                (findViewById<View>(R.id.recognize_mic) as Button).setText(R.string.stop_microphone)
//                resultView!!.text = getString(R.string.say_something)
//                findViewById<View>(R.id.recognize_file).isEnabled = false
//                findViewById<View>(R.id.recognize_mic).isEnabled = true
//                findViewById<View>(R.id.pause).isEnabled = true
//            }
//            else -> throw IllegalStateException("Unexpected value: $state")
//        }
//    }

    private fun setErrorState(message: String?) {
//        resultView!!.text = message
//        (findViewById<View>(R.id.recognize_mic) as Button).setText(R.string.recognize_microphone)
//        findViewById<View>(R.id.recognize_file).isEnabled = false
//        findViewById<View>(R.id.recognize_mic).isEnabled = false
    }

    private fun recognizeMicrophone() {
        if (speechService != null) {
//            setUiState(STATE_DONE)
            speechService!!.stop()
            speechService = null
        } else {
//            setUiState(STATE_MIC)
            try {
                val rec = Recognizer(model, 16000.0f)
                speechService = SpeechService(rec, 16000.0f)
                speechService!!.startListening(this)
            } catch (e: IOException) {
                setErrorState(e.message)
            }
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