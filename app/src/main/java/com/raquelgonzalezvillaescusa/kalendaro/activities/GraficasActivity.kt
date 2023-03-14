package com.raquelgonzalezvillaescusa.kalendaro.activities

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.raquelgonzalezvillaescusa.kalendaro.R
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.StaticLabelsFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries


class GraficasActivity : AppCompatActivity() {
    private lateinit var series: LineGraphSeries<DataPoint>
    private var puntos = mutableListOf<DataPoint>()
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
        setLabels ()
        //Segun el zoom tendremos vista en años, meses o dias
        if(getCurrentZoom().equals(ZoomLevel.DAY_ZOOM)){
            // TODO: Buscamos los dias que hay contestados en la bdd
            val dias = listOf(1,2,3,4,5,6,7,8,9,10)
            //TODO: fin todo
            for (dia in dias){
            puntos.add(DataPoint(dia.toDouble(), getMediaEstadoDeAnimoDay()))
            }
        }else if(getCurrentZoom().equals(ZoomLevel.MONTH_ZOOM)){

            // TODO: Buscamos los dias de cada mes que hay contestados en la bdd
            val meses = listOf(3,4,5,6,7)
            //TODO: fin todo
            for (mes in meses){
                puntos.add(DataPoint(mes.toDouble(), getMediaEstadoDeAnimoMonth()))
            }
        }else{
            // TODO: Buscamos los anios que hay contestados en la bdd
            val anios = listOf(2022.toDouble(), 2023.toDouble())
            //TODO: fin todo
            for (anio in anios){
               puntos.add(DataPoint(anio, getMediaEstadoDeAnimoYear()))
            }

        }
        series = LineGraphSeries(puntos.toTypedArray())
        series.color = Color.parseColor("#8B00FF")
        // Configurar la gráfica
        grafica.addSeries(series)
        grafica.viewport.isScalable = true
        grafica.viewport.isXAxisBoundsManual = true
        grafica.viewport.isYAxisBoundsManual = true
        grafica.gridLabelRenderer.numVerticalLabels = 3
        grafica.gridLabelRenderer.horizontalAxisTitle = "Tiempo"
        grafica.gridLabelRenderer.verticalAxisTitle = "Estado de ánimo"
        grafica.gridLabelRenderer.padding = 100
        grafica.gridLabelRenderer.verticalAxisTitleTextSize = 30f
        grafica.gridLabelRenderer.horizontalAxisTitleTextSize = 30f

    }

    private fun setLabels () {
        val estadosAnimo = arrayOf( "Triste", "Normal", "Contento")
        val anios = arrayOf( "2021", "2022", "2023")
        val meses = arrayOf( "Enero", "Febrero", "Marzo")
        val dias = arrayOf( "27", "28", "31")

        val staticLabelsFormatter = StaticLabelsFormatter(grafica)
        staticLabelsFormatter.setVerticalLabels(
            estadosAnimo
        )

        staticLabelsFormatter.setHorizontalLabels(
            when {
                ZoomLevel.YEAR_ZOOM.equals(getCurrentZoom()) -> anios
                ZoomLevel.MONTH_ZOOM.equals(getCurrentZoom()) -> meses
                else -> dias
            }
        )

        grafica.gridLabelRenderer.labelFormatter = staticLabelsFormatter
        grafica.gridLabelRenderer.setHorizontalLabelsAngle(90)
        grafica.gridLabelRenderer.labelVerticalWidth = 100
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

    /*TODO: Hacer la busqueda en la bdd*/
    private fun getMediaEstadoDeAnimoYear(): Double{
    //Calculamos la media de estados de animo del año entre 1, 2, y 3 estados
        //TODO: buscar en bdd:
        var estados = arrayOf(1,1,1,2,3,3,2,2,3)
        return estados.average();
    }

    private fun getMediaEstadoDeAnimoMonth(): Double{
        //TODO: buscar en bdd:
        var estados = arrayOf(1,1,1,2,3,2,2,3)
        return estados.average();
    }

    private fun getMediaEstadoDeAnimoDay(): Double{
        //TODO: buscar en bdd:
        return 3.toDouble();
    }


}
