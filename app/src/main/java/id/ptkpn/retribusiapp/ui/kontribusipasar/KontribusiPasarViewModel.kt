package id.ptkpn.retribusiapp.ui.kontribusipasar

import androidx.lifecycle.ViewModel
import id.ptkpn.retribusiapp.localdb.Transaksi
import id.ptkpn.retribusiapp.localdb.TransaksiRepository

class KontribusiPasarViewModel(
    val transaksiRepository: TransaksiRepository
): ViewModel() {

    fun insertTransaksi(transaksi: Transaksi) {
        transaksiRepository.insertTransaksi(transaksi)
    }
}