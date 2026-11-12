package com.dailydua.tracker.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** A few icons that are not in material-icons-core, drawn from Material path data. */
object AppIcons {
    private fun icon(name: String, path: String) = ImageVector.Builder(
        name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f,
    ).addPath(addPathNodes(path), fill = SolidColor(Color.Black)).build()

    /** Half-filled circle: reading brightness. */
    val Contrast: ImageVector by lazy {
        icon("Contrast", "M12,22C6.48,22 2,17.52 2,12S6.48,2 12,2s10,4.48 10,10 -4.48,10 -10,10zM12,4v16c4.41,0 8,-3.59 8,-8s-3.59,-8 -8,-8z")
    }

    val Sun: ImageVector by lazy { icon("Sun", "M12,7.5a4.5,4.5 0 1,0 0.001,0z M18.30,12.90 L21.60,12.90 L21.60,11.10 L18.30,11.10 z M15.82,17.09 L18.15,19.42 L19.42,18.15 L17.09,15.82 z M11.10,18.30 L11.10,21.60 L12.90,21.60 L12.90,18.30 z M6.91,15.82 L4.58,18.15 L5.85,19.42 L8.18,17.09 z M5.70,11.10 L2.40,11.10 L2.40,12.90 L5.70,12.90 z M8.18,6.91 L5.85,4.58 L4.58,5.85 L6.91,8.18 z M12.90,5.70 L12.90,2.40 L11.10,2.40 L11.10,5.70 z M17.09,8.18 L19.42,5.85 L18.15,4.58 L15.82,6.91 z") }

    val Moon: ImageVector by lazy { icon("Moon", "M12,3c-4.97,0 -9,4.03 -9,9s4.03,9 9,9 9,-4.03 9,-9c0,-0.46 -0.04,-0.92 -0.1,-1.36 -0.98,1.37 -2.58,2.26 -4.4,2.26 -2.98,0 -5.4,-2.42 -5.4,-5.4 0,-1.81 0.89,-3.42 2.26,-4.4 -0.44,-0.06 -0.9,-0.1 -1.36,-0.1z") }

    /** Open book. */
    val Book: ImageVector by lazy {
        icon(
            "Book",
            "M21,5c-1.11,-0.35 -2.33,-0.5 -3.5,-0.5c-1.95,0 -4.05,0.4 -5.5,1.5c-1.45,-1.1 -3.55,-1.5 -5.5,-1.5S2.45,4.9 1,6v14.65" +
                "c0,0.25 0.25,0.5 0.5,0.5c0.1,0 0.15,-0.05 0.25,-0.05C3.1,20.45 5.05,20 6.5,20c1.95,0 4.05,0.4 5.5,1.5" +
                "c1.35,-0.85 3.8,-1.5 5.5,-1.5c1.65,0 3.35,0.3 4.75,1.05c0.1,0.05 0.15,0.05 0.25,0.05c0.25,0 0.5,-0.25 0.5,-0.5V6" +
                "C22.4,5.55 21.75,5.25 21,5zM21,18.5c-1.1,-0.35 -2.3,-0.5 -3.5,-0.5c-1.7,0 -4.15,0.65 -5.5,1.5V8" +
                "c1.35,-0.85 3.8,-1.5 5.5,-1.5c1.2,0 2.4,0.15 3.5,0.5V18.5z",
        )
    }

    /** Music queue. */
    val Queue: ImageVector by lazy {
        icon(
            "Queue",
            "M15,6H3v2h12V6zM15,10H3v2h12V10zM3,16h8v-2H3V16zM17,6v8.18C16.69,14.07 16.35,14 16,14c-1.66,0 -3,1.34 -3,3" +
                "s1.34,3 3,3s3,-1.34 3,-3V8h3V6H17z",
        )
    }

    /** Skip to next track. */
    val Next: ImageVector by lazy { icon("Next", "M6,18l8.5,-6L6,6V18zM16,6v12h2V6H16z") }

    /** Skip to previous track. */
    val Previous: ImageVector by lazy { icon("Previous", "M6,6h2v12H6V6zM9.5,12l8.5,6V6L9.5,12z") }

    /** Pause. */
    val Pause: ImageVector by lazy { icon("Pause", "M6,19h4V5H6V19zM14,5v14h4V5H14z") }

    val Fullscreen: ImageVector by lazy { icon("Fullscreen", "M7,14H5v5h5v-2H7v-3zM5,10h2V7h3V5H5v5zM17,17h-3v2h5v-5h-2v3zM14,5v2h3v3h2V5h-5z") }

    val FullscreenExit: ImageVector by lazy { icon("FullscreenExit", "M5,16h3v3h2v-5H5v2zM8,8H5v2h5V5H8v3zM14,19h2v-3h3v-2h-5v5zM16,8V5h-2v5h5V8h-3z") }

    /** Clock. */
    val Clock: ImageVector by lazy {
        icon(
            "Clock",
            "M11.99,2C6.47,2 2,6.48 2,12s4.47,10 9.99,10C17.52,22 22,17.52 22,12S17.52,2 11.99,2zM12,20c-4.42,0 -8,-3.58 -8,-8" +
                "s3.58,-8 8,-8 8,3.58 8,8 -3.58,8 -8,8zM12.5,7H11v6l5.25,3.15 0.75,-1.23 -4.5,-2.67z",
        )
    }

    /** Repeat. */
    val Repeat: ImageVector by lazy { icon("Repeat", "M7,7h10v3l4,-4 -4,-4v3H5v6h2V7zM17,17H7v-3l-4,4 4,4v-3h12v-6h-2v4z") }
}
