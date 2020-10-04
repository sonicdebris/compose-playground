package sonicdebris.composepanner

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.gesture.DragObserver
import androidx.compose.ui.gesture.dragGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import kotlinx.coroutines.GlobalScope
import sonicdebris.composepanner.ui.ComposePannerTheme

@Composable
fun PannerView(panner: Panner) {

    val started by panner.started.observeAsState(false)
    val x by panner.pan.observeAsState(0f)
    val y by panner.gain.observeAsState(0f)

    val levIn by panner.inputLevels.observeAsState(Levels(0f,0f))
    val levOut by panner.outputLevels.observeAsState(Levels(0f,0f))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16f.dp).fillMaxSize()
    ) {
        Button(
            onClick = { panner.toggle() },
            Modifier.width(150f.dp)
        ) {
            Text(if (started) "Stop" else "Start")
        }

        Row(Modifier.weight(1f).padding(5.dp)) {
            Box(Modifier.weight(1f)) { Meter(levIn.l, Color.Blue) }
            Box(Modifier.weight(1f)) { Meter(levIn.r, Color.Magenta) }
        }

        Row(Modifier.weight(1f).padding(5.dp)) {
            Box(Modifier.weight(1f)) {
                ControlPad(
                    x, y,
                    { panner.updatePan(it) },
                    { panner.updateGain(it) }
                )
            }
        }

        Row(Modifier.weight(1f).padding(5.dp)) {
            Box(Modifier.weight(1f)) { Meter(levOut.l, Color.Blue) }
            Box(Modifier.weight(1f)) { Meter(levOut.r, Color.Magenta) }
        }
    }

}

/**
 * A graphical meter for an input value,
 * draws a rectangle whose height is proportional to the level
 */
@Composable
private fun Meter(level: Float, color: Color) {

    Canvas(modifier = Modifier.fillMaxSize()) {

        drawRect(Color.Yellow, Offset.Zero, size)

        drawRect(
            color,
            topLeft = Offset(0f, size.height * (1f - level.coerceIn(0f, 1f))),
            size = Size(size.width, size.height * level)
        )
    }
}


/**
 *  An XY control pad. Values are normalized in [0,1], vertical 0 is at the bottom
 */
@Composable
private fun ControlPad(
    x: Float,
    y: Float,
    onXchanged: (Float) -> Unit,
    onYchanged: (Float) -> Unit
) {
    val drag = remember { ControlPadDragObserver(onXchanged, onYchanged) }

    Canvas(
        Modifier.fillMaxSize()
            .dragGestureFilter(drag, startDragImmediately = true)
            .onSizeChanged { drag.size = Size(it.width.toFloat(), it.height.toFloat()) }
    ) {
        clipRect(0f, 0f, size.width, size.height) {

            drawRect(Color.Yellow, Offset.Zero, size)

            drawCircle(
                Color.Blue,
                radius = 20f * density,
                center = Offset(x * size.width, (1-y) * size.height),
            )
        }
    }
}

class ControlPadDragObserver(
    private  val onXchanged: (Float) -> Unit,
    private val onYchanged: (Float) -> Unit
) : DragObserver {

    var size: Size? = null
    var prevPos: Offset? = null

    override fun onStart(downPosition: Offset) {
        size?.let { (w, h) ->
            prevPos = downPosition
            onXchanged(downPosition.x / w)
            onYchanged(1 - downPosition.y / h)
        }
    }

    override fun onDrag(dragDistance: Offset): Offset {

        size?.let { (w,h ) -> prevPos?.let { (xPrev, yPrev) ->

            val newX = xPrev + dragDistance.x
            val newY = yPrev + dragDistance.y
            onXchanged(newX / w)
            onYchanged(1 - newY / h)
            prevPos = Offset(newX, newY)
        }}

        return dragDistance
    }

    override fun onStop(velocity: Offset) {
        prevPos = null
    }

    override fun onCancel() {
        prevPos = null
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposePannerTheme {
        PannerView(Panner(GlobalScope))
    }
}