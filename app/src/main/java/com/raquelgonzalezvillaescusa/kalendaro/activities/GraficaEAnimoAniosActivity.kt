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
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.raquelgonzalezvillaescusa.kalendaro.*
import com.raquelgonzalezvillaescusa.kalendaro.R
import kotlinx.android.synthetic.main.activity_grafica_ea_anios.*
import java.text.NumberFormat


class GraficaEAnimoAniosActivity : AppCompatActivity() {
    val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance()}
    var currentUser : String = mAuth.uid.toString()
    private lateinit var toolbar: Toolbar
    private lateinit var fecha : String

    private lateinit var grafica: GraphView
    private lateinit var series: LineGraphSeries<DataPoint>
    private var puntos = mutableListOf<DataPoint>()
    private var labelsX = mutableListOf<String>()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grafica_ea_anios)
        displayConceptualMenu()
        toolbar = findViewById(R.id.toolbar)
        setUpToolbar(toolbar)
        fecha = "Últimos años"

        grafica = findViewById(R.id.graficaEstadosAnimoMesActual)
        labelsX = setLabelsX()
        addDataPointsDaysOfCurrentMonth { puntosArray ->
            makeGraph(puntosArray)
        }

    }
    private fun makeGraph(puntosArray: List<DataPoint>) {
        this.puntos.clear()
        this.puntos.addAll(puntosArray)
        series = LineGraphSeries(puntos.toTypedArray())
        series.setTitle("Ultimos años") // Agrega esta línea para la leyenda
        grafica.addSeries(series)

        grafica.viewport.isXAxisBoundsManual = false


        grafica.gridLabelRenderer.setHumanRounding(true)
        val minX = 1.0 // valor mínimo en el eje X
        val maxX = numeroDiasMesActual().toDouble() // valor máximo en el eje X
        grafica.viewport.setMinX(minX)
        grafica.viewport.setMaxX(maxX)
        grafica.gridLabelRenderer.horizontalAxisTitle = "Ultimos años" //eje X
        grafica.gridLabelRenderer.horizontalAxisTitleTextSize = 40f
        grafica.gridLabelRenderer.horizontalAxisTitleColor = Color.parseColor("#8B00FF")

        grafica.gridLabelRenderer.numHorizontalLabels = numeroDiasMesActual()// numero de etiquetas
        grafica.gridLabelRenderer.setHorizontalLabelsColor(Color.parseColor("#8B00FF"))

        grafica.gridLabelRenderer.numVerticalLabels = 3 // numero de etiquetas
        grafica.gridLabelRenderer.padding = 10
        grafica.gridLabelRenderer.horizontalAxisTitleTextSize = 60f
        grafica.gridLabelRenderer.setHorizontalLabelsAngle(90)
        grafica.gridLabelRenderer.setLabelsSpace(-5)
        series.color = Color.parseColor("#8B00FF")
        val integerFormat = NumberFormat.getIntegerInstance()
        val defaultLabelsFormatter = DefaultLabelFormatter(integerFormat,integerFormat)
        grafica.getGridLabelRenderer().setVerticalLabelsVisible(false); // oculta los labels del eje Y
        grafica.getGridLabelRenderer().setVerticalAxisTitle(""); // establece un título vacío para el eje Y
        grafica.gridLabelRenderer.labelFormatter = defaultLabelsFormatter
    }

    private fun addDataPointsDaysOfCurrentMonth(callback: (List<DataPoint>) -> Unit) {
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
                        val mes = convertDateToString(datosDiaActualData.key.toString()).substring(4,6)
                        val diaMes = convertDateToString(datosDiaActualData.key.toString()).substring(6,8)
                        for(datosDia in datosDiaActualData.children){
                            faceNumber = 0.0
                            if(mes.equals(mesActual())){
                                if (datosDia.key.toString() == "faceNumber") {
                                    faceNumber = datosDia.value.toString().toDouble()
                                    if(!puntosArray.contains(DataPoint(diaMes.toInt().toDouble(), faceNumber))){
                                        puntosArray.add(DataPoint(diaMes.toInt().toDouble(), faceNumber))
                                    }
                                }
                            }
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
        for (i in 1..numeroDiasMesActual()) {
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
            R.id.editarPerfil -> goToActivity<EditarPerfilActivity> {}
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