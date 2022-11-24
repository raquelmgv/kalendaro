package com.raquelgonzalezvillaescusa.kalendaro

import android.app.Activity
import android.content.Intent
import android.util.Patterns
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import java.util.regex.Pattern
import androidx.appcompat.widget.Toolbar

fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_LONG) = Toast.makeText(this, message, duration).show()
fun Activity.toast(resourceId: Int, duration: Int = Toast.LENGTH_LONG) = Toast.makeText(this, resourceId, duration).show()
fun ViewGroup.inflate(layoutId: Int) = LayoutInflater.from(context).inflate(layoutId, this,false)!!
//fun ImageView.loadByURL(url:String) = Picasso.get().load(url).into(this)
//fun ImageView.loadByResource(resource: Int) = Picasso.get().load(resource).fit().into(this)

inline fun <reified T : Activity> Activity.goToActivity(noinline init: Intent.() -> Unit = {}){
    val intent = Intent(this, T::class.java)
    intent.init()
    startActivity(intent)
}
fun Activity.isValidEmail(email: String): Boolean{
    val pattern = Patterns.EMAIL_ADDRESS
    return pattern.matcher(email).matches()
}
fun Activity.isValidPassword(password: String): Boolean{
    // Necesita contener AL MENOS -> 1 num/1 minuscula/1 mayuscula/1 Special/minimo 8 caracteres
    val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    val pattern = Pattern.compile(passwordPattern)
    return pattern.matcher(password).matches()
}
fun Activity.isValidConfirmPassword(password: String, confirmPassword: String): Boolean{
    return password == confirmPassword
}

fun Activity.isValidName(name: String): Boolean{
    // letras y numeros maximo 20 digitos minimo 1 a-zA-ZñÑáéíóúÁÉÍÓÚ\s
    //    val namePattern = "^[a-zA-Z0-9_ñÑáéíóúÁÉÍÓÚ\\s\\W]+([ a-zA-Z0-9_ñÑáéíóúÁÉÍÓÚ\\s\\W]+){1,20}\$"
    val namePattern = "^[ a-zA-Z0-9_ñÑáéíóúÁÉÍÓÚ\\s\\W]{1,20}\$"
    val pattern = Pattern.compile(namePattern)
    return pattern.matcher(name).matches()
}

fun Activity.isValidHour(hour: String): Boolean{
    //Horario24h ^([01]?[0-9]|2[0-3]):[0-5][0-9]$
    //Horario12h:^(?:0?[1-9]|1[0-2]):[0-5][0-9]\s?(?:[aApP](\.?)[mM]\1)
    val namePattern = "^([01]?[0-9]|2[0-3]):[0-5][0-9]\$"
    val pattern = Pattern.compile(namePattern)
    return pattern.matcher(hour).matches()
}

