package id.ptkpn.retribusiapp.localdb

import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import id.ptkpn.retribusiapp.utils.TRANSAKSI_DB

@Database(
    entities = [Transaksi::class],
    version = 1,
    exportSchema = false
)
abstract class TransaksiDatabase : RoomDatabase() {
    abstract fun transaksiDao(): TransaksiDao

    companion object {

        @Volatile
        private var INSTANCE: TransaksiDatabase? = null

        fun getInstance(context: Context): TransaksiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TransaksiDatabase::class.java,
                    TRANSAKSI_DB
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}