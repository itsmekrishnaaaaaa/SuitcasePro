package np.edu.ismt.krishna.suitcasepro.files

import android.content.Context
import android.widget.Toast

class Uiutiity {
    companion object {
        fun showToast(context: Context?, message: String) {
            context?.apply {
                Toast.makeText(this.applicationContext, message, Toast.LENGTH_LONG).show()
            }
        }
    }
}