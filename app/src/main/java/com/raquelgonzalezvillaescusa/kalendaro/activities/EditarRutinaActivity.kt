package com.raquelgonzalezvillaescusa.kalendaro.activities

import Data.CrearRutinaHelper
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
import com.raquelgonzalezvillaescusa.kalendaro.Fragments.DatePickerFragment
import com.raquelgonzalezvillaescusa.kalendaro.R
import kotlinx.android.synthetic.main.activity_crear_rutina.*
import java.io.ByteArrayOutputStream
import java.util.*

class EditarRutinaActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var currentUser: String = mAuth.uid.toString()
    private lateinit var toolbar: Toolbar
    private val REQUEST_GALLERY =
        1001 //cualquier numero distinto de -1 quiere decir que si tiene permiso
    private val REQUEST_CAMERA = 1002
    private lateinit var categoriaActual: String
    private lateinit var rutinaActual: String
    private var fechaInicioString: String = ""
    private var fechaFinString: String = ""

    private var dia = ""
    private var mes = ""
    private var anio = ""

    private var diaFin = ""
    private var mesFin = ""
    private var anioFin = ""

    private var diaInt = 0
    private var mesInt = 0
    private var anioInt = 0

    private var diaFinInt = 0
    private var mesFinInt = 0
    private var anioFinInt = 0

    private lateinit var bundle: Bundle
    private lateinit var rootNode: FirebaseDatabase
    private lateinit var reference: DatabaseReference

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
        if (intent !== null && intent.extras !== null) {
            bundle = intent.extras
            categoriaActual = bundle.getString("categoria")
            rutinaActual = bundle.getString("rutina")
            if (bundle.getString("fechaInicioString") != null) {
                fechaInicioString = bundle.getString("fechaInicioString")
            }
            if (bundle.getString("fechaFinString") != null) {
                fechaFinString = bundle.getString("fechaFinString")
            }
        }
        rootNode = FirebaseDatabase.getInstance()
        reference =
            rootNode.getReference("$currentUser/categoriasRutinaData/$categoriaActual/rutinas")
        //referenceDiaActual = rootNode.getReference("$currentUser/diaActualData/rutinas")


        /*2*/
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        if (!fechaInicioString.isBlank()) {
            diaInt = fechaInicioString.split(" ")[0].toInt();
            mesInt = convertStringMonthToInt(fechaInicioString.split(" ")[1]);
            anioInt = fechaInicioString.split(" ")[2].toInt();
            onDateInitSelected(anioInt, mesInt, diaInt)
            DatePickerFragment({ anioInt, mesInt, diaInt ->
                onDateInitSelected(anioInt, mesInt, diaInt)
            })

        }

        if (!fechaFinString.isBlank()) {
            diaFinInt = fechaFinString.split(" ")[0].toInt();
            mesFinInt = convertStringMonthToInt(fechaFinString.split(" ")[1]);
            anioFinInt = fechaFinString.split(" ")[2].toInt();
            onDateFinSelected(anioFinInt, mesFinInt, diaFinInt)
            DatePickerFragment({ anioFinInt, mesFinInt, diaFinInt ->
                onDateFinSelected(anioFinInt, mesFinInt, diaFinInt)
            })

        }

        val iconImageInicial = getDrawable(R.drawable.arasaac_fotografia)
        crearRutinaImagen?.setImageDrawable(iconImageInicial)

        editTextCrearRutina.setText(rutinaActual)
        mostrarImagenRutina()

        rutinaInputFechaInicio.setOnClickListener { showDateInitPickerDialog() }
        rutinaInputFechaFin.setOnClickListener { showDateFinPickerDialog() }

        buttonRutinaImagenGaleria.setOnClickListener { clickOnGaleria() }
        buttonRutinaImagenCamara.setOnClickListener { clickOnCamara() }
        buttonGuardarRutina.setOnClickListener {
            val rutName = editTextCrearRutina.text.toString()
            var fechaInicio = ""
            var fechaFin = ""
            if (!anio.isNullOrBlank() && !mes.isNullOrBlank() && !dia.isNullOrBlank()) {
                fechaInicio = DDMMMMYYYYFormatByYearMonthDay(
                    Date(anio.toInt() - 1900, mes.toInt() - 1, dia.toInt())
                )
            }
            if (!anioFin.isNullOrBlank() && !mesFin.isNullOrBlank() && !diaFin.isNullOrBlank()) {
                fechaFin = DDMMMMYYYYFormatByYearMonthDay(
                    Date(anioFin.toInt() - 1900, mesFin.toInt() - 1, diaFin.toInt())
                )
            }
            if (rutName.isBlank()) {
                editTextCrearRutina.error = "Debe introducir un nombre para la rutina"
            } else if (!isValidName(rutName)) {
                editTextCrearRutina.error =
                    "El nombre puede contener únicamente letras o numeros hasta 20 caracteres"
            } else if (!fechaFin.isNullOrBlank() && !fechaInicio.isNullOrBlank() && fechaFin.compareTo(
                    fechaInicio
                ) < 0
            ) {
                rutinaInputFechaInicio.error = "La fecha de inicio es menor a la de fin"
            } else {
                guardarDatosDB(editTextCrearRutina.text.toString())
                goToRutinasActivity()
            }
        }
    }

    private fun showDateInitPickerDialog() {
        val datePicker = DatePickerFragment { year, month, day ->
            onDateInitSelected(year, month + 1, day)
        }
        datePicker.show(supportFragmentManager, "datePicker")
    }

    private fun showDateFinPickerDialog() {
        val datePicker = DatePickerFragment { yearFin, monthFin, dayFin ->
            onDateFinSelected(yearFin, monthFin + 1, dayFin)
        }
        datePicker.show(supportFragmentManager, "datePicker")
    }

    private fun onDateInitSelected(year: Int, month: Int, day: Int) {
        dia = day.toString()
        mes = month.toString()
        anio = year.toString()
        if (day < 10)
            dia = "0$dia"
        if (month < 10)
            mes = "0$mes"
        rutinaInputFechaInicio.setHint("$dia/$mes/$anio")
        if (diaFin.isBlank() || mesFin.isBlank() || anioFin.isBlank()) {
            diaFin = dia
            mesFin = mes
            anioFin = anio
            rutinaInputFechaFin.setHint("$dia/$mes/$anio")
        }
    }

    private fun onDateFinSelected(yearFin: Int, monthFin: Int, dayFin: Int) {
        diaFin = dayFin.toString()
        mesFin = monthFin.toString()
        anioFin = yearFin.toString()
        if (dayFin < 10)
            diaFin = "0$diaFin"
        if (monthFin < 10)
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

    private fun mostrarImagenRutina() {
        var fotoRutPath = "$currentUser/images/rutinas/cat_$categoriaActual/rut_$rutinaActual"
        var fotoRutRef = storageReference?.child(fotoRutPath)
        fotoRutRef?.metadata?.addOnSuccessListener { metadata ->
            // mirar si la imagen existe
            val exists = metadata.sizeBytes > 0
            if (exists) {
                GlideApp.with(this)
                    .load(fotoRutRef)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions().override(100, 100))
                    .fitCenter()
                    .centerCrop()
                    .into(crearRutinaImagen)
            } else {
                GlideApp.with(this)
                    .load(R.drawable.arasaac_fotografia)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(RequestOptions().override(100, 100))
                    .fitCenter()
                    .centerCrop()
                    .into(crearRutinaImagen)
            }
        }
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
            crearRutinaImagen?.setImageDrawable(null)
            fotoGaleria = data?.data
            crearRutinaImagen.setImageURI(fotoGaleria)
        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CAMERA) {
            crearRutinaImagen?.setImageDrawable(null)
            crearRutinaImagen.setImageURI(fotoCamara)
        }
    }

    private fun guardarDatosDB(rutinaNombre: String) {
        /*3*/
        if (fotoGaleria != null || fotoCamara != null || (crearRutinaImagen.drawable != null
                    // && crearRutinaImagen.drawable != resources.getDrawable(R.drawable.arasaac_fotografia)
                    )
        ) {
            var fotoRutinaRef =
                storageReference?.child("$currentUser/images/rutinas/cat_$categoriaActual/rut_$rutinaNombre")
            crearRutinaImagen.destroyDrawingCache()
            crearRutinaImagen.buildDrawingCache()
            val bitmap = (crearRutinaImagen.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            fotoRutinaRef?.metadata?.addOnSuccessListener { metadata ->
                // mirar si la imagen existe
                val exists = metadata.sizeBytes > 0
                if (exists) {
                    fotoRutinaRef.delete()
                }
            }
            fotoRutinaRef?.putBytes(data)
        }
        var crearRutinaHelper: CrearRutinaHelper
        if (!anio.isBlank() && !mes.isBlank() && !dia.isBlank()) {
            crearRutinaHelper = CrearRutinaHelper(
                rutinaNombre, DDMMMMYYYYFormatByYearMonthDay(
                    Date(anio.toInt() - 1900, mes.toInt() - 1, dia.toInt())
                ), DDMMMMYYYYFormatByYearMonthDay(
                    Date(anioFin.toInt() - 1900, mesFin.toInt() - 1, diaFin.toInt())
                )
            )
        } else {
            crearRutinaHelper = CrearRutinaHelper(rutinaNombre, "", "")

        }
        if (!rutinaNombre.equals(rutinaActual)) {
            storageReference?.child("$currentUser/images/rutinas/cat_$categoriaActual/rut_$rutinaActual")?.delete()
            reference.child(rutinaActual).removeValue()
        }
        reference.child(rutinaNombre).setValue(crearRutinaHelper)
        goToRutinasActivity()
    }

    fun goToRutinasActivity() {
        intent = Intent(this@EditarRutinaActivity, RutinasActivity::class.java)
        val b: Bundle = Bundle()
        b.putString("categoria", categoriaActual)
        intent.putExtras(b)
        startActivity(intent)
        finish()
    }
    override fun onBackPressed() {
        super.onBackPressed()
        goToRutinasActivity()
    }

}
