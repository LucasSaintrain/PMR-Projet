package com.example.pmr_projet

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.google.ar.core.*
import com.google.ar.sceneform.math.Vector3
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Quaternion
import dev.romainguy.kotlin.math.length
import dev.romainguy.kotlin.math.normalize
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.*
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.math.*
import java.util.*
import kotlin.math.*

class ArSceneviewFragment : Fragment(R.layout.fragment_ar_sceneview) {
    lateinit var sceneView: ArSceneView
    lateinit var loadingView: View
    lateinit var scenes: Map<String,ArScene>

    var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sceneView = view.findViewById(R.id.sceneView)

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

        val catPose = Pose.makeTranslation(-0.20f,0f,-0.25f)
            .compose(Pose.makeRotation(Quaternion.fromEuler(0f,1.5f).toFloatArray()))

        val catModel = ArScene.ArModel("models/Persian.glb", catPose, 0.1f)
        val spiderbotModel = ArScene.ArModel("models/spiderbot.glb",Pose.makeTranslation(0.35f,0f,0f),0.2f)

        val alienModel = ArScene.ArModel("models/Predator_s.glb",Pose.IDENTITY,0.3f)
        val shipModel = ArScene.ArModel("models/ship.glb",Pose.IDENTITY,0.3f)

        val livingRoomScene = ArScene(setOf(catModel,spiderbotModel))
        val dystopiaScene = ArScene(setOf(spiderbotModel))
        val planetScene = ArScene(setOf(alienModel))
        val oceanScene = ArScene(setOf(shipModel))

        scenes = mapOf("living room" to livingRoomScene, "futuristic dystopia" to dystopiaScene, "alien planet" to planetScene, "ocean" to oceanScene)

        sceneView.onArFrame = {
            isLoading = scenes.any { it.value.isLoading }

            for (img in it.updatedAugmentedImages) {
                if (img.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING) {
                    scenes[img.name]?.run {
                        if (!isLoaded && !isLoading) {
                            it.updatedAugmentedImages.filter { it.trackingMethod != AugmentedImage.TrackingMethod.FULL_TRACKING }.forEach {
                                scenes[it.name]?.unload()
                            }
                            img.let {
                                load(it.createAnchor(it.centerPose), it.extentX, it.extentZ, sceneView)
                            }
                        }
                        sceneNode?.isVisible = true
                    }
                } else if (img.trackingMethod == AugmentedImage.TrackingMethod.LAST_KNOWN_POSE) {
                    // Hide scene if the image angle > 60
                    val cameraDirection = it.camera.pose.zDirection.toVector3().normalized()
                    val imageDirection = img.centerPose.yDirection.toVector3().normalized()
                    val angle = (cameraDirection to imageDirection).let { (v1,v2) ->
                        acos(v1.x*v2.x + v1.y*v2.y + v1.z*v2.z) * 180/PI
                    }

                    if (angle > 60) {
                        scenes[img.name]?.sceneNode?.isVisible = false
                    }
                }
            }
        }

        loadingView = view.findViewById(R.id.loadingView)
    }
}