package com.raquelgonzalezvillaescusa.kalendaro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
/*
TODO Cosas a corregir
* No se pueden poner imágenes que sean PNG IMPORTANTE.

*******P0*********
---- OK -----
- Crear en dia actual la opcion de añadir rutina (que lleve a categorias-> rutinas-> actividad (fecha por defecto la de hoy))--- OKIII (testear bien)
-------------
- Poder eliminar una rutina del dia actual (y probar bien la eliminacion de rutinas que se llaman igual en distintas categorias)
- Cambiar el botón de back por el de inicio que lleve al dia actual o quitarlo mejor (o hacerlo bien con el bundle)
- Añadir recuadro de texto de anotación en dia actual.

- Al clickar en una actividad ver sus detalles (pudiendo editar los campos)
    - Hora
    - Titulo
    - Foto

- Redirigir la galeria a un album solo de fotos TFG

*******P8*********
-TESTS CAJA NEGRA ETC (junit o tests con firebase)
-- AÑADIR GIT AL PROYECTO
- Al cambiar la contraseña te deja meter la que sea sin controles (mirar en firebase si se puede configurar unas normas)
- (No muy importante, ya que funciona el boton de atras del movil) Rutas boton back (del calendario vuelve siempre al dia actual, debe volver a donde viene)
-
* */