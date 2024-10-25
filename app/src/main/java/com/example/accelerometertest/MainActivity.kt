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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
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

// should be a set of ["1S","2S","3S"..."12C","13C"]
val originalCards = createDeck()
fun shuffledCards(): SnapshotStateList<String> {

//    val firstCard = cardList.first()
//    // performing a swap
//    shuffledCards[0] = firstCard.also { shuffledCards[shuffledCards.indexOf(firstCard)] = shuffledCards[0] }
////        shuffledCards
    return originalCards.shuffled().toMutableStateList()
}

fun sortedCards(): SnapshotStateList<String> {
    return originalCards.toMutableStateList()
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen() {
    var isSorted by remember { mutableStateOf(true) }
    val animate by remember { mutableStateOf(false) }
    // Whenever cardListChanges, re render the app. although we don't want the first card to look
    // any different, we want the cards below to be shuffled or sorted, which can only happen on a re render I think
    val cardList = remember { createDeck().toMutableStateList()  }
    var index = remember { mutableIntStateOf(0)  }

    val alterDeck: () -> Unit = {
        if (isSorted) {
            cardList.clear()
            cardList.addAll(shuffledCards())
        } else {
            cardList.clear()
            cardList.addAll(sortedCards())
        }
        isSorted = !isSorted
    }

    val nextCard: () -> Unit = {
        index.intValue++
    }


//    val playAnimation: () -> Unit = {
//        animate = !animate
//    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ){
        Text(cardList[index.value], fontSize = 50.sp, modifier = Modifier.clickable(onClick = nextCard))
    }

    Accelerometer(alterDeck, null)
    Gyroscope()
}

//fun maintainFirstCard(currentCardList: MutableList<String>, listToChangeTo: MutableList<String>): MutableList<String> {
//    val firstCard = currentCardList.first()
//    val elementToSwapWithFirstCard = listToChangeTo[listToChangeTo.indexOf(firstCard)]
//    listToChangeTo[0] = firstCard.also { currentCardList[currentCardList.indexOf(elementToSwapWithFirstCard)] =  }
//    return listToChangeTo
//}

fun createDeck(): List<String> {
    val deck = mutableListOf<String>()
    val values = 1..13
    val suits = setOf("S","H","D","C")

    suits.forEach { suit ->
        values.forEach { number ->
            deck.add("$number$suit")
        }
    }
    return deck
}

@Composable
fun Accelerometer(onShake: () -> Unit, onTilt: (() -> Unit)?) {

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

//                    val tiltVector = //calculate tilt
//                    else {
//                        if (tiltVector >= TILT_THRESHOLD) {
//                            onTilt()
//                        }
//                    }
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

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showSystemUi = true)
@Composable
fun Preview(){
    MainScreen()
}
