package com.example.accelerometertest

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
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

// should be a set of ["1Spades","2Spades","3Spades"..."12Clubs","13Clubs"]
val originalCards = createDeck()
fun shuffledCards(): SnapshotStateList<String> {
    return originalCards.shuffled().toMutableStateList()
}

fun sortedCards(): SnapshotStateList<String> {
    return originalCards.toMutableStateList()
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen() {
    var isSorted by remember { mutableStateOf(true) }
    // Whenever cardListChanges, re render the app. although we don't want the first card to look
    // any different, we want the cards below to be shuffled or sorted, which can only happen on a re render I think
    val cardList = remember { createDeck().toMutableStateList()  }
    val index = remember { mutableIntStateOf(0)  }

    val alterDeck: () -> Unit = {
        val firstCard = cardList[index.intValue]
        if (isSorted) {
            cardList.clear()
            cardList.addAll(shuffledCards())

        } else {
            cardList.clear()
            cardList.addAll(sortedCards())
        }
        index.intValue = cardList.indexOf(firstCard)
        isSorted = !isSorted
    }

    val nextCard: () -> Unit = {
        if (index.intValue == 52) {
            index.intValue = 0
        } else {
            index.intValue++
        }
    }

    Image(painter = painterResource(id = getCard(cardList[index.intValue])), contentDescription = "card", modifier = Modifier
        .fillMaxSize()
        .clickable(onClick = nextCard)
        .graphicsLayer(
            2.3f,
            2.3f
        ))
    Accelerometer(alterDeck)
    Gyroscope()
}


fun getCard(card: String): Int {
    return when (card) {
        "1Spades" -> R.drawable.ace_spades
        "2Spades" -> R.drawable.two_spades
        "3Spades" -> R.drawable.three_spades
        "4Spades" -> R.drawable.four_spades
        "5Spades" -> R.drawable.five_spades
        "6Spades" -> R.drawable.six_spades
        "7Spades" -> R.drawable.seven_spades
        "8Spades" -> R.drawable.eight_spades
        "9Spades" -> R.drawable.nine_spades
        "10Spades" -> R.drawable.ten_spades
        "11Spades" -> R.drawable.jack_spades
        "12Spades" -> R.drawable.queen_spades
        "13Spades" -> R.drawable.king_spades

        "1Hearts" -> R.drawable.ace_hearts
        "2Hearts" -> R.drawable.two_hearts
        "3Hearts" -> R.drawable.three_hearts
        "4Hearts" -> R.drawable.four_hearts
        "5Hearts" -> R.drawable.five_hearts
        "6Hearts" -> R.drawable.six_hearts
        "7Hearts" -> R.drawable.seven_hearts
        "8Hearts" -> R.drawable.eight_hearts
        "9Hearts" -> R.drawable.nine_hearts
        "10Hearts" -> R.drawable.ten_hearts
        "11Hearts" -> R.drawable.jack_hearts
        "12Hearts" -> R.drawable.queen_hearts
        "13Hearts" -> R.drawable.king_hearts

        "1Diamonds" -> R.drawable.ace_diamonds
        "2Diamonds" -> R.drawable.two_diamonds
        "3Diamonds" -> R.drawable.three_diamonds
        "4Diamonds" -> R.drawable.four_diamonds
        "5Diamonds" -> R.drawable.five_diamonds
        "6Diamonds" -> R.drawable.six_diamonds
        "7Diamonds" -> R.drawable.seven_diamonds
        "8Diamonds" -> R.drawable.eight_diamonds
        "9Diamonds" -> R.drawable.nine_diamonds
        "10Diamonds" -> R.drawable.ten_diamonds
        "11Diamonds" -> R.drawable.jack_diamonds
        "12Diamonds" -> R.drawable.queen_diamonds
        "13Diamonds" -> R.drawable.king_diamonds

        "1Clubs" -> R.drawable.ace_clubs
        "2Clubs" -> R.drawable.two_clubs
        "3Clubs" -> R.drawable.three_clubs
        "4Clubs" -> R.drawable.four_clubs
        "5Clubs" -> R.drawable.five_clubs
        "6Clubs" -> R.drawable.six_clubs
        "7Clubs" -> R.drawable.seven_clubs
        "8Clubs" -> R.drawable.eight_clubs
        "9Clubs" -> R.drawable.nine_clubs
        "10Clubs" -> R.drawable.ten_clubs
        "11Clubs" -> R.drawable.jack_clubs
        "12Clubs" -> R.drawable.queen_clubs
        "13Clubs" -> R.drawable.king_clubs

        else -> R.drawable.ace_spades
    }
}

fun createDeck(): List<String> {
    val deck = mutableListOf<String>()
    val values = 1..13
    val suits = setOf("Spades","Hearts","Diamonds","Clubs")

    suits.forEach { suit ->
        values.forEach { number ->
            deck.add("$number$suit")
        }
    }
    deck.add("Finished")
    return deck
}

@Composable
fun Accelerometer(onShake: () -> Unit) {

    val SHAKE_THRESHOLD = 2.5f
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
                        vibrateForMillis(context, 70L)
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

private fun vibrateForMillis(context: Context, duration: Long) {
    val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        context.getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vib.vibrate(
            VibrationEffect.createOneShot(
                duration,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
        )
    } else {
        //deprecated in API 26
        vib.vibrate(duration)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showSystemUi = true)
@Composable
fun Preview(){
    MainScreen()
}
