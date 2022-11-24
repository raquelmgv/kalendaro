package Data

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.raquelgonzalezvillaescusa.kalendaro.R
import kotlinx.android.synthetic.main.list_row_rutina_especifica.view.*

class ListViewRutinasEspecificasAdapter(private val mContext: Context, private val rutinasList: MutableList<String>, private val categoriasList: MutableList<String>): ArrayAdapter<String>(mContext, 0, rutinasList) {
    /*1*/
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var currentUser : String = mAuth.uid.toString()
    internal var storage: FirebaseStorage? = null
    internal var storageReference: StorageReference? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        /*2*/
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference

        val layout_row = LayoutInflater.from(mContext).inflate(R.layout.list_row_rutina_especifica, parent,false)
        val rutina = rutinasList[position]
        val categoriaRutina = categoriasList[position]
        /*3*/
        var fotoRutinaPath = "$currentUser/images/rutinas/cat_$categoriaRutina/rut_$rutina"
        var fotoRutinaRef = storageReference?.child(fotoRutinaPath)
        GlideApp.with(mContext)
            .load(fotoRutinaRef)
            .apply(RequestOptions().override(100, 100))
            .fitCenter()
            .centerCrop()
            .into(layout_row.categoriaImagenListView)
        layout_row.rutinaNombreListView.text = rutina
        layout_row.categoriaNombreListView.text = categoriaRutina
        return layout_row
    }
}
