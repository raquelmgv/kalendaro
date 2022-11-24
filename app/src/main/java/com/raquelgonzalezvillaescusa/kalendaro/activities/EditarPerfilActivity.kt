package com.raquelgonzalezvillaescusa.kalendaro.activities

import android.graphics.PorterDuff
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.raquelgonzalezvillaescusa.kalendaro.R
import com.raquelgonzalezvillaescusa.kalendaro.goToActivity

class EditarPerfilActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance()}
    private lateinit var toolbar: Toolbar

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        toolbar = findViewById(R.id.toolbar)
        setUpToolbar(toolbar)
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
/*        val item_editarPerfil: MenuItem = menu.findItem(R.id.editarPerfil) as MenuItem
        item_editarPerfil.setVisible(false)
        val item_categoria: MenuItem = menu.findItem(R.id.crearCategoria) as MenuItem
        item_categoria.setVisible(false)*/
        return super.onCreateOptionsMenu(menu)
    }

    public override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.editarPerfil -> goToActivity<EditarPerfilActivity> {}
            R.id.logOut -> goToActivity<LoginActivity> {}
        }
        return super.onOptionsItemSelected(item);
    }
}
