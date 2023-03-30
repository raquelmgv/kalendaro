package com.raquelgonzalezvillaescusa.kalendaro.activities

import Data.ListViewActividadesAdapter
import android.content.Intent
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
import kotlinx.android.synthetic.main.activity_actividades.*
import kotlinx.android.synthetic.main.activity_rutinas.conceptualMenuIconsBar
import kotlinx.android.synthetic.main.popup_delete_item.view.*

class ActividadesActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var currentUser : String = mAuth.uid.toString()

    private lateinit var toolbar: Toolbar
    private lateinit var bundle: Bundle
    private lateinit var categoriaActual: String
    private lateinit var rutinaActual: String
    private lateinit var actividadesNameList: MutableList<String>
    private lateinit var actividadesHourList: MutableList<String>
    private lateinit var rootNode : FirebaseDatabase
    private lateinit var reference_actividades : DatabaseReference
    /*1*/
    internal var storage: FirebaseStorage? = null
    internal var storageReference: StorageReference? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividades)

        toolbar = findViewById(R.id.toolbar)
        setUpToolbar(toolbar)
        displayConceptualMenu()
        /*2*/
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        bundle = intent.extras
        categoriaActual = bundle.getString("categoria")
        rutinaActual = bundle.getString("nombreRutinaCreada")

        displayBarActualView()
        rootNode = FirebaseDatabase.getInstance()
        reference_actividades = rootNode.getReference("$currentUser/categoriasRutinaData/$categoriaActual/rutinas/$rutinaActual/actividades")

        getActividadesDB()

        buttonAniadirActividad.setOnClickListener {
            intent = Intent(this@ActividadesActivity, CrearActividadActivity::class.java)
            val b: Bundle = Bundle()
            //Paso la categoria y la rutina por si se repiten los nombres de las rutinas en distintas categorias
            b.putString("categoria", categoriaActual)
            b.putString("nombreRutinaCreada", rutinaActual)

            intent.putExtras(b)
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle("")
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        /*val iconMenuDrawable = getDrawable(R.drawable.ic_access_time_black_24dp)
        iconMenuDrawable?.setColorFilter(
            getColor(R.color.colorPrimaryText),
            PorterDuff.Mode.SRC_ATOP
        )
        supportActionBar?.setHomeAsUpIndicator(iconMenuDrawable)*/
    }

    private fun displayBarActualView(){
       barActualViewDetails.setText(rutinaActual.capitalize())
    }

    public override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    public override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.graficas -> goToActivity<GraficasActivity> {}
            R.id.editarPerfil -> goToActivity<EditarPerfilActivity> {}
            R.id.logOut -> goToActivity<LoginActivity> {}
        }
        return super.onOptionsItemSelected(item);
    }

    private fun displayConceptualMenu(){
        conceptualMenuIconsBar.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.diaActual -> { goToActivity<DiaActual> {}
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)}
                R.id.calendario -> { goToActivity<CalendarActivity>{}
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)}
                R.id.rutinas -> { goToActivity<CategoriasActivity> {}
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)}
            }
            true }
    }


    private fun getActividadesDB() {
        actividadesNameList = mutableListOf()
        actividadesHourList = mutableListOf()
        reference_actividades.addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    actividadesNameList.clear()
                    actividadesHourList.clear()
                    for (act in snapshot.children) {
                        for (i in act.children) {
                            if (i.key.toString() == "actividad") {
                                actividadesNameList.add(i.value.toString())
                            }
                            if(i.key.toString() == "hora"){
                                actividadesHourList.add(i.value.toString())
                            }
                        }
                    }
                    Log.w("HOUR ARRAY","$actividadesHourList")
                    Log.w("NAME ARRAY","$actividadesNameList")

                    var adaptador = ListViewActividadesAdapter(applicationContext, actividadesHourList, actividadesNameList, rutinaActual, categoriaActual)
                    actividadesListView.adapter = adaptador
                    //Detectar cuando se seleccione un elemento de la lista
                    actividadesListView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                        val actHour = actividadesHourList.get(i)
                        intent = Intent(this@ActividadesActivity, ActividadActualActivity::class.java)
                        var dato: String = actHour
                        val b: Bundle = Bundle()
                        b.putString("actividadHora", dato)
                        intent.putExtras(b)
                        startActivity(intent)
                    }
                    actividadesListView.onItemLongClickListener = AdapterView.OnItemLongClickListener { adapterView, view, i, l ->
                        val actName = actividadesNameList.get(i)
                        val actHour = actividadesHourList.get(i)
                        showDeleteView(actName, actHour)
                        true
                    }
                }
            }
        })
    }

    fun showDeleteView(actName: String, actHour: String){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.popup_delete_item, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView).setTitle(actName)
        mDialogView.textView_popup_delete.setText(R.string.dialog_delete_act)
        val mAlertDialog = mBuilder.show()
        mDialogView.button_cancelar.setOnClickListener { mAlertDialog.dismiss() }
        mDialogView.button_eliminar.setOnClickListener {
            eliminarActividad(actHour)
            mAlertDialog.dismiss()
           // recargarActivity()
        }
        mDialogView.button_editar.setOnClickListener {
            editarActividad(actName, actHour)
            mAlertDialog.dismiss()
            recargarActivity()
        }
    }

    private fun eliminarActividad(actHour: String){
        reference_actividades.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (act in snapshot.children) {
                        for (i in act.children) {
                            if (i.key.toString() == "hora") {
                                val actividad_hora = act.key.toString()
                                if(actividad_hora == actHour){
                                    /*remove del storage*/
                                    var fotoActividadPath = "$currentUser/images/rutina_details/cat_$categoriaActual/rut_$rutinaActual/act_$actividad_hora"
                                    var fotoActividadRef = storageReference?.child(fotoActividadPath)
                                    if (fotoActividadRef != null) {
                                        fotoActividadRef.delete()
                                    };
                                    reference_actividades.child(actividad_hora).removeValue()
                                }
                            }
                        }
                    }
                }
            }
        })
    }
    private fun editarActividad(actName: String, actHour: String){
        reference_actividades.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (act in snapshot.children) {
                        for (i in act.children) {
                            if (i.key.toString() == "hora" && i.value.toString() == actHour) {
                                intent = Intent(this@ActividadesActivity, EditarActividadActivity::class.java)
                                val b: Bundle = Bundle()
                                b.putString("categoria", categoriaActual)
                                b.putString("nombreRutinaCreada", rutinaActual)
                                b.putString("nombreActividadEditar", actName)
                                b.putString("horaActividadEditar", actHour)
                                intent.putExtras(b)
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
        })
    }

    private fun recargarActivity(){
        intent = Intent(this@ActividadesActivity, ActividadesActivity::class.java)
        var cat: String = categoriaActual
        var rut: String = rutinaActual
        val b: Bundle = Bundle()
        b.putString("categoria", cat)
        b.putString("nombreRutinaCreada", rut)
        intent.putExtras(b)
        startActivity(intent)
    }
}
