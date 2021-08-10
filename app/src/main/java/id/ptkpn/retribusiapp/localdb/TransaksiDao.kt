package id.ptkpn.retribusiapp.localdb

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TransaksiDao {

    @Query("SELECT * FROM transaksi")
    fun getAllTransaksi(): LiveData<List<Transaksi>>

    @Query("SELECT COUNT(id) FROM transaksi WHERE jenisPedagang = :type ")
    fun countType(type: String): LiveData<Int>

    @Query("SELECT COUNT(id) FROM transaksi")
    fun countAll(): LiveData<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(transaksi: Transaksi): Long

    @Query("DELETE FROM transaksi")
    fun deleteAll()
}