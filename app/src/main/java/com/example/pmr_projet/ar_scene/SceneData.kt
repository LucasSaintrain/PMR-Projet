package com.example.pmr_projet

import com.google.ar.core.Pose
import io.github.sceneview.SceneView


data class SceneData(val models: Map<String, ModelData>,
                     val actions: Map<String, (ArSceneNode)->Unit > = mapOf()) {

    fun loadAR(sceneView: SceneView) : ArSceneNode {
        return ArSceneNode(this, sceneView)
    }
}

data class ModelData(val glbPath: String, val initialPose: Pose = Pose.IDENTITY, val initialScale: Float = 1f,
                     val modelScaleAxis: DirectionXYZ = DirectionXYZ.X, val parentScaleAxis: DirectionXYZ = DirectionXYZ.X,
                     val autoAnimate: Boolean = true, val initialVisibility: Boolean = true) {

    enum class DirectionXYZ(val value: Int) {
        X(0), Y(1), Z(2)
    }
}