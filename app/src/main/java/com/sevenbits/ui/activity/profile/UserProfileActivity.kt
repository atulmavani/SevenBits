package com.sevenbits.ui.activity.profile

import android.Manifest
import android.app.Activity
import android.content.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.sevenbits.R
import kotlinx.android.synthetic.main.activity_user_profile.*
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sevenbits.util.SharedPrefsConstant
import com.sevenbits.util.get
import com.sevenbits.util.put
import android.graphics.Bitmap
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import com.sevenbits.base.BaseActivity
import kotlinx.android.synthetic.main.toolbar.*
import java.io.ByteArrayOutputStream
import java.io.InputStream


class UserProfileActivity : BaseActivity(), View.OnClickListener {
    var mContext = this@UserProfileActivity
    lateinit var user_image : Uri
    lateinit var sharedPreferences: SharedPreferences
    var encodedImage = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        profile_image.visibility = View.GONE
        txt_title.text = getString(R.string.my_profile)
        sharedPreferences = this.getSharedPreferences(
            SharedPrefsConstant.SHARED_PREFERENCE_FILE_NAME,
            Context.MODE_PRIVATE
        )
        setdata()
        setonclicklistner()
    }

    //set data if stored in sharedpreference
    private fun setdata() {
        edt_user_name.setText(sharedPreferences.get(SharedPrefsConstant.USER_NAME,""))
        edt_mobile_number.setText(sharedPreferences.get(SharedPrefsConstant.USER_NUMBER,""))
        edt_email.setText(sharedPreferences.get(SharedPrefsConstant.USER_EMAIL,""))
        encodedImage = sharedPreferences.get(SharedPrefsConstant.USER_IMAGE,"")
        if(encodedImage != ""){
            val decodedString: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            user_Image.setImageBitmap(decodedByte)
        }
        if(sharedPreferences.get(SharedPrefsConstant.USER_NAME,"") != "" ||
            sharedPreferences.get(SharedPrefsConstant.USER_NUMBER,"") != "" ||
            sharedPreferences.get(SharedPrefsConstant.USER_EMAIL,"") != "" || encodedImage != ""){
                btn_save_update.text = getString(R.string.update)
        }
    }

    //set onclick listner
    private fun setonclicklistner() {
        user_Image.setOnClickListener(this)
        btn_save_update.setOnClickListener(this)
        ic_back.setOnClickListener(this)
    }

    //onclick event handling
    override fun onClick(p0: View?) {
        when(p0!!.id){
            R.id.user_Image -> {
                showDialog()
            }
            R.id.btn_save_update ->{
                if(validate()) {
                    hideSoftKeyboard()
                    sharedPreferences.put(SharedPrefsConstant.USER_IMAGE, encodedImage)
                    sharedPreferences.put(SharedPrefsConstant.USER_NAME, edt_user_name.text.toString())
                    sharedPreferences.put(SharedPrefsConstant.USER_NUMBER, edt_mobile_number.text.toString())
                    sharedPreferences.put(SharedPrefsConstant.USER_EMAIL, edt_email.text.toString())
                    Toast.makeText(mContext, getString(R.string.save_success), Toast.LENGTH_SHORT).show()
                }
            }
            R.id.ic_back -> {
                onBackPressed()
            }
        }
    }

    private fun validate(): Boolean {
        if(edt_user_name.text.toString().isNotEmpty()){
            if(edt_mobile_number.text.toString().isNotEmpty()){
                if(edt_email.text.toString().isNotEmpty()){
                    if(isValidEmail(edt_email.text.toString())) {
                        return true
                    }else{
                        Toast.makeText(mContext, getString(R.string.enter_valid_email), Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(mContext, getString(R.string.enter_email), Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(mContext, getString(R.string.enter_mobile_number), Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(mContext, getString(R.string.enter_name), Toast.LENGTH_SHORT).show()
        }
        return false
    }

    //convert bitmap to encoded string
    private fun encodeImage(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b: ByteArray = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    //show dialog
    fun showDialog() {
        var bottomSheet = BottomSheetDialog(this)
        val view: View =
            LayoutInflater.from(this).inflate(R.layout.dialog_camera_gallery, null)
        val llCamera: LinearLayout = view.findViewById(R.id.llCamera)
        val llGallery: LinearLayout = view.findViewById(R.id.llGallery)
        llCamera.setOnClickListener {
            bottomSheet.dismiss()
            requestStoragePermission(true)
        }
        llGallery.setOnClickListener {
            bottomSheet.dismiss()
            requestStoragePermission(false)
        }
        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    //get permission from user if not allow
    private fun requestStoragePermission(isCamera: Boolean) {
        Dexter.withContext(mContext)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        if (isCamera) {
                            val values = ContentValues()
                            values.put(MediaStore.Images.Media.TITLE, "New Picture")
                            values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
                            user_image = contentResolver.insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
                            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, user_image)
                            cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1)
                            resultLaunchercamera.launch(cameraIntent)
                        } else {
                            val photoPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
                            photoPickerIntent.type = "image/*"
                            resultLauncherstorage.launch(photoPickerIntent)
                        }
                    }
                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        // show alert dialog navigating to Settings
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
            .withErrorListener {
                Toast.makeText(this, "Error occurred! ", Toast.LENGTH_SHORT).show()
            }
            .onSameThread()
            .check()
    }

    //shoe dialog
    fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Need Permissions")
        builder.setMessage(
            "This app needs permission to use this feature. You can grant them in app settings."
        )
        builder.setPositiveButton("GOTO SETTINGS") { dialog: DialogInterface, which: Int ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog: DialogInterface, which: Int -> dialog.cancel() }
        builder.show()
    }

    // navigating user to app settings
    fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    //result launcher for gallery
    var resultLauncherstorage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            if(data!!.data != null){
                user_image = data.data!!
                user_Image.setImageURI(data.data)
                val imageStream: InputStream? = contentResolver.openInputStream(user_image)
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                encodedImage = encodeImage(selectedImage)
            }
        }
    }

    //result launcher for camera
    var resultLaunchercamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            user_Image.setImageURI(user_image)
            val imageStream: InputStream? = contentResolver.openInputStream(user_image)
            val selectedImage = BitmapFactory.decodeStream(imageStream)
            encodedImage = encodeImage(selectedImage)
        }
    }
}