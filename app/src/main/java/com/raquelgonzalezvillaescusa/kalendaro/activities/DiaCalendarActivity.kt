package com.raquelgonzalezvillaescusa.kalendaro.activities

import Data.DiaActualHelper
import android.content.Intent

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.raquelgonzalezvillaescusa.kalendaro.*
import com.raquelgonzalezvillaescusa.kalendaro.R
import kotlinx.android.synthetic.main.activity_dia_actual.*

class DiaCalendarActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance()}
    private var currentUser : String = mAuth.uid.toString()

    private lateinit var toolbar: Toolbar
    private lateinit var bundle : Bundle

    private lateinit var fechaSeleccionada : String
    private lateinit var fecha : String
    private lateinit var rootNode : FirebaseDatabase
    private lateinit var reference : DatabaseReference
    private lateinit var referenceCategorias : DatabaseReference

    internal var storage: FirebaseStorage? = null
    internal var storageReference: StorageReference? = null

    private var faceNumber : Int = 0
    private var comerState : Int = 0
    private var banioState: Int = 0
    private var dormirState: Int = 0

    private lateinit var rutinasList: MutableList<String>
    private lateinit var categoriaCorrespondienteList: MutableList<String>

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dia_calendar)

        toolbar = findViewById(R.id.toolbar)
        setUpToolbar(toolbar)

        displayConceptualMenu()

        bundle = intent.extras
        fechaSeleccionada = bundle.getString("fecha_seleccionada")

        fecha = displayBarActualView(fechaSeleccionada)

        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference
        rootNode = FirebaseDatabase.getInstance()
        reference = rootNode.getReference("$currentUser/diaActualData")
        referenceCategorias = rootNode.getReference("$currentUser/categoriasRutinaData")

        getFaceNumberDB()
        getDailiesDB()
        getRutinasDiaCorrespondiente(rootNode, storageReference, currentUser, referenceCategorias,this@DiaCalendarActivity, fecha);

        customButton_feliz.setOnClickListener{
            if (faceNumber == 1){ faceNumber = 0 } else{ faceNumber = 1}
            var diaActHelper: DiaActualHelper = DiaActualHelper(fecha, faceNumber, comerState, banioState, dormirState)
            reference.child(fecha).setValue(diaActHelper)
        }
        customButton_normal.setOnClickListener{
            if (faceNumber == 2){ faceNumber = 0 } else{ faceNumber = 2}
            var diaActHelper: DiaActualHelper = DiaActualHelper(fecha, faceNumber, comerState, banioState, dormirState)
            reference.child(fecha).setValue(diaActHelper)
        }
        customButton_triste.setOnClickListener{
            if (faceNumber == 3){ faceNumber = 0 } else{ faceNumber = 3}
            var diaActHelper: DiaActualHelper = DiaActualHelper(fecha, faceNumber, comerState, banioState, dormirState)
            reference.child(fecha).setValue(diaActHelper)
        }
        customButton_banio.setOnClickListener{
            when (banioState) {
                0 -> banioState = 1
                1 -> banioState = 2
                2 -> banioState = 3
                3 -> banioState = 0
            }
            var diaActHelper: DiaActualHelper = DiaActualHelper(fecha, faceNumber, comerState, banioState, dormirState)
            reference.child(fecha).setValue(diaActHelper)
        }
        customButton_comer.setOnClickListener{
            when (comerState) {
                0 -> comerState = 1
                1 -> comerState = 2
                2 -> comerState = 3
                3 -> comerState = 0
            }
            var diaActHelper: DiaActualHelper = DiaActualHelper(fecha, faceNumber, comerState, banioState, dormirState)
            reference.child(fecha).setValue(diaActHelper)
        }
        customButton_dormir.setOnClickListener{
            when (dormirState) {
                0 -> dormirState = 1
                1 -> dormirState = 2
                2 -> dormirState = 3
                3 -> dormirState = 0
            }
            var diaActHelper: DiaActualHelper = DiaActualHelper(fecha, faceNumber, comerState, banioState, dormirState)
            reference.child(fecha).setValue(diaActHelper)
        }

        buttonCrearRutina.setOnClickListener {
            intent = Intent(this@DiaCalendarActivity, CategoriasActivity::class.java)
            var dato: String = fecha
            val b: Bundle = Bundle()
            b.putString("fechaString", dato)
            intent.putExtras(b)
            startActivity(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpToolbar(toolbar: Toolbar){
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle("")
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun getFaceNumberDB(){
        var reference_fecha: DatabaseReference =  rootNode.getReference("$currentUser/diaActualData/" + "$fecha" + "/faceNumber")
        reference_fecha.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.value != null){
                    var value = dataSnapshot.value.toString()
                    faceNumber = value.toInt()
                } else{
                    faceNumber = 0
                }
                displayCustomFace(faceNumber)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

    }
    private fun getDailiesDB(){
        var reference_comer: DatabaseReference =  rootNode.getReference("$currentUser/diaActualData/" + "$fecha" + "/comerState")
        var reference_banio: DatabaseReference =  rootNode.getReference("$currentUser/diaActualData/" + "$fecha" + "/banioState")
        var reference_dormir: DatabaseReference =  rootNode.getReference("$currentUser/diaActualData/" + "$fecha" + "/dormirState")
        reference_comer.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.value != null){
                    var value = dataSnapshot.value.toString()
                    comerState = value.toInt()
                } else{
                    comerState = 0
                }
                displayCustomComer(comerState)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        reference_banio.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.value != null){
                    var value = dataSnapshot.value.toString()
                    banioState = value.toInt()
                } else{
                    banioState = 0
                }
                displayCustomBanio(banioState)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        reference_dormir.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.value != null){
                    var value = dataSnapshot.value.toString()
                    dormirState = value.toInt()
                } else{
                    dormirState = 0
                }
                displayCustomDormir(dormirState)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater.inflate(R.menu.menu, menu)
/*        val item_categoria: MenuItem = menu.findItem(R.id.crearCategoria) as MenuItem
        item_categoria.setVisible(false)*/
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.editarPerfil -> goToActivity<EditarPerfilActivity> {}
            R.id.logOut -> goToActivity<LoginActivity> {}
        }
        return super.onOptionsItemSelected(item);
    }

    fun displayBarActualView(fechaSelec: String): String {
        barActualView.setText(fechaSelec)
        return fechaSelec
    }

    private fun displayConceptualMenu(){
        conceptualMenuIconsBar.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.diaActual -> { goToActivity<DiaActual> {} }
                R.id.calendario -> { goToActivity<CalendarActivity>{} }
                R.id.rutinas -> { goToActivity<CategoriasActivity> {} }
            }
            true }
    }
}
