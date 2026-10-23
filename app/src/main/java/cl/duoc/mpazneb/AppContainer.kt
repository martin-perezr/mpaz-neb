package cl.duoc.mpazneb

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import cl.duoc.mpazneb.data.local.AppDatabase
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.AsignacionRepository
import cl.duoc.mpazneb.data.repository.AuthRepository
import cl.duoc.mpazneb.data.sesion.SessionManager
import cl.duoc.mpazneb.device.AlmacenImagenes

/**
 * Inyección de dependencias manual: aquí se crean una sola vez la base de datos
 * y los repositorios, y los ViewModel los piden desde su Factory.
 */
class AppContainer(context: Context) {
    private val db = AppDatabase.obtener(context)
    private val sesion = SessionManager(context)

    val authRepository = AuthRepository(db.usuarioDao(), sesion)
    val actividadRepository = ActividadRepository(db.actividadDao(), authRepository)
    val asignacionRepository = AsignacionRepository(db.asignacionDao(), db.actividadDao(), authRepository)
    val almacenImagenes = AlmacenImagenes(context)
}

/** Atajo para usar dentro de `viewModelFactory { initializer { ... } }`. */
fun CreationExtras.contenedor(): AppContainer =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MpazNebApp).container
