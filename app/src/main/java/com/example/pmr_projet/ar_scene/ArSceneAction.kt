package com.example.pmr_projet.ar_scene

import com.example.pmr_projet.ArSceneNode
import com.google.ar.core.Pose
import io.github.sceneview.math.Scale
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation

typealias ArSceneAction = (ArSceneNode)->Unit

object ArSceneActions {
    fun changeVisibility(targetID: String = "root", visibility: Boolean? = null) : ArSceneAction = { it ->
        if (visibility != null)  it.modelNodes[targetID]?.isVisible = visibility
    }

    fun changeSmoothSpeed(targetID: String = "root", smoothSpeed: Float? = null) : ArSceneAction = { it ->
        if (smoothSpeed != null)  it.modelNodes[targetID]?.smoothSpeed = smoothSpeed
    }

    fun changeScale(targetID: String = "root", scale: Scale = Scale()) : ArSceneAction = { it ->
        it.modelNodes[targetID]?.scale = scale
    }

    fun transform(targetID: String = "root", position: Position = Position(), rotation: Rotation = Rotation()) : ArSceneAction = { it ->
        it.modelNodes[targetID]?.transform(position = position, rotation = rotation)
    }

    fun transformPosition(targetID: String = "root", position: Position = Position()) : ArSceneAction = { it ->
        it.modelNodes[targetID]?.transform(position = position)
    }
    fun transformTranslate(targetID: String = "root", translation: Position = Position()) : ArSceneAction = { it ->
        it.modelNodes[targetID]?.let { it.transform(position = it.position+translation) }
    }

    fun transformRotation(targetID: String = "root", rotation: Rotation = Rotation()) : ArSceneAction = { it ->
        it.modelNodes[targetID]?.transform(rotation = rotation)
    }
    fun transformRotate(targetID: String = "root", rotation: Rotation = Rotation()) : ArSceneAction = { it ->
        it.modelNodes[targetID]?.let { it.transform(rotation = it.rotation+rotation) }
    }


    fun smooth(targetID: String = "root", position: Position = Position(), rotation: Rotation = Rotation(), speed: Float? = null) : ArSceneAction = { it ->
        it.modelNodes[targetID]?.let {
            if (speed != null)  it.smooth(position = position,rotation = rotation,speed = speed)
            else                it.smooth(position = position, rotation = rotation)
        }
    }

    fun smoothPosition(targetID: String = "root", position: Position = Position(), speed: Float? = null) : ArSceneAction = { it ->
        it.modelNodes[targetID]?.let {
            if (speed != null)  it.smooth(position = position, speed = speed)
            else                it.smooth(position = position)
        }
    }
    fun smoothTranslate(targetID: String = "root", translation: Position = Position(), speed: Float? = null) : ArSceneAction = { it ->
        it.modelNodes[targetID]?.let {
            if (speed != null)  it.smooth(position = it.position+translation, speed = speed)
            else                it.smooth(position = it.position+translation)
        }
    }

    fun smoothRotation(targetID: String = "root", rotation: Rotation = Rotation(), speed: Float? = null) : ArSceneAction = { it ->
        it.modelNodes[targetID]?.let {
            if (speed != null)  it.smooth(rotation = rotation, speed = speed)
            else                it.smooth(rotation = rotation)
        }
    }
    fun smoothRotate(targetID: String = "root", rotation: Rotation = Rotation(), speed: Float? = null) : ArSceneAction = { it ->
        it.modelNodes[targetID]?.let {
            if (speed != null)  it.smooth(rotation = it.rotation+rotation, speed = speed)
            else                it.smooth(rotation = it.rotation+rotation)
        }
    }


    fun addChild(parentID: String = "root", childID: String? = null) : ArSceneAction = { it ->
        val parentNode = it.modelNodes[parentID]
        val childNode = it.modelNodes[childID]
        if (parentNode!=null && childNode!=null)
            parentNode.addChild(childNode)
    }

    fun animate(targetID: String? = null, animateAction: AnimateAction = AnimateAction.START, animation: String? = null,
                repeatMode: Int? = null, repeatCount: Int? = null) : ArSceneAction = { it ->
        it.modelNodes[targetID]?.modelInstance?.animate(animation)?.run {
            when (animateAction) {
                AnimateAction.START -> start()
                AnimateAction.PAUSE -> pause()
                AnimateAction.RESUME -> resume()
            }
            repeatMode?.let { this.repeatMode = it }
            repeatCount?.let { this.repeatCount = it }
        }
    }

    fun animateAll(targetID: String? = null, animateAction: AnimateAction = AnimateAction.START,
                   repeat: Boolean = true, repeatMode: Int? = null, repeatCount: Int? = null) : ArSceneAction = { it ->
        it.modelNodes[targetID]?.modelInstance?.animate(repeat)?.run {
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