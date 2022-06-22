package com.example.pmr_projet.parser

import android.animation.ValueAnimator
import com.example.pmr_projet.ArSceneNode
import com.example.pmr_projet.ModelData
import com.example.pmr_projet.SceneData
import com.example.pmr_projet.ar_scene.ArSceneActions
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.io.InputStream

class BookXmlParser {

    //private var book : Book? = null
    //private var pages : ...<Page> = ...
    //private var page : Page? = null

    private var keywordGroups : MutableList<Triple<String,String,String>> = mutableListOf()  // (keyword,scene,action)
    private var currentKeywordAction: String? = null

    private var scenes : MutableMap<String,SceneData> = mutableMapOf()
    private val sceneProperties = object {
        lateinit var id : String
        lateinit var models : MutableMap<String,ModelData>
        lateinit var actions : MutableMap<String,(ArSceneNode)->Unit>
        var visible = true
        lateinit var scale : Scale
        lateinit var position : Position
        lateinit var rotation : Rotation

        fun reset() {
            id = ""
            models = mutableMapOf()
            actions = mutableMapOf() //...
            visible = true //...
            scale = Scale(1f) //...
            position = Position() //...
            rotation = Rotation() //...
        }
    }
    private val modelProperties = object {
        lateinit var id : String
        lateinit var path : String
        lateinit var scale : Scale
        lateinit var position : Position
        lateinit var rotation : Rotation
        lateinit var modelScaleAxis : ModelData.DirectionXYZ
        lateinit var parentScaleAxis : ModelData.DirectionXYZ
        lateinit var parent : String
        var visible = true
        var autoAnimate = true

        fun reset() {
            id = ""
            path = ""
            scale = ModelData.defaultScale
            position = ModelData.defaultPosition
            rotation = ModelData.defaultRotation
            modelScaleAxis = ModelData.defaultModelScaleAxis
            parentScaleAxis = ModelData.defaultParentScaleAxis
            parent = ModelData.defaultParent
            visible = ModelData.defaultVisibility
            autoAnimate = ModelData.defaultAutoAnimate
        }
    }
    private val actionProperties = object {
        lateinit var id : String
        lateinit var list : MutableList< (ArSceneNode)->Unit >

        fun reset() {
            id = ""
            list = mutableListOf()
        }
    }

    private var scale : Scale? = null
    private var position : Position? = null
    private var rotation : Rotation? = null
    private var text : String = ""

    fun parse(inputStream: InputStream): Map<String,SceneData> {
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(inputStream, null)

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tag = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val attributes = parser.attributeMap

                        when (tag) {
                            "scene" -> {
                                sceneProperties.apply {
                                    reset()
                                    attributes["id"]?.let { id = it }
                                }
                            }
                            "root" -> {
                                resetScalePositionRotation()
                                sceneProperties.apply {
                                    attributes["visible"]?.let { it.toBooleanStrictOrNull()?.let { visible = it } }
                                }
                            }
                            "model" -> {
                                resetScalePositionRotation()
                                modelProperties.apply {
                                    reset()
                                    attributes["id"]?.let { id = it }
                                    attributes["path"]?.let { path = it }
                                    attributes["parent"]?.let { parent = it }
                                }
                            }
                            "scale" -> {
                                scale = parseFloat3(attributes, 1f)
                                parseScaleAxis(attributes)
                            }
                            "position" -> position = parseFloat3(attributes)
                            "rotation" -> rotation = parseFloat3(attributes)
                            "action" -> {
                                actionProperties.apply {
                                    reset()
                                    attributes["id"]?.let { id = it }
                                }
                            }
                            "changeVisibility" -> {
                                val target = attributes["target"] ?: "root"
                                val visibility = attributes["target"]?.toBooleanStrictOrNull()
                                val subAction = ArSceneActions.changeVisibility(target,visibility)
                                actionProperties.list.add(subAction)
                            }
                            "changeSmoothSpeed" -> {
                                val target = attributes["target"] ?: "root"
                                val smoothSpeed = attributes["smoothSpeed"]?.toFloatOrNull()
                                val subAction = ArSceneActions.changeSmoothSpeed(target,smoothSpeed)
                                actionProperties.list.add(subAction)
                            }
                            "changeScale" -> {
                                val target = attributes["target"] ?: "root"
                                val scale = parseFloat3(attributes)
                                val subAction = ArSceneActions.changeScale(target,scale)
                                actionProperties.list.add(subAction)
                            }
                            "transformPosition" -> {
                                val target = attributes["target"] ?: "root"
                                val position = parseFloat3(attributes)
                                val subAction = ArSceneActions.transformPosition(target,position)
                                actionProperties.list.add(subAction)
                            }
                            "transformTranslate" -> {
                                val target = attributes["target"] ?: "root"
                                val translation = parseFloat3(attributes)
                                val subAction = ArSceneActions.transformTranslate(target,translation)
                                actionProperties.list.add(subAction)
                            }
                            "transformRotation" -> {
                                val target = attributes["target"] ?: "root"
                                val rotation = parseFloat3(attributes)
                                val subAction = ArSceneActions.transformRotation(target,rotation)
                                actionProperties.list.add(subAction)
                            }
                            "transformRotate" -> {
                                val target = attributes["target"] ?: "root"
                                val rotation = parseFloat3(attributes)
                                val subAction = ArSceneActions.transformRotate(target,rotation)
                                actionProperties.list.add(subAction)
                            }
                            "addChild" -> {
                                val parent = attributes["parent"] ?: "root"
                                val child = attributes["child"]
                                val subAction = ArSceneActions.addChild(parent,child)
                                actionProperties.list.add(subAction)
                            }
                            "animate" -> {
                                val target = attributes["target"]
                                val animation = attributes["animation"]
                                val repeatCount = attributes["repeatCount"]?.toIntOrNull() ?: 0

                                val animateAction = (attributes["action"]).let { string ->
                                    ArSceneActions.AnimateAction.values().firstOrNull {it.name.equals(string,true)}
                                } ?: ArSceneActions.AnimateAction.START

                                val repeatMode = (attributes["repeatMode"]).let { string ->
                                    when {
                                        string.equals("REVERSE",true) -> ValueAnimator.REVERSE
                                        string.equals("RESTART",true) -> ValueAnimator.RESTART
                                        else -> null
                                    }
                                }

                                val subAction = ArSceneActions.animate(target,animateAction,animation,repeatMode,repeatCount)
                                actionProperties.list.add(subAction)
                            }
                            "animateAll" -> {
                                val target = attributes["target"]
                                val repeat = attributes["repeat"]?.toBooleanStrictOrNull() ?: true
                                val repeatCount = attributes["repeatCount"]?.toIntOrNull() ?: 0

                                val animateAction = (attributes["action"]).let { string ->
                                    ArSceneActions.AnimateAction.values().firstOrNull {it.name.equals(string,true)}
                                } ?: ArSceneActions.AnimateAction.START

                                val repeatMode = (attributes["repeatMode"]).let { string ->
                                    when {
                                        string.equals("REVERSE",true) -> ValueAnimator.REVERSE
                                        string.equals("RESTART",true) -> ValueAnimator.RESTART
                                        else -> null
                                    }
                                }

                                val subAction = ArSceneActions.animateAll(target,animateAction,repeat,repeatMode,repeatCount)
                                actionProperties.list.add(subAction)
                            }

                            "content" -> {}
                            "keyword" -> {
                                currentKeywordAction = attributes["action"]
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (tag) {
                            "scene" -> {
                                sceneProperties.run {
                                    scenes[id] = SceneData(
                                        models, actions, visible,
                                        scale, position, rotation
                                    )
                                }
                            }
                            "root" -> {
                                scale?.let { sceneProperties.scale = it }
                                position?.let { sceneProperties.position = it }
                                rotation?.let { sceneProperties.rotation = it }
                            }
                            "model" -> {
                                scale?.let { modelProperties.scale = it }
                                position?.let { modelProperties.position = it }
                                rotation?.let { modelProperties.rotation = it }

                                modelProperties.run {
                                    sceneProperties.models[id] = ModelData(
                                        path,scale,position,rotation,
                                        modelScaleAxis,parentScaleAxis,
                                        parent,visible,autoAnimate
                                    )
                                }
                            }
                            "action" -> {
                                actionProperties.run {
                                    sceneProperties.actions[id] = { scene ->
                                        list.forEach { it.invoke(scene) }
                                    }
                                }
                            }
                            "content" -> {}
                            "keyword" -> {
                                currentKeywordAction?.let {
                                    keywordGroups.add(
                                        Triple(text,sceneProperties.id,it)
                                    )
                                }
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        text = parser.text
                    }
                }
                eventType = parser.next()
            }

        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return scenes
    }



    private fun parseScaleAxis(attributes: Map<String, String>) {
        attributes["modelScaleAxis"]?.let { attr ->
            val direction = ModelData.DirectionXYZ.values().firstOrNull { it.name == attr }
            direction?.let { modelProperties.modelScaleAxis = it }
        }
        attributes["parentScaleAxis"]?.let { attr ->
            val direction = ModelData.DirectionXYZ.values().firstOrNull { it.name == attr }
            direction?.let { modelProperties.parentScaleAxis = it }
        }
    }

    private fun resetScalePositionRotation() {
        scale = null
        position = null
        rotation = null
    }

    private fun parseFloat3(attributes: Map<String,String>, defaultValue: Float = 0f) : Float3 {
        var x = defaultValue
        var y = defaultValue
        var z = defaultValue

        attributes["xyz"]?.let {
            x = it.toFloatOrNull() ?: 0f
            y = x
            z = x
        }
        attributes["xy"]?.let {
            x = it.toFloatOrNull() ?: 0f
            y = x
        }
        attributes["xz"]?.let {
            x = it.toFloatOrNull() ?: 0f
            z = x
        }
        attributes["yz"]?.let {
            y = it.toFloatOrNull() ?: 0f
            z = x
        }

        attributes["x"]?.let { x = it.toFloatOrNull() ?: 0f }
        attributes["y"]?.let { y = it.toFloatOrNull() ?: 0f }
        attributes["z"]?.let { z = it.toFloatOrNull() ?: 0f }

        return Float3(x,y,z)
    }

    private val XmlPullParser.attributeMap: Map<String,String>
        get() {
            val map = mutableMapOf<String,String>()
            for (i in 0 until attributeCount) {
                map[getAttributeNamespace(i)] = getAttributeValue(i)
            }
            return map
        }
}