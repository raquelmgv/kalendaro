package com.raquelgonzalezvillaescusa.kalendaro.activities

import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.raquelgonzalezvillaescusa.kalendaro.*
import com.raquelgonzalezvillaescusa.kalendaro.R
import kotlinx.android.synthetic.main.activity_graficas_banio.*


class GraficasBanioActivity : AppCompatActivity() {
    val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance()}
    var currentUser : String = mAuth.uid.toString()
    private lateinit var toolbar: Toolbar

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graficas_banio)
        displayConceptualMenu()
        toolbar = findViewById(R.id.toolbar)
        setUpToolbar(toolbar)

        button_graficaMesActual.setOnClickListener {
            goToActivity<GraficaBanioMesActivity>()
        }
        button_graficaMeses.setOnClickListener {
            goToActivity<GraficaBanioMesesActivity>()
        }
        button_graficaAnios.setOnClickListener {
            goToActivity<GraficaBanioAniosActivity>()
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpToolbar(toolbar: Toolbar){
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle("")
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.logOut -> goToActivity<LoginActivity> {}
        }
        return super.onOptionsItemSelected(item);
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun displayConceptualMenu(){
        conceptualMenuIconsBar.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.calendario -> { goToActivity<CalendarActivity>{} }
                R.id.rutinas -> { goToActivity<CategoriasActivity> {} }
                R.id.diaActual -> { goToActivity<DiaActual> {} }
            }
            true }
    }
}
