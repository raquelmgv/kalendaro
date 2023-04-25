package com.raquelgonzalezvillaescusa.kalendaro.activities

import Data.ListViewCategoriasAdapter
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.raquelgonzalezvillaescusa.kalendaro.R
import com.raquelgonzalezvillaescusa.kalendaro.goToActivity
import kotlinx.android.synthetic.main.activity_categorias.*
import kotlinx.android.synthetic.main.activity_categorias.conceptualMenuIconsBar
import kotlinx.android.synthetic.main.popup_delete_item.view.*


class CategoriasActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance()}
    private var currentUser : String = mAuth.uid.toString()
    private lateinit var toolbar: Toolbar

    private lateinit var bundle: Bundle
    private lateinit var rootNode : FirebaseDatabase
    private lateinit var reference : DatabaseReference
    private var fechaString : String = ""

    private lateinit var categoriasRutinasList: MutableList<String>

    internal var storage: FirebaseStorage? = null
    internal var storageReference: StorageReference? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categorias)

        toolbar = findViewById(R.id.toolbar)
        setUpToolbar(toolbar)
        displayConceptualMenu()

        if(intent !== null && intent.extras !== null){
            bundle = intent.extras
            fechaString = bundle.getString("fechaString")
        }

        rootNode = FirebaseDatabase.getInstance()
        reference = FirebaseDatabase.getInstance().getReference("$currentUser/categoriasRutinaData")
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        getCategoriasDB()
        buttonCrearCategoria.setOnClickListener { goToActivity<CrearCategoriaActivity> {} }
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

    public override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater.inflate(R.menu.menu, menu)
/*        val item: MenuItem = menu.findItem(R.id.rutinas) as MenuItem
        //item.setVisible(false)*/
        return super.onCreateOptionsMenu(menu)
    }

    public override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.graficas -> goToActivity<GraficasActivity> {}
            R.id.logOut -> goToActivity<LoginActivity> {}
        }
        return super.onOptionsItemSelected(item);
    }

    private fun getCategoriasDB(){
        categoriasRutinasList = mutableListOf()
        reference.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot!!.exists()){
                    categoriasRutinasList.clear()
                    for (cat in snapshot.children){
                        val categoria = cat.key.toString()
                        categoriasRutinasList.add(categoria!!)
                    }
                    var adaptador = ListViewCategoriasAdapter(applicationContext, categoriasRutinasList)
                    //pasamos solo el nombre pero a partir de este podemos sacar la imagen de firebase
                    categoriasListView.adapter = adaptador
                    //Detectar cuando se seleccione un elemento de la lista
                    categoriasListView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
                        val catName = categoriasRutinasList.get(i)
                        intent = Intent(this@CategoriasActivity, RutinasActivity::class.java)
                        var dato: String = catName
                        val b: Bundle = Bundle()
                        b.putString("categoria", dato)
                        if(!fechaString.isBlank()){
                            b.putString("fechaString", fechaString)
                        }
                        intent.putExtras(b)
                        startActivity(intent)
                    }
                    categoriasListView.onItemLongClickListener = AdapterView.OnItemLongClickListener { adapterView, view, i, l ->
                        val catName = categoriasRutinasList.get(i)
                        showDeleteView(catName)
                        true
                    }
                }
            }
        })
    }

    private fun showDeleteView(catName: String){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.popup_delete_item, null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView).setTitle(catName)
        mDialogView.textView_popup_delete.setText(R.string.dialog_delete_cat)
        val mAlertDialog = mBuilder.show()
        mDialogView.button_cancelar.setOnClickListener { mAlertDialog.dismiss() }
        mDialogView.button_eliminar.setOnClickListener {
            /*remove foto categoria del storage*/
            var fotoCategoriaPath = "$currentUser/images/categorias/cat_$catName"
            var fotoCategoriaRef = storageReference?.child(fotoCategoriaPath)
            if (fotoCategoriaRef != null) {
                eliminarDirectorioRutinasStorage(catName)
                fotoCategoriaRef.delete()
            }
            reference.child(catName).removeValue()
            goToActivity<CategoriasActivity> { }
            mAlertDialog.dismiss()
        }
    }

    private fun eliminarDirectorioRutinasStorage(catName: String){
        var rutinasReference: DatabaseReference =  rootNode.getReference("$currentUser/categoriasRutinaData/$catName/rutinas")
        rutinasReference.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (rut in snapshot.children) {
                        //ELIMINAMOS RUTINA
                        val rutName = rut.key.toString()
                        var fotosRutinaPath = "$currentUser/images/rutinas/cat_$catName/rut_$rutName"
                        var fotoRutinaRef = storageReference?.child(fotosRutinaPath)
                        if (fotoRutinaRef != null) {
                            fotoRutinaRef.delete()
                        }
                        //ELIMINAMOS ACTIVIDADES
                        for(i in rut.children){
                            for(j in i.children) {
                                val actividad_hora = j.key.toString()
                                var fotosActividadPath = "$currentUser/images/rutina_details/cat_$catName/rut_$rutName/act_$actividad_hora"
                                Log.w("PATH***", fotosActividadPath)
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
            }
            true }
    }
}
