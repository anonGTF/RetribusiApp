package id.ptkpn.retribusiapp.ui.history

import androidx.lifecycle.ViewModel
import id.ptkpn.retribusiapp.localdb.TransaksiRepository
import id.ptkpn.retribusiapp.utils.BAKULAN
import id.ptkpn.retribusiapp.utils.PAKAI_KIOS
import id.ptkpn.retribusiapp.utils.PAKAI_MEJA

class HistoryViewModel(
    private val transaksiRepository: TransaksiRepository
): ViewModel() {

    fun getAllTransaksi() = transaksiRepository.getAllTransaksi()

    fun deleteAllTransaksi() = transaksiRepository.deleteAllTransaksi()

    fun countBakulan() = transaksiRepository.countType(BAKULAN)

    fun countPakaiMeja() = transaksiRepository.countType(PAKAI_MEJA)

    fun countPakaiKios() = transaksiRepository.countType(PAKAI_KIOS)

}