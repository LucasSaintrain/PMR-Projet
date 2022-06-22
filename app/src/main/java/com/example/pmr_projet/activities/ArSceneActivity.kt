package com.example.pmr_projet.activities

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
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
import java.lang.Exception

class ArSceneActivity : AppCompatActivity(R.layout.activity_ar_scene), RecognitionListener {
    lateinit var sceneviewFragment: ArSceneviewFragment

    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var speechStreamService: SpeechStreamService? = null
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

        sceneviewFragment = supportFragmentManager.findFragmentById(R.id.containerFragment) as ArSceneviewFragment

        // Button for testing
        findViewById<Button>(R.id.button).setOnClickListener {
            sceneviewFragment.invokeSceneAction("living room","mover gato")
        }

        audioManager = applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager

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

    }


    private fun initModel() {
        StorageService.unpack(this, "model-en-us", "model",
            { model: Model? ->
                this.model = model
                recognizeMicrophone()
            }
        ) { exception: IOException -> setErrorState("Failed to unpack the model" + exception.message) }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ArSceneActivity.PERMISSIONS_REQUEST_RECORD_AUDIO) {
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

        val result = Gson().fromJson(hypothesis, VoskActivity.ResultClass::class.java)
        for ((key, value) in ListActions.commandToSceneAction)
        {
            if (result.text.contains(key, ignoreCase = true)){
                sceneviewFragment.invokeSceneAction(value.first, value.second)
            }
        }


    }

    override fun onFinalResult(hypothesis: String) {
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
    }



    private fun setErrorState(message: String?) {
    }


    private fun recognizeMicrophone() {
        if (speechService != null) {
            speechService!!.stop()
            speechService = null
        } else {
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