package com.raquelgonzalezvillaescusa.kalendaro.activities

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import com.raquelgonzalezvillaescusa.kalendaro.*
import com.raquelgonzalezvillaescusa.kalendaro.R
import kotlinx.android.synthetic.main.activity_grafica_dormir_anios.*


class GraficaDormirAniosActivity : AppCompatActivity() {
    val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance()}
    var currentUser : String = mAuth.uid.toString()
    private lateinit var toolbar: Toolbar
    private lateinit var grafica: GraphView
    private lateinit var series: BarGraphSeries<DataPoint>
    private var puntos = mutableListOf<DataPoint>()
    private var labelsX = mutableListOf<String>()
    //val ultimosAnios = listOf("2019","2020","2021","2022","2023")


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grafica_dormir_anios)
        displayConceptualMenu()
        toolbar = findViewById(R.id.toolbar)
        setUpToolbar(toolbar)
        grafica = findViewById(R.id.graficaDormirAnios)
        labelsX = setLabelsX()
        addDataPointsYears { puntosArray ->
            val puntosOrdenados = puntosArray.sortedBy { it.x }
            val puntosConPromedio = getPuntosPromedio(puntosOrdenados)
            makeGraph(puntosConPromedio)
        }
    }

    private fun getPuntosPromedio(puntosOrdenados: List<DataPoint>): List<DataPoint>{
        val puntosAgrupadosMap = mutableMapOf<Double, MutableList<Double>>()
        var puntosConPromedio = mutableListOf<DataPoint>()
        /*Agregar los puntos a sus respectivos grupos*/
        for (punto in puntosOrdenados) {
            if (punto.x in puntosAgrupadosMap) {
                puntosAgrupadosMap[punto.x]!!.add(punto.y)
            } else {
                puntosAgrupadosMap[punto.x] = mutableListOf(punto.y)
            }
        }
        for ((x, puntos) in puntosAgrupadosMap) {
            val promedio = puntos.average()
            puntosConPromedio.add(DataPoint(x, promedio))
        }
        return puntosConPromedio;
    }

    private fun makeGraph(puntosConPromedio: List<DataPoint>) {
        this.puntos.clear()
        this.puntos.addAll(puntosConPromedio)
        series = BarGraphSeries(puntos.toTypedArray())
        series.setTitle("Grafica del mes actual")
        grafica.addSeries(series)
        grafica.viewport.isXAxisBoundsManual = true
        grafica.gridLabelRenderer.setHumanRounding(true)
        val minX = 2019.0
        val maxX = 2023.0 // Ultimos 5 anios eje X
        grafica.viewport.setMinX(minX)
        grafica.viewport.setMaxX(maxX)
        val minY = 0.0
        val maxY = 3.0
        grafica.viewport.setMinY(minY)
        grafica.viewport.setMaxY(maxY)
        grafica.gridLabelRenderer.horizontalAxisTitle = "Últimos 5 años"//eje X
        grafica.gridLabelRenderer.horizontalAxisTitleTextSize = 40f
        grafica.gridLabelRenderer.horizontalAxisTitleColor = Color.parseColor("#C6AADB")
        grafica.gridLabelRenderer.labelHorizontalHeight = 100 // Posición de los labels
        grafica.gridLabelRenderer.setHorizontalLabelsColor(Color.parseColor("#C6AADB"))
        grafica.gridLabelRenderer.numVerticalLabels = 4
        grafica.gridLabelRenderer.padding = 10
        grafica.gridLabelRenderer.horizontalAxisTitleTextSize = 60f
        grafica.gridLabelRenderer.setHorizontalLabelsAngle(90)
        grafica.gridLabelRenderer.setLabelsSpace(5)
        series.color = Color.parseColor("#C6AADB")
        //DefaultLabelFormatter
        val labelFormatter = object : DefaultLabelFormatter() {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                return if (isValueX) {
                    // Si es el eje X, obtener el label a partir del valor redondeado
                    val year = value.toInt().toString()
                    year
                } else {
                    // Si es el eje Y, utilizar el formateador predeterminado
                    super.formatLabel(value, isValueX)
                }
            }
        }
        grafica.gridLabelRenderer.labelFormatter = labelFormatter
        grafica.getGridLabelRenderer().setVerticalLabelsVisible(false);
        grafica.getGridLabelRenderer().setVerticalAxisTitle("");
    }

    private fun addDataPointsYears(callback: (List<DataPoint>) -> Unit) {
        var faceNumber: Double
        val rootNode = FirebaseDatabase.getInstance()
        lateinit var reference: DatabaseReference
        reference = rootNode.getReference("$currentUser/diaActualData")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                val puntosArray = mutableListOf<DataPoint>()
                if (snapshot.exists()) {
                    puntosArray.clear()
                    for (datosDiaActualData in snapshot.children) {
                        val anio = convertDateToString(datosDiaActualData.key.toString()).substring(0,4)
                        for(datosDia in datosDiaActualData.children){
                            faceNumber = 0.0
                            //if(anio.equals(anioActual())){
                                if (datosDia.key.toString() == "dormirState") {
                                    faceNumber = datosDia.value.toString().toDouble()
                                    if(!puntosArray.contains(DataPoint(anio.toInt().toDouble(), faceNumber))){
                                        puntosArray.add(DataPoint(anio.toInt().toDouble(), faceNumber))
                                    }
                                }
                            //}
                        }
                    }
                }
                callback(puntosArray)
            }
        })
    }

    private fun setLabelsX () : MutableList<String> {
        var labelsX = mutableListOf<String>()
        // Obtenemos el número de días del mes actual y lo recorremos para poner loe labels en el eje X
        for (i in 1..12) {
            if(!labelsX.contains(i.toString())){
                labelsX.add(i.toString())
            }
        }
        return labelsX;
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpToolbar(toolbar: Toolbar){
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle("")
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.graficas -> goToActivity<GraficasActivity> {}
            R.id.logOut -> goToActivity<LoginActivity> {}
        }
        return super.onOptionsItemSelected(item);
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun displayConceptualMenu(){
        conceptualMenuIconsBar.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.calendario -> { goToActivity<CalendarActivity>{} }
                R.id.rutinas -> { goToActivity<CategoriasActivity> {} }
                R.id.diaActual -> { goToActivity<DiaActual> {} }
            }
            true }
    }

}
