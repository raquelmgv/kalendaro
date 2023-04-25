package com.raquelgonzalezvillaescusa.kalendaro.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.raquelgonzalezvillaescusa.kalendaro.R
import com.raquelgonzalezvillaescusa.kalendaro.goToActivity
import kotlinx.android.synthetic.main.activity_calendar.*
import kotlinx.android.synthetic.main.popup_delete_item.view.*
import java.util.*


class CalendarActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance()}
    private var currentUser : String = mAuth.uid.toString()
    private lateinit var toolbar: Toolbar
    private lateinit var rutinasList: MutableList<String>
    private lateinit var categoriaCorrespondienteList: MutableList<String>
    private lateinit var categoriaSeleccionada: String
    private var day : String = "0"
    private var month : String = "0"
    private var year : String = "0"

    private lateinit var rootNode : FirebaseDatabase
    private lateinit var reference : DatabaseReference
    internal var storage: FirebaseStorage? = null
    internal var storageReference: StorageReference? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        toolbar = findViewById(R.id.toolbar)
        setUpToolbar(toolbar)
        displayConceptualMenu()

        val c: Calendar = Calendar.getInstance()
        day = c.get(Calendar.DAY_OF_MONTH).toString()
        month = (c.get(Calendar.MONTH)+1).toString()
        year = c.get(Calendar.YEAR).toString()


        if(month.toInt()<10){
            month = "0$month"
        }
        if(day.toInt()<10){
            day = "0$day"
        }

        reference = FirebaseDatabase.getInstance().getReference("$currentUser/categoriasRutinaData")
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference
        rootNode = FirebaseDatabase.getInstance()
        //setNavMenuItemThemeColors(getColor(R.color.colorGooglePlus))

      myCalendarView.setOnDateChangeListener { calendarView, i, i2, i3 ->

            intent = Intent(this@CalendarActivity, DiaCalendarActivity::class.java)
           var dato : String = convertirFecha(i3, i2, i)
           val b : Bundle = Bundle()
           b.putString("fecha_seleccionada", dato)
           intent.putExtras(b)
           startActivity(intent)
       }

        displayPictoMes(month.toInt());

    }

    private fun convertirFecha(dia: Int, mes: Int, anio: Int) : String{
        var fechaCompleta: String
        var nombreMes : String
        when (mes) {
            0 -> {nombreMes = "enero"}
            1 -> {nombreMes = "febrero"}
            2 -> {nombreMes = "marzo"}
            3 -> {nombreMes = "abril"}
            4 -> {nombreMes = "mayo"}
            5 -> {nombreMes = "junio"}
            6 -> {nombreMes = "julio"}
            7 -> {nombreMes = "agosto"}
            8 -> {nombreMes = "septiembre"}
            9 -> {nombreMes = "octubre"}
            10 -> {nombreMes = "noviembre"}
            else-> {nombreMes = "diciembre"}
        }
        if(dia < 10)
            fechaCompleta = "0$dia $nombreMes $anio"
        else
            fechaCompleta = "$dia $nombreMes $anio"
        return fechaCompleta
    }

    private fun  displayPictoMes(mes: Int){
        when (mes) {
            1 -> {imageView.setImageResource(R.drawable.mes_01_enero)}
            2 -> {imageView.setImageResource(R.drawable.mes_02_febrero)}
            3 -> {imageView.setImageResource(R.drawable.mes_03_marzo)}
            4 -> {imageView.setImageResource(R.drawable.mes_04_abril)}
            5 -> {imageView.setImageResource(R.drawable.mes_05_mayo)}
            6 -> {imageView.setImageResource(R.drawable.mes_06_junio)}
            7 -> {imageView.setImageResource(R.drawable.mes_07_julio)}
            8 -> {imageView.setImageResource(R.drawable.mes_08_agosto)}
            9 -> {imageView.setImageResource(R.drawable.mes_09_septiembre)}
            10 -> {imageView.setImageResource(R.drawable.mes_10_octubre)}
            11 -> {imageView.setImageResource(R.drawable.mes_11_noviembre)}
            else-> {imageView.setImageResource(R.drawable.mes_12_diciembre)}
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle("")
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    @SuppressLint("ResourceType")
    public override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater.inflate(R.menu.menu, menu)
        //val item_calendario: MenuItem = menu.findItem(R.id.calendario) as MenuItem
        //item_calendario.setVisible(false)
        return super.onCreateOptionsMenu(menu)
    }

    public override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.graficas -> goToActivity<GraficasActivity> {}
            R.id.logOut -> goToActivity<LoginActivity> {}
        }
        return super.onOptionsItemSelected(item);
    }

    private fun displayConceptualMenu(){
        conceptualMenuIconsBar.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.diaActual -> { goToActivity<DiaActual> {}
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)}
                R.id.rutinas -> { goToActivity<CategoriasActivity> {} }
            }
            true }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    public fun setNavMenuItemThemeColors(color: Int) {
        val navDefaultTextColor: Int =  getColor(R.color.colorPrimaryText)
        val navDefaultIconColor: Int = getColor(R.color.colorPrimaryText)
        val navMenuTextList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_enabled),
                intArrayOf(android.R.attr.state_pressed),
                intArrayOf(android.R.attr.state_focused)
            ), intArrayOf(
                color,
                navDefaultTextColor,
                navDefaultTextColor,
                navDefaultTextColor
            )
        )
        val navMenuIconList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_enabled),
                intArrayOf(android.R.attr.state_pressed),
                intArrayOf(android.R.attr.state_focused)
            ), intArrayOf(
                color,
                navDefaultIconColor,
                navDefaultIconColor,
                navDefaultIconColor
            )
        )
        conceptualMenuIconsBar.setItemTextColor(navMenuTextList)
        conceptualMenuIconsBar.setItemIconTintList(navMenuIconList)
    }




    private fun showDeleteView(rutName: String, catName: String){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.popup_delete_item, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView).setTitle(rutName)
        mDialogView.textView_popup_delete.setText(R.string.dialog_delete_rut)
        val mAlertDialog = mBuilder.show()
        mDialogView.button_cancelar.setOnClickListener { mAlertDialog.dismiss() }
        mDialogView.button_eliminar.setOnClickListener {
            eliminarDirectorioActividadesStorage(rutName)
            reference.child(rutName).removeValue()
            /*remove del storage*/
            var fotoRutinaPath = "$currentUser/images/rutinas/cat_$catName/rut_$rutName"
            var fotoRutinaRef = storageReference?.child(fotoRutinaPath)
            if (fotoRutinaRef != null) {
                fotoRutinaRef.delete()
            };
            mAlertDialog.dismiss()
            recargarActivity()
        }
    }

    private fun eliminarDirectorioActividadesStorage(rutName: String){
        var rutinasReference: DatabaseReference =  rootNode.getReference("$currentUser/categoriasRutinaData/$categoriaSeleccionada/rutinas")
        rutinasReference.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (rut in snapshot.children) {
                        for(i in rut.children){
                            for(j in i.children) {
                                val actividad_hora = j.key.toString()
                                var fotosActividadPath = "$currentUser/images/rutina_details/cat_$categoriaSeleccionada/rut_$rutName/act_$actividad_hora"
                                var fotoActividadRef = storageReference?.child(fotosActividadPath)
                                if (fotoActividadRef != null) {
                                    fotoActividadRef.delete()
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    private fun recargarActivity(){
        intent = Intent(this@CalendarActivity, CalendarActivity::class.java)
        var dato: String = categoriaSeleccionada
        val b: Bundle = Bundle()
        b.putString("categoria", dato)
        intent.putExtras(b)
        startActivity(intent)
    }

}
