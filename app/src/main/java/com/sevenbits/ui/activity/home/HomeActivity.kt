package com.sevenbits.ui.activity.home

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sevenbits.R
import com.sevenbits.base.BaseActivity
import com.sevenbits.model.Product
import com.sevenbits.ui.activity.profile.UserProfileActivity
import com.sevenbits.ui.adapter.ProductAdapter
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.toolbar.*
import android.widget.AdapterView

import android.widget.AdapterView.OnItemLongClickListener
import com.sevenbits.util.SharedPrefsConstant
import com.sevenbits.util.get
import kotlinx.android.synthetic.main.activity_user_profile.*
import kotlinx.android.synthetic.main.toolbar.profile_image


class HomeActivity : BaseActivity(), View.OnClickListener {
    var mContext = this@HomeActivity
    var productlist = ArrayList<Product>()
    var db = Firebase.firestore
    var adapter : ProductAdapter? = null
    lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        sharedPreferences = this.getSharedPreferences(
            SharedPrefsConstant.SHARED_PREFERENCE_FILE_NAME,
            Context.MODE_PRIVATE
        )
        setonclicklistner()
        getdata()
    }

    override fun onResume() {
        super.onResume()
        var encodedImage = sharedPreferences.get(SharedPrefsConstant.USER_IMAGE,"")
        if(encodedImage != ""){
            val decodedString: ByteArray = Base64.decode(encodedImage, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            profile_image.setImageBitmap(decodedByte)
        }
    }

    private fun getdata() {
        db.collection("product")
            .get()
            .addOnSuccessListener { result ->
                for (product in result) {
                    productlist.add(Product(product.data["image"].toString(), product.data["name"].toString(),
                        product.data["oldprice"].toString().toInt(), product.data["newprice"].toString().toInt(), false))
                }
                adapter = ProductAdapter(mContext, productlist)
                grid_view.adapter = adapter
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun setonclicklistner() {
        profile_image.setOnClickListener(this)
        ic_back.setOnClickListener(this)

        grid_view.onItemLongClickListener =
            OnItemLongClickListener { arg0, arg1, position, arg3 ->
                adapter!!.update()
                false
            }
    }

    override fun onClick(p0: View?) {
        when(p0!!.id){
            R.id.profile_image -> {
                startActivity(Intent(mContext, UserProfileActivity::class.java))
            }
            R.id.ic_back -> {
                onBackPressed()
            }
        }
    }
}