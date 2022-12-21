package edu.ncku.application.util;

import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import edu.ncku.application.io.IOConstatnt;

/**
 * 安全工具類別，使用AES CBC加密密碼
 */
public class Security implements IOConstatnt{

    private final Cipher cipher;
    private final SecretKeySpec key;
    private AlgorithmParameterSpec spec;
    public static final String SEED_16_CHARACTER = "secretKEY@system";

    public Security() throws Exception {
        // hash password with SHA-256 and crop the output to 128-bit for key
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(SEED_16_CHARACTER.getBytes("UTF-8"));
        byte[] keyBytes = new byte[32];
        System.arraycopy(digest.digest(), 0, keyBytes, 0, keyBytes.length);
        //bytes[i] & 0xff
        if(showLogMsg){
            Log.e("Security1", ""+ (SEED_16_CHARACTER.getBytes("UTF-8")[0] & 0xff));
        }
        for (byte b: SEED_16_CHARACTER.getBytes("UTF-8")) {
            if(showLogMsg){
                Log.e("byte", ":" + (b & 0xff));
            }
        }
        //Log.e("Security1", new String(keyBytes, "UTF-8"));
        cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        key = new SecretKeySpec(keyBytes, "AES");
        spec = getIV();
    }

    /**
     * 取得Initail Vector
     * @return
     */
    public AlgorithmParameterSpec getIV() {
        byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, };
        IvParameterSpec ivParameterSpec;
        ivParameterSpec = new IvParameterSpec(iv);
        return ivParameterSpec;
    }

    /**
     * 加密明文
     *
     * @param plainText 明文
     * @return
     * @throws Exception
     */
    public String encrypt(String plainText) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
        String encryptedText = new String(Base64.encode(encrypted,
                Base64.DEFAULT), "UTF-8");
        return encryptedText;
    }

    /**
     * 解密密文
     *
     * @param cryptedText 密文
     * @return
     * @throws Exception
     */
    public String decrypt(String cryptedText) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        byte[] bytes = Base64.decode(cryptedText, Base64.DEFAULT);
        byte[] decrypted = cipher.doFinal(bytes);
        String decryptedText = new String(decrypted, "UTF-8");
        return decryptedText;
    }

}
