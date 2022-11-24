package com.raquelgonzalezvillaescusa.kalendaro.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.raquelgonzalezvillaescusa.kalendaro.R
import com.raquelgonzalezvillaescusa.kalendaro.goToActivity
import kotlinx.android.synthetic.main.activity_dia_actual.*

class RutinaActualActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rutina_actual)

        displayConceptualMenu()
    }

    private fun displayConceptualMenu(){
        conceptualMenuIconsBar.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.diaActual -> { goToActivity<DiaActual> {} }
                R.id.calendario -> { goToActivity<CalendarActivity>{} }
                R.id.rutinas -> { goToActivity<CategoriasActivity> {} }
            }
            true }
    }
}


