package com.raquelgonzalezvillaescusa.kalendaro.activities

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.StaticLabelsFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.raquelgonzalezvillaescusa.kalendaro.*
import com.raquelgonzalezvillaescusa.kalendaro.R
import java.util.*


class GraficasActivity : AppCompatActivity() {
    val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance()}
    var currentUser : String = mAuth.uid.toString()
    private lateinit var series: LineGraphSeries<DataPoint>
    private var puntos = mutableListOf<DataPoint>()
    private lateinit var grafica: GraphView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graficas)
        grafica = findViewById(R.id.graficaEstadosAnimo)
        makeGraph()

    }
    fun makeGraph() {
        setLabels()
        //resetLabelsXNumDaysOfCurrentMonth()
        addDataPointsDaysOfCurrentMonth()
        //series = LineGraphSeries(resetLabelsXNumDaysOfCurrentMonth().toTypedArray())
        series = LineGraphSeries(puntos.toTypedArray())
        // Configurar la gráfica
        grafica.addSeries(series)
        val viewport = grafica.viewport
        viewport.isScalable = true
        viewport.isScrollable = true
        viewport.isXAxisBoundsManual = true
        val minX = 1.0 // valor mínimo en el eje X
        val maxX = numeroDiasMesActual().toDouble() // valor máximo en el eje X
        viewport.setMinX(minX)
        viewport.setMaxX(maxX)
        grafica.gridLabelRenderer.setHumanRounding(true)
        grafica.gridLabelRenderer.padding = 100
        grafica.gridLabelRenderer.verticalAxisTitleTextSize = 50f
        grafica.gridLabelRenderer.horizontalAxisTitleTextSize = 50f
        grafica.gridLabelRenderer.setHorizontalLabelsAngle(90)
        grafica.gridLabelRenderer.labelVerticalWidth = 100
        series.color = Color.parseColor("#8B00FF")
    }

    private fun setLabels() {

        val gridLabelX = grafica.gridLabelRenderer
        val gridLabelY = grafica.gridLabelRenderer
        gridLabelX.horizontalAxisTitle = "Días del mes" //eje X
        gridLabelY.verticalAxisTitle = "Estado de ánimo" // eje Y

        gridLabelX.numHorizontalLabels = numeroDiasMesActual() // numero de etiquetas
        gridLabelY.numVerticalLabels = 3 // numero de etiquetas
        val staticLabelsFormatter = StaticLabelsFormatter(grafica)
        staticLabelsFormatter.setVerticalLabels(arrayOf("Triste", "Normal", "Contento"))
        staticLabelsFormatter.setHorizontalLabels(getLabelsX().toTypedArray())
        grafica.gridLabelRenderer.labelFormatter = staticLabelsFormatter

    }

    private fun addDataPointsDaysOfCurrentMonth(){
        var faceNumber: Double
        val rootNode = FirebaseDatabase.getInstance()
        lateinit var reference : DatabaseReference
        reference = rootNode.getReference("$currentUser/diaActualData")
        reference.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    puntos.clear()
                    for (datosDiaActualData in snapshot.children) {
                        val mes = convertDateToString(datosDiaActualData.key.toString()).substring(4,6)
                        for(datosDia in datosDiaActualData.children){
                            faceNumber = 0.0
                            if(mes.equals(mesActual())){
                                if (datosDia.key.toString() == "faceNumber") {
                                    faceNumber = datosDia.value.toString().toDouble()
                                    puntos.add(DataPoint(mes.toInt().toDouble(), faceNumber))
                                }
                            }
                        }
                    }
                }
            }
        })

    }

    private fun resetLabelsXNumDaysOfCurrentMonth () : MutableList<DataPoint> {
        var dataPointsZero = mutableListOf<DataPoint>()
        // Obtenemos el número de días del mes actual
        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in 1..daysInMonth) {
            dataPointsZero.add(DataPoint(i.toDouble(), 0.0))
        }
        return dataPointsZero;
    }

    private fun getLabelsX () : MutableList<String> {
        var labelsX = mutableListOf<String>()
        // Obtenemos el número de días del mes actual y lo recorremos para poner loe labels en el eje X
        for (i in 1..numeroDiasMesActual()) {
            labelsX.add(i.toString())
        }
        return labelsX;
    }

}
