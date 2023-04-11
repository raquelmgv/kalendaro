package Data

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.raquelgonzalezvillaescusa.kalendaro.R
import kotlinx.android.synthetic.main.activity_dia_actual.*
import kotlinx.android.synthetic.main.list_actividades_row.view.*
import java.text.SimpleDateFormat
import java.util.*

class ListViewActividadesAdapter(private val mContext: Context, private val actividadesHourList: MutableList<String>,
                                 private val actividadesNameList : MutableList<String>, private val rutina: String,
                                 private val categoria: String): ArrayAdapter<String>(mContext, 0, actividadesHourList)  {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var currentUser : String = mAuth.uid.toString()
    private lateinit var hora_actividad_reference : DatabaseReference
    internal var storage: FirebaseStorage? = null
    internal var storageReference: StorageReference? = null


    @SuppressLint("ResourceAsColor")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        /*2*/
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        val layout_row = LayoutInflater.from(mContext).inflate(R.layout.list_actividades_row, parent,false)
        val actividad_hora = actividadesHourList[position] //hora de la actividad (PK)
        val actividad_nombre = actividadesNameList[position]
        Log.w("ACTIVIDAD_HORA_POSITION", actividad_hora)
        /*3*/
        var fotoActividadPath = "$currentUser/images/rutina_details/cat_$categoria/rut_$rutina/act_$actividad_hora"
        var fotoActividadRef = storageReference?.child(fotoActividadPath)

        fotoActividadRef?.metadata?.addOnSuccessListener { metadata ->
            // mirar si la imagen existe
            val exists = metadata.sizeBytes > 0
            if (exists) {
                GlideApp.with(mContext)
                    .load(fotoActividadRef)
                    .apply(RequestOptions().override(100, 100))
                    .fitCenter()
                    .centerCrop()
                    .into(layout_row.actividad_imagen)
            } else { // si la imagen no existe pongo la imagen por defecto
                GlideApp.with(mContext)
                    .load(R.drawable.arasaac_fotografia)
                    .apply(RequestOptions().override(100, 100))
                    .fitCenter()
                    .centerCrop()
                    .into(layout_row.actividad_imagen)
            }
        }

        layout_row.actividad_display_hora.text = actividad_hora
        layout_row.actividad_display_name.text =  actividad_nombre
        /*PONER FONDO MORADO SEGUN LA HORA*/
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        /*if("$hour:$minute" <= actividad_hora){
            /*TODO************** horas marcar actividad*/
            Log.w("HORA***", "$hour:$minute ------- ${layout_row.actividad_display_hora.text}")
            layout_row.actividad_display_hora.setBackgroundColor(R.color.colorGooglePlus)
        }*/

        return layout_row
    }

    fun displayHoraActual(): String {
        val date = System.currentTimeMillis()
        val sdf = SimpleDateFormat("HHmm")
        val dateString: String = sdf.format(date)
        return dateString
    }
}