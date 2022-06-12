package com.example.pmr_projet

import android.content.Context
import androidx.lifecycle.Lifecycle
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Pose
import com.google.ar.sceneform.rendering.RenderableInternalFilamentAssetData
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.Filament.renderableManager
import io.github.sceneview.SceneView
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.light.destroy
import io.github.sceneview.model.destroy
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node

class ArSceneNode(val data: SceneData, sceneView: SceneView) : ArNode() {
    val modelNodes : Map<String, ModelRenderNode> = data.models.keys.associateWith { ModelRenderNode(data.models[it]!!, sceneView) }

    val isLoading
        get() = modelNodes.values.any { it.isLoading }

    init {
        sceneView.addChild(this)
        modelNodes.values.forEach { addChild(it) }
    }

    fun bindToAugmentedImage(image: AugmentedImage) {
        image.let {
            anchor = it.createAnchor(it.centerPose)
            worldScale = Float3(it.extentX, 1f, it.extentZ)
        }

        modelNodes.values.forEach {
            it.worldScale = Float3(it.worldScale[it.data.parentScaleAxis.value])
        }
    }

    fun reset() {
        modelNodes.values.forEach { it.reset() }
    }

    override fun destroy() {
        modelNodes.values.forEach() { it.destroy() }
        super.destroy()
    }
}

class ModelRenderNode(val data: ModelData, sceneView: SceneView) : ArNode() {
    var isLoading = true

    init {
        reset()

        sceneView.let {
            loadModelAsync(it.context,it.lifecycle, data.glbPath,data.autoAnimate,false
            ) {
                //Set scale (using the chosen axis)
                val halfExtent = it.filamentAsset?.boundingBox!!.halfExtent[data.modelScaleAxis.value]
                modelScale = Float3(0.5f / halfExtent)

                isLoading = false
            }
        }
    }

    fun reset() {  // Initial parameters of the model
        data.let {
            isVisible = it.initialVisibility
            pose = it.initialPose
            scale = Float3(it.initialScale)
        }
    }
}