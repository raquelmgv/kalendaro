package com.raquelgonzalezvillaescusa.kalendaro.activities

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.StaticLabelsFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.raquelgonzalezvillaescusa.kalendaro.*
import com.raquelgonzalezvillaescusa.kalendaro.R


class GraficasActivity : AppCompatActivity() {
    private lateinit var series: LineGraphSeries<DataPoint>
    private var dataPointLsit = mutableListOf<DataPoint>()
    private lateinit var grafica: GraphView
    enum class ZoomLevel {
        NO_ZOOM,
        DAY_ZOOM,
        MONTH_ZOOM,
        YEAR_ZOOM
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graficas)
        grafica = findViewById(R.id.graficaEstadosAnimo)
        makeGraph()
        grafica.setOnTouchListener { _, event ->
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_MOVE -> {
                    setLabels()
                }
                MotionEvent.ACTION_UP -> {
                    setLabels()
                }
            }
            false
        }
    }

    fun makeGraph() {
        setLabels()
        // Configurar la gráfica
        grafica.addSeries(series)
        grafica.viewport.isScalable = true
        grafica.viewport.isXAxisBoundsManual = true
        grafica.viewport.isYAxisBoundsManual = true
        grafica.gridLabelRenderer.numVerticalLabels = 3
        grafica.gridLabelRenderer.numHorizontalLabels = dataPointLsit.size
        grafica.gridLabelRenderer.horizontalAxisTitle = "Tiempo"
        grafica.gridLabelRenderer.verticalAxisTitle = "Estado de ánimo"
        grafica.gridLabelRenderer.padding = 100
        grafica.gridLabelRenderer.verticalAxisTitleTextSize = 30f
        grafica.gridLabelRenderer.horizontalAxisTitleTextSize = 30f

    }

    /*Cambiamos los labels de la gráfica*/
    private fun setLabels () {
        addDataPoints()
        val estadosAnimo = arrayOf("Triste", "Normal", "Contento")
        val staticLabelsFormatter = StaticLabelsFormatter(grafica)
        staticLabelsFormatter.setVerticalLabels(
            estadosAnimo
        )
        staticLabelsFormatter.setHorizontalLabels(
            when {
                ZoomLevel.YEAR_ZOOM.equals(getCurrentZoom()) -> setLabelsXZoom("Y").toTypedArray()
                ZoomLevel.MONTH_ZOOM.equals(getCurrentZoom()) -> setLabelsXZoom("M").toTypedArray()
                else -> setLabelsXZoom("D").toTypedArray()
            }
        )
        grafica.gridLabelRenderer.labelFormatter = staticLabelsFormatter
        grafica.gridLabelRenderer.setHorizontalLabelsAngle(90)
        grafica.gridLabelRenderer.labelVerticalWidth = 100
    }

    private fun setLabelsXZoom(typeDate: String) : List<String> {
        //Convierte las labels de vista año en string para mostrarlas
        // typeDate: Y -> anios, M-> meses, else-> dias
        var arrayDatapointX  : List<Double> = getXValuesFromDataPoints(dataPointLsit.toTypedArray())
        lateinit var arrayYears : MutableList<String>
        arrayYears = mutableListOf() // inicialización de la lista vacía
        for (dataPointX: Double in arrayDatapointX){
            when (typeDate){
                "Y" -> arrayYears.add(dataPointX.toString().substring(0,5))
                "M" -> arrayYears.add(dataPointX.toString().substring(5,7))
                else -> arrayYears.add(dataPointX.toString().substring(7,9))
            }
        }

        if(arrayYears.isNullOrEmpty()){
          arrayYears.add(" ")
        }
        if (arrayYears.size < 2) {
            // Agrega etiquetas por defecto
            arrayYears.add(" ")
        }
        return arrayYears
    }

    private fun addDataPoints(){
        var faceNumber: Double
        val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance()}
        var currentUser : String = mAuth.uid.toString()
        val rootNode = FirebaseDatabase.getInstance()
        lateinit var reference : DatabaseReference
        reference = rootNode.getReference("$currentUser/diaActualData")


        reference.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    dataPointLsit.clear()
                    for (datosDiaActualData in snapshot.children) {
                        val date = convertStringDateToStringNumber(datosDiaActualData.key.toString()).toInt()
                        for(datosDia in datosDiaActualData.children){
                            faceNumber = 0.0;
                            if (datosDia.key.toString() == "faceNumber") {
                                faceNumber = datosDia.value.toString().toDouble()
                                dataPointLsit.add(DataPoint(date.toDouble(), faceNumber))
                            }

                        }
                    }
                }
            }
        })
        series = LineGraphSeries(dataPointLsit.toTypedArray())
        series.color = Color.parseColor("#8B00FF")
        grafica.addSeries(series)
    }

    private fun getCurrentZoom(): ZoomLevel {
        val viewport = grafica.viewport

        // Calcular los valores de zoom mínimo y máximo en función del porcentaje de zoom
        val minX = viewport.getMinX(false)
        val maxX = viewport.getMaxX(false)
        val visibleRange = maxX - minX -1
        val totalRange = viewport.getMaxX(true)

        return when {
            (visibleRange == totalRange) || (visibleRange > totalRange*60/100)-> ZoomLevel.YEAR_ZOOM
            (visibleRange < totalRange*60/100) && (visibleRange > totalRange*30/100) -> ZoomLevel.MONTH_ZOOM
            (visibleRange < totalRange*30/100) -> ZoomLevel.DAY_ZOOM
            else -> ZoomLevel.NO_ZOOM
        }
    }
}
