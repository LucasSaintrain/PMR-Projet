package com.example.pmr_projet.ar_scene

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.ToggleButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.example.pmr_projet.ArSceneNode
import com.example.pmr_projet.ModelData
import com.example.pmr_projet.R
import com.example.pmr_projet.SceneData
import com.example.pmr_projet.activities.VoskActivity
import com.google.ar.core.*
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.*
import io.github.sceneview.math.*
import org.vosk.Model
import org.vosk.android.SpeechService
import org.vosk.android.SpeechStreamService
import kotlin.math.*

class ArSceneviewFragment : Fragment(R.layout.fragment_ar_sceneview) {
    lateinit var sceneView: ArSceneView
    lateinit var loadingView: View
    lateinit var button: Button
    lateinit var scenes: Map<String, SceneData>
    val activeSceneNodes: MutableMap<String, ArSceneNode?> = mutableMapOf()

    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var speechStreamService: SpeechStreamService? = null
    private var resultView: TextView? = null
    private lateinit var audioManager: AudioManager


    var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
        }

    private fun setupSceneData() {
        val catModel = ModelData("models/Persian.glb",
            Scale(0.1f), Position(-0.20f,0f,-0.25f), Rotation(0f,90f, 0f)
        )
        val spiderbotModel = ModelData("models/spiderbot.glb",
            Scale(0.2f), Position(0.35f,0f,0f)
        )
        val alienModel = ModelData("models/Predator_s.glb", Scale(0.3f))
        val shipModel = ModelData("models/ship.glb", Scale(0.3f))
        val miguelComeCu = ModelData("models/wer.glb", Scale(5f))

        val action = SceneAction.changePosition("cat", Position(0.2f,0f,0f))

        val livingRoomScene = SceneData(mapOf("cat" to catModel, "spiderbot" to spiderbotModel), mapOf("mover gato" to action))
        val dystopiaScene = SceneData(mapOf("spiderbot" to spiderbotModel))
        val planetScene = SceneData(mapOf("predator" to alienModel))
        val oceanScene = SceneData(mapOf("prince" to miguelComeCu))


        scenes = mapOf("living room" to livingRoomScene, "futuristic dystopia" to dystopiaScene, "alien planet" to planetScene, "ocean" to oceanScene)
    }


/*    fun voskCreate(view: View) {
        view.run {
            audioManager = applicationContext.getSystemService(Activity.AUDIO_SERVICE) as AudioManager

            // Setup layout
            resultView = findViewById(R.id.result_text)
            setUiState(VoskActivity.STATE_START)
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
                    VoskActivity.PERMISSIONS_REQUEST_RECORD_AUDIO
                )
            } else {
                initModel()
            }
        }

    }*/


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingView = view.findViewById(R.id.loadingView)


        button = view.findViewById(R.id.button)
        button.setOnClickListener {
            activeSceneNodes["living room"]?.invokeAction("mover gato")
        }

        sceneView = view.findViewById(R.id.sceneView)
        setupSceneData()

        sceneView.onArSessionCreated = {
            val imageDatabase = AugmentedImageDatabase(it)
            val config = it.config

            config.augmentedImageDatabase = requireContext().assets.let {
                imageDatabase.apply {
                    addImage("ocean", it.open("backgrounds/ocean.png").use { BitmapFactory.decodeStream(it) })
                    addImage("alien planet", it.open("backgrounds/alien planet.png").use { BitmapFactory.decodeStream(it) })
                    addImage("living room", it.open("backgrounds/living room.png").use { BitmapFactory.decodeStream(it) })
                    addImage("futuristic dystopia", it.open("backgrounds/futuristic dystopia.png").use { BitmapFactory.decodeStream(it) })
                }
            }

            it.configure(config)
        }

        sceneView.onArFrame = {
            isLoading = activeSceneNodes.values.any { it?.isLoading ?: false }

            for (img in it.updatedAugmentedImages) {
                if (img.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING) {
                    if (!activeSceneNodes.containsKey(img.name)) {
                        // Unload old scenes
                        it.updatedAugmentedImages.filter { it.trackingMethod != AugmentedImage.TrackingMethod.FULL_TRACKING }.forEach {
                            activeSceneNodes[it.name]?.destroy()
                            activeSceneNodes.remove(it.name)
                        }

                        // Load new scene
                        val newSceneNode = scenes[img.name]?.loadAR(sceneView)
                        newSceneNode?.bindToAugmentedImage(img)
                        activeSceneNodes[img.name] = newSceneNode
                    }
                    activeSceneNodes[img.name]?.isVisible = true
                    activeSceneNodes[img.name]?.worldScale = Float3(img.extentX, 1f, img.extentZ)

                } else if (img.trackingMethod == AugmentedImage.TrackingMethod.LAST_KNOWN_POSE) {
                    // Hide scene if the image angle > 60
                    val cameraDirection = it.camera.pose.zDirection.toVector3().normalized()
                    val imageDirection = img.centerPose.yDirection.toVector3().normalized()
                    val angle = (cameraDirection to imageDirection).let { (v1,v2) ->
                        acos(v1.x*v2.x + v1.y*v2.y + v1.z*v2.z) * 180/PI
                    }

                    if (angle > 60) {
                        activeSceneNodes[img.name]?.isVisible = false
                    }
                }
            }
        }
    }
}