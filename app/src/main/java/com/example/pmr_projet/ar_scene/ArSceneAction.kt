package com.example.pmr_projet.ar_scene

import com.example.pmr_projet.ArSceneNode
import com.google.ar.core.Pose
import io.github.sceneview.math.Scale
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation

typealias ArSceneAction = (ArSceneNode)->Unit

object ArSceneActions {
    fun changeScale(targetID: String = "root", scale: Scale) : ArSceneAction = { it ->
        it.modelNodes[targetID]!!.scale = scale
    }

    fun changeSmoothSpeed(targetID: String, smoothSpeed: Float) : ArSceneAction = { it ->
        it.modelNodes[targetID]!!.smoothSpeed = smoothSpeed
    }


    fun changeVisibility(targetID: String, visibility: Boolean) : ArSceneAction = { it ->
        it.modelNodes[targetID]!!.isVisible = visibility
    }

    fun transform(targetID: String = "root", position: Position, rotation: Rotation) : ArSceneAction = { it ->
        it.modelNodes[targetID]!!.transform(position = position, rotation = rotation)
    }

    fun transformPosition(targetID: String = "root", position: Position) : ArSceneAction = { it ->
        it.modelNodes[targetID]!!.transform(position = position)
    }
    fun transformTranslate(targetID: String = "root", translation: Position) : ArSceneAction = { it ->
        val position = it.position + translation
        it.modelNodes[targetID]!!.transform(position = position)
    }

    fun transformRotation(targetID: String = "root", rotation: Rotation) : ArSceneAction = { it ->
        it.modelNodes[targetID]!!.transform(rotation = rotation)
    }
    fun transformRotate(targetID: String = "root", rotation: Rotation) : ArSceneAction = { it ->
        val endRotation = it.rotation + rotation
        it.modelNodes[targetID]!!.transform(rotation = endRotation)
    }


    fun smooth(targetID: String = "root", position: Position, rotation: Rotation, speed: Float? = null) : ArSceneAction = { it ->
        if (speed != null)
            it.modelNodes[targetID]!!.smooth(position = position, rotation = rotation, speed = speed)
        else
            it.modelNodes[targetID]!!.smooth(position = position, rotation = rotation)
    }

    fun smoothPosition(targetID: String = "root", position: Position, speed: Float? = null) : ArSceneAction = { it ->
        if (speed != null)
            it.modelNodes[targetID]!!.smooth(position = position, speed = speed)
        else
            it.modelNodes[targetID]!!.smooth(position = position)
    }
    fun smoothTranslate(targetID: String = "root", translation: Position, speed: Float? = null) : ArSceneAction = { it ->
        val position = it.position + translation
        if (speed != null)
            it.modelNodes[targetID]!!.smooth(position = position, speed = speed)
        else
            it.modelNodes[targetID]!!.smooth(position = position)
    }

    fun smoothRotation(targetID: String = "root", rotation: Rotation, speed: Float? = null) : ArSceneAction = { it ->
        if (speed != null)
            it.modelNodes[targetID]!!.smooth(rotation = rotation, speed = speed)
        else
            it.modelNodes[targetID]!!.smooth(rotation = rotation)
    }
    fun smoothRotate(targetID: String = "root", rotation: Rotation, speed: Float? = null) : ArSceneAction = { it ->
        val endRotation = it.rotation + rotation
        if (speed != null)
            it.modelNodes[targetID]!!.smooth(rotation = endRotation, speed = speed)
        else
            it.modelNodes[targetID]!!.smooth(rotation = endRotation)
    }


    fun addChild(parentID: String, childID: String) : ArSceneAction = { it ->
        val parentNode = it.modelNodes[parentID]!!
        val childNode = it.modelNodes[childID]!!
        parentNode.addChild(childNode)
    }

    fun animate(targetID: String, animateAction: AnimateAction, animation: String,
                repeatMode: Int? = null, repeatCount: Int? = null) : ArSceneAction = { it ->
        it.modelNodes[targetID]!!.modelInstance?.animate(animation)?.run {
            when (animateAction) {
                AnimateAction.START -> start()
                AnimateAction.PAUSE -> pause()
                AnimateAction.RESUME -> resume()
            }
            repeatMode?.let { this.repeatMode = it }
            repeatCount?.let { this.repeatCount = it }
        }
    }

    fun animateAll(targetID: String, animateAction: AnimateAction = AnimateAction.START,
                   repeat: Boolean = true, repeatMode: Int? = null, repeatCount: Int? = null) : ArSceneAction = { it ->
        it.modelNodes[targetID]!!.modelInstance?.animate(repeat)?.run {
            when (animateAction) {
                AnimateAction.START -> start()
                AnimateAction.PAUSE -> pause()
                AnimateAction.RESUME -> resume()
            }
            repeatMode?.let { this.repeatMode = it }
            repeatCount?.let { this.repeatCount = it }
        }
    }

    enum class AnimateAction {
        START, PAUSE, RESUME
    }
}