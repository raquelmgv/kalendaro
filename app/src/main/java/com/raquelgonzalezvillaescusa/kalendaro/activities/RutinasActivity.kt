package com.raquelgonzalezvillaescusa.kalendaro.activities

import Data.ListViewRutinasAdapter
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.raquelgonzalezvillaescusa.kalendaro.R
import com.raquelgonzalezvillaescusa.kalendaro.goToActivity
import kotlinx.android.synthetic.main.activity_rutinas.*
import kotlinx.android.synthetic.main.activity_rutinas.barActualView
import kotlinx.android.synthetic.main.activity_rutinas.conceptualMenuIconsBar
import kotlinx.android.synthetic.main.popup_delete_item.view.*

class RutinasActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance()}
    private var currentUser : String = mAuth.uid.toString()

    private lateinit var toolbar: Toolbar
    private lateinit var bundle : Bundle
    private lateinit var categoriaSeleccionada : String
    private lateinit var rutinasList: MutableList<String>
    private var fechaString : String = ""

    private lateinit var rootNode : FirebaseDatabase
    private lateinit var reference : DatabaseReference

    /*1*/
    internal var storage: FirebaseStorage? = null
    internal var storageReference: StorageReference? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rutinas)

        toolbar = findViewById(R.id.toolbar)
        setUpToolbar(toolbar)
        displayConceptualMenu()
        if(intent !== null && intent.extras !== null) {
            bundle = intent.extras
            categoriaSeleccionada = bundle.getString("categoria")
            if(bundle.getString("fechaString")!== null){
                fechaString = bundle.getString("fechaString");
            }
        }
        displayBarActualView()
        reference = FirebaseDatabase.getInstance().getReference("$currentUser/categoriasRutinaData/$categoriaSeleccionada/rutinas")
        /*2*/
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference
        rootNode = FirebaseDatabase.getInstance()

        getRutinasDB()

        buttonCrearRutina.setOnClickListener {
            intent = Intent(this@RutinasActivity, CrearRutinaActivity::class.java)
            var dato: String = categoriaSeleccionada
            val b: Bundle = Bundle()
            b.putString("categoria", dato)
            if(!fechaString.isBlank()) {
                b.putString("fechaString", fechaString)
            }
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
    }

    public override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater.inflate(R.menu.menu, menu)
/*      val item_rutinas: MenuItem = menu.findItem(R.id.rutinas) as MenuItem
        item_rutinas.setVisible(false)
*/
        return super.onCreateOptionsMenu(menu)
    }

    public override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.editarPerfil -> goToActivity<EditarPerfilActivity> {}
            R.id.logOut -> goToActivity<LoginActivity> {}
        }
        return super.onOptionsItemSelected(item);
    }

    private fun displayBarActualView(){
        barActualView.setText("Rutinas de " + categoriaSeleccionada)
    }

    private fun getRutinasDB(){
        rutinasList = mutableListOf()
        reference.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    rutinasList.clear()
                    for (rut in snapshot.children){
                        val rutina = rut.key.toString()
                        rutinasList.add(rutina)
                    }
                    var adaptador = ListViewRutinasAdapter(applicationContext, rutinasList, categoriaSeleccionada)
                    rutinasListView.adapter = adaptador
                    rutinasListView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                        val rutName = rutinasList.get(i)
                        intent = Intent(this@RutinasActivity, ActividadesActivity::class.java)
                        var dato_rutina: String = rutName
                        val b: Bundle = Bundle()
                        b.putString("nombreRutinaCreada", dato_rutina)
                        b.putString("categoria", categoriaSeleccionada)
                        intent.putExtras(b)
                        startActivity(intent)
                    }
                    rutinasListView.onItemLongClickListener = AdapterView.OnItemLongClickListener { adapterView, view, i, l ->
                        val rutName = rutinasList.get(i)
                        showDeleteView(rutName, categoriaSeleccionada)
                        true
                    }
                }
            }
        })

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

    private fun recargarActivity(){
        intent = Intent(this@RutinasActivity, RutinasActivity::class.java)
        var dato: String = categoriaSeleccionada
        val b: Bundle = Bundle()
        b.putString("categoria", dato)
        intent.putExtras(b)
        startActivity(intent)
    }

}
