package com.tjpu.pk.common.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SM4Utils {
    private String secretKey = "";

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    private String iv = "";
    private boolean hexString = false;

    public SM4Utils() {
    }

    public String encryptData_ECB(String plainText) {
        try {
            SM4_Context ctx = new SM4_Context();
            ctx.isPadding = true;
            ctx.mode = SM4.SM4_ENCRYPT;

            byte[] keyBytes;
            if (hexString) {
                keyBytes = Util.hexStringToBytes(secretKey);
            } else {
                keyBytes = secretKey.getBytes();
            }

            SM4 sm4 = new SM4();
            sm4.sm4_setkey_enc(ctx, keyBytes);
            byte[] encrypted = sm4.sm4_crypt_ecb(ctx, plainText.getBytes("GBK"));
            String cipherText = new BASE64Encoder().encode(encrypted);
            if (cipherText != null && cipherText.trim().length() > 0) {
                Pattern p = Pattern.compile("\\s*|\t|\r|\n");
                Matcher m = p.matcher(cipherText);
                cipherText = m.replaceAll("");
            }
            return cipherText;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String decryptData_ECB(String cipherText) {
        try {
            SM4_Context ctx = new SM4_Context();
            ctx.isPadding = true;
            ctx.mode = SM4.SM4_DECRYPT;

            byte[] keyBytes;
            if (hexString) {
                keyBytes = Util.hexStringToBytes(secretKey);
            } else {
                keyBytes = secretKey.getBytes();
            }

            SM4 sm4 = new SM4();
            sm4.sm4_setkey_dec(ctx, keyBytes);
            byte[] decrypted = sm4.sm4_crypt_ecb(ctx, new BASE64Decoder().decodeBuffer(cipherText));
            return new String(decrypted, "GBK");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String encryptData_CBC(String plainText) {
        try {
            SM4_Context ctx = new SM4_Context();
            ctx.isPadding = true;
            ctx.mode = SM4.SM4_ENCRYPT;

            byte[] keyBytes;
            byte[] ivBytes;
            if (hexString) {
                keyBytes = Util.hexStringToBytes(secretKey);
                ivBytes = Util.hexStringToBytes(iv);
            } else {
                keyBytes = secretKey.getBytes();
                ivBytes = iv.getBytes();
            }

            SM4 sm4 = new SM4();
            sm4.sm4_setkey_enc(ctx, keyBytes);
            byte[] encrypted = sm4.sm4_crypt_cbc(ctx, ivBytes, plainText.getBytes("GBK"));
            String cipherText = new BASE64Encoder().encode(encrypted);
            if (cipherText != null && cipherText.trim().length() > 0) {
                Pattern p = Pattern.compile("\\s*|\t|\r|\n");
                Matcher m = p.matcher(cipherText);
                cipherText = m.replaceAll("");
            }
            return cipherText;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String decryptData_CBC(String cipherText) {
        try {
            SM4_Context ctx = new SM4_Context();
            ctx.isPadding = true;
            ctx.mode = SM4.SM4_DECRYPT;

            byte[] keyBytes;
            byte[] ivBytes;
            if (hexString) {
                keyBytes = Util.hexStringToBytes(secretKey);
                ivBytes = Util.hexStringToBytes(iv);
            } else {
                keyBytes = secretKey.getBytes();
                ivBytes = iv.getBytes();
            }

            SM4 sm4 = new SM4();
            sm4.sm4_setkey_dec(ctx, keyBytes);
            byte[] decrypted = sm4.sm4_crypt_cbc(ctx, ivBytes, new BASE64Decoder().decodeBuffer(cipherText));
            return new String(decrypted, "GBK");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 加密
     */
    public static String getEncStr(String inputStr, String secretKey) {
        SM4Utils sm4 = new SM4Utils();
        sm4.secretKey = secretKey;  //meF8U9wHFOMfs2Y9
        sm4.hexString = false;

        //System.out.println("ECB模式");
        String cipherText = sm4.encryptData_ECB(inputStr);
        //System.out.println("ECB模式");
        return cipherText;
    }

    /**
     * 解密
     */
    public static String getDecStr(String inputStr, String secretKey) {
        SM4Utils sm4Util = new SM4Utils();
        sm4Util.secretKey = secretKey; // meF8U9wHFOMfs2Y9
        sm4Util.hexString = false;
        String plainText = sm4Util.decryptData_ECB(inputStr);
        return plainText;
    }

    public static String decryptData(String SecretKey, String str) {
        SM4Utils sm = new SM4Utils();
        sm.setSecretKey(SecretKey);
        String cipher = sm.encryptData_ECB(str);
        return cipher;

    }

    public static String DecStr(String SecretKey, String str) {
        SM4Utils jm = new SM4Utils();
        return jm.getDecStr(str, SecretKey);


    }

    public static void main(String[] args) {
        String key = "aDOu84clZKOi9erK";
        String userInfo = "15371865986";
        String a = String.valueOf(System.currentTimeMillis());
        //str为标准的josn格式的字符串
        String str = "{\n" +
                "        \"key\":\"" + key + "\",\n" +
                "        \"userInfo\": \"15371865986\",\n" +
                "        \"time\":" + a + "\n" +
                "}";
        String encryptData = getEncStr(str, key);
        String deData = getDecStr(encryptData, key);
        System.out.println("生成的key值:" + encryptData);
        System.out.println(deData);
    }


}
