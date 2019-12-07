package com.ltosc.droidtracker.security;

import android.util.Log;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ltosc on 03/08/2016.
 */
public class Md5Hash
{
    public static String getMd5(String input)
    {
        try
        {
            MessageDigest msgDigest = MessageDigest.getInstance("MD5");
            byte[] messageDigest = msgDigest.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);

            String md5 = number.toString(16);

            while (md5.length() < 32)
            {
                md5 = "0" + md5;
            }

            return md5;
        }
        catch (NoSuchAlgorithmException ex)
        {
            Log.e(Md5Hash.class.getName(), ex.getLocalizedMessage());
        }

        return null;
    }
}
