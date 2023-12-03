package np.edu.ismt.krishna.suitcasepro.database

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "user_table")
@Parcelize
data class User(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    val fullName: String,
    val email: String,
    val password: String,
    val conformPassword: String,

): Parcelable {
    constructor(
        fullName: String,
        email: String,
        password: String,
        conformPassword: String
    ): this(
        0,
        fullName,
        email,
        password,
        conformPassword
    )
}
