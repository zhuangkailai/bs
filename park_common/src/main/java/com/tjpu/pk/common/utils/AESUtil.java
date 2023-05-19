package com.tjpu.pk.common.utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author: lip
 * @date: 2018年6月13日 上午10:51:17
 * @Description:引自ROYI,AES加密算法
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version V1.0
 *
 */
public class AESUtil {

	private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";// 默认的加密算法

	public static final String KEY_Secret = "2018201820182018";

	private static final byte[] plusbyte = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };

	 /**
	  * 
	  * @author: lip
	  * @date: 2018年7月3日 上午9:22:41
	  * @Description: 加密操作
	  * @updateUser:
	  * @updateDate:
	  * @updateDescription:
	  * @param src 加密字符串
	  * @param key 秘钥
	  * @return
	  * @throws Exception
	  */
	public static String Encrypt(String src, String key){

        try {
            // 判断密钥是否为空
            if (key == null) {
                System.out.print("密钥不能为空");
                return null;
            }

            // 密钥补位
            int plus = 16 - key.length();
            byte[] data = key.getBytes("utf-8");
            byte[] raw = new byte[16];

            for (int i = 0; i < 16; i++) {
                if (data.length > i)
                    raw[i] = data[i];
                else
                    raw[i] = plusbyte[plus];
            }

            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM); // 算法/模式/补码方式
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(src.getBytes("utf-8"));
            // return new Base64().encodeToString(encrypted);//base64
            return parseByte2HexStr(encrypted).toLowerCase(); // 十六进制
        } catch (UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

	
	
	  /**将二进制转换成16进制 
     * @param buf 
     * @return 
     */  
    public static String parseByte2HexStr(byte buf[]) {  
            StringBuffer sb = new StringBuffer();  
            for (int i = 0; i < buf.length; i++) {  
                    String hex = Integer.toHexString(buf[i] & 0xFF);  
                    if (hex.length() == 1) {  
                            hex = '0' + hex;  
                    }  
                    sb.append(hex.toUpperCase());  
            }  
            return sb.toString();  
    } 
	
	 /**
	  * 
	  * @author: lip
	  * @date: 2018年7月3日 上午9:23:08
	  * @Description: 解密操作
	  * @updateUser:
	  * @updateDate:
	  * @updateDescription:
	  * @param src 
	  * @param key
	  * @return
	  * @throws Exception
	  */
	public static String Decrypt(String src, String key) throws Exception {
		try {
			// 判断Key是否正确
			if (key == null) {
				System.out.print("Key为空null");
				return null;
			}

			// 密钥补位
			int plus = 16 - key.length();
			byte[] data = key.getBytes("utf-8");
			byte[] raw = new byte[16];
			for (int i = 0; i < 16; i++) {
				if (data.length > i)
					raw[i] = data[i];
				else
					raw[i] = plusbyte[plus];
			}

			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);

			// byte[] encrypted1 = new Base64().decode(src);//base64
			byte[] encrypted1 = toByteArray(src);// 十六进制

			try {
				byte[] original = cipher.doFinal(encrypted1);
				String originalString = new String(original, "utf-8");
				return originalString;
			} catch (Exception e) {
				System.out.println(e.toString());
				return null;
			}
		} catch (Exception ex) {
			System.out.println(ex.toString());
			return null;
		}
	}
	/**
	 * 16进制的字符串表示转成字节数组
	 *
	 * @param hexString
	 *            16进制格式的字符串
	 * @return 转换后的字节数组
	 **/
	public static byte[] toByteArray(String hexString) {
		if (hexString.isEmpty())
			throw new IllegalArgumentException("this hexString must not be empty");

		hexString = hexString.toLowerCase();
		final byte[] byteArray = new byte[hexString.length() / 2];
		int k = 0;
		for (int i = 0; i < byteArray.length; i++) {// 因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
			byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
			byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
			byteArray[i] = (byte) (high << 4 | low);
			k += 2;
		}
		return byteArray;
	}

}
