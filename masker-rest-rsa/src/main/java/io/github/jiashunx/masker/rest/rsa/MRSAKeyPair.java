package io.github.jiashunx.masker.rest.rsa;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Objects;

public class MRSAKeyPair {

    private final KeyPair keyPair;
    private final MRSAKeySize keySize;
    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    private final String publicKeyBase64;
    private final String privateKeyBase64;
    private final String publicKeyPem;
    private final String privateKeyPem;
    private final String keyAlgorithm;

    public MRSAKeyPair(KeyPair keyPair, MRSAKeySize keySize) {
        this.keyPair = Objects.requireNonNull(keyPair);
        this.keySize = Objects.requireNonNull(keySize);
        this.publicKey = this.keyPair.getPublic();
        this.privateKey = this.keyPair.getPrivate();
        this.publicKeyBase64 = MRSAHelper.getPublicKeyBase64(this.keyPair);
        this.privateKeyBase64 = MRSAHelper.getPrivateKeyBase64(this.keyPair);
        this.publicKeyPem = MRSAHelper.getPublicKeyPem(this.keyPair);
        this.privateKeyPem = MRSAHelper.getPrivateKeyPem(this.keyPair);
        this.keyAlgorithm = this.publicKey.getAlgorithm();
    }

    public MRSAKeyPair(KeyPair keyPair, int keysize) {
        this(keyPair, MRSAKeySize.parseKeySize(keysize));
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public String getPublicKeyBase64() {
        return publicKeyBase64;
    }

    public String getPrivateKeyBase64() {
        return privateKeyBase64;
    }

    public String getPublicKeyPem() {
        return publicKeyPem;
    }

    public String getPrivateKeyPem() {
        return privateKeyPem;
    }

    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public String publicKeyEncrypt(String content) {
        return MRSAHelper.publicKeyEncrypt(content, getPublicKey(), this.keySize.getKeysize());
    }
    public String privateKeyDecrypt(String encryptBase64) {
        return MRSAHelper.privateKeyDecrypt(encryptBase64, getPrivateKey(), this.keySize.getKeysize());
    }
    public String privateKeyEncrypt(String content) {
        return MRSAHelper.privateKeyEncrypt(content, getPrivateKey(), this.keySize.getKeysize());
    }
    public String publicKeyDecrypt(String encryptBase64) {
        return MRSAHelper.publicKeyDecrypt(encryptBase64, getPublicKey(), this.keySize.getKeysize());
    }
    public String sign(String signAlgorithm, String encryptBase64) {
        return MRSAHelper.sign(signAlgorithm, encryptBase64, getPrivateKey());
    }
    public boolean verify(String signAlgorithm, String encryptBase64, String signBase64) {
        return MRSAHelper.verify(signAlgorithm, encryptBase64, getPublicKey(), signBase64);
    }
    public String defaultSign(String encryptBase64) {
        return MRSAHelper.defaultSign(encryptBase64, getPrivateKey());
    }
    public boolean defaultVerify(String encryptBase64, String signBase64) {
        return MRSAHelper.defaultVerify(encryptBase64, getPublicKey(), signBase64);
    }

}
