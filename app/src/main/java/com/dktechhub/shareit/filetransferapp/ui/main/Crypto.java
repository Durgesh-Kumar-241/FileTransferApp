package com.dktechhub.shareit.filetransferapp.ui.main;

import android.util.Base64;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
    private static KeyPair serverKP;
    public  static String serverPublic ="";
    private static SecretKey secretKey;
    private SecretKey clientKey;
    public static void iniServerKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        serverKP = keyPairGenerator.generateKeyPair();
        serverPublic =Base64.encodeToString(serverKP.getPublic().getEncoded(),Base64.URL_SAFE|Base64.NO_WRAP);
    }

    public static void iniClientKeys() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        secretKey = keyGenerator.generateKey();
    }

    public static String getEncryptedSecret(String serverPublic) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(Base64.decode(serverPublic,Base64.URL_SAFE|Base64.NO_WRAP));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(publicSpec);
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE,publicKey);
        return Base64.encodeToString(cipher.doFinal(secretKey.getEncoded()),Base64.URL_SAFE|Base64.NO_WRAP);
    }

    public static void decryptSecretKey(String clinets) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE,serverKP.getPrivate());
        byte[] secBytes = cipher.doFinal(Base64.decode(clinets,Base64.URL_SAFE|Base64.NO_WRAP));
        secretKey = new SecretKeySpec(secBytes,0,secBytes.length,"AES");
    }

    //MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDCyHAgnc6dlGCVuQZRhZYOGBMePJtbjdFs9v33QRC_fI_Fz6YHm7FfEdLmogVjIFWZNbWxo2hJ62LPIQki_DKxKKg9sUzoutC_EXTt3VxxWiOe9vhklPE4ac3DaPCIswMHWoUDW21rttyexoYRJvNw2TdpNb4zWRR7bYfb6fpj-QIDAQAB
    //MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDCyHAgnc6dlGCVuQZRhZYOGBMePJtbjdFs9v33QRC_fI_Fz6YHm7FfEdLmogVjIFWZNbWxo2hJ62LPIQki_DKxKKg9sUzoutC_EXTt3VxxWiOe9vhklPE4ac3DaPCIswMHWoUDW21rttyexoYRJvNw2TdpNb4zWRR7bYfb6fpj-QIDAQAB
    //MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDCyHAgnc6dlGCVuQZRhZYOGBMePJtbjdFs9v33QRC_fI_Fz6YHm7FfEdLmogVjIFWZNbWxo2hJ62LPIQki_DKxKKg9sUzoutC_EXTt3VxxWiOe9vhklPE4ac3DaPCIswMHWoUDW21rttyexoYRJvNw2TdpNb4zWRR7bYfb6fpj-QIDAQAB

}
