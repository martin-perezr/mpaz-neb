package cl.duoc.mpazneb

import android.app.Application

class MpazNebApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
