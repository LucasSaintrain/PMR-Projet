package com.example.pmr_projet

import android.os.Bundle
import android.view.*
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

    lateinit var sceneView: ArSceneView
    lateinit var loadingView: View
    lateinit var actionButton: ExtendedFloatingActionButton

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