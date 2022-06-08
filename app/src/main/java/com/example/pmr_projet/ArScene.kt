package com.example.pmr_projet

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.google.ar.core.*
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.SceneView
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.position
import io.github.sceneview.ar.arcore.quaternion
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.ar.node.EditableTransform
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node

class ArScene(val models: Set<ArModel>) {
    val modelNodes = mutableSetOf<ArNode>()
    val sceneNode  = ArNode()
    var isLoaded = false

    val isLoading
        get() = models.any { it.isLoading }

    fun load(anchor: Anchor, sceneView: SceneView) : ArNode {
        isLoaded = true

        sceneNode.anchor = anchor
        sceneView.addChild(sceneNode)

        models.forEach {
            val modelNode = ArNode()
            sceneNode.addChild(modelNode)
            modelNodes.add(modelNode)

            modelNode.pose = it.pose
            loadModel(it, modelNode, sceneView.context, sceneView.lifecycle)
        }

        return sceneNode
    }

    fun unload() {
        modelNodes.forEach {
            it.destroy()
        }
        modelNodes.clear()
        sceneNode.destroy()

        isLoaded = false
    }

    private fun loadModel(model: ArModel, modelNode: ArNode, context: Context, lifecycle: Lifecycle) {
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