package com.example.pmr_projet

import io.github.sceneview.SceneView
import io.github.sceneview.math.Scale
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation

data class SceneData(val models: Map<String, ModelData>,
                     val actions: Map<String, (ArSceneNode)->Unit > = mapOf(),
                     val initialVisibility: Boolean = true,
                     val initialScale: Scale = Scale(1f),
                     val initialPosition: Position = Position(),
                     val initialRotation: Rotation = Rotation())
{
    fun loadAR(sceneView: SceneView) : ArSceneNode {
        return ArSceneNode(this, sceneView)
    }
}

data class ModelData(val glbPath: String,
                     val initialScale: Scale = Scale(1f),
                     val initialPosition: Position = Position(),
                     val initialRotation: Rotation = Rotation(),
                     val modelScaleAxis: DirectionXYZ = DirectionXYZ.X,
                     val parentScaleAxis: DirectionXYZ = DirectionXYZ.X,
                     val parent: String = "root",
                     val initialVisibility: Boolean = true,
                     val autoAnimate: Boolean = true)
{
    enum class DirectionXYZ(val value: Int) {
        X(0), Y(1), Z(2)
    }
}