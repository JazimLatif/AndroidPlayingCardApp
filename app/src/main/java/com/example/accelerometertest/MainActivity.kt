package com.example.accelerometertest

import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            MainScreen()

        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen() {
    var isSorted by remember { mutableStateOf(false) }
    var animate by remember { mutableStateOf(false) }

    val alterDeck: () -> Unit = {
        isSorted = !isSorted
    }

    val playAnimation: () -> Unit = {
        animate = !animate
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ){
        Text(if (isSorted) "Sorted" else "Shuffled", fontSize = 50.sp)
        Row {
            Text(text = if (animate) "animated!" else "")
        }
    }

    Accelerometer(alterDeck, playAnimation)
    Gyroscope()
}

@Composable
fun Accelerometer(onShake: () -> Unit, onTilt: () -> Unit) {

    val SHAKE_THRESHOLD = 2.5f
    val TILT_THRESHOLD = 2.5f
    val SHAKE_SLOP_TIME_MS = 500

    var shakeTimestamp: Long = 0

    val context = LocalContext.current

    // Disposable Effect for cleaning up (don't want to keep registered as drains battery)
    // https://developer.android.com/develop/ui/compose/side-effects#disposableeffect
    DisposableEffect(LocalLifecycleOwner.current) {
        val sensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
        val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val sensorEventListener = object: SensorEventListener {

            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    for (i in event.values) {

                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]

                        val gX = x/SensorManager.GRAVITY_EARTH
                        val gY = y/SensorManager.GRAVITY_EARTH
                        val gZ = z/SensorManager.GRAVITY_EARTH

                        // Magnitudee of the vector of shake
                        val gForce = sqrt(gX*gX + gY*gY + gZ*gZ)
                        if (gForce > SHAKE_THRESHOLD) {
                            val now = System.currentTimeMillis()

                            if (shakeTimestamp + SHAKE_SLOP_TIME_MS > now){
                                return
                            }
                            shakeTimestamp = now

                            onShake()
                        }

                        val tiltVector = //calculate tilt
                        else {
                            if (tiltVector >= TILT_THRESHOLD) {
                                onTilt()
                            }
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Not needed
            }
        }
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)

        // Cleanup: Unregister the sensor when Composable leaves composition
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun Preview(){
    MainScreen()
}
