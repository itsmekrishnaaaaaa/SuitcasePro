package np.edu.ismt.krishna.suitcasepro.files

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import np.edu.ismt.krishna.suitcasepro.R
import np.edu.ismt.krishna.suitcasepro.database.Product
import np.edu.ismt.krishna.suitcasepro.database.TestDatabase
import np.edu.ismt.krishna.suitcasepro.databinding.ActivityAddorUpdateBinding
import java.io.IOException

class AddorUpdateActivity : AppCompatActivity() {
    private lateinit var addOrUpdateItemBinding: ActivityAddorUpdateBinding
    private var receivedProduct: Product? = null
    private var isForUpdate = false
    private var imageUriPath = ""
    private var productLocation = ""

    private lateinit var startCustomCameraActivityForResult: ActivityResultLauncher<Intent>
    private lateinit var startGalleryActivityForResult: ActivityResultLauncher<Array<String>>
    private lateinit var startMapActivityForResult: ActivityResultLauncher<Intent>

    companion object {
        const val RESULT_CODE_COMPLETE = 1001
        const val RESULT_CODE_CANCEL = 1002
        const val GALLERY_PERMISSION_REQUEST_CODE = 11
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addOrUpdateItemBinding = ActivityAddorUpdateBinding.inflate(layoutInflater)
        setContentView(addOrUpdateItemBinding.root)
        bindCustomCameraActivityForResult()
        bindGalleryActivityForResult()
        bindMapsActivityForResult()

        receivedProduct = intent.getParcelableExtra(AppConstants.KEY_PRODUCT)
        receivedProduct?.apply {
            isForUpdate = true
            addOrUpdateItemBinding.tieTitle.setText(this.title)
            addOrUpdateItemBinding.tiePrice.setText(this.price)
            addOrUpdateItemBinding.tieDescription.setText(this.description)
            loadThumbnailImage(this.image!!)
        }

        addOrUpdateItemBinding.ibBack.setOnClickListener {
            setResultWithFinish(RESULT_CODE_CANCEL, null)
        }

        addOrUpdateItemBinding.ibAddImage.setOnClickListener {
            handleImageAddButtonClicked()
        }

        addOrUpdateItemBinding.mbLocation.setOnClickListener {
            startMapActivity()
        }

        addOrUpdateItemBinding.mbSubmit.setOnClickListener {
            val title = addOrUpdateItemBinding.tieTitle.text.toString().trim()
            val price = addOrUpdateItemBinding.tiePrice.text.toString().trim()
            val description = addOrUpdateItemBinding.tieDescription.text.toString().trim()
            val location = addOrUpdateItemBinding.mbLocation.text.toString().trim()
            if (title.isEmpty() && description.isEmpty() && location.isEmpty() && price.isEmpty() && imageUriPath.isEmpty()) {
                showToast("Fields cannot be empty")
                return@setOnClickListener
            }

            // Validate title
            if (title.isEmpty()) {
                showToast("Title cannot be empty")
                return@setOnClickListener
            }

            // Validate price
            if (price.isEmpty()) {
                showToast("Price cannot be empty")
                return@setOnClickListener
            }

            // Validate description
            if (description.isEmpty()) {
                showToast("Description cannot be empty")
                return@setOnClickListener
            }

            // Validate image URI
            if (imageUriPath.isEmpty()) {
                showToast("Please provide an image")
                return@setOnClickListener
            }

            // Validate location
            if (location.isEmpty()) {
                showToast("Location cannot be empty")
                return@setOnClickListener
            }

            val product = Product(
                title,
                price,
                description,
                imageUriPath,
                productLocation,

            )

            val testDatabase = TestDatabase.getInstance(applicationContext)
            val productDao = testDatabase.getProductDao()

            Thread {
                try {
                    if (isForUpdate) {
                        product.id = receivedProduct!!.id
                        productDao.updateProduct(product)
                    } else {
                        productDao.insertNewProduct(product)
                    }
                    runOnUiThread {
                        clearFieldsData()
                        showToast("Product added successfully...")
                        setResultWithFinish(RESULT_CODE_COMPLETE, product)
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    runOnUiThread {
                        showToast("Couldn't add product. Try again...")
                    }
                }
            }.start()
        }

    }

    private fun showToast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun clearFieldsData() {
        addOrUpdateItemBinding.tieTitle.text?.clear()
        addOrUpdateItemBinding.tiePrice.text?.clear()
        addOrUpdateItemBinding.tieDescription.text?.clear()
    }

    private fun setResultWithFinish(resultCode: Int, product: Product?) {
        val intent = Intent()
        intent.putExtra(AppConstants.KEY_PRODUCT, product)
        setResult(resultCode, intent)
        finish()
    }

    private fun handleImageAddButtonClicked() {
        val pickImageBottomSheetDialog = BottomSheetDialog(this)
        pickImageBottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_pick_image)

        val linearLayoutPickByCamera: LinearLayout = pickImageBottomSheetDialog
            .findViewById(R.id.ll_pick_by_camera)!!
        val linearLayoutPickByGallery: LinearLayout = pickImageBottomSheetDialog
            .findViewById(R.id.ll_pick_by_gallery)!!

        linearLayoutPickByCamera.setOnClickListener {
            pickImageBottomSheetDialog.dismiss()
            startCameraActivity()
        }
        linearLayoutPickByGallery.setOnClickListener {
            pickImageBottomSheetDialog.dismiss()
            startGalleryToPickImage()
        }

        pickImageBottomSheetDialog.setCancelable(true)
        pickImageBottomSheetDialog.show()
    }

    private fun startCameraActivity() {
        val intent = Intent(this, CustomCameraActivity::class.java)
        startCustomCameraActivityForResult.launch(intent)
    }

    private fun allPermissionForGalleryGranted(): Boolean {
        var granted = false
        for (permission in getPermissionsRequiredForCamera()) {
            if (ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED
            ) {
                granted = true
            }
        }
        return granted
    }

    private fun getPermissionsRequiredForCamera(): List<String> {
        val permissions: MutableList<String> = ArrayList()
        permissions.add(Manifest.permission.CAMERA)
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return permissions
    }

    private fun startGalleryToPickImage() {
        if (allPermissionForGalleryGranted()) {
            startActivityForResultFromGalleryToPickImage()
        } else {
            requestPermissions(
                getPermissionsRequiredForCamera().toTypedArray(),
                GALLERY_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun startActivityForResultFromGalleryToPickImage() {
        val intent = Intent(
            Intent.ACTION_OPEN_DOCUMENT,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
//        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startGalleryActivityForResult.launch(arrayOf("image/*"))
    }

    private fun loadThumbnailImage(imageUriPath: String) {
        addOrUpdateItemBinding.ibAddImage.post(Runnable {
            var bitmap: Bitmap?
            try {
                bitmap = MediaStore.Images.Media.getBitmap(
                    contentResolver,
                    Uri.parse(imageUriPath)
                )
                bitmap = BitmapScalar.stretchToFill(
                    bitmap,
                    addOrUpdateItemBinding.ibAddImage.width,
                    addOrUpdateItemBinding.ibAddImage.height
                )
                addOrUpdateItemBinding.ibAddImage.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        })
    }

    private fun startMapActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        startMapActivityForResult.launch(intent)
    }

    private fun onLocationDataFetched() {
        if (productLocation.isBlank()) {
            return
        }

        try {
            val lat = productLocation.split(",")[0]
            val lng = productLocation.split(",")[1]
            val geoCodedAddress = GeoCoding.reverseTheGeoCodeToAddress(this, lat, lng)
            addOrUpdateItemBinding.mbLocation.setText(geoCodedAddress)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        productLocation.isNotBlank().apply {

        }
    }

    private fun bindCustomCameraActivityForResult() {
        startCustomCameraActivityForResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == CustomCameraActivity.CAMERA_ACTIVITY_SUCCESS_RESULT_CODE) {
                imageUriPath = it.data?.getStringExtra(CustomCameraActivity.CAMERA_ACTIVITY_OUTPUT_FILE_PATH)!!
                loadThumbnailImage(imageUriPath)
            } else {
                imageUriPath = "";
                addOrUpdateItemBinding.ibAddImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }

    private fun bindGalleryActivityForResult() {
        startGalleryActivityForResult = registerForActivityResult(
            ActivityResultContracts.OpenDocument()) {
            if (it != null) {
                imageUriPath = it.toString()
                contentResolver.takePersistableUriPermission(
                    Uri.parse(imageUriPath),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                loadThumbnailImage(imageUriPath)
            } else {
                imageUriPath = "";
                addOrUpdateItemBinding.ibAddImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }

    private fun bindMapsActivityForResult() {
        startMapActivityForResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == MapsActivity.MAPS_ACTIVITY_SUCCESS_RESULT_CODE) {
                productLocation = it.data?.getStringExtra(AppConstants.KEY_PRODUCT_LOCATION).toString()
                onLocationDataFetched()
            }
        }
    }
    override fun onBackPressed() {
        setResultWithFinish(RESULT_CODE_CANCEL, null)
    }
}