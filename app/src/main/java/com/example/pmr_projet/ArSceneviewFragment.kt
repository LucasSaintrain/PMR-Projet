package com.example.pmr_projet

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.google.android.filament.utils.Quaternion
import com.google.ar.core.*
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.EditableTransform
import io.github.sceneview.ar.node.PlacementMode
import java.util.*

class ArSceneviewFragment : Fragment(R.layout.fragment_ar_sceneview) {
    lateinit var sceneView: ArSceneView
    lateinit var loadingView: View
    lateinit var sceneNode: ArModelNode
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

        val catPose = Pose.makeTranslation(-0.03f,0f,-0.025f)
            .compose(Pose.makeRotation(Quaternion.fromEulerZYX(0f,1.5f).toFloatArray()))

        val catModel = ArScene.ArModel("models/Persian.glb", catPose, 0.008f)
        val spiderbotModel = ArScene.ArModel("models/spiderbot.glb",Pose.makeTranslation(0.04f,0f,0f),0.01f)

        val alienModel = ArScene.ArModel("models/Predator_s.glb",Pose.IDENTITY,0.03f)
        val shipModel = ArScene.ArModel("models/ship.glb",Pose.IDENTITY,0.03f)

        val livingRoomScene = ArScene(setOf(catModel,spiderbotModel))
        val dystopiaScene = ArScene(setOf(spiderbotModel))
        val planetScene = ArScene(setOf(alienModel))
        val oceanScene = ArScene(setOf(shipModel))

        sceneView.onArFrame = {
            isLoading = livingRoomScene.isLoading && dystopiaScene.isLoading && planetScene.isLoading && oceanScene.isLoading
            for (img in it.updatedAugmentedImages) {
                if (img.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING) {
                    when (img.name) {
                        "ocean" -> {
                            if (!oceanScene.isLoaded) {
                                oceanScene.load(img.createAnchor(img.centerPose), sceneView)
                            }
                            oceanScene.sceneNode.isVisible = true
                        }
                        "alien planet" -> {
                            if (!planetScene.isLoaded) {
                                planetScene.load(img.createAnchor(img.centerPose), sceneView)
                            }
                            planetScene.sceneNode.isVisible = true
                        }
                        "living room" -> {
                            if (!livingRoomScene.isLoaded) {
                                livingRoomScene.load(img.createAnchor(img.centerPose), sceneView)
                            }
                            livingRoomScene.sceneNode.isVisible = true
                        }
                        "futuristic dystopia" -> {
                            if (!dystopiaScene.isLoaded) {
                                dystopiaScene.load(img.createAnchor(img.centerPose), sceneView)
                            }
                            dystopiaScene.sceneNode.isVisible = true
                        }
                    }
                } else {
                    when(img.name){
                        "ocean" -> oceanScene.sceneNode.isVisible = false
                        "alien planet" -> planetScene.sceneNode.isVisible = false
                        "living room" -> livingRoomScene.sceneNode.isVisible = false
                        "futuristic dystopia" -> dystopiaScene.sceneNode.isVisible = false
                    }
                }
            }
        }

        loadingView = view.findViewById(R.id.loadingView)
    }
}