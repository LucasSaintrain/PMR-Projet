package com.example.pmr_projet

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import com.google.ar.core.*
import com.google.gson.ToNumberStrategy
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

    fun load(anchor: Anchor, extentX: Float, extentZ: Float, sceneView: SceneView) : ArNode {
        isLoaded = true

        sceneView.addChild(sceneNode)
        sceneNode.anchor = anchor
        sceneNode.worldScale = Float3(extentX, 1f, extentZ)

        models.forEach {
            val modelNode = ArNode()
            sceneNode.addChild(modelNode)
            modelNodes.add(modelNode)

            modelNode.apply {
                pose = it.initialPose
                scale = Float3(it.initialScale)
                worldScale = Float3(worldScale[it.parentScaleAxis.value])
            }
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
                //Set scale (using the choosen axis)
                it.filamentAsset?.let { asset ->
                    val halfExtent = asset.boundingBox.halfExtent[modelScaleAxis.value]
                    modelNode.modelScale = Float3(0.5f / halfExtent)
                }
                isLoading = false
            }
        }
    }

    data class ArModel(val glbPath: String, val initialPose: Pose, val initialScale: Float,
                       val modelScaleAxis: DirectionXYZ = DirectionXYZ.X,
                       val parentScaleAxis: DirectionXYZ = DirectionXYZ.X,
                       val autoAnimate: Boolean = true) {
        var isLoading = false

        enum class DirectionXYZ(val value: Int) {
            X(0), Y(1), Z(2)
        }
    }
}