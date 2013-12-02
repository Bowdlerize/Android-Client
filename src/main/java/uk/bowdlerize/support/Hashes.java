package uk.bowdlerize.support;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashes
{
    public static String MD5(String target)
    {
        MessageDigest mdEnc = null;
        try
        {
            mdEnc = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e)
        {
            //Log.e("Exception","Exception while encrypting to md5");
            e.printStackTrace();
        }

        mdEnc.update(target.getBytes(), 0, target.length());
        String md5 = new BigInteger(1, mdEnc.digest()).toString(16) ;
        return md5;
    }
}
