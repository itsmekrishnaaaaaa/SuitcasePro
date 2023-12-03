package np.edu.ismt.krishna.suitcasepro.Adaptar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import np.edu.ismt.krishna.suitcasepro.database.Product

class HomeViewModel : ViewModel() {

    private val _username = MutableLiveData<String>()
    val username: LiveData<String> get() = _username

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> get() = _email

    private val _imageUri = MutableLiveData<String?>()
    val imageUri: LiveData<String?> get() = _imageUri


    private val _purchasedItems = MutableLiveData<List<Product>>()
    val purchasedItems: LiveData<List<Product>> get() = _purchasedItems

    fun setUsername(username: String?) {
        _username.value = username
    }
    fun setEmail(email: String?) {
        _email.value = email
    }
    fun setImageUri(uri: String?) {
        _imageUri.value = uri
    }

    fun setPurchasedItems(items: List<Product>) {
        _purchasedItems.value = items
    }

}
