package edu.kpi.kote.encryption;

import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class TripleDESEncryptor {

    private static final String key = "HG58YZ3CR9";

	public static void main(final String[] args) throws Exception {

    	final String text = "Hello everybody";

    	final byte[] codedtext = TripleDESEncryptor.encrypt(text.getBytes(), key);
    	final byte[] decodedtext = TripleDESEncryptor.decrypt(codedtext, key);

    	System.out.println(codedtext); // this is a byte array, you'll just see a reference to an array
    	System.out.println(new String(decodedtext)); // This correctly shows "kyle boon"
    }

    public static byte[] encrypt(final byte[] message, final String encryptionKey) throws Exception {
    	final MessageDigest md = MessageDigest.getInstance("md5");
    	final byte[] digestOfPassword = md.digest(encryptionKey
    			.getBytes("utf-8"));
    	final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
    	for (int j = 0, k = 16; j < 8;) {
    		keyBytes[k++] = keyBytes[j++];
    	}

    	final SecretKey key = new SecretKeySpec(keyBytes, "DESede");
    	final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
    	final Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
    	cipher.init(Cipher.ENCRYPT_MODE, key, iv);

    	final byte[] cipherText = cipher.doFinal(message);
    	// final String encodedCipherText = new sun.misc.BASE64Encoder()
    	// .encode(cipherText);

    	return cipherText;
    }

    public static byte[] decrypt(final byte[] message, final String encryptionKey) throws Exception {
    	final MessageDigest md = MessageDigest.getInstance("md5");
    	final byte[] digestOfPassword = md.digest(encryptionKey
    			.getBytes("utf-8"));
    	final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
    	for (int j = 0, k = 16; j < 8;) {
    		keyBytes[k++] = keyBytes[j++];
    	}

    	final SecretKey key = new SecretKeySpec(keyBytes, "DESede");
    	final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
    	final Cipher decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
    	decipher.init(Cipher.DECRYPT_MODE, key, iv);

    	// final byte[] encData = new
    	// sun.misc.BASE64Decoder().decodeBuffer(message);
    	final byte[] plainText = decipher.doFinal(message);

    	return plainText;
    }
}