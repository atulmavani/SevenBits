package com.sevenbits.ui.activity.login

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.sevenbits.R
import com.sevenbits.base.BaseActivity
import com.sevenbits.ui.activity.home.HomeActivity
import kotlinx.android.synthetic.main.activity_otp.*
import java.util.concurrent.TimeUnit
import android.os.CountDownTimer


class OtpActivity : BaseActivity(), View.OnClickListener {
    var mContext = this@OtpActivity
    private lateinit var auth: FirebaseAuth
    var number = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var verificationid: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        auth = FirebaseAuth.getInstance()
        number = intent.getStringExtra("number").toString()
        initiateotp()
        seonclicklistner()
        starttimer()
    }

    private fun starttimer() {
        object : CountDownTimer(60000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                txt_timer.text = (millisUntilFinished / 1000).toString()+getString(R.string.sec)
                //here you can have your logic to set text to edittext
            }

            override fun onFinish() {
                btn_resend_otp.isEnabled = true
            }
        }.start()
    }

    private fun seonclicklistner() {
        btn_verify_otp.setOnClickListener(this)
        btn_resend_otp.setOnClickListener(this)
    }

    private fun initiateotp() {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendotp(){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .setForceResendingToken(resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            if(credential.smsCode != null) {
                otpView.setText(credential.smsCode.toString())
            }
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {


            if (e is FirebaseAuthInvalidCredentialsException) {
                //Log.e(TAG, "onVerificationFailed: invalid credentials", e)
                Toast.makeText(mContext, "Invalid credentials", Toast.LENGTH_SHORT).show()
            } else if (e is FirebaseTooManyRequestsException) {
                //Log.e(TAG, "onVerificationFailed: SMS Quota expired", e)
                Toast.makeText(mContext, "SMS Quota expired", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(mContext, "Verification failed plz try after some time.", Toast.LENGTH_SHORT).show()
            }

        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {

            verificationid = verificationId
            resendToken = token

            Toast.makeText(mContext, "OTP sent to mobile number.", Toast.LENGTH_SHORT).show()
            //resend.visibility = View.GONE
            //startCounter()
        }
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                    //firebase_token = user!!.uid
                    val i = Intent(mContext, HomeActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(i)
                    //Toast.makeText(mContext,"success", Toast.LENGTH_SHORT).show()
                } else {

                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(mContext,"invalid otp.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    override fun onClick(p0: View?) {
        when(p0!!.id){
            R.id.btn_verify_otp -> {
                hideSoftKeyboard()
                signInWithPhoneAuthCredential(PhoneAuthProvider.getCredential(verificationid, otpView.text.toString()))
            }
            R.id.btn_resend_otp -> {
                resendotp()
                btn_resend_otp.isEnabled = false
                starttimer()
            }
        }
    }
}