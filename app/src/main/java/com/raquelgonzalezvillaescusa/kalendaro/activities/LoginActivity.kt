package com.raquelgonzalezvillaescusa.kalendaro.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.raquelgonzalezvillaescusa.kalendaro.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.popup_delete_item.view.*


class LoginActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance()}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            if (!isValidEmail(email)){
                editTextEmail.error = "La direccion de correo no es válida"
            }
            if(!isValidPassword(password)){
                editTextPassword.error = "Formato de contraseña incorrecto"
            }
            if(isValidEmail(email) && isValidPassword(password)){
                loginByEmail(email, password)
            }
        }

        textViewForgotPassword.setOnClickListener {
            goToActivity<ForgotPasswordActivity>{}
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        buttonCreateAccount.setOnClickListener {
            goToActivity<SignUpActivity>{}
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }

    private fun loginByEmail(email:String, password:String){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                if(mAuth.currentUser!!.isEmailVerified){
                        goToActivity<DiaActual>{}
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                        toast("Se ha iniciado sesión con la dirección de correo: $email")
                }else{
                    toast("El usuario debe confirmar el email antes de iniciar sesión")
                }
            } else {
                toast("Ha ocurrido un error, por favor, confirme que los datos son correctos.")
            }
        }
    }
}
