package id.ptkpn.retribusiapp.ui.kontribusipasar

import androidx.lifecycle.ViewModel
import id.ptkpn.retribusiapp.localdb.Transaksi
import id.ptkpn.retribusiapp.localdb.TransaksiRepository
import id.ptkpn.retribusiapp.utils.BAKULAN
import id.ptkpn.retribusiapp.utils.PAKAI_KIOS
import id.ptkpn.retribusiapp.utils.PAKAI_MEJA

class KontribusiPasarViewModel(
    val transaksiRepository: TransaksiRepository
): ViewModel() {

    fun countBakulan() = transaksiRepository.countType(BAKULAN)

    fun countPakaiMeja() = transaksiRepository.countType(PAKAI_MEJA)

    fun countPakaiKios() = transaksiRepository.countType(PAKAI_KIOS)

    fun countAll() = transaksiRepository.countAll()

    fun insertTransaksi(transaksi: Transaksi) {
        transaksiRepository.insertTransaksi(transaksi)
    }
}