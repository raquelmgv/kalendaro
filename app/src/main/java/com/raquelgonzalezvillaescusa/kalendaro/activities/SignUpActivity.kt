package com.raquelgonzalezvillaescusa.kalendaro.activities
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.raquelgonzalezvillaescusa.kalendaro.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.activity_sign_up.editTextEmail
import kotlinx.android.synthetic.main.activity_sign_up.editTextPassword


class SignUpActivity : AppCompatActivity() {
    private val mAuth: FirebaseAuth by lazy {FirebaseAuth.getInstance()}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        buttonGoLogin.setOnClickListener {
            goToActivity<LoginActivity>{
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        buttonSignUp.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val confirmPassword = editTextConfirmPassword.text.toString()
            if (!isValidEmail(email)){
                editTextEmail.error = "La direccion de correo no es válida"
            }
            if(!isValidPassword(password)){
                editTextPassword.error = "La contraseña ha de tener 8 caracteres, al menos un numero, una mayúscula, una minúscula y un signo especial"
            }
            if(!isValidConfirmPassword(password, confirmPassword)){
                editTextConfirmPassword.error = "Las contraseñas no coinciden"
            }
            if(isValidEmail(email) && isValidPassword(password) && isValidConfirmPassword(password, confirmPassword)){
                signUpByEmail(email, password)
            }
        }
    }

    private fun signUpByEmail(email:String, password:String){

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    mAuth.currentUser!!.sendEmailVerification().addOnCompleteListener(this) {
                        toast("Se ha enviado un email a su dirección de correo electrónico, por favor, confirme antes de iniciar sesión y revise su carpeta Spam.")
                        goToActivity<LoginActivity>{
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                } else {
                    toast("Ha ocurrido un error inesperado, por favor, intentelo de nuevo.")
                }
            }
    }

}
