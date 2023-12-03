package np.edu.ismt.krishna.suitcasepro.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [User::class, Product::class], version = 1)
abstract class TestDatabase: RoomDatabase() {
    abstract fun getUserDao(): UserDao


    abstract fun getProductDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: TestDatabase? = null

        fun getInstance(context: Context): TestDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context,
                    TestDatabase::class.java,
                    "test.db"
                ).build()
            }
            return INSTANCE!!
        }
    }
}