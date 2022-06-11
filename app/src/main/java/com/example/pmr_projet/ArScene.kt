package com.example.pmr_projet

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.google.ar.core.Anchor
import com.google.ar.core.Pose
import com.google.ar.sceneform.rendering.RenderableInternalFilamentAssetData
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.Filament.renderableManager
import io.github.sceneview.SceneView
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.light.destroy
import io.github.sceneview.model.destroy

class ArScene(val models: Set<ArModel>) {
    private val modelNodes = mutableSetOf<ArNode>()
    var sceneNode : ArNode? = null
    var isLoaded = false

    var isLoading = false
        get() = field || models.any { it.isLoading }

    fun load(anchor: Anchor, extentX: Float, extentZ: Float, sceneView: SceneView) : ArNode? {
        if (isLoaded || isLoading) return sceneNode
        isLoading = true
        sceneNode = ArNode()

        sceneNode!!.apply {
            sceneView.addChild(this)
            this.anchor = anchor
            worldScale = Float3(extentX, 1f, extentZ)
        }

        models.forEach {
            ArNode().apply {
                modelNodes.add(this)
                sceneNode!!.addChild(this)

                pose = it.initialPose
                scale = Float3(it.initialScale)
                worldScale = Float3(worldScale[it.parentScaleAxis.value])

                loadModel(it, this, sceneView.context, sceneView.lifecycle)
            }
        }

        isLoaded = true
        isLoading = false
        return sceneNode
    }

    fun unload() {
        if (!isLoaded || isLoading) return
        isLoading = true

        modelNodes.forEach { it.destroy() }
        modelNodes.clear()

        sceneNode?.destroy()
        sceneNode = null

        isLoaded = false
        isLoading = false
    }

    private fun loadModel(model: ArModel, modelNode: ArNode, context: Context, lifecycle: Lifecycle) {
        model.run {
            isLoading = true
            modelNode.loadModelAsync(context,lifecycle,glbPath,autoAnimate,false
            ) {
                //Set scale (using the chosen axis)
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