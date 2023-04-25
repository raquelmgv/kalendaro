package com.raquelgonzalezvillaescusa.kalendaro.activities

import Data.CrearActividadHelper
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.raquelgonzalezvillaescusa.kalendaro.*
import com.raquelgonzalezvillaescusa.kalendaro.Fragments.TimePickerFragment
import com.raquelgonzalezvillaescusa.kalendaro.R
import kotlinx.android.synthetic.main.activity_crear_actividad.*
import java.io.ByteArrayOutputStream

class CrearActividadActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var currentUser : String = mAuth.uid.toString()
    private lateinit var toolbar: Toolbar
    private val REQUEST_GALLERY = 1001 //cualquier numero distinto de -1 quiere decir que si tiene permiso
    private val REQUEST_CAMERA = 1002
    private var exist:Boolean = false
    private lateinit var categoriaActual : String
    private lateinit var rutinaActual : String

    private lateinit var grupoImagenesRutina : ArrayList<String>
    private lateinit var bundle: Bundle
    private lateinit var rootNode : FirebaseDatabase
    private lateinit var reference_actividades : DatabaseReference
    /*1*/
    internal var storage: FirebaseStorage? = null
    internal var storageReference: StorageReference? = null

    var fotoGaleria: Uri? = null
    var fotoCamara: Uri? = null //Uri : direccion interna de un archivo (identificador de recursos)
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_actividad)
        toolbar = findViewById(R.id.toolbar)
        setUpToolbar(toolbar)

        bundle = intent.extras
        categoriaActual = bundle.getString("categoria")
        rutinaActual = bundle.getString("nombreRutinaCreada")

        displayBarActualView()
        /*2*/
        rootNode = FirebaseDatabase.getInstance()
        reference_actividades = rootNode.getReference("$currentUser/categoriasRutinaData/$categoriaActual/rutinas/$rutinaActual/actividades")
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        grupoImagenesRutina = ArrayList()

        val iconImageInicial = getDrawable(R.drawable.arasaac_fotografia)
        crearActividadImagen?.setImageDrawable(iconImageInicial)

        buttonActividadImagenGaleria.setOnClickListener{clickOnGaleria()}
        buttonActividadImagenCamara.setOnClickListener{clickOnCamara()}
        buttonGuardarActividad.setOnClickListener {
            val actName = editTextCrearActividad.text.toString()
            if (!isValidName(actName)) {
                editTextCrearActividad.error = "El nombre es obligatorio y puede contener letras y numeros hasta 20 caracteres"
            }else if (!isValidHour(actividad_input_hora.hint.toString())) {
                actividad_input_hora.error = "Introduzca hora de la actividad"
            }else {
                guardarDatosDB()
                intent = Intent(this@CrearActividadActivity, ActividadesActivity::class.java)
                val b: Bundle = Bundle()
                b.putString("categoria", categoriaActual)
                b.putString("nombreRutinaCreada", rutinaActual)
                intent.putExtras(b)
                startActivity(intent)
                //finish()
            }
        }
        actividad_input_hora.setOnClickListener { showTimePickerDialog() }
    }

    private fun showTimePickerDialog(){
        val timePicker = TimePickerFragment{time->onTimeSelected(time)}
        timePicker.show(supportFragmentManager, "time")
    }
    private fun onTimeSelected(time: String){
        actividad_input_hora.setHint(time)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle("")
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun displayBarActualView(){
        barActualViewActividad.setText("Crear Actividad de "+ rutinaActual)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater.inflate(R.menu.menu, menu)
/*        val item_categoria: MenuItem = menu.findItem(R.id.crearCategoria) as MenuItem
        item_categoria.setVisible(false)*/
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.graficas -> goToActivity<GraficasActivity> {}
            R.id.logOut -> goToActivity<LoginActivity> {}
        }
        return super.onOptionsItemSelected(item);
    }

    private fun clickOnGaleria() {
        //Comprobamos que la version de android que tiene en el movil es mayor que la Marsmallow
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //Preguntamos si tiene permiso
            if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                //Pedimos permiso al user:
                val permisosGaleria = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permisosGaleria, REQUEST_GALLERY)
            }else{
                muestraGaleria()
            }
        }else{
            muestraGaleria()
        }
    }

    private fun clickOnCamara(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
            ) {
                val permisosCamara = arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                requestPermissions(permisosCamara, REQUEST_CAMERA)
            }else{
                abreCamara()
            }
        } else {
            abreCamara()
        }
    }

    private fun muestraGaleria() {
        val intentGaleria = Intent(Intent.ACTION_PICK) //ACTION PICK: Seleccionar una imagen
        intentGaleria.type = "image/*"
        startActivityForResult(intentGaleria, REQUEST_GALLERY)
    }

    private fun abreCamara() {
        val bitsFoto = ContentValues() //Contentvalues: manejador de contenidos de base de datos
        // (crea un espacio de memoria vacia para llenarlo con los bits de la foto)
        bitsFoto.put(MediaStore.Images.Media.TITLE, "Nueva imagen")
        fotoCamara = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, bitsFoto)
        val camaraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        camaraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fotoCamara)
        startActivityForResult(camaraIntent, REQUEST_CAMERA)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_GALLERY->{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){muestraGaleria()}
                else{toast("No ha sido posible acceder a sus fotos de la galería." +
                        "\nRevise los permisos de la aplicación en Ajustes")}
            }
            REQUEST_CAMERA->{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){abreCamara()}
                else{toast("No ha sido posible aceder a la camara. " +
                        "\n Revise los permisos de la aplicación en Ajunstes")
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_GALLERY){
            crearActividadImagen?.setImageDrawable(null)
            fotoGaleria = data?.data
            crearActividadImagen.setImageURI(fotoGaleria)
        }
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CAMERA){
            crearActividadImagen?.setImageDrawable(null)
            crearActividadImagen.setImageURI(fotoCamara)
        }
    }

    private fun guardarDatosDB() {
        var horaActividad = actividad_input_hora.hint.toString()
        /*3*/
        if(fotoGaleria != null || fotoCamara != null) {
            var fotoActividadRef =
                storageReference?.child("$currentUser/images/rutina_details/cat_$categoriaActual/rut_$rutinaActual/act_$horaActividad")
            crearActividadImagen.isDrawingCacheEnabled = true
            crearActividadImagen.buildDrawingCache()
            val bitmap = (crearActividadImagen.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            fotoActividadRef?.putBytes(data)
        }

        var crearActividadHelper = CrearActividadHelper(actividad_input_hora.hint.toString(), editTextCrearActividad.text.toString(), "$rutinaActual+$categoriaActual")
        reference_actividades.child(actividad_input_hora.hint.toString()).setValue(crearActividadHelper)
        gotoActividadesActivity()
    }

    private fun gotoActividadesActivity(){
        intent = Intent(this@CrearActividadActivity, ActividadesActivity::class.java)
        val b: Bundle = Bundle()
        b.putString("categoria", categoriaActual)
        b.putString("nombreRutinaCreada", rutinaActual)
        intent.putExtras(b)
        startActivity(intent)
        //finish()
    }
    override fun onBackPressed() {
        super.onBackPressed()
        intent = Intent(this@CrearActividadActivity, ActividadesActivity::class.java)
        val b: Bundle = Bundle()
        b.putString("categoria", categoriaActual)
        b.putString("nombreRutinaCreada", rutinaActual)
        intent.putExtras(b)
        startActivity(intent)
    }
}