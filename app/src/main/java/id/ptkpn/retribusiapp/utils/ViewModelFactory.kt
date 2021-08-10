package id.ptkpn.retribusiapp.utils

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import id.ptkpn.retribusiapp.localdb.TransaksiRepository
import id.ptkpn.retribusiapp.ui.history.HistoryViewModel
import id.ptkpn.retribusiapp.ui.kontribusipasar.KontribusiPasarViewModel

class ViewModelFactory private constructor(private val transaksiRepository: TransaksiRepository) :
    ViewModelProvider.Factory{

    companion object {
        @Volatile
        private var instance: ViewModelFactory? = null

        fun getInstance(context: Context): ViewModelFactory =
            instance ?: synchronized(this) {
                instance ?: ViewModelFactory(
                    TransaksiRepository.getInstance(context)
                )
            }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(KontribusiPasarViewModel::class.java) -> {
                KontribusiPasarViewModel(transaksiRepository) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(transaksiRepository) as T
            }
            else -> throw Throwable("Unknown ViewModel class: " + modelClass.name)
        }
}