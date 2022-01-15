package io.github.jiashunx.masker.rest.rsa;

import java.util.ArrayList;
import java.util.List;

public enum MRSAKeySize {

    $512(512),
    $1024(1024),
    $2048(2048);
    private final int keysize;
    private final int encryptOffSet;
    private final int decryptOffSet;
    MRSAKeySize(int keysize) {
        if ((keysize / 512) < 1 || (keysize % 512) != 0) {
            throw new IllegalArgumentException();
        }
        this.keysize = keysize;
        this.decryptOffSet = this.keysize / 8;
        this.encryptOffSet = this.decryptOffSet - 11;
    }

    public int getKeysize() {
        return keysize;
    }

    public int getEncryptOffSet() {
        return encryptOffSet;
    }

    public int getDecryptOffSet() {
        return decryptOffSet;
    }

    public static MRSAKeySize parseKeySize(int keysize) {
        MRSAKeySize[] keySizeArr = values();
        List<Integer> keys = new ArrayList<>(keySizeArr.length);
        for (MRSAKeySize obj: keySizeArr) {
            keys.add(obj.getKeysize());
            if (obj.getKeysize() == keysize) {
                return obj;
            }
        }
        throw new IllegalArgumentException(String.format("illegal keysize, expected keysize value: %s", keys.toString()));
    }

}
