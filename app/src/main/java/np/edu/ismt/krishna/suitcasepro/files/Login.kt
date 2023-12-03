package np.edu.ismt.krishna.suitcasepro.files

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Login(
    val email: String,
    val password: String
) : Parcelable
