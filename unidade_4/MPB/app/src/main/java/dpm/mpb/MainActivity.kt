package dpm.mpb

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import kotlin.math.truncate


class MainActivity : AppCompatActivity(), SensorEventListener {

    lateinit var sensorManager: SensorManager
    lateinit var circle: ImageView;
    lateinit var luz : ImageView;
    lateinit var btGps : Button
    lateinit var valuesLinearAccelerometer: TextView
    lateinit var valuesOrientation: TextView
    var azimuth: Double = 0.0
    var scaleX: Double = 1.0
    var scaleY: Double = 1.0
    var brightness: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestedOrientation = (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        btGps = findViewById(R.id.btGps)

        btGps.setOnClickListener {

            val telaGps = Intent(this, MapsActivity::class.java)
            startActivity(telaGps)
        }

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
        luz = findViewById(R.id.imageViewLuz);

        luz.x = metrics.widthPixels.toFloat() - 150
        luz.y = DisplayMetrics().ydpi + 50
        //btGps.y = 700f


        luz.layoutParams.width = 100;
        luz.layoutParams.height = 100;

        circle.x = 445F
        circle.y = 150F

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Accelerometer
        valuesLinearAccelerometer = findViewById(R.id.textViewValuesLinearAccelerometer)
        valuesLinearAccelerometer.isVisible = false

        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
            SensorManager.SENSOR_DELAY_GAME)


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

    private fun setUpSensorStuff(){
        brightness = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    private fun brightness(brightness: Float): String{
        return when (brightness.toInt()){
            0 -> "0" // Ausência de Luz
            in 1..10 ->"1" // Escuro
            in 11..50 -> "2" //Luz Media
            in 51..5000 -> "3" // Normal
            in 5001..25000 -> "4"// Luz muito forte
            else -> "5" // Excesso de Luz
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, brightness, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_STATUS_ACCURACY_LOW)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_STATUS_ACCURACY_LOW)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT))
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION))
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event!!.sensor.type == Sensor.TYPE_LIGHT){
            val light = event.values[0]
            if (brightness(light) == "0" || brightness(light) == "1"){
                luz.setImageDrawable(resources.getDrawable(R.drawable.light1))
            }else{
                luz.setImageDrawable(resources.getDrawable(R.drawable.light2))
            }
        }

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
        azimuth = increaseAngle(truncate(event!!.values[0].toDouble()), 74.0)
        circle.rotation = increaseAngle(azimuth, -90.0).toFloat() // -90 por que a tela do celular é na horizontal (paisagem)

        if(valuesOrientation.isVisible)
            valuesOrientation.text =
                    "Azimuth = ${azimuth}\n\n"+
                    "ScaleX = ${String.format("%.4f", scaleX)}\n\n"+
                    "ScaleY = ${String.format("%.4f", scaleY)}"
    }

}