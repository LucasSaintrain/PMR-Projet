package com.example.pmr_projet.ar_scene

import com.example.pmr_projet.ArSceneNode
import io.github.sceneview.math.Scale
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation

object SceneAction {
    fun changeScale(targetID: String = "root", scale: Scale) = { it: ArSceneNode ->
        it.modelNodes[targetID]!!.scale = scale
    }

    fun changePosition(targetID: String = "root", position: Position) = { it: ArSceneNode ->
        //it.modelNodes[targetID]!!.position = position
        it.modelNodes[targetID]!!.smooth(position, speed = 0.5f)
    }
    fun translate(targetID: String = "root", translation: Position) = { it: ArSceneNode ->
        it.modelNodes[targetID]!!.position += translation
    }

    fun changeRotation(targetID: String = "root", rotation: Rotation) = { it: ArSceneNode ->
        it.modelNodes[targetID]!!.rotation = rotation
    }
    fun rotate(targetID: String = "root", rotation: Rotation) = { it: ArSceneNode ->
        it.modelNodes[targetID]!!.rotation += rotation
    }

    fun changeVisibility(targetID: String, visibility: Boolean) = { it: ArSceneNode ->
        it.modelNodes[targetID]!!.isVisible = visibility
    }

    fun addChild(parentID: String, childID: String) = { it: ArSceneNode ->
        val parentNode = it.modelNodes[parentID]!!
        val childNode = it.modelNodes[childID]!!
        parentNode.addChild(childNode)
        Unit
    }


    fun animate(targetID: String, action: String, animation: String,
                repeatMode: Int? = null, repeatCount: Int? = null) = { it: ArSceneNode ->
        it.modelNodes[targetID]!!.modelInstance?.animate(animation)?.run {
            when (action) {
                "start" -> start()
                "pause" -> pause()
                "resume" -> resume()
            }
            repeatMode?.let { this.repeatMode = it }
            repeatCount?.let { this.repeatCount = it }
        }
        Unit
    }

    fun animateAll(targetID: String, action: String, repeat: Boolean = true,
                      repeatMode: Int? = null, repeatCount: Int? = null) = { it: ArSceneNode ->
        it.modelNodes[targetID]!!.modelInstance?.animate(repeat)?.run {
            when (action) {
                "start" -> start()
                "pause" -> pause()
                "resume" -> resume()
            }
            repeatMode?.let { this.repeatMode = it }
            repeatCount?.let { this.repeatCount = it }
        }
        Unit
    }
}