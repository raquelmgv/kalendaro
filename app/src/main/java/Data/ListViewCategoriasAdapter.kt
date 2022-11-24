package Data

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.raquelgonzalezvillaescusa.kalendaro.R
import kotlinx.android.synthetic.main.list_row_layout.view.*

class ListViewCategoriasAdapter(private val mContext: Context, private val categoriasRutinasList: MutableList<String>): ArrayAdapter<String>(mContext, 0, categoriasRutinasList) {
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private var currentUser : String = mAuth.uid.toString()
    internal var storage: FirebaseStorage? = null
    internal var storageReference: StorageReference? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        storage = FirebaseStorage.getInstance()
        storageReference = storage!!.reference
        val layout_row = LayoutInflater.from(mContext).inflate(R.layout.list_row_layout, parent,false)
        val categoria = categoriasRutinasList[position]
        Log.w("CATEGORIA", categoria)

        var fotoCategoriaPath = "$currentUser/images/categorias/cat_$categoria"
        var fotoCategoriaRef = storageReference?.child(fotoCategoriaPath)
        GlideApp.with(mContext)
            .load(fotoCategoriaRef)
            .apply(RequestOptions().override(100, 100))
            .fitCenter()
            .centerCrop()
            .into(layout_row.categoriaImagenListView)
        layout_row.categoriaNombreListView.text = categoria
        return layout_row
    }
}
