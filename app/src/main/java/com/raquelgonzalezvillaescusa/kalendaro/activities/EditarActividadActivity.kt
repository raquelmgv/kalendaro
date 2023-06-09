package com.raquelgonzalezvillaescusa.kalendaro.activities

import Data.CrearActividadHelper
import Data.GlideApp
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.raquelgonzalezvillaescusa.kalendaro.*
import com.raquelgonzalezvillaescusa.kalendaro.Fragments.TimePickerFragment
import com.raquelgonzalezvillaescusa.kalendaro.R
import kotlinx.android.synthetic.main.activity_crear_actividad.*
import java.io.ByteArrayOutputStream

class EditarActividadActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var currentUser: String = mAuth.uid.toString()
    private lateinit var toolbar: Toolbar
    private val REQUEST_GALLERY =
        1001 //cualquier numero distinto de -1 quiere decir que si tiene permiso
    private val REQUEST_CAMERA = 1002
    private lateinit var categoriaActual: String
    private lateinit var rutinaActual: String
    private var nombreActividadEditar: String = ""
    private var horaActividadEditar: String = ""

    private lateinit var grupoImagenesRutina: ArrayList<String>
    private lateinit var bundle: Bundle
    private lateinit var rootNode: FirebaseDatabase
    private lateinit var reference_actividades: DatabaseReference

    /*1*/
    internal var storage: FirebaseStorage? = null
    internal var storageReference: StorageReference? = null

    var fotoGaleria: Uri? = null
    var fotoCamara: Uri? = null //Uri : direccion interna de un archivo (identificador de recursos)

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_actividad)
        toolbar = findViewById(R.id.toolbar)
        setUpToolbar(toolbar)

        bundle = intent.extras
        categoriaActual = bundle.getString("categoria")
        rutinaActual = bundle.getString("nombreRutinaCreada")
        nombreActividadEditar = bundle.getString("nombreActividadEditar")
        horaActividadEditar = bundle.getString("horaActividadEditar")

        displayBarActualView()
        /*2*/
        rootNode = FirebaseDatabase.getInstance()
        reference_actividades =
            rootNode.getReference("$currentUser/categoriasRutinaData/$categoriaActual/rutinas/$rutinaActual/actividades")
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        grupoImagenesRutina = ArrayList()

        val iconImageInicial = getDrawable(R.drawable.arasaac_fotografia)
        crearActividadImagen?.setImageDrawable(iconImageInicial)

        editTextCrearActividad.setText(nombreActividadEditar)
        actividad_input_hora.setHint(horaActividadEditar)
        mostrarImagenActividad()

        buttonActividadImagenGaleria.setOnClickListener { clickOnGaleria() }
        buttonActividadImagenCamara.setOnClickListener { clickOnCamara() }
        buttonGuardarActividad.setOnClickListener {
            val actName = editTextCrearActividad.text.toString()
            if (!isValidName(actName)) {
                editTextCrearActividad.error =
                    "El nombre es obligatorio y puede contener letras y numeros hasta 20 caracteres"
            } else if (!isValidHour(actividad_input_hora.hint.toString())) {
                actividad_input_hora.error = "Introduzca hora de la actividad"
            } else {
                guardarDatosDB()
                goToActividadesActivity()
            }
        }
        actividad_input_hora.setOnClickListener { showTimePickerDialog() }
    }

    private fun showTimePickerDialog() {
        val timePicker = TimePickerFragment { time -> onTimeSelected(time) }
        timePicker.show(supportFragmentManager, "time")
    }

    private fun onTimeSelected(time: String) {
        actividad_input_hora.setHint(time)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle("")
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun displayBarActualView() {
        barActualViewActividad.setText("Editar actividad " + nombreActividadEditar)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Preguntamos si tiene permiso
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                //Pedimos permiso al user:
                val permisosGaleria = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permisosGaleria, REQUEST_GALLERY)
            } else {
                muestraGaleria()
            }
        } else {
            muestraGaleria()
        }
    }

    private fun clickOnCamara() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
            ) {
                val permisosCamara = arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                requestPermissions(permisosCamara, REQUEST_CAMERA)
            } else {
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_GALLERY -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    muestraGaleria()
                } else {
                    toast(
                        "No ha sido posible acceder a sus fotos de la galería." +
                                "\nRevise los permisos de la aplicación en Ajustes"
                    )
                }
            }
            REQUEST_CAMERA -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    abreCamara()
                } else {
                    toast(
                        "No ha sido posible aceder a la camara. " +
                                "\n Revise los permisos de la aplicación en Ajunstes"
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_GALLERY) {
            crearActividadImagen?.setImageDrawable(null)
            crearActividadImagen.setImageURI(data?.data)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CAMERA) {
            crearActividadImagen?.setImageDrawable(null)
            crearActividadImagen.setImageURI(fotoCamara)
        }
    }

    private fun guardarDatosDB() {
        var horaActividad = actividad_input_hora.hint.toString()
        /*3*/
        var fotoActividadRef =
            storageReference?.child("$currentUser/images/rutina_details/cat_$categoriaActual/rut_$rutinaActual/act_$horaActividad")
        crearActividadImagen.isDrawingCacheEnabled = true
        crearActividadImagen.buildDrawingCache()
        val bitmap = (crearActividadImagen.drawable as BitmapDrawable).bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        fotoActividadRef?.metadata?.addOnSuccessListener { metadata ->
            val exists = metadata.sizeBytes > 0
            if (exists) {
                fotoActividadRef.delete()
            }
        }
        fotoActividadRef?.putBytes(data)
        var crearActividadHelper = CrearActividadHelper(
            actividad_input_hora.hint.toString(),
            editTextCrearActividad.text.toString(),
            "$rutinaActual+$categoriaActual"
        )
        if (horaActividadEditar != horaActividad) {
            eliminarActividad(horaActividadEditar)
        }
        reference_actividades.child(horaActividad)
            .setValue(crearActividadHelper)

        //var uploadTask = fotoActividadRef?.putBytes(data)
        /* if (uploadTask != null) {
             uploadTask.addOnFailureListener {
                 toast("Error, no se han actualizado los cambios")
             }.addOnSuccessListener { taskSnapshot ->
                 toast("Actividad guardada correctamente")
                 var crearActividadHelper = CrearActividadHelper(
                     actividad_input_hora.hint.toString(),
                     editTextCrearActividad.text.toString(),
                     "$rutinaActual+$categoriaActual"
                 )
                 reference_actividades.child(actividad_input_hora.hint.toString())
                     .setValue(crearActividadHelper)
                 if (horaActividadEditar != horaActividad) {
                     eliminarActividad(horaActividadEditar)
                 }
             }
         }*/
        goToActividadesActivity()

    }

    private fun eliminarActividad(actHour: String) {
        reference_actividades.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (act in snapshot.children) {
                        for (i in act.children) {
                            if (i.key.toString() == "hora") {
                                val actividad_hora = act.key.toString()
                                if (actividad_hora == actHour) {
                                    /*remove del storage*/
                                    var fotoActividadPath =
                                        "$currentUser/images/rutina_details/cat_$categoriaActual/rut_$rutinaActual/act_$actividad_hora"
                                    var fotoActividadRef =
                                        storageReference?.child(fotoActividadPath)
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

    private fun mostrarImagenActividad() {
        var fotoActPath =
            "$currentUser/images/rutina_details/cat_$categoriaActual/rut_$rutinaActual/act_$horaActividadEditar"
        var fotoActRef = storageReference?.child(fotoActPath)
        fotoActRef?.metadata?.addOnSuccessListener { metadata ->
            // mirar si la imagen existe
            val exists = metadata.sizeBytes > 0
            if (exists) {
                GlideApp.with(this)
                    .load(fotoActRef)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions().override(100, 100))
                    .fitCenter()
                    .centerCrop()
                    .into(crearActividadImagen)
            } else {
                GlideApp.with(this)
                    .load(R.drawable.arasaac_fotografia)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions().override(100, 100))
                    .fitCenter()
                    .centerCrop()
                    .into(crearActividadImagen)
            }
        }
    }

    fun goToActividadesActivity() {
        intent = Intent(this@EditarActividadActivity, ActividadesActivity::class.java)
        val b: Bundle = Bundle()
        b.putString("categoria", categoriaActual)
        b.putString("nombreRutinaCreada", rutinaActual)
        intent.putExtras(b)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        goToActividadesActivity()
    }

}