package np.edu.ismt.krishna.suitcasepro.files

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.telephony.SmsManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import np.edu.ismt.krishna.suitcasepro.R
import np.edu.ismt.krishna.suitcasepro.database.Product
import np.edu.ismt.krishna.suitcasepro.database.TestDatabase
import np.edu.ismt.krishna.suitcasepro.databinding.ActivityItemsDetailView1Binding
import java.io.IOException

class ItemsDetailViewActivity : AppCompatActivity() {
    private lateinit var itemDetailViewBinding: ActivityItemsDetailView1Binding
    private var receivedproduct: Product? = null
    private var tieContact: TextInputEditText? = null


    companion object {
        const val RESULT_CODE_CANCEL = 2001
        const val RESULT_CODE_REFRESH = 2002
        const val SMS_PERMISSIONS_REQUEST_CODE = 111
    }

    private lateinit var startAddItemActivity: ActivityResultLauncher<Intent>
    private lateinit var startContactActivityForResult: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemDetailViewBinding = ActivityItemsDetailView1Binding.inflate(layoutInflater)
        setContentView(itemDetailViewBinding.root)
        bindAddOrUpdateActivityForResult()
        bindContactPickerActivityForResult()

        receivedproduct = intent.getParcelableExtra(AppConstants.KEY_PRODUCT)
        receivedproduct?.apply {
            populateDataToTheViews(this)
        }
        setUpButtons()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSIONS_REQUEST_CODE && areSmsPermissionsGranted(this)) {
            showSmsBottomSheetDialog()
        } else {
            Uiutiity.showToast(this, "Please provide permission for SMS")
        }
    }

    private fun populateDataToTheViews(product: Product?) {
        itemDetailViewBinding.titleDetails.text = product?.title
        itemDetailViewBinding.itemPrice.text = product?.price
        itemDetailViewBinding.itemDescription.text = product?.description

        /**
         * Scaling the image into the view based on its width and heigh
         */
        itemDetailViewBinding.imageDetails.post {
            var bitmap: Bitmap?
            try {
                bitmap = MediaStore.Images.Media.getBitmap(
                    applicationContext.contentResolver,
                    Uri.parse(product?.image)
                )
                bitmap = BitmapScalar.stretchToFill(
                    bitmap,
                    itemDetailViewBinding.imageDetails.width,
                    itemDetailViewBinding.imageDetails.height
                )
                itemDetailViewBinding.imageDetails.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        /**
         * Splitting the latlng string into lat and lng
         * Based on the latlng, reverse geocoding to get the actual location
         */
        try {
            val lat = product?.location!!.split(",")[0]
            val lng = product.location.split(",")[1]
            val geoCodedAddress = GeoCoding.reverseTheGeoCodeToAddress(this, lat, lng)
            itemDetailViewBinding.itemLocation.text = geoCodedAddress
        } catch (exception: java.lang.Exception) {
            exception.printStackTrace()
        }
    }

    private fun setUpButtons() {
        setUpBackButton()
        setUpEditButton()
        setUpDeleteButton()
        setUpShareButton()
    }

    private fun setUpBackButton() {
        itemDetailViewBinding.ibBack.setOnClickListener {
            setResultWithFinish(RESULT_CODE_REFRESH)
        }
    }

    private fun setUpEditButton() {
        itemDetailViewBinding.ibEdit.setOnClickListener {
            val intent = Intent(
                this,
                AddorUpdateActivity::class.java
            ).apply {
                this.putExtra(AppConstants.KEY_PRODUCT, receivedproduct)
            }
            startAddItemActivity.launch(intent)
        }
    }

    private fun setUpDeleteButton() {
        itemDetailViewBinding.deleteItem.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Alert")
                .setMessage("Do you want to delete this product?")
                .setPositiveButton(
                    "Yes",
                    DialogInterface.OnClickListener {
                            dialogInterface,
                            i -> deleteProduct()
                    })
                .setNegativeButton(
                    "No",
                    DialogInterface.OnClickListener {
                            dialogInterface,
                            i ->  dialogInterface.dismiss()

                    })
                .show()
        }
    }

    private fun setUpShareButton() {
        itemDetailViewBinding.ibShare.setOnClickListener {
            if (areSmsPermissionsGranted(this)) {
                showSmsBottomSheetDialog()
            } else {
                requestPermissions(
                    smsPermissionsList().toTypedArray(),
                    SMS_PERMISSIONS_REQUEST_CODE
                )
            }
        }
    }



    private fun deleteProduct() {
        val testDatabase = TestDatabase.getInstance(this.applicationContext)
        val productDao = testDatabase.getProductDao()

        Thread {
            try {
                receivedproduct?.apply {
                    productDao.deleteProduct(this)
                    runOnUiThread {
                        Uiutiity.showToast(
                            this@ItemsDetailViewActivity,
                            "Product deleted successfully"
                        )
                        setResultWithFinish(RESULT_CODE_REFRESH)
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
                runOnUiThread {
                    Uiutiity.showToast(
                        this@ItemsDetailViewActivity,
                        "Cannot delete product."
                    )
                }
            }
        }.start()
    }

    private fun setResultWithFinish(resultCode: Int) {
        setResult(resultCode)
        finish()
    }

    private fun showSmsBottomSheetDialog() {
        val smsBottomSheetDialog = BottomSheetDialog(this)
        smsBottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_send_sms)

        val tilContact: TextInputLayout? = smsBottomSheetDialog.findViewById(R.id.til_contact)
        tieContact = smsBottomSheetDialog.findViewById(R.id.tie_contact)
        val sendSmsButton: MaterialButton? = smsBottomSheetDialog.findViewById(R.id.mb_send_sms)

        tilContact?.setEndIconOnClickListener {
            //TODO open Contact Activity
            val pickContact = Intent(Intent.ACTION_PICK)
            pickContact.setDataAndType(
                ContactsContract.Contacts.CONTENT_URI,
                ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
            )
            startContactActivityForResult.launch(pickContact)
        }

        sendSmsButton?.setOnClickListener {
            val contact = tieContact?.text.toString()
            //TODO validation
            if (contact.isBlank()) {
                tilContact?.error = "Enter Contact"
            } else {
                sendSms(contact)
                smsBottomSheetDialog.dismiss()
            }
        }
        smsBottomSheetDialog.setCancelable(true)
        smsBottomSheetDialog.show()
    }

    private fun areSmsPermissionsGranted(context: Context): Boolean {
        var areAllPermissionGranted = false
        for (permission in smsPermissionsList()!!) {
            if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED
            ) {
                areAllPermissionGranted = true
            } else {
                areAllPermissionGranted = false
                break
            }
        }
        return areAllPermissionGranted
    }
    private fun smsPermissionsList(): List<String> {
        val smsPermissions: MutableList<String> = ArrayList()
        smsPermissions.add(Manifest.permission.READ_SMS)
        smsPermissions.add(Manifest.permission.SEND_SMS)
        smsPermissions.add(Manifest.permission.READ_CONTACTS)
        return smsPermissions
    }

    private fun sendSms(contact: String) {
        Thread {
            try {
                val smsManager: SmsManager = SmsManager.getDefault()
                val message = """
            Item: ${receivedproduct!!.title}
            Price: ${receivedproduct!!.price}
            Description: ${receivedproduct!!.description}
            """.trimIndent()
                smsManager.sendTextMessage(
                    contact,
                    null,
                    message,
                    null,
                    null
                )
                runOnUiThread {
                    Uiutiity.showToast(this, "SMS Sent. Please check your message app...")
                }
            } catch (exception: Exception) {
                runOnUiThread {
                    Uiutiity.showToast(this, "Couldn't Send SMS...")
                }
            }
        }.start()


        //If the above SMS manager didn't work
//        openSmsAppToSendMessage(contact, message)
    }

    private fun openSmsAppToSendMessage(contact: String, message: String) {
        val sendIntent = Intent(Intent.ACTION_VIEW)
        sendIntent.data = Uri.parse("smsto:$contact")
        sendIntent.putExtra("sms_body", message)
        startActivity(intent)
    }

    private fun fetchContactNumberFromData(data: Intent) {
        val contactUri = data.data

        // Specify which fields you want
        // your query to return values for
        val queryFields = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        this.contentResolver
            .query(
                contactUri!!,
                null,
                null,
                null,
                null
            ).use { cursor ->
                // Double-check that you
                // actually got results
                if (cursor!!.count == 0) return

                // Pull out the first column of
                // the first row of data
                // that is your contact's name
                cursor.moveToFirst()
                val contactNumberIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val contactNumber = cursor.getString(contactNumberIndex).apply {
                    //Replacing the brackets and hyphens with empty string as we don't need here
                    this.replace(
                        Regex("[()\\-\\s]+"),
                        ""
                    )
                }
                tieContact?.setText(contactNumber)
            }
    }

    private fun bindAddOrUpdateActivityForResult() {
        startAddItemActivity = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == AddorUpdateActivity.RESULT_CODE_COMPLETE) {
                val product = it.data?.getParcelableExtra<Product>(AppConstants.KEY_PRODUCT)
                populateDataToTheViews(product)
            } else {
                // TODO do nothing
            }
        }
    }

    private fun bindContactPickerActivityForResult() {
        startContactActivityForResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
            if (it != null) {
                fetchContactNumberFromData(it.data!!)
            }
        }
    }
}