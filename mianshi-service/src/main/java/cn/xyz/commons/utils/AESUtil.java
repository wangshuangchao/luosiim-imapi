package cn.xyz.commons.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {  
    // 密钥  
	public static String key = "abcd1234!@#$%ole";  
    private static String charset = "utf-8";  
    private static String vi = "01A2B3C4D5E6F789";
    // 偏移量  
    private static int offset = 16;  
    private static String transformation = "AES/CBC/PKCS5Padding";  
    private static String algorithm = "AES";  
  
    /** 
     * 加密 
     *  
     * @param content 
     * @return 
     */  
    public static String encrypt(String content) {  
        return encrypt(content, key);  
    }  
  
    /** 
     * 解密 
     *  
     * @param content 
     * @return 
     */  
    public static String decrypt(String content) {  
        return decrypt(content, key);  
    }  
  
    /** 
     * 加密 
     *  
     * @param content 
     *            需要加密的内容 
     * @param key 
     *            加密密码 
     * @return 
     */  
    public static String encrypt(String content, String key) {  
        try {  
            SecretKeySpec skey = new SecretKeySpec(key.getBytes(), algorithm);  
            //IvParameterSpec iv = new IvParameterSpec(key.getBytes(), 0, offset);  
            IvParameterSpec iv = new IvParameterSpec(vi.getBytes());  
            Cipher cipher = Cipher.getInstance(transformation);  
            byte[] byteContent = content.getBytes(charset);  
            cipher.init(Cipher.ENCRYPT_MODE, skey, iv);// 初始化  
            byte[] result = cipher.doFinal(byteContent);  
            return Base64.encode(result); // 加密  
        } catch (Exception e) {  
        }  
        return null;  
    }  
  
    /** 
     * AES（256）解密 
     *  
     * @param content 
     *            待解密内容 
     * @param key 
     *            解密密钥 
     * @return 解密之后 
     * @throws Exception 
     */  
    public static String decrypt(String content, String key) {  
        try {  
  
            SecretKeySpec skey = new SecretKeySpec(key.getBytes("UTF-8"), algorithm);  
            //IvParameterSpec iv = new IvParameterSpec(key.getBytes(), 0, offset);  
            IvParameterSpec iv = new IvParameterSpec(vi.getBytes("UTF-8"));  
            Cipher cipher = Cipher.getInstance(transformation);  
            cipher.init(Cipher.DECRYPT_MODE, skey, iv);// 初始化  
            byte[] result = cipher.doFinal(new Base64().decode(content));  
            return new String(result,"UTF-8"); // 解密  
        } catch (Exception e) {  
        }  
        return null;  
    }  
  
    public static void main(String[] args) throws Exception {  
        String s = "{\"content\":\"我在哪\",\"filter\":1,\"fromUserName\":\"测试九\",\"messageId\":\"b449cac7fa0d458199d3571e1761f08b\",\"timeSend\":1.526010748116E9,\"type\":1}";  
        // 加密  
        String key="1111111111111111";
        byte[] bytes = key.getBytes();
        for (byte b : bytes) {
			System.out.print(b);
		}
        System.out.println(""+bytes.length+bytes);
        System.out.println("加密前：" + s);  
        String encryptResultStr = encrypt(s,"1111111111111111");  
        System.out.println("加密后：" + encryptResultStr);  
        // 解密  
        System.out.println("解密后：" + decrypt(encryptResultStr,"1111111111111111"));  
        String encryptResultStr2 = "mnMn1V6QbOFW7sWKI8bilQ==";
        System.out.println("内容解密后：" + decrypt(encryptResultStr2));
    }  
} 
