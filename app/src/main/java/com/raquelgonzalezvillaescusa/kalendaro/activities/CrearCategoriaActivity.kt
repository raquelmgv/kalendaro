package com.raquelgonzalezvillaescusa.kalendaro.activities

import Data.CrearCategoriaHelper
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.raquelgonzalezvillaescusa.kalendaro.R
import com.raquelgonzalezvillaescusa.kalendaro.goToActivity
import com.raquelgonzalezvillaescusa.kalendaro.isValidName
import com.raquelgonzalezvillaescusa.kalendaro.toast
import kotlinx.android.synthetic.main.activity_crear_categoria.*
import java.io.ByteArrayOutputStream

class CrearCategoriaActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var currentUser : String = mAuth.uid.toString()
    private lateinit var toolbar: Toolbar
    private val REQUEST_GALLERY = 1001 //cualquier numero distinto de -1 quiere decir que si tiene permiso
    private val REQUEST_CAMERA = 1002

    private var fechaString : String = ""
    private lateinit var rootNode : FirebaseDatabase
    private lateinit var reference : DatabaseReference
    private lateinit var bundle: Bundle

    /*1*/
    internal var storage: FirebaseStorage? = null
    internal var storageReference: StorageReference? = null

    var fotoGaleria: Uri? = null
    var fotoCamara: Uri? = null //Uri : direccion interna de un archivo (identificador de recursos)

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_categoria)
        toolbar = findViewById(R.id.toolbar)
        setUpToolbar(toolbar)

        if(intent !== null && intent.extras !== null) {
            bundle = intent.extras
            fechaString = bundle.getString("fechaString")
        }
        rootNode = FirebaseDatabase.getInstance()
        reference = rootNode.getReference("$currentUser/categoriasRutinaData")

        /*2*/
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        val iconImageInicial = getDrawable(R.drawable.arasaac_fotografia)
        crearCategoriaImagen?.setImageDrawable(iconImageInicial)

        buttonCategoriaImagenGaleria.setOnClickListener{clickOnGaleria()}
        buttonCategoriaImagenCamara.setOnClickListener{clickOnCamara()}
        buttonGuardarCategoria.setOnClickListener {
            val catName = editTextCrearCategoria.text.toString()
            if (!isValidName(catName)) {
                editTextCrearCategoria.error = "El nombre puede contener únicamente letras o numeros hasta 20 caracteres"
            }else {
                guardarDatosDB(editTextCrearCategoria.text.toString())
                if(!fechaString.isBlank()) {
                    intent = Intent(this@CrearCategoriaActivity, CategoriasActivity::class.java)
                    val b: Bundle = Bundle()
                    b.putString("fechaString", fechaString)
                    intent.putExtras(b)
                    startActivity(intent)
                }
                goToActivity<CategoriasActivity>()
            }
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
/*        val item_categoria: MenuItem = menu.findItem(R.id.crearCategoria) as MenuItem
        item_categoria.setVisible(false)*/
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
            crearCategoriaImagen?.setImageDrawable(null)
            fotoGaleria = data?.data
            crearCategoriaImagen.setImageURI(fotoGaleria)
        }
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CAMERA){
            crearCategoriaImagen?.setImageDrawable(null)
            crearCategoriaImagen.setImageURI(fotoCamara)
        }
    }

    private fun guardarDatosDB(categoriaNombre: String){
       /*3*/
        if(fotoGaleria != null || fotoCamara != null) {
            var fotoCategoriaRef =
                storageReference?.child("$currentUser/images/categorias/cat_$categoriaNombre")
            crearCategoriaImagen.isDrawingCacheEnabled = true
            crearCategoriaImagen.buildDrawingCache()
            val bitmap = (crearCategoriaImagen.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            fotoCategoriaRef?.putBytes(data)
        }
        var crearCategoriaHelper = CrearCategoriaHelper(categoriaNombre)
        reference.child(categoriaNombre).setValue(crearCategoriaHelper)

    }

}