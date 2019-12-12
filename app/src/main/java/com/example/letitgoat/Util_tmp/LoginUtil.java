package com.example.letitgoat.Util_tmp;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.letitgoat.R;


public class LoginUtil {
    public static void setLogin(Context context, String userName, String userEmail, String selfie) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isLogin", true);
        editor.putString("userName", userName);
        editor.putString("userEmail", userEmail);
        editor.putString("selfie", selfie);
        editor.commit();
    }

    public static void setLogout(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isLogin", false);
        editor.putString("userName", null);
        editor.putString("userEmail", null);
        editor.putString("selfie", null);
        editor.commit();
    }

    public static boolean isLogin(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getBoolean("isLogin", false);
    }

    public static String getUserName(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getString("userName", null);
    }

    public static String getUserEmail(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getString("userEmail", null);
    }

    public static String getUserSelfie(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getString("selfie", null);
    }

    public static void setUserSelfie(Context context, String selfie) {
        SharedPreferences sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("selfie", selfie);
        editor.apply();
    }
}
