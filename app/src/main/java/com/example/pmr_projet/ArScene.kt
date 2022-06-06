package com.example.pmr_projet

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.google.ar.core.*
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.EditableTransform
import io.github.sceneview.ar.node.PlacementMode

class ArScene(val models: Set<ArModel>) {
    val modelNodes = mutableSetOf<ArModelNode>()
    var isLoaded = false

    val isLoading
        get() = models.any { it.isLoading }

    var isVisible = true
        set(value) {
            modelNodes.forEach {
                it.isVisible = value
            }
            field = value
        }


    fun load(image: AugmentedImage, context: Context, lifecycle: Lifecycle, sceneView: ArSceneView) {
        isLoaded = true

        models.forEach {
            val modelNode = newModelNode(sceneView)
            loadModel(it,modelNode, context,lifecycle)

            val pose = image.centerPose.compose(it.pose)
            modelNode.anchor = image.createAnchor(pose)
        }
    }

    fun unload() {
        modelNodes.forEach {
            it.destroy()
        }
        isLoaded = false
    }

    private fun newModelNode(sceneView: ArSceneView): ArModelNode {
        val modelNode = ArModelNode(placementMode = PlacementMode.BEST_AVAILABLE).apply {
            //editableTransforms = EditableTransform.NONE
        }
        sceneView.addChild(modelNode)
        sceneView.gestureDetector.onTouchNode(modelNode)

        modelNodes.add(modelNode)
        return modelNode
    }

    private fun loadModel(model: ArModel, modelNode: ArModelNode, context: Context, lifecycle: Lifecycle) {
        model.run {
            isLoading = true
            modelNode.loadModelAsync(context,lifecycle,glbPath,autoAnimate,false
            ) {
                //Set scale (using the x-axis)
                it.filamentAsset?.let { asset ->
                    val halfExtent = asset.boundingBox.halfExtent[0]
                    modelNode.modelScale = Float3(scale / halfExtent)
                }
                isLoading = false
            }
        }
    }

    data class ArModel(val glbPath: String, val pose: Pose, val scale: Float, val autoAnimate: Boolean = true){
        var isLoading = false
    }
}