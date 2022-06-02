package com.example.pmr_projet

import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.EditableTransform
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import io.github.sceneview.utils.doOnApplyWindowInsets

class ArSceneviewFragment : Fragment(R.layout.fragment_ar_sceneview) {

    val modelPaths = listOf("models/ship.glb","models/spiderbot.glb","models/Persian.glb","models/gladiador.glb","models/OilCan.glb",
                            "models/Predator_s.glb")
    var currentModelIndex = 0

    lateinit var sceneView: ArSceneView
    lateinit var loadingView: View
    lateinit var actionButton: ExtendedFloatingActionButton
    lateinit var changeModelButton: Button
    lateinit var modelNode: ArModelNode

    var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)

        sceneView = view.findViewById(R.id.sceneView)
        loadingView = view.findViewById(R.id.loadingView)
        changeModelButton = view.findViewById<Button?>(R.id.changeModelButton).apply {
            setOnClickListener { nextModel() }
        }
        actionButton = view.findViewById<ExtendedFloatingActionButton>(R.id.actionButton).apply {
            // Add system bar margins
            val bottomMargin = (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
            doOnApplyWindowInsets { systemBarsInsets ->
                (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                    systemBarsInsets.bottom + bottomMargin
            }
            setOnClickListener { actionButtonClicked() }
        }

        isLoading = true
        modelNode = ArModelNode(placementMode = PlacementMode.BEST_AVAILABLE).apply {
            loadModelAsync(
                context = requireContext(),
                glbFileLocation = "models/ship.glb",
                lifecycle = lifecycle,
                autoAnimate = true,
                autoScale = true,
                // Place the model origin at the bottom center
                centerOrigin = Position(y = -1.0f)
            ) {
                isLoading = false
            }
            onTrackingChanged = { _, isTracking, _ ->
                actionButton.isGone = !isTracking
            }
            editableTransforms = EditableTransform.ALL
        }
        sceneView.addChild(modelNode)
        // Quick workaround until the Node Pick is fixed
        sceneView.gestureDetector.onTouchNode(modelNode)
    }

    private fun nextModel() {
        currentModelIndex++
        if(currentModelIndex >= modelPaths.size) {
            currentModelIndex = 0
        }
        val path = modelPaths[currentModelIndex]
        changeModel(path)
    }

    private fun changeModel(modelPath : String) {
        isLoading = true

        modelNode.loadModelAsync(
            context = requireContext(),
            glbFileLocation = modelPath,
            lifecycle = lifecycle,
            autoAnimate = true,
            autoScale = true,
            // Place the model origin at the bottom center
            centerOrigin = Position(y = -1.0f)
        ) {
            isLoading = false
        }
    }

    fun actionButtonClicked() {
        if (!modelNode.isAnchored && modelNode.anchor()) {
            actionButton.text = getString(R.string.move_object)
            actionButton.setIconResource(R.drawable.ic_target)
            sceneView.planeRenderer.isVisible = false
        } else {
            modelNode.anchor = null
            actionButton.text = getString(R.string.place_object)
            actionButton.setIconResource(R.drawable.ic_anchor)
            sceneView.planeRenderer.isVisible = true
        }
    }
}