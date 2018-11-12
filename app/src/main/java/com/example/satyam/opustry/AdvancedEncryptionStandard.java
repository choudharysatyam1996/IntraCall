package com.example.satyam.opustry;

import java.nio.ByteBuffer;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AdvancedEncryptionStandard
{
    private byte[] key;

    private static final String ALGORITHM = "AES";

    public AdvancedEncryptionStandard(byte[] key)
    {
        this.key = key;
    }

    /**
     * Encrypts the given plain text
     *
     * @param plainText The plain text to encrypt
     */
    public byte[] encrypt(byte[] plainText) throws Exception
    {
        SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        if (plainText.length > Short.MAX_VALUE - 2)
            throw new UnsupportedOperationException();
        return cipher.doFinal(combineArrays(ByteBuffer.allocate(2).putShort((short) plainText.length).array(),plainText));
    }

    private byte[] combineArrays(byte[] one,byte[] two)
    {
        byte[] combined = new byte[one.length + two.length];
        System.arraycopy(one,0,combined,0         ,one.length);
        System.arraycopy(two,0,combined,one.length,two.length);
        return  combined;
    }


    /**
     * Decrypts the given byte array
     *
     * @param cipherText The data to decrypt
     */
    public byte[] decrypt(byte[] cipherText) throws Exception
    {
        SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypted = cipher.doFinal(cipherText);
        short len = ByteBuffer.wrap(decrypted,0, 2).getShort();
        byte[] data = new byte[len];
        System.arraycopy(decrypted,2,data,0,len);
        return data;
    }
}
