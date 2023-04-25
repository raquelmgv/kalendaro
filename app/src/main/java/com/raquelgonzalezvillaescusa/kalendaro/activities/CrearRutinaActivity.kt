package com.raquelgonzalezvillaescusa.kalendaro.activities

import Data.CrearRutinaHelper
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
import com.raquelgonzalezvillaescusa.kalendaro.*
import com.raquelgonzalezvillaescusa.kalendaro.Fragments.DatePickerFragment
import kotlinx.android.synthetic.main.activity_crear_rutina.*
import java.io.ByteArrayOutputStream
import java.util.*

class CrearRutinaActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var currentUser : String = mAuth.uid.toString()
    private lateinit var toolbar: Toolbar
    private val REQUEST_GALLERY = 1001 //cualquier numero distinto de -1 quiere decir que si tiene permiso
    private val REQUEST_CAMERA = 1002
    private lateinit var categoriaActual : String
    private var fechaString : String = ""
    private var dia = ""
    private var mes = ""
    private var anio = ""

    private var diaFin = ""
    private var mesFin = ""
    private var anioFin = ""

    private var diaInt = 0
    private var mesInt = 0
    private var anioInt = 0

    private lateinit var bundle: Bundle
    private lateinit var rootNode : FirebaseDatabase
    private lateinit var reference : DatabaseReference
    //private lateinit var referenceDiaActual : DatabaseReference
    /*1*/
    internal var storage: FirebaseStorage? = null
    internal var storageReference: StorageReference? = null

    var fotoGaleria: Uri? = null
    var fotoCamara: Uri? = null //Uri : direccion interna de un archivo (identificador de recursos)
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_rutina)
        toolbar = findViewById(R.id.toolbar)
        setUpToolbar(toolbar)
        if(intent !== null && intent.extras !== null) {
            bundle = intent.extras
            categoriaActual = bundle.getString("categoria")
            if(bundle.getString("fechaString")!== null){
                fechaString = bundle.getString("fechaString");
            }
        }
        rootNode = FirebaseDatabase.getInstance()
        reference = rootNode.getReference("$currentUser/categoriasRutinaData/$categoriaActual/rutinas")
        //referenceDiaActual = rootNode.getReference("$currentUser/diaActualData/rutinas")


        /*2*/
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        if(!fechaString.isBlank()){
            diaInt = fechaString.split(" ")[0].toInt();
            mesInt = convertStringMonthToInt(fechaString.split(" ")[1]);
            anioInt = fechaString.split(" ")[2].toInt();
            onDateInitSelected(anioInt, mesInt, diaInt)
            DatePickerFragment({anioInt, mesInt, diaInt ->
                onDateInitSelected(anioInt, mesInt, diaInt)
            })

        }

        val iconImageInicial = getDrawable(R.drawable.arasaac_fotografia)
        crearRutinaImagen?.setImageDrawable(iconImageInicial)

        rutinaInputFechaInicio.setOnClickListener { showDateInitPickerDialog() }
        rutinaInputFechaFin.setOnClickListener { showDateFinPickerDialog() }
        buttonRutinaImagenGaleria.setOnClickListener{clickOnGaleria()}
        buttonRutinaImagenCamara.setOnClickListener{clickOnCamara()}
        buttonGuardarRutina.setOnClickListener {
            val rutName = editTextCrearRutina.text.toString()
            if (!isValidName(rutName)) {
                editTextCrearRutina.error = "El nombre puede contener únicamente letras o numeros hasta 20 caracteres"
            }else {
                guardarDatosDB(editTextCrearRutina.text.toString())
                intent = Intent(this@CrearRutinaActivity, ActividadesActivity::class.java)
                val b: Bundle = Bundle()
                b.putString("categoria", categoriaActual)
                b.putString("nombreRutinaCreada", editTextCrearRutina.text.toString())
                intent.putExtras(b)
                startActivity(intent)
            }
        }
    }

    private fun showDateInitPickerDialog(){
      val datePicker = DatePickerFragment { year, month, day ->
          onDateInitSelected(year, month + 1, day)
      }
        datePicker.show(supportFragmentManager, "datePicker")
    }

    private fun showDateFinPickerDialog(){
        val datePicker = DatePickerFragment { year, month, day ->
            onDateFinSelected(year, month + 1, day)
        }
        datePicker.show(supportFragmentManager, "datePicker")
    }
    private fun onDateInitSelected(year: Int, month: Int, day: Int){
        dia = day.toString()
        mes = month.toString()
        anio = year.toString()
        if(day < 10)
            dia = "0$dia"
        if(month < 10)
            mes = "0$mes"
        rutinaInputFechaInicio.setHint("$dia/$mes/$anio")
        if(diaFin.isBlank() ||mesFin.isBlank() || anioFin.isBlank()){
            diaFin = dia
            mesFin = mes
            anioFin = anio
            rutinaInputFechaFin.setHint("$dia/$mes/$anio")
        }
    }

    private fun onDateFinSelected(year: Int, month: Int, day: Int){
        diaFin = day.toString()
        mesFin = month.toString()
        anioFin = year.toString()
        if(day < 10)
            diaFin = "0$diaFin"
        if(month < 10)
            mesFin = "0$mesFin"
        rutinaInputFechaFin.setHint("$diaFin/$mesFin/$anioFin")
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
            crearRutinaImagen?.setImageDrawable(null)
            fotoGaleria = data?.data
            crearRutinaImagen.setImageURI(fotoGaleria)
        }
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CAMERA){
            crearRutinaImagen?.setImageDrawable(null)
            crearRutinaImagen.setImageURI(fotoCamara)
        }
    }

    private fun guardarDatosDB(rutinaNombre: String) {
        /*3*/
        if(fotoGaleria != null || fotoCamara != null) {
            var fotoCategoriaRef = storageReference?.child("$currentUser/images/rutinas/cat_$categoriaActual/rut_$rutinaNombre")
            crearRutinaImagen.isDrawingCacheEnabled = true
            crearRutinaImagen.buildDrawingCache()
            val bitmap = (crearRutinaImagen.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            fotoCategoriaRef?.putBytes(data)
        }

        var crearRutinaHelper = CrearRutinaHelper(rutinaNombre, DDMMMMYYYYFormatByYearMonthDay(
            Date(anio.toInt() - 1900, mes.toInt()-1, dia.toInt())
        ), DDMMMMYYYYFormatByYearMonthDay(
            Date(anioFin.toInt() - 1900, mesFin.toInt()-1, diaFin.toInt())))

        reference.child(rutinaNombre).setValue(crearRutinaHelper)
    }
}
