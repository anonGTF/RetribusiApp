package id.ptkpn.retribusiapp.ui.history

import androidx.lifecycle.ViewModel
import id.ptkpn.retribusiapp.localdb.TransaksiRepository

class HistoryViewModel(
    val transaksiRepository: TransaksiRepository
): ViewModel() {

    fun getAllTransaksi() = transaksiRepository.getAllTransaksi()

    fun deleteAllTransaksi() = transaksiRepository.deleteAllTransaksi()

}