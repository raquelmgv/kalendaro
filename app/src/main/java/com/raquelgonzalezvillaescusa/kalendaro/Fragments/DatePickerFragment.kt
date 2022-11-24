package com.raquelgonzalezvillaescusa.kalendaro.Fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment(val listener: (day:Int, month:Int, year:Int)->Unit): DialogFragment(), DatePickerDialog.OnDateSetListener {
    /*Función que se ejecuta cuando alguien selecciona una fecha (nos devuelve el dia mes y año seleccionados)*/
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        listener(year, month, day) /*Este listener ejecuta el código de la funcion en CrearRutinaActivity (showDatePickerDialog())*/
    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c: Calendar = Calendar.getInstance()
        val day: Int = c.get(Calendar.DAY_OF_MONTH)
        val month: Int = c.get(Calendar.MONTH)
        val year: Int = c.get(Calendar.YEAR)
        val picker = DatePickerDialog(activity as Context, this, year, month, day)
        return picker
    }
}