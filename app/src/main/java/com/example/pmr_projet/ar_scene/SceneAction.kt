package com.example.pmr_projet.ar_scene

import com.example.pmr_projet.ArSceneNode
import com.google.ar.core.Pose
import io.github.sceneview.math.Scale
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation

object SceneAction {
    fun changeScale(targetID: String = "root", scale: Scale) = { it: ArSceneNode ->
        it.modelNodes[targetID]!!.scale = scale
    }

    fun changeSmoothSpeed(targetID: String, smoothSpeed: Float) = { it: ArSceneNode ->
        it.modelNodes[targetID]!!.smoothSpeed = smoothSpeed
    }


    fun changeVisibility(targetID: String, visibility: Boolean) = { it: ArSceneNode ->
        it.modelNodes[targetID]!!.isVisible = visibility
    }

    fun transform(targetID: String = "root", position: Position, rotation: Rotation) = { it: ArSceneNode ->
        it.modelNodes[targetID]!!.transform(position = position, rotation = rotation)
    }

    fun transformPosition(targetID: String = "root", position: Position) = { it: ArSceneNode ->
        it.modelNodes[targetID]!!.transform(position = position)
    }
    fun transformTranslate(targetID: String = "root", translation: Position) = { it: ArSceneNode ->
        val position = it.position + translation
        it.modelNodes[targetID]!!.transform(position = position)
    }

    fun transformRotation(targetID: String = "root", rotation: Rotation) = { it: ArSceneNode ->
        it.modelNodes[targetID]!!.transform(rotation = rotation)
    }
    fun transformRotate(targetID: String = "root", rotation: Rotation) = { it: ArSceneNode ->
        val endRotation = it.rotation + rotation
        it.modelNodes[targetID]!!.transform(rotation = endRotation)
    }


    fun smooth(targetID: String = "root", position: Position, rotation: Rotation, speed: Float? = null) = { it: ArSceneNode ->
        if (speed != null)
            it.modelNodes[targetID]!!.smooth(position = position, rotation = rotation, speed = speed)
        else
            it.modelNodes[targetID]!!.smooth(position = position, rotation = rotation)
    }

    fun smoothPosition(targetID: String = "root", position: Position, speed: Float? = null) = { it: ArSceneNode ->
        if (speed != null)
            it.modelNodes[targetID]!!.smooth(position = position, speed = speed)
        else
            it.modelNodes[targetID]!!.smooth(position = position)
    }
    fun smoothTranslate(targetID: String = "root", translation: Position, speed: Float? = null) = { it: ArSceneNode ->
        val position = it.position + translation
        if (speed != null)
            it.modelNodes[targetID]!!.smooth(position = position, speed = speed)
        else
            it.modelNodes[targetID]!!.smooth(position = position)
    }

    fun smoothRotation(targetID: String = "root", rotation: Rotation, speed: Float? = null) = { it: ArSceneNode ->
        if (speed != null)
            it.modelNodes[targetID]!!.smooth(rotation = rotation, speed = speed)
        else
            it.modelNodes[targetID]!!.smooth(rotation = rotation)
    }
    fun smoothRotate(targetID: String = "root", rotation: Rotation, speed: Float? = null) = { it: ArSceneNode ->
        val endRotation = it.rotation + rotation
        if (speed != null)
            it.modelNodes[targetID]!!.smooth(rotation = endRotation, speed = speed)
        else
            it.modelNodes[targetID]!!.smooth(rotation = endRotation)
    }


    fun addChild(parentID: String, childID: String) = { it: ArSceneNode ->
        val parentNode = it.modelNodes[parentID]!!
        val childNode = it.modelNodes[childID]!!
        parentNode.addChild(childNode)
        Unit
    }

    fun animate(targetID: String, animateAction: AnimateAction, animation: String,
                repeatMode: Int? = null, repeatCount: Int? = null) = { it: ArSceneNode ->
        it.modelNodes[targetID]!!.modelInstance?.animate(animation)?.run {
            when (animateAction) {
                AnimateAction.START -> start()
                AnimateAction.PAUSE -> pause()
                AnimateAction.RESUME -> resume()
            }
            repeatMode?.let { this.repeatMode = it }
            repeatCount?.let { this.repeatCount = it }
        }
        Unit
    }

    fun animateAll(targetID: String, animateAction: AnimateAction = AnimateAction.START,
                   repeat: Boolean = true, repeatMode: Int? = null, repeatCount: Int? = null) = { it: ArSceneNode ->
        it.modelNodes[targetID]!!.modelInstance?.animate(repeat)?.run {
            when (animateAction) {
                AnimateAction.START -> start()
                AnimateAction.PAUSE -> pause()
                AnimateAction.RESUME -> resume()
            }
            repeatMode?.let { this.repeatMode = it }
            repeatCount?.let { this.repeatCount = it }
        }
        Unit
    }

    enum class AnimateAction {
        START, PAUSE, RESUME
    }
}