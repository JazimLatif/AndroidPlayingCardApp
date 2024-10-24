package com.example.accelerometertest

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.Duration.between

@Composable
fun ShimmerOnTilt(tiltY: Float, tiltZ: Float) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.Blue.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val infiniteTransition = rememberInfiniteTransition()
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
//        Canvas(modifier = Modifier.fillMaxSize()) {
//            val width = size.width
//            val height = size.height
//
//            // Apply the tilt effect by scaling the tilt values (adjust the scale factor for a noticeable effect)
//
//
//            // Use the tilt values to dynamically influence the shimmer position
////            drawRect(
////                brush = Brush.linearGradient(
////                    colors = shimmerColors,
////                    start = Offset(translateAnim + scaledTiltX, translateAnim + scaledTiltY),
////                    end = Offset(translateAnim + width + scaledTiltX, translateAnim + height + scaledTiltY)
////                ),
////                size = size
////            )
//
//
//        }


    }


    val tiltY = tiltY/SensorManager.GRAVITY_EARTH
    val tiltZ = tiltZ/SensorManager.GRAVITY_EARTH

}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Gyroscope() {
    // Get the current context
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(SensorManager::class.java) as SensorManager
    }

    var tiltX by remember { mutableStateOf(0f) }
    var tiltY by remember { mutableStateOf(0f) }

    // Sensor listener
    val sensorEventListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    // Get tilt data from the accelerometer
                    tiltX = event.values[0]  // Adjust based on axis
                    tiltY = event.values[1]
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Do nothing
            }
        }
    }

    // Register sensor listener on composition
    DisposableEffect(sensorManager) {
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(sensorEventListener, gyroscope, 1000000000)

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    // Pass tilt data to the shimmer function
    ShimmerOnTilt(tiltX, tiltY)
}
