package com.raquelgonzalezvillaescusa.kalendaro.Fragments

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment(val listener:(String)-> Unit):DialogFragment(), TimePickerDialog.OnTimeSetListener {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val dialog = TimePickerDialog(activity as Context, this, hour, minute, true)
        return dialog
    }
    // Avisa de cuando se selecciona la hora:
    override fun onTimeSet(view: TimePicker?, hourOfday: Int, minute: Int) {

        if(minute<10){
            if(hourOfday<10)listener("0$hourOfday:0$minute")else listener("$hourOfday:0$minute")

        }else{
            if(hourOfday<10)listener("0$hourOfday:$minute")else listener("$hourOfday:$minute")
        }
    }
}