package com.example.pmr_projet

import io.github.sceneview.SceneView
import com.google.ar.core.AugmentedImage
import io.github.sceneview.ar.node.ArNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import dev.romainguy.kotlin.math.Float3

class ArSceneNode(val data: SceneData, sceneView: SceneView) : ArNode() {
    val modelNodes : Map<String, ModelRenderNode> = data.models.keys.associateWith { ModelRenderNode(data.models[it]!!, sceneView) }
    val rootNode = Node()

    val isLoading
        get() = modelNodes.values.any { it.isLoading }

    init {
        sceneView.addChild(this)
        resetRoot()
        resetParentTree()
    }

    fun bindToAugmentedImage(image: AugmentedImage) {  // Associates scene with an image
        image.let {
            anchor = it.createAnchor(it.centerPose)
            worldScale = Float3(it.extentX, 1f, it.extentZ)
        }

        modelNodes.values.forEach {
            it.worldScale = Float3(it.worldScale[it.data.parentScaleAxis.value])
        }
    }

    fun invokeAction(id: String) {  // Invokes the corresponding action
        data.actions[id]?.invoke(this)
    }

    fun reset() {  // "Reloads" the scene
        resetRoot()
        resetParentTree()
        modelNodes.values.forEach { it.reset() }
    }

    private fun resetRoot() {  // Initial parameters of the root node
        rootNode.apply {
            isVisible = data.initialVisibility
            scale = data.initialScale
            position = data.initialPosition
            rotation = data.initialRotation
        }
    }

    private fun resetParentTree() {  // Every node will point to its initial parent
        addChild(rootNode)

        modelNodes.values.forEach {
            val parent = it.data.parent
            if (parent == "root") { rootNode.addChild(it) }
            else { modelNodes[parent]?.addChild(it) }
        }
    }

    override fun destroy() {
        modelNodes.values.forEach() { it.destroy() }
        rootNode.destroy()
        super.destroy()
    }
}

class ModelRenderNode(val data: ModelData, sceneView: SceneView) : ArNode() {
    var isLoading = true

    init {
        reset()  // Setup of the initial node parameters

        sceneView.let {  // Loads model and normalizes the scale
            loadModelAsync(it.context,it.lifecycle, data.glbPath,data.autoAnimate,false
            ) {
                // Set scale (using the chosen axis)
                val halfExtent = it.filamentAsset?.boundingBox!!.halfExtent[data.modelScaleAxis.value]
                modelScale = Float3(0.5f / halfExtent)

                isLoading = false
            }
        }
    }

    fun reset() {  // Initial parameters of the model
        data.let {
            scale = it.initialScale
            position = it.initialPosition
            rotation = it.initialRotation
            isVisible = it.initialVisibility
        }
    }
}