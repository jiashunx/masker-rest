package io.github.jiashunx.masker.rest.rsa;

import io.github.jiashunx.masker.rest.rsa.exception.*;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 加密算法及签名算法均固定, 可自由调整.
 * (客户端-服务端) 公钥加密 ------------------------------ 私钥解密
 * (服务端-客户端) 私钥加密 - (私钥签名 - 公钥验证签名) - 公钥解密
 */
public final class MRSAHelper {

    /**
     * 加密算法
     */
    public static final String DEFAULT_KEY_ALGORITHM = "RSA";

    /**
     * 签名算法
     */
    public static final String DEFAULT_SIGNATURE_ALGORITHM = "MD5withRSA";

    public static MRSAKeyPair generateDefaultRSAKeyPair() {
        return generateRSAKeyPair(MRSAKeySize.$1024, DEFAULT_KEY_ALGORITHM);
    }

    public static MRSAKeyPair generateDefaultRSAKeyPair(int keysize) {
        return generateRSAKeyPair(keysize, DEFAULT_KEY_ALGORITHM);
    }

    public static MRSAKeyPair generateDefaultRSAKeyPair(MRSAKeySize keySize) {
        return generateRSAKeyPair(keySize, DEFAULT_KEY_ALGORITHM);
    }

    public static MRSAKeyPair generateDefaultRSAKeyPair(String keyAlgorithm) {
        return generateRSAKeyPair(MRSAKeySize.$1024, keyAlgorithm);
    }

    public static MRSAKeyPair generateRSAKeyPair(MRSAKeySize keySize, String keyAlgorithm) {
        return new MRSAKeyPair(generateKeyPair(keySize, keyAlgorithm), keySize);
    }

    public static MRSAKeyPair generateRSAKeyPair(int keysize, String keyAlgorithm) {
        return new MRSAKeyPair(generateKeyPair(keysize, keyAlgorithm), keysize);
    }

    public static KeyPair generateDefaultKeyPair() {
        return generateKeyPair(MRSAKeySize.$1024, DEFAULT_KEY_ALGORITHM);
    }

    public static KeyPair generateDefaultKeyPair(int keysize) {
        return generateKeyPair(keysize, DEFAULT_KEY_ALGORITHM);
    }

    public static KeyPair generateDefaultKeyPair(MRSAKeySize keySize) {
        return generateKeyPair(keySize, DEFAULT_KEY_ALGORITHM);
    }

    public static KeyPair generateDefaultKeyPair(String keyAlgorithm) {
        return generateKeyPair(MRSAKeySize.$1024, keyAlgorithm);
    }

    public static KeyPair generateKeyPair(MRSAKeySize keysize, String keyAlgorithm) {
        return generateKeyPair(keysize.getKeysize(), keyAlgorithm);
    }

    public static KeyPair generateKeyPair(int keysize, String keyAlgorithm) {
        try {
            // 创建秘钥对生成对象(密钥生成器)
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyAlgorithm);
            // 创建安全的随机数
            SecureRandom secureRandom = new SecureRandom(String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
            // 初始化密钥对
            keyPairGenerator.initialize(keysize, secureRandom);
            // 生成密钥对, 此时公钥私钥已经在keyPair对象中
            return keyPairGenerator.generateKeyPair();
        } catch (Throwable throwable) {
            throw new MRSACommonException(String.format("generate KeyPair failed, keysize=[%d], keyAlgorithm=[%s]", keysize, keyAlgorithm), throwable);
        }
    }

    public static String getPublicKeyBase64(KeyPair keyPair){
        if (keyPair == null) {
            return null;
        }
        return getPublicKeyBase64(keyPair.getPublic());
    }

    public static String getPublicKeyBase64(PublicKey publicKey) {
        if (publicKey == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public static String getPublicKeyPem(KeyPair keyPair) {
        if (keyPair == null) {
            return null;
        }
        return getPublicKeyPem(keyPair.getPublic());
    }

    public static String getPublicKeyPem(PublicKey publicKey) {
        return getPublicKeyPem("PUBLIC KEY", publicKey);
    }

    public static String getPublicKeyPem(String type, PublicKey publicKey) {
        if (publicKey == null) {
            return null;
        }
        try (StringWriter stringWriter = new StringWriter()) {
            PemWriter pemWriter = new PemWriter(stringWriter);
            pemWriter.writeObject(new PemObject(type, publicKey.getEncoded()));
            pemWriter.flush();
            return stringWriter.toString();
        } catch (Throwable throwable) {
            throw new MRSAKeyTransferException(String.format("generate RSA publicKey pem failed, type=[%s], publicKeyBase64=[%s]", type, getPublicKeyBase64(publicKey)), throwable);
        }
    }

    public static PublicKey getPublicKey(String publicKeyBase64, String keyAlgorithm) {
        try {
            // 公钥Base64解码
            byte[] decodePublicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
            // 调用X509EncodedKeySpec对象,转换格式
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodePublicKeyBytes);
            // 调用密钥工厂
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
            return keyFactory.generatePublic(keySpec);
        } catch (Throwable throwable) {
            throw new MRSAKeyTransferException(String.format("generate RSA publicKey failed, publicKeyBase64=[%s]", publicKeyBase64), throwable);
        }
    }

    public static String getPrivateKeyBase64(KeyPair keyPair) {
        if (keyPair == null) {
            return null;
        }
        return getPrivateKeyBase64(keyPair.getPrivate());
    }

    public static String getPrivateKeyBase64(PrivateKey privateKey) {
        if (privateKey == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    public static String getPrivateKeyPem(KeyPair keyPair) {
        if (keyPair == null) {
            return null;
        }
        return getPrivateKeyPem(keyPair.getPrivate());
    }

    public static String getPrivateKeyPem(PrivateKey privateKey) {
        return getPrivateKeyPem("PRIVATE KEY", privateKey);
    }

    public static String getPrivateKeyPem(String type, PrivateKey privateKey) {
        if (privateKey == null) {
            return null;
        }
        try (StringWriter stringWriter = new StringWriter()) {
            PemWriter pemWriter = new PemWriter(stringWriter);
            pemWriter.writeObject(new PemObject(type, privateKey.getEncoded()));
            pemWriter.flush();
            return stringWriter.toString();
        } catch (Throwable throwable) {
            throw new MRSAKeyTransferException(String.format("generate RSA privateKey pem failed, type=[%s], privateKeyBase64=[%s]", type, getPrivateKeyBase64(privateKey)), throwable);
        }
    }

    public static PrivateKey getPrivateKey(String privateKeyBase64, String keyAlgorithm) {
        try {
            // 私钥Base64解码
            byte[] decodePrivateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
            // 调用PKCS8EncodedKeySpec对象
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodePrivateKeyBytes);
            // 调用密钥工厂
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
            return keyFactory.generatePrivate(keySpec);
        } catch (Throwable throwable) {
            throw new MRSAKeyTransferException(String.format("generate RSA privateKey failed, privateKeyBase64=[%s]", privateKeyBase64), throwable);
        }
    }

    public static String publicKeyEncrypt(String content, KeyPair keyPair, int keysize) {
        return publicKeyEncrypt(content, keyPair.getPublic(), keysize);
    }

    public static String publicKeyEncrypt(String content, String publicKeyBase64, int keysize, String keyAlgorithm) {
        return publicKeyEncrypt(content, getPublicKey(publicKeyBase64, keyAlgorithm), keysize);
    }

    public static String defaultPublicKeyEncrypt(String content, String publicKeyBase64, int keysize) {
        return publicKeyEncrypt(content, publicKeyBase64, keysize, DEFAULT_KEY_ALGORITHM);
    }

    public static String publicKeyEncrypt(String content, PublicKey publicKey, int keysize) {
        try {
            return doEncrypt(content, publicKey, keysize);
        } catch (Throwable throwable) {
            throw new MRSAEncryptException(String.format("RSA publicKey encrypt failed, keysize=[%d], content=[%s]"
                    , keysize, content), throwable);
        }
    }

    public static String privateKeyDecrypt(String encryptBase64, KeyPair keyPair, int keysize) {
        return privateKeyDecrypt(encryptBase64, keyPair.getPrivate(), keysize);
    }

    public static String privateKeyDecrypt(String encryptBase64, String privateKeyBase64, int keysize, String keyAlgorithm) {
        return privateKeyDecrypt(encryptBase64, getPrivateKey(privateKeyBase64, keyAlgorithm), keysize);
    }

    public static String defaultPrivateKeyDecrypt(String encryptBase64, String privateKeyBase64, int keysize) {
        return privateKeyDecrypt(encryptBase64, privateKeyBase64, keysize, DEFAULT_KEY_ALGORITHM);
    }

    public static String privateKeyDecrypt(String encryptBase64, PrivateKey privateKey, int keysize) {
        try {
            return doDecrypt(encryptBase64, privateKey, keysize);
        } catch (Throwable throwable) {
            throw new MRSADecryptException(String.format("RSA privateKey decrypt failed, keysize=[%d], encryptBase64=[%s]"
                    , keysize, encryptBase64), throwable);
        }
    }

    public static String privateKeyEncrypt(String content, KeyPair keyPair, int keysize) {
        return privateKeyEncrypt(content, keyPair.getPrivate(), keysize);
    }

    public static String privateKeyEncrypt(String content, String privateKeyBase64, int keysize, String keyAlgorithm) {
        return privateKeyEncrypt(content, getPrivateKey(privateKeyBase64, keyAlgorithm), keysize);
    }

    public static String defaultPrivateKeyEncrypt(String content, String privateKeyBase64, int keysize) {
        return privateKeyEncrypt(content, privateKeyBase64, keysize, DEFAULT_KEY_ALGORITHM);
    }

    public static String privateKeyEncrypt(String content, PrivateKey privateKey, int keysize) {
        try {
            return doEncrypt(content, privateKey, keysize);
        } catch (Throwable throwable) {
            throw new MRSAEncryptException(String.format("RSA privateKey encrypt failed, keysize=[%d], content=[%s]"
                    , keysize, content), throwable);
        }
    }

    public static String publicKeyDecrypt(String encryptBase64, KeyPair keyPair, int keysize) {
        return publicKeyDecrypt(encryptBase64, keyPair.getPublic(), keysize);
    }

    public static String publicKeyDecrypt(String encryptBase64, String publicKeyBase64, int keysize, String keyAlgorithm) {
        return publicKeyDecrypt(encryptBase64, getPublicKey(publicKeyBase64, keyAlgorithm), keysize);
    }

    public static String defaultPublicKeyDecrypt(String encryptBase64, String publicKeyBase64, int keysize) {
        return publicKeyDecrypt(encryptBase64, publicKeyBase64, keysize, DEFAULT_KEY_ALGORITHM);
    }

    public static String publicKeyDecrypt(String encryptBase64, PublicKey publicKey, int keysize) {
        try {
            return doDecrypt(encryptBase64, publicKey, keysize);
        } catch (Throwable throwable) {
            throw new MRSADecryptException(String.format("RSA publicKey decrypt failed, keysize=[%d],, encryptBase64=[%s]"
                    , keysize, encryptBase64), throwable);
        }
    }

    public static String doEncrypt(String content, Key key, int keysize) throws Throwable {
        try (ByteArrayOutputStream baos =new ByteArrayOutputStream()) {
            // 调用Java加密的Cipher对象
            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
            // 第一个参数表示这是加密模式，第二个参数表示密钥
            cipher.init(Cipher.ENCRYPT_MODE, key);
            // 使用URLEncoder对中文进行编码
            byte[] contentBytes = URLEncoder.encode(content, "UTF-8").getBytes();
            int inputLen = contentBytes.length;
            int offset = 0;
            int i = 0;
            int encryptOffSet = keysize / 8 - 11;
            // 分段加密
            while (inputLen - offset > 0) {
                byte [] cache;
                if (inputLen - offset > encryptOffSet) {
                    cache = cipher.doFinal(contentBytes, offset, encryptOffSet);
                } else {
                    cache = cipher.doFinal(contentBytes, offset,inputLen - offset);
                }
                baos.write(cache);
                i++;
                offset = encryptOffSet * i;
            }
            // 加密结果进行Base64编码
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    public static String doDecrypt(String encryptBase64, Key key, int keysize) throws Throwable {
        try (ByteArrayOutputStream baos =new ByteArrayOutputStream()) {
            // 调用Java加密的Cipher对象，
            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
            // 第一个参数表示这是解密模式，第二个参数表示密钥
            cipher.init(Cipher.DECRYPT_MODE, key);
            // 密文进行Base64解码
            byte[] encryptBytes = Base64.getDecoder().decode(encryptBase64.getBytes());
            int inputLen = encryptBytes.length;
            int offset = 0;
            int i = 0;
            int decryptOffSet = keysize / 8;
            while (inputLen - offset > 0) {
                byte[] cache;
                if (inputLen - offset > decryptOffSet){
                    cache = cipher.doFinal(encryptBytes, offset, decryptOffSet);
                } else {
                    cache = cipher.doFinal(encryptBytes, offset,inputLen - offset);
                }
                baos.write(cache, 0, cache.length);
                i++;
                offset = decryptOffSet * i;
            }
            return URLDecoder.decode(new String(baos.toByteArray(), StandardCharsets.UTF_8), "UTF-8");
        }
    }

    public static String defaultSign(String encryptBase64, KeyPair keyPair) {
        return sign(DEFAULT_SIGNATURE_ALGORITHM, encryptBase64, keyPair);
    }

    public static String defaultSign(String encryptBase64, String privateKeyBase64, String keyAlgorithm) {
        return sign(DEFAULT_SIGNATURE_ALGORITHM, encryptBase64, privateKeyBase64, keyAlgorithm);
    }

    public static String defaultSign(String encryptBase64, PrivateKey privateKey) {
        return sign(DEFAULT_SIGNATURE_ALGORITHM, encryptBase64, privateKey);
    }

    public static String sign(String signAlgorithm, String encryptBase64, KeyPair keyPair) {
        return sign(signAlgorithm, encryptBase64, keyPair.getPrivate());
    }

    public static String sign(String signAlgorithm, String encryptBase64, String privateKeyBase64, String keyAlgorithm) {
        return sign(signAlgorithm, encryptBase64, getPrivateKey(privateKeyBase64, keyAlgorithm));
    }

    public static String sign(String signAlgorithm, String encryptBase64, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance(signAlgorithm);
            SecureRandom secureRandom = new SecureRandom(String.valueOf(System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
            signature.initSign(privateKey, secureRandom);
            // 使用原始密文进行签名
            signature.update(Base64.getDecoder().decode(encryptBase64));
            // 原始签名进行Base64编码
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Throwable throwable) {
            throw new MRSASignatureException(String.format("RSA privateKey sign failed, encryptBase64=[%s]", encryptBase64), throwable);
        }
    }

    public static boolean defaultVerify(String encryptBase64, KeyPair keyPair, String signBase64) {
        return verify(DEFAULT_SIGNATURE_ALGORITHM, encryptBase64, keyPair, signBase64);
    }

    public static boolean defaultVerify(String encryptBase64, String publicKeyBase64, String signBase64, String keyAlgorithm) {
        return verify(DEFAULT_SIGNATURE_ALGORITHM, encryptBase64, getPublicKey(publicKeyBase64, keyAlgorithm), signBase64);
    }

    public static boolean defaultVerify(String encryptBase64, PublicKey publicKey, String signBase64) {
        return verify(DEFAULT_SIGNATURE_ALGORITHM, encryptBase64, publicKey, signBase64);
    }

    public static boolean verify(String signAlgorithm, String encryptBase64, KeyPair keyPair, String signBase64) {
        return verify(signAlgorithm, encryptBase64, keyPair.getPublic(), signBase64);
    }

    public static boolean verify(String signAlgorithm, String encryptBase64, String publicKeyBase64, String signBase64, String keyAlgorithm) {
        return verify(signAlgorithm, encryptBase64, getPublicKey(publicKeyBase64, keyAlgorithm), signBase64);
    }

    public static boolean verify(String signAlgorithm, String encryptBase64, PublicKey publicKey, String signBase64) {
        try {
            Signature signature = Signature.getInstance(signAlgorithm);
            signature.initVerify(publicKey);
            // 使用原始密文进行签名验证
            signature.update(Base64.getDecoder().decode(encryptBase64));
            // 使用原始签名进行签名验证
            return signature.verify(Base64.getDecoder().decode(signBase64));
        } catch (Throwable throwable) {
            throw new MRSASignatureException(String.format("RSA publicKey verify failed, signBase64=[%s], encryptBase64=[%s]"
                    , signBase64, encryptBase64), throwable);
        }
    }

}
