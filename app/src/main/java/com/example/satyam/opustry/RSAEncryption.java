package com.example.satyam.opustry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;

public class RSAEncryption {
    public static String getSaltString(int n) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < n) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
    public static KeyPair generateKeyPair()
    {
        // Get an instance of the RSA key generator
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // Generate the keys — might take sometime on slow computers
        KeyPair myPair = kpg.generateKeyPair();
        return myPair;
    }
    private static byte[] combineArrays(byte[] one, byte[] two)
    {
        byte[] combined = new byte[one.length + two.length];
        System.arraycopy(one,0,combined,0         ,one.length);
        System.arraycopy(two,0,combined,one.length,two.length);
        return  combined;
    }
    public static byte[] encrypt(byte[] data,Key key)
    {
        // Get an instance of the Cipher for RSA encryption/decryption
        Cipher c = null;
        try {
            c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            c.init(Cipher.ENCRYPT_MODE, key);
            if (data.length > Short.MAX_VALUE - 2)
                throw new UnsupportedOperationException();
            return c.doFinal(combineArrays(ByteBuffer.allocate(2).putShort((short) data.length).array(),data));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static byte[] decrypt(byte[] input, Key key) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decrypted = cipher.doFinal(input);
        short len = ByteBuffer.wrap(decrypted,0, 2).getShort();
        byte[] data = new byte[len];
        System.arraycopy(decrypted,2,data,0,len);
        return cipher.doFinal(data);
    }
}
