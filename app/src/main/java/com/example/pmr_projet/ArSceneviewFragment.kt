package com.example.pmr_projet

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.*
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.Quaternion
import dev.romainguy.kotlin.math.max
import dev.romainguy.kotlin.math.rotation
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.quaternion
import io.github.sceneview.ar.getScene
import io.github.sceneview.ar.localRotation
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.EditableTransform
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import io.github.sceneview.utils.doOnApplyWindowInsets

class ArSceneviewFragment : Fragment(R.layout.fragment_ar_sceneview) {

    val modelPaths = listOf("models/ship.glb","models/spiderbot.glb","models/Persian.glb","models/gladiador.glb","models/OilCan.glb",
                            "models/Predator_s.glb")
    var currentModelIndex = 0
    var currentModel: String? = null

    lateinit var sceneView: ArSceneView
    lateinit var loadingView: View
    lateinit var changeModelButton: Button
    lateinit var modelNode: ArModelNode

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

        sceneView.onArFrame = {
            for (img in it.updatedAugmentedImages) {
                if (img.trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING) {
                    modelNode.isVisible = true

                    val anchor = img.anchors.firstOrNull() ?: img.createAnchor(img.centerPose.compose(Pose.makeRotation(Quaternion.fromEuler(0f,3f).toFloatArray())))
                    when (img.name) {
                        "ocean" -> changeModel("models/ship.glb", anchor)
                        "alien planet" -> changeModel("models/Predator_s.glb", anchor)
                        "living room" -> changeModel("models/Persian.glb", anchor)
                        "futuristic dystopia" -> changeModel("models/spiderbot.glb", anchor)
                    }
                } else {
                    for (anchor in img.anchors){anchor.detach()}
                    modelNode.isVisible = false
                }
            }
        }

        loadingView = view.findViewById(R.id.loadingView)
        changeModelButton = view.findViewById<Button?>(R.id.changeModelButton).apply {
            setOnClickListener { nextModel() }
        }

        modelNode = ArModelNode(placementMode = PlacementMode.BEST_AVAILABLE).apply {
            editableTransforms = EditableTransform.ALL
        }
        sceneView.addChild(modelNode)
        //nextModel()
        // Quick workaround until the Node Pick is fixed
        sceneView.gestureDetector.onTouchNode(modelNode)
    }

    private fun nextModel() {
        currentModelIndex++
        if(currentModelIndex >= modelPaths.size) {
            currentModelIndex = 0
        }
        val path = modelPaths[currentModelIndex]
        changeModel(path)
    }

    private fun changeModel(modelPath : String, anchor: Anchor? = null, units : Float = 0.04f) {
        if (modelPath != currentModel) {
            currentModel = modelPath

            isLoading = true
            modelNode.loadModelAsync(
                context = requireContext(),
                glbFileLocation = modelPath,
                lifecycle = lifecycle,
                autoAnimate = true
            ) {
                isLoading = false
                it.filamentAsset?.let { asset ->
                    val halfExtent = asset.boundingBox.halfExtent[0]
                    modelNode.modelScale = Float3(units / halfExtent)
                }
            }
        }
        if (modelNode.anchor != anchor) {
            modelNode.anchor = anchor
        }
    }
}