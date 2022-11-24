package com.raquelgonzalezvillaescusa.kalendaro.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthSettings
import com.raquelgonzalezvillaescusa.kalendaro.R
import com.raquelgonzalezvillaescusa.kalendaro.goToActivity
import com.raquelgonzalezvillaescusa.kalendaro.isValidEmail
import com.raquelgonzalezvillaescusa.kalendaro.toast
import kotlinx.android.synthetic.main.activity_forgot_password.*

class ForgotPasswordActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        buttonGoLogin.setOnClickListener {
            goToActivity<LoginActivity>{
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        buttonForgot.setOnClickListener {
            val email = editTextEmail.text.toString()
            if(isValidEmail(email)){
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(this) {
                    toast("Un email ha sido envíado a su direccion de correo para resetear la contraseña")
                    goToActivity<LoginActivity>{
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }else{
                editTextEmail.error = "La direccion de correo no es válida"
            }
        }
    }
}
