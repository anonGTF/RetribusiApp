package id.ptkpn.retribusiapp.localdb

import android.content.Context
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TransaksiRepository(private val transaksiDao: TransaksiDao, private val executor: ExecutorService) {

    companion object {

        @Volatile
        private var instance: TransaksiRepository? = null

        fun getInstance(context: Context): TransaksiRepository {
            return instance ?: synchronized(this) {
                if (instance == null) {
                    val database = TransaksiDatabase.getInstance(context)
                    instance = TransaksiRepository(
                        database.transaksiDao(),
                        Executors.newSingleThreadExecutor()
                    )
                }
                return instance as TransaksiRepository
            }

        }
    }

    fun getAllTransaksi() = transaksiDao.getAllTransaksi()

    fun countType(type: String) = transaksiDao.countType(type)

    fun countAll() = transaksiDao.countAll()

    fun insertTransaksi(transaksi: Transaksi) {
        executor.execute {
            transaksiDao.insert(transaksi)
        }
    }

    fun deleteAllTransaksi() {
        executor.execute {
            transaksiDao.deleteAll()
        }
    }
}