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
- AÑADIR FECHA DE FIN A LAS RUTINAS (opcional) para que se añadan a varios dias
-- AÑADIR GIT AL PROYECTO
- Gráfica de ultimos 5 años (Regular)
- Cambiar icono de foto uno para foto categorias otro para rutinas otro para actividades,(hechos por mi) ahora sale negro cuando no metemos foto png (recuperar codigo anterior si no)

-------------

- Poder marcar si se ha realizado bien una actividad
- Añadir icono de "No content" en las listas o paginas sin contenido
- Poder eliminar una rutina del dia actual (y probar bien la eliminacion de rutinas que se llaman igual en distintas categorias)
- Poder editar una categoria y una rutina (horas titulo etc)
- Cambiar el botón de back por el de inicio que lleve al dia actual o quitarlo mejor (o hacerlo bien con el bundle)
- Añadir recuadro de texto de anotación en dia actual (boton ver anotaciones o recuadro de texto en la lista de rutinas).

- Al clickar en una actividad poder marcar si esta bien hecha mal hecha o regurlar
    Y poder eliminarla pero no editarla.

- Redirigir la galeria a un album solo de fotos TFG
- Marcar los días del calendario donde hay rutinas creadas (haciendo otro objeto json de fechas solo para inicializar el calendario)

*******P8*********
-TESTS CAJA NEGRA ETC (junit o tests con firebase)
- Al cambiar la contraseña te deja meter la que sea sin controles (mirar en firebase si se puede configurar unas normas)
- (No muy importante, ya que funciona el boton de atras del movil) Rutas boton back (del calendario vuelve siempre al dia actual, debe volver a donde viene)
-
* */


/**ERRORES IMPORTANTES**/
/*Si no me reconoce el código hago file-> sync project with gradle files*/