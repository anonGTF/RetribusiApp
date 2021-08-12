package id.ptkpn.retribusiapp.localdb

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import id.ptkpn.retribusiapp.utils.TRANSAKSI_TABLE

@Entity(
    tableName = TRANSAKSI_TABLE
)
data class Transaksi(

    @PrimaryKey(autoGenerate = true)
    @NonNull
    val id: Int = 0,
    val jenisPedagang: String? = null,
    val jumlahBayar: Int? = null,
    val tanggal: String? = null,
    val waktu: String? = null
)
