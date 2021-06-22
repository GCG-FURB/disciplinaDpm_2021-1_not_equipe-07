package dpm.mpb

import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlin.math.truncate


class MainActivity : AppCompatActivity(), SensorEventListener {

    lateinit var sensorManager: SensorManager
    lateinit var circle: ImageView;
    lateinit var valuesLinearAccelerometer: TextView
    lateinit var valuesOrientation: TextView
    var azimuth: Double = 0.0
    var scaleX: Double = 1.0
    var scaleY: Double = 1.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestedOrientation = (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

        // Scale
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val usableHeight = metrics.heightPixels
        //windowManager.defaultDisplay.getRealMetrics(metrics)
        //val realHeight = metrics.heightPixels
        //scaleX = (metrics.widthPixels / 100F).toDouble()
        //scaleY = (metrics.heightPixels / 100F).toDouble()

        scaleX = metrics.widthPixels.toDouble()
        scaleY = metrics.heightPixels.toDouble()

        // Circle
        circle = findViewById(R.id.imageViewCircle);

        circle.x = 445F
        circle.y = 150F

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Accelerometer
        valuesLinearAccelerometer = findViewById(R.id.textViewValuesLinearAccelerometer)
        valuesLinearAccelerometer.isVisible = false

        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_GAME)

        // Orientation
        valuesOrientation = findViewById(R.id.textViewValuesOrientation)
        valuesOrientation.isVisible = false
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_STATUS_ACCURACY_LOW)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_STATUS_ACCURACY_LOW)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION))
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    override fun onSensorChanged(event: SensorEvent?) {
        when(event!!.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> linearAccelerometer(event)
            Sensor.TYPE_ORIENTATION -> orientation(event)
        }
    }

    private fun linearAccelerometer(event: SensorEvent?){

        if(Math.abs(event!!.values[0]) > 0.2F) {
            circle.x = (0.5 * Math.cos(Math.PI * increaseAngle(azimuth, 180.0) / 180.0) + circle.x).toFloat()
            //circle.x = (((event!!.values[0]*100)/scaleX) * Math.cos(Math.PI * azimuth / 180) + circle.x).toFloat() // Eixo X
            //circle.x = (-Math.abs(event!!.values[0]) * Math.cos(Math.PI * azimuth / 180) + circle.x).toFloat() // Eixo X
        }

        if(Math.abs(event!!.values[1]) > 0.2F) {
            circle.y = (0.5 * Math.sin(Math.PI * increaseAngle(azimuth, 180.0) / 180.0) + circle.y).toFloat();
            //circle.y = (((event!!.values[1]*100)/scaleY) * Math.sin(Math.PI * azimuth / 180) + circle.y).toFloat() // Eixo Y
            //circle.y = (-Math.abs(event!!.values[1]) * Math.sin(Math.PI * azimuth / 180) + circle.y).toFloat() // Eixo Y
        }

        if(valuesLinearAccelerometer.isVisible)
            valuesLinearAccelerometer.text =
                    "x = ${String.format("%.4f", event!!.values[0])}\n\n"+
                            "y = ${String.format("%.4f", event!!.values[1])}\n\n"+
                            "z = ${String.format("%.4f", event!!.values[2])}"
    }

    private fun increaseAngle(currentAngle: Double, valueToAdd: Double): Double{
        var result = currentAngle + valueToAdd
        while(result < 0 || result > 360){
            if(result > 360)
                result -= 360
            else
                if(result < 0)
                    result = 360 - Math.abs(result)
        }
        return result
    }

    private fun orientation(event: SensorEvent?){
        //azimuth = increaseAngle(event!!.values[0].toDouble(), -111.8)
        azimuth = increaseAngle(truncate(event!!.values[0].toDouble()),68.0)

        if(valuesOrientation.isVisible)
            valuesOrientation.text =
                    "Azimuth = ${azimuth}\n\n"+
                    "ScaleX = ${String.format("%.4f", scaleX)}\n\n"+
                    "ScaleY = ${String.format("%.4f", scaleY)}"
    }

}