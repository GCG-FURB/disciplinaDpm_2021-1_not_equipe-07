package dpm.mpb

import android.content.Context
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

        circle.x = 50F
        circle.y = 50F

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Accelerometer
        valuesLinearAccelerometer = findViewById(R.id.textViewValuesLinearAccelerometer)
        valuesLinearAccelerometer.isVisible = false

        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                SensorManager.SENSOR_DELAY_FASTEST)

        // Orientation
        valuesOrientation = findViewById(R.id.textViewValuesOrientation)
        valuesOrientation.isVisible = true
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_FASTEST)
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

        if(Math.abs(event!!.values[0]) > 0.01F) {
            //circle.x = (((event!!.values[0]*100)/scaleX) * Math.cos(Math.PI * azimuth / 180) + circle.x).toFloat() // Eixo X
            circle.x = (-Math.abs(event!!.values[0]) * Math.cos(Math.PI * azimuth / 180) + circle.x).toFloat() // Eixo X
        }

        if(Math.abs(event!!.values[1]) > 0.01F) {
            //circle.y = (((event!!.values[1]*100)/scaleY) * Math.sin(Math.PI * azimuth / 180) + circle.y).toFloat() // Eixo Y
            circle.y = (-Math.abs(event!!.values[1]) * Math.sin(Math.PI * azimuth / 180) + circle.y).toFloat() // Eixo Y
        }

        if(valuesLinearAccelerometer.isVisible)
            valuesLinearAccelerometer.text =
                    "x = ${String.format("%.4f", event!!.values[0])}\n\n"+
                            "y = ${String.format("%.4f", event!!.values[1])}\n\n"+
                            "z = ${String.format("%.4f", event!!.values[2])}"
    }

    private fun orientation(event: SensorEvent?){
        azimuth = event!!.values[0].toDouble()

        if(valuesOrientation.isVisible)
            valuesOrientation.text =
                    "Azimuth = ${String.format("%.4f", event!!.values[0])}\n\n"+
                    "Pitch = ${String.format("%.4f", event!!.values[1])}\n\n"+
                    "Roll = ${String.format("%.4f", event!!.values[2])}\n\n"+
                    "ScaleX = ${String.format("%.4f", scaleX)}\n\n"+
                    "ScaleY = ${String.format("%.4f", scaleY)}"
    }

}