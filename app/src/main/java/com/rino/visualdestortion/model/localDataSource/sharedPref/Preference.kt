package com.rino.visualdestortion.model.localDataSource.sharedPref

interface Preference {
    fun getFirstTimeLaunch(): Boolean
    fun setFirstTimeLaunch(firstTimeLaunch: Boolean)
    fun isLogin(): Boolean
    fun setLogin(login: Boolean)
    fun setEmail(email: String)
    fun getEmail(): String
    fun setPass(pass: String)
    fun getPass(): String
    fun setToken(token: String)
    fun getToken(): String
    fun setRefreshToken(refreshToken: String)
    fun getRefreshToken(): String
}