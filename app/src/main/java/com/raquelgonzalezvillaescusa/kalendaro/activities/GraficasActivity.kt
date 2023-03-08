package com.raquelgonzalezvillaescusa.kalendaro.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.raquelgonzalezvillaescusa.kalendaro.R
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.Viewport
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.helper.StaticLabelsFormatter


class GraficasActivity : AppCompatActivity() {
    private lateinit var series: BarGraphSeries<DataPoint>
    private lateinit var graph: GraphView
    private lateinit var viewport: Viewport
    var num_feliz  = 30
    var num_normal  = 3
    var num_triste  = 27

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graficas)
        graph = findViewById(R.id.graficaEstadosAnimo)
        series = BarGraphSeries()
        graph.addSeries(series)

        makeGraph()

    }

    fun makeGraph() {
        // Give x and y axises their range
        viewport = graph.viewport
        graph.gridLabelRenderer.setHumanRounding(true)
        viewport.setMinY(0.0)
        viewport.isScrollable = true

        val staticLabelsFormatter = StaticLabelsFormatter(graph)
        staticLabelsFormatter.setHorizontalLabels(
            arrayOf(
                "Contento",
                "Normal",
                "Triste"
            )
        )

        //Aniadimos los datos
        series.appendData(DataPoint(1.0, num_feliz.toDouble()), true, 3)
        series.appendData(DataPoint(2.0, num_normal.toDouble()), true, 3)
        series.appendData(DataPoint(3.0, num_triste.toDouble()), true, 3 )
        //series.appendData(DataPoint(4.0, 0.0), true, 4)

    }
}
