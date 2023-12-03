package np.edu.ismt.krishna.suitcasepro.files

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import np.edu.ismt.krishna.suitcasepro.Adaptar.HomeViewModel
import np.edu.ismt.krishna.suitcasepro.R
import np.edu.ismt.krishna.suitcasepro.databinding.ActivityProfileEditBinding
import np.edu.ismt.krishna.suitcasepro.fragments.ProfileFragment

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding:ActivityProfileEditBinding
    private lateinit var viewModel: HomeViewModel
    private var imageUriPath = ""

    private val GALLERY_PERMISSION_REQUEST_CODE = 11

    private val startGalleryActivityForResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageUriPath = uri.toString()
                loadThumbnailImage(imageUriPath)
                saveImageUriPathToPreferences(imageUriPath)
                viewModel.setImageUri(imageUriPath)
            } else {
                imageUriPath = ""
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        // Retrieve data from SharedPreferences
        val sharedPreferences = getSharedPreferences(
            AppConstants.FILE_SHARED_PREF_LOGIN,
            Context.MODE_PRIVATE
        )
        val email = sharedPreferences.getString("email", null)
        val username = sharedPreferences.getString("username", null)
        Log.d("ProfileEditActivity", "Retrieved username from SharedPreferences: $username")
        Log.d("ProfileEditActivity", "Retrieved email from SharedPreferences: $email")

        val savedImageUriPath = sharedPreferences.getString("imageUriPath", null)
        savedImageUriPath?.let { viewModel.setImageUri(it) }

        // Set the data in the ViewModel
        viewModel.setUsername(username)
        viewModel.setEmail(email)
        viewModel.imageUri.observe(this, Observer { imageUri ->
            loadThumbnailImage(imageUri)
        })
        // Observe the data and update UI
        viewModel.username.observe(this, Observer { updatedUsername ->
            binding.username1.text = updatedUsername ?: "N/A"
        })

        viewModel.email.observe(this, Observer { updatedEmail ->
            binding.email1.text = updatedEmail ?: "N/A"
        })

        binding.back.setOnClickListener {
            val intent = Intent(this, ProfileFragment::class.java)
            startActivity(intent)
            finish()
        }
        binding.profilepic.setOnClickListener {
            handleImageAddButtonClicked()
        }
    }
    private fun handleImageAddButtonClicked() {
        val pickImageBottomSheetDialog = BottomSheetDialog(this)
        pickImageBottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_pick_image)

        val linearLayoutPickByGallery: LinearLayout =
            pickImageBottomSheetDialog.findViewById(R.id.ll_pick_by_gallery)!!

        linearLayoutPickByGallery.setOnClickListener {
            pickImageBottomSheetDialog.dismiss()
            startGalleryToPickImage()
        }

        pickImageBottomSheetDialog.setCancelable(true)
        pickImageBottomSheetDialog.show()
    }

    private fun startGalleryToPickImage() {
        if (allPermissionForGalleryGranted()) {
            startGalleryActivityForResult.launch("image/*")
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                    getPermissionsRequiredForGallery().toTypedArray(),
                    GALLERY_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun allPermissionForGalleryGranted(): Boolean {
        val permissions = getPermissionsRequiredForGallery()
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getPermissionsRequiredForGallery(): List<String> {
        val permissions = mutableListOf<String>()
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        return permissions
    }

    private fun loadThumbnailImage(imageUriPath: String?) {
        Glide.with(this)
            .asBitmap()
            .load(Uri.parse(imageUriPath))
            .circleCrop()  // Use circleCrop for a circular image
            .into(binding.profilepic)
    }
    private fun saveImageUriPathToPreferences(imageUriPath: String) {
        val sharedPreferences = getSharedPreferences(
            AppConstants.FILE_SHARED_PREF_LOGIN,
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putString("imageUriPath", imageUriPath)
        editor.apply()
        viewModel.setImageUri(imageUriPath) // Update ViewModel with the image URI

    }
}

