package com.sevenbits.ui.activity.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.sevenbits.R
import com.sevenbits.base.BaseActivity
import com.sevenbits.ui.adapter.CountryAdapter
import com.sevenbits.model.Country
import com.sevenbits.ui.activity.home.HomeActivity
import com.sevenbits.util.CountryUtils
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity(), AdapterView.OnItemSelectedListener, View.OnClickListener {
    var countrylist = ArrayList<Country>()
    var mContext = this@MainActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        countrylist = CountryUtils.getAllCountries(mContext)

        setspinerdata()
        setonclicklistner()
    }

    private fun setonclicklistner() {
        btn_get_otp.setOnClickListener(this)
    }

    private fun setspinerdata() {
        sp_country.adapter = CountryAdapter(mContext,countrylist)
        sp_country.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        txt_country_code.text = getString(R.string.phone_code,countrylist[position].phoneCode)
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    override fun onClick(p0: View?) {
        when(p0!!.id){
            R.id.btn_get_otp -> {
                if(validate()){
                    startActivity(Intent(mContext, OtpActivity::class.java).putExtra("number", txt_country_code.text.toString()+edt_mobile_no.text.toString()))
                }
            }
        }
    }

    private fun validate(): Boolean {
        if(edt_mobile_no.text.toString().isNotEmpty()){
            return true
        }else{
            Toast.makeText(mContext, getString(R.string.enter_mobile_number), Toast.LENGTH_SHORT).show()
        }

        return false
    }
}