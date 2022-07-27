package com.rino.visualdestortion.model.localDataSource

import com.rino.visualdestortion.model.localDataSource.sharedPref.Preference
import com.rino.visualdestortion.utils.*

  open class PreferenceDataSource(private val sharedPreference: MySharedPreference): Preference {
    override fun getFirstTimeLaunch(): Boolean {
        return sharedPreference.getBoolean(FIRST_TIME_LAUNCH_KEY,false)
    }

    override fun setFirstTimeLaunch(firstTimeLaunch: Boolean) {
        return sharedPreference.setBoolean(FIRST_TIME_LAUNCH_KEY,firstTimeLaunch)
    }

    override fun isLogin(): Boolean {
        return sharedPreference.getBoolean(LOGIN_KEY)
    }

    override fun setLogin(login: Boolean) {
        return sharedPreference.setBoolean(LOGIN_KEY,login)
    }

    override fun setEmail(email: String) {
        return sharedPreference.setString(EMAIL_KEY,email)
    }

    override fun getEmail(): String {
        return sharedPreference.getString(EMAIL_KEY)
    }

    override fun setPass(pass: String) {
        return sharedPreference.setString(PASS_KEY,pass)
    }

    override fun getPass(): String {
        return sharedPreference.getString(PASS_KEY)
    }

    override fun setToken(token: String) {
        return sharedPreference.setString(TOKEN_KEY,token)
    }

    override fun getToken(): String {
        return sharedPreference.getString(TOKEN_KEY)
    }

    override fun setRefreshToken(refreshToken: String) {
        return sharedPreference.setString(REFRESH_TOKEN_KEY,refreshToken)
    }

    override fun getRefreshToken(): String {
        return sharedPreference.getString(REFRESH_TOKEN_KEY)
    }

}