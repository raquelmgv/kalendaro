package com.raquelgonzalezvillaescusa.kalendaro

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_dia_actual.*
import com.google.firebase.storage.StorageReference
import com.raquelgonzalezvillaescusa.kalendaro.activities.RutinasActivity
import kotlinx.android.synthetic.main.popup_delete_item.view.*
import java.text.SimpleDateFormat
import java.util.*


fun Activity.displayBarActualView(): String {
    val date = System.currentTimeMillis()
    val sdf = SimpleDateFormat("dd MMMM yyyy")
    val dateString: String = sdf.format(date)
    barActualView.setText(dateString)
    return dateString
}

 fun Activity.DDMMMMYYYYFormatByYearMonthDay (date: Date) : String{
    val sdf = SimpleDateFormat("dd MMMM yyyy")
    val dateString: String = sdf.format(date)
    return dateString
}

/**
 * Convertir la fecha alfabetica en numérica con el fin de situar las rutinas en el calendario
 */
fun Activity.ConvertDateBarViewToYYYYMMDD (fecha: String) : String{
    var anioNumber = fecha.split(" ")[2]
    var monthNumber = convertStringMonthToInt(fecha.split(" ")[1]);
    var diaNumber = fecha.split(" ")[0]
    var fechaNumber = "${anioNumber}${monthNumber}${diaNumber}"
    return fechaNumber
}

fun Activity.displayCustomFace(faceNumber : Int) {
    when (faceNumber) {
        1 -> {
            customButton_feliz.setBackgroundResource(R.drawable.boton_cara_feliz_pulsado)
            customButton_normal.setBackgroundResource(R.drawable.boton_cara_normal)
            customButton_triste.setBackgroundResource(R.drawable.boton_cara_triste)
        }

        2 -> {
            customButton_normal.setBackgroundResource(R.drawable.boton_cara_normal_pulsado)
            customButton_feliz.setBackgroundResource(R.drawable.boton_cara_feliz)
            customButton_triste.setBackgroundResource(R.drawable.boton_cara_triste)
        }
        3 -> {
            customButton_triste.setBackgroundResource(R.drawable.boton_cara_triste_pulsado)
            customButton_feliz.setBackgroundResource(R.drawable.boton_cara_feliz)
            customButton_normal.setBackgroundResource(R.drawable.boton_cara_normal)
        }
        else -> {
            customButton_feliz.setBackgroundResource(R.drawable.boton_cara_feliz)
            customButton_normal.setBackgroundResource(R.drawable.boton_cara_normal)
            customButton_triste.setBackgroundResource(R.drawable.boton_cara_triste)
        }
    }
}

fun Activity.displayCustomComer(comerState : Int) {
    when (comerState) {
        1 -> {
            customButton_comer.setBackgroundResource(R.drawable.diaactual_arasaac_comer_1)
        }
        2 -> {
            customButton_comer.setBackgroundResource(R.drawable.diaactual_arasaac_comer_2)
        }
        3 -> {
            customButton_comer.setBackgroundResource(R.drawable.diaactual_arasaac_comer_3)
        }
        else -> {
            customButton_comer.setBackgroundResource(R.drawable.diaactual_arasaac_comer)
        }
    }
}

fun Activity.displayCustomBanio(banioState : Int) {
    when (banioState) {
        1 -> {
            customButton_banio.setBackgroundResource(R.drawable.diaactual_arasaac_ir_al_banio_1)
        }
        2 -> {
            customButton_banio.setBackgroundResource(R.drawable.diaactual_arasaac_ir_al_banio_2)
        }
        3 -> {
            customButton_banio.setBackgroundResource(R.drawable.diaactual_arasaac_ir_al_banio_3)
        }
        else -> {
            customButton_banio.setBackgroundResource(R.drawable.diaactual_arasaac_ir_al_banio)
        }
    }
}

fun Activity.displayCustomDormir(dormirState : Int) {
    when (dormirState) {
        1 -> {
            customButton_dormir.setBackgroundResource(R.drawable.diaactual_arasaac_dormir_1)
        }
        2 -> {
            customButton_dormir.setBackgroundResource(R.drawable.diaactual_arasaac_dormir_2)
        }
        3 -> {
            customButton_dormir.setBackgroundResource(R.drawable.diaactual_arasaac_dormir_3)
        }
        else -> {
            customButton_dormir.setBackgroundResource(R.drawable.diaactual_arasaac_dormir)
        }
    }
}


fun Activity.deleteActivitiesDirectoryStorage(rootNode : FirebaseDatabase, storageReference: StorageReference?,
                                                 rutName: String, currentUser: String, selectedCategory: String){
    var rutinasReference: DatabaseReference =  rootNode.getReference("$currentUser/categoriasRutinaData/$selectedCategory/rutinas")
    rutinasReference.addValueEventListener(object: ValueEventListener {
        override fun onCancelled(error: DatabaseError) {}
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                for (rut in snapshot.children) {
                    for(i in rut.children){
                        for(j in i.children) {
                            val actividad_hora = j.key.toString()
                            var fotosActividadPath = "$currentUser/images/rutina_details/cat_$selectedCategory/rut_$rutName/act_$actividad_hora"
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
fun Activity.reloadActivity(context: Context, data: String?, putStringId: String?){
    intent = Intent(context, RutinasActivity::class.java)
    val b: Bundle = Bundle()
    if(data != null && putStringId != null){
        b.putString(putStringId, data)
        intent.putExtras(b)
    }
    startActivity(intent)
}

fun Activity.convertStringMonthToInt(mes: String) : Int{
    var fechaCompleta: String
    var nombreMes : Int
    when (mes) {
        "enero" -> {nombreMes = 1}
        "febrero" -> {nombreMes = 2}
        "marzo" -> {nombreMes = 3}
        "abril" -> {nombreMes = 4}
        "mayo" -> {nombreMes = 5}
        "junio" -> {nombreMes = 6}
        "julio" -> {nombreMes = 7}
        "agosto" -> {nombreMes = 8}
        "septiembre" -> {nombreMes = 9}
        "octubre" -> {nombreMes = 10}
        "noviembre" -> {nombreMes = 11}
        else-> {nombreMes = 12}
    }
    return nombreMes
}

fun Activity.showRutinaDeleteView(mContext: Context, rootNode : FirebaseDatabase, storageReference: StorageReference?,
                            rutName: String, currentUser: String, catName: String, reference: DatabaseReference) {
    val mDialogView = LayoutInflater.from(mContext).inflate(R.layout.popup_delete_item, null)
    val mBuilder = AlertDialog.Builder(mContext).setView(mDialogView).setTitle(rutName)
    mDialogView.textView_popup_delete.setText(R.string.dialog_delete_rut)
    val mAlertDialog = mBuilder.show()
    mDialogView.button_cancelar.setOnClickListener { mAlertDialog.dismiss() }
    mDialogView.button_eliminar.setOnClickListener {
        deleteActivitiesDirectoryStorage(
            rootNode, storageReference,
            rutName, currentUser, catName
        )
        reference.child(rutName).removeValue()
        /*remove del storage*/
        var fotoRutinaPath = "$currentUser/images/rutinas/cat_$catName/rut_$rutName"
        var fotoRutinaRef = storageReference?.child(fotoRutinaPath)
        if (fotoRutinaRef != null) {
            fotoRutinaRef.delete()
        };
        mAlertDialog.dismiss()
        reloadActivity(mContext, null, null)
    }


}