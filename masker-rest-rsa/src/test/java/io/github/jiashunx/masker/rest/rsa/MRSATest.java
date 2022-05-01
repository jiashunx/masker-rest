package io.github.jiashunx.masker.rest.rsa;

import io.github.jiashunx.masker.rest.framework.MRestServer;
import io.github.jiashunx.masker.rest.framework.serialize.MRestSerializer;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class MRSATest {

    private static MRSAKeyPair test(String keyAlgorithm, String signAlgorithm) {
        String content = "啊阿萨的浪费空间卢卡斯京东方立即释放的秦沛儒跑去哦i额u日破i确认破秋儿剖切啊撒旦解放拉萨觉得拉萨大家发的立法精神阿斯利康都是浪费大家了解拉萨酱豆腐跑去哦i微软破求而抛弃哦i恶如破弃如破i却皮肉iu钦佩日去陪日u强迫恶如脾气";
        System.out.println("原文: " + content);
        System.out.println("-----------------------------------------------------");
        MRSAKeyPair keyPair = MRSAHelper.generateRSAKeyPair(2048, keyAlgorithm);
        System.out.println("publicKey: " + keyPair.getPublicKeyBase64());
        System.out.println("privateKey: " + keyPair.getPrivateKeyBase64());
        System.out.println("-----------------------------------------------------");

        long startTimeMillis = 0L;
        long endTimeMillis = 0L;

        startTimeMillis = System.currentTimeMillis();
        String encryptBase64 = keyPair.publicKeyEncrypt(content);
        endTimeMillis = System.currentTimeMillis();
        System.out.println("公钥加密密文: " + encryptBase64);
        System.out.println("公钥加密耗时: " + (endTimeMillis - startTimeMillis) + "ms");

        startTimeMillis = System.currentTimeMillis();
        String decryptStr = keyPair.privateKeyDecrypt(encryptBase64);
        endTimeMillis = System.currentTimeMillis();
        System.out.println("私钥解密原文: " + decryptStr);
        System.out.println("私钥解密耗时: " + (endTimeMillis - startTimeMillis) + "ms");

        System.out.println("-----------------------------------------------------");

        startTimeMillis = System.currentTimeMillis();
        encryptBase64 = keyPair.privateKeyEncrypt(content);
        endTimeMillis = System.currentTimeMillis();
        System.out.println("私钥加密密文: " + encryptBase64);
        System.out.println("私钥加密耗时: " + (endTimeMillis - startTimeMillis) + "ms");

        startTimeMillis = System.currentTimeMillis();
        String signBase64 = keyPair.sign(signAlgorithm, encryptBase64);
        endTimeMillis = System.currentTimeMillis();
        System.out.println("私钥加密签名: " + signBase64);
        System.out.println("私钥签名耗时: " + (endTimeMillis - startTimeMillis) + "ms");

        startTimeMillis = System.currentTimeMillis();
        boolean verified = keyPair.verify(signAlgorithm, encryptBase64, signBase64);
        endTimeMillis = System.currentTimeMillis();
        System.out.println("公钥签名验证结果: " + verified);
        System.out.println("公钥签名验证耗时: " + (endTimeMillis - startTimeMillis) + "ms");

        startTimeMillis = System.currentTimeMillis();
        decryptStr = keyPair.publicKeyDecrypt(encryptBase64);
        endTimeMillis = System.currentTimeMillis();
        System.out.println("公钥解密原文: " + decryptStr);
        System.out.println("公钥解密耗时: " + (endTimeMillis - startTimeMillis) + "ms");

        System.out.println("-----------------------------------------------------");
        System.out.println("-----------------------------------------------------");
        return keyPair;
    }

    public static void main(String[] args) {
        test(MRSAHelper.DEFAULT_KEY_ALGORITHM, MRSAHelper.DEFAULT_SIGNATURE_ALGORITHM);
        test(MRSAHelper.DEFAULT_KEY_ALGORITHM, "SHA256withRSA");
        test(MRSAHelper.DEFAULT_KEY_ALGORITHM, "SHA1withRSA");
        System.out.println("-----------------------------------------------------");
        System.out.println("-----------------------------------------------------");
        System.out.println("-----------------------------------------------------");
        System.out.println("-----------------------------------------------------");
        System.out.println("-----------------------------------------------------");
        System.out.println(MRSAHelper.privateKeyDecrypt(
                "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"
                , "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDdjww5LDJccE1jApKPr4zsSiWANNaMoZDLLYY1+lEBVvKwG5F89QkojxO50F5I2iZTqbdJ9cToGbQNUm/GsXD1L5g7cKCzxU+6Cc56WwUpcymoprW6/GfifTpUqoszanv0nrP+qI6/JBeUfAnwb92/zrrl8o3Q4UxnpM2p0tgkqwdeiscypli3CvTXcSvSaoA6uJF/L5vS8kGMjROiQebPMgUFNcFXQMxXDqsPvclf5YYGWTHRcQBpQTmviupSYhBfg4XU6yEa4Vh6lA8XRAuvs7kk0RF9ITrFt/S/f8gYiJ/8TykUZfZeF8V1CUSZ7OesnbSYBIv+8l6WvZumpCafAgMBAAECggEANr8cJBx5rUHKvYZrNLoKNnDz50QnJ8TchHgRS9E4pv4XJdEKGDhsNOt10lpckd/lzJKJSetC5p9DUG9oU06RPOSv1zAzZXiCBNRbgvxuwBlG1/epvDSWbu7GtRkXIHNVgdKdY3W/IBgJA5XK1vCteLHamOtgg5bITGv6rdX9tsFeb/Dbg8qnNdR5tY9xUy8B1cMtfajZaFU6G2UNvnXfirlnmtc1uIs1EWlkbxMw/WcN8ZRNmsUELDmyPymvw7yZNdPN5YQsOE3TyNjjH26QRCKj7CvURWY4ggki16nAUmIga90Q+iggZ+cTlDGCk0dsZXk5fvtly0Kh7hxqTjtNUQKBgQDyKomKkFEK/Zu7sbmTaPjKMoVbK/U8kHmU3Cb9y0YHo2jC8EObARom8uMOBC8twoWBEcszA9m3KmTE3yqGHiBVI/HK7D+p1n7G3dwr9X0Oq1ZrKHrldmhCYDklKmXbHdHGlI0i7G5z7fSmpGijyneXYgEH1aYLDU/5NitAYT7K4wKBgQDqNyViHwbQtFliJ0+4fi3ww3sm4gj4VCqrWfDFAPH5yGZrVnFVq6jJTErif+FxRUpXNjfynRziF6iY+EcddIcVVVo3NGP8HN0M3/TQIBMwcj/mdb+bRA0ScczDfgMA/ZWTZ/zBJjXhWcra2cdw9kB8wwu6koz3x8x9lXX9ZBoWFQKBgQCaEUQGOwDEjErrJAwb47n88h1zvsi8KnKfqVzJNO0t6OG6vB9W22GnC/o2raTSaSKE1ixAHaAC717C/fRQ9ZO+dyra3mk2hXeM0BPGh91zWsCVVmCx4h48tHlG0u2QdS1EOLh+UouHCAoRhyHnquVhE4HyWRPVjjemJrdnsza+3wKBgQCyKgLg8hMgHaDEvnyxLiLKPBlpSZZjOLtAn2k8ouxvAteOHTKXZb4+ai+ovniCzp+vWcoTXxqCjlatDmgiaur6+nNm/Q510IsCaTZ0P4H5X19mcvmvJYQAHNsTdr7YOQ5YFEk7sFJaOxDJi9JgBqKlhUVdxm8/aRgQC+q2Nzh0xQKBgDINu2rJp27bm9/VNVda59/8TDfcP8IJ5oYYVpkAar8iesVO2D2xLf85KzolSN4LzGIr4JKuI7145dgLvGHz+tnzLzq/cYEH/FWR0XC6JPZdLbUJ4bLSgjEEaROqbXELQQRO8FySUacJgC+uwHmq5TkqjuCrfcPaKKQXBn3NBi15"
                , 2048,"RSA"));
        System.out.println(MRSAHelper.privateKeyDecrypt(
                "FXnE8v7upjJovaovcRyMOYfIyVE6J/ClgblxfGDDvUBHF2ZXTGCFq+PWcYcLqEqK/A+VWv9tJf+PUdZWPxtBUfUxxUrKr1B0aUuJ6kifMsZ4cgmYHoExnan8f2e7TfmdiyTEMynep0RyNveAAPjqqA8XmrPOakLuKj5ALKXfRRoRNcSj+7LdMEiFUwsKl5WI1WkUkAeDH8eDWzWTmzSgqeDrOz1E7w14bKQ5PgRg2B1O6drD57pRFksnFmJL4ZgG5yZR6TyURr0SZCRgnHW/fNDe9gU/60f9MrcB3VrG+yQe4ErkPpAP1NHYQVpFbB68cFiC6jDlQ0/aMJPMG19ms6TFl5sgn/uxuvtc44uZDUAqfI3SmNbQsfJIhOukcZSvtS7JZAXkthDedD0b11AFeUC9qcNFOicdi1rQXXPUnIICFJWUzku/G0NaR53PZ24DZ/WScBbME3xmzRnod01FBmyKvPvhRb9NOtfD1C3nQNZte6w1Mu8+ZjfEHF8fJuC3A8LGynRS0WRz48Y5hWtc/Ld0U2Y5diGeZXh8f53vW2WjIybLuWhSiYEXGTQZfmeeMnVZiLTv7+kIIhJE0FWVraGx7WIO8zfIy+nWwmXCuZVPvYRwH8iyFz7dQ20cqwNGtPJ7cUe/GjwonKA0nIx/vJnHwHdfpR9iOXi3d1xXDWhTStCndfQqgNX0gVKLkuHfB3s9s6yospKjAykLrSXlBF+6wG58QLqfoVUuH6kgdpR58y/ZVHpPisNTuPBdxxkWbq2rZ6Tgk3eA7NEKwcNmHwgLQuELCM8q6GXbz03m0R8nzCo0yVAjlIC3nicHDewdvgp8OwX4QC1T8HOsMUefx7HJFScLr4NdkcNfoOn+8beKPMdAs/JyUGsNNCBPHjoAd7QI61vve+hXf9mparwRFXxqBZFLcsBFVZoCrDzQC8NNaCmCvmgfHV7Bd1BOQYymUM8BwWRRjI+j0jheVHccEdXMTl3JLi2PUG3G4JknrHy/nQ0JDJZ8dkeJWUzaacywjUB3uT3WQfdE+dMOQNnK5bjKParCoA1nTNjE2TevP2FRfvoXlctlTp5snOY3c5GMVP/1pQpPXprCtsMgrOlE4dX9u89ocKnIIcKV+roi9rXy1egtQsb+HENkyJJ1lmg8ntsRrpqbPEkDigezT0mMA2MpKyWmWshUOoV33CeKnIbHTThwOd0gyDvqlSV8YHpTkO4DdWBbedB1zlrsH/fMNelSMVHFoMHZij/8j9fu+AjwUPpLSVsla1HfYFBLnsGeOJp7lrTr49+oqfhA3hL/U94judU2uQ2+9eO4Jvcuna3csIF1XkMeNmjkRX7tSpFy0B4hFcOsIzUAXcgrBmn05g=="
                , "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANkMehCca9X2Rqde1pmg1iMFp64Rkov1PnMpVB0tPLpXuyYNsA152/usMmLjfvrK1F1XWEFeEZOppHpMw0KDE1HOej471T7ayDCV3oNlRsSyoyH1CWk7IyXtg4C3rJ8IcaNnqNE+cOl0HIoBA5DTPpkWs14wnKEthwQHcmhA9bb5AgMBAAECgYBNDADIpL4i1al/AEttfYqGJBvBzrsWs5fXiXQyZzyHaV0lxPYZ0ogkbpv0Po10uUtpVvgdPCdBI5cfUuB9W8ofOKIFjKQxBx0H9KdtXv8E25vPgZxtyvpN6ofjhZ7t9diMdk8H2tFFgNmAoc1HLa7w50jSIF+vDVMob0HxxIaNgQJBAPJEiLnu+Jy/OKrrbQV+sOGjbPhh5phouE0X7tbHz+6VT+pgOriOf944ZdZrXkUcQdzNfKR9rbg069pm+NkROS8CQQDlWf+lW49chAx/u22h2JXP72L38f4nCnC+HImhe6h9cUCps6gFbo62TFsmkWh8ouJtBrOrQ8FP+GnYEQ7ZtzhXAkBbUYf0BebCNrXmfy0t+A42TwKhMU3ZPwfRT2cgoFp5RODFqJYZwOTj34BLbVcwV7FfyDjmS9UybQhF37CHKZ3TAkAUabs4M3sCB/q4z6zFT+qdlDfhy5cMUICSWyXgyonHiHHx6LyWpy1qikc+moiLN1Ah1gvdvW0SOhDpmzMQK5+HAkEAkoe3/HeZDhzajK9rY0urAyr3Qf0/+GzwyyIYVaUdKhZbJxQ5WgL6ZXmzKn4seaRG7no4ZEAql82EaiJzjPWGSw=="
                , 1024,"RSA"));
        System.out.println(MRSAHelper.privateKeyDecrypt(
                "hZbZDJv4Zm8c9yUflArhWFscGP8sykyxF7UK5DB1WECV7JRFbI2g73BIKRjwG86XPbvQE8KMXn/Bgb/eKs1MwKhwVbeEpx+zEbV1xsOrrsE9Q5DSSJ1/k5AYWUqsBtq/jOkX4DjlXxKYjusaGjVRLmx29QoHC5bKsm4nKPtl4CM24WGMjugUYeKCUle2vt+mttlvp9+i+bPJWrB79iFkiS+804Wo5EIVVyr8SzgGoNDtMxZzEsTEHrMhMQtcceYsdNVwuJ9GlfQj5F6VzskiBkpIylNI7rH99ApDpiJk5EvMpGzb/W2Y9L3Hu1CEx89DFY46SSBpi3sXt1/w5CttLl4VIhyaoA4Q0vaEb3KI2dCWdl/GsFO1UR51FDmuWu/PclZF4oDuvfpD4cf8w77ASGMwTwKk1OXQQE1XbRfuZT478HcSL6W52Lp92135SuSPn6mFqoEROvJyco4/hhbE7P4MI8GHg7f7aWtfRe/F7muWq9GZwY5r+p4wK0TuLfcMtaGxVNvNvZU56B6oqsl15YFdy95iSyMSbEzcmjZ0DAJafOeCKnoyH6Y3UTpAd0KLa3k+io83Y3n/YdrB/dki5HFEXWdF1m72xJYGviYlwntbvSiR25RlUDYUZjiph9SBJcIUzmgWqHFG1y5BfBmksVMLpYt/cJ2plwUVamXSn2/CO5b8Q51NNLedpi9EjlMLYh+9edNYfsd5ZxZkt3lOnHB+dHhdmjR39a6P0NHsKIi1dwnKD0AUBGTLzJBeC44dSla8ZNENvOuR+LaFDTpScOH8SM2bMkPZhpra7CD+rMTz6H+HS9s2FBvWOk/bNvyWEZT4ElITVNtEu/A9zuX6gRTQ7e1WUaC4RHY1XU+vXIqNVD1Vd4Xgr0wRCTMEZxmqv//RpoTX9PUFihOpKbqMwNPpCZLHUutp5/yWF2xt+IF0d7LZSygrp9ronvyEbyRdbvekWC5wvUlGv1FvVBzaofcycBtlokKzyhQl8hGMjshOF8thBh1zrQEQyUiqn/prVsMFDtyHHdm4/m5SE1viCjIKCRMrQcSpbnGzJG8ZS/9CPFZY1LjgTAw1eWTMc3rMyqsX/65DSEplN+3NzNrNP0x0cDG70kVnwsonYVq0zPOTsa1J7+M5t+FyG7Wuzj2SqOZmwPyHbcxMAp/U1hBFWEhcHWXCB6aKeSwZjJtt75bD3BOiJPJsCYF/ti/M1P3cV0TOrXOY2jPsKS4/PpvahqUl/RxSo29uXjijRpTbXQsEPIeRAXnACe4pHMKSZhjObFzXisMYYjxGOk8Zy3vgMAhmCdXPJb+8Ho6ImjJImma+Y4czVPI1n52W40+iN0qDIavM3AX5PLJICm1UVWcWKLhn4+Z9Dk+uCMhW2XVSMGSVOF6zN8yCmhlt3EKUvN2clEtUihplEcGT4+iZlKQoKpcBo6mQ+PiO+9QOmW/xi9NMwx6rVfsOVqrrxuOKMat6HwUTe/rIXwl5TaH6SZ5kaS7+bae2CDzscMAnbwYOvDEOrigBiA4u+RiIwgugrJLO"
                , "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANkMehCca9X2Rqde1pmg1iMFp64Rkov1PnMpVB0tPLpXuyYNsA152/usMmLjfvrK1F1XWEFeEZOppHpMw0KDE1HOej471T7ayDCV3oNlRsSyoyH1CWk7IyXtg4C3rJ8IcaNnqNE+cOl0HIoBA5DTPpkWs14wnKEthwQHcmhA9bb5AgMBAAECgYBNDADIpL4i1al/AEttfYqGJBvBzrsWs5fXiXQyZzyHaV0lxPYZ0ogkbpv0Po10uUtpVvgdPCdBI5cfUuB9W8ofOKIFjKQxBx0H9KdtXv8E25vPgZxtyvpN6ofjhZ7t9diMdk8H2tFFgNmAoc1HLa7w50jSIF+vDVMob0HxxIaNgQJBAPJEiLnu+Jy/OKrrbQV+sOGjbPhh5phouE0X7tbHz+6VT+pgOriOf944ZdZrXkUcQdzNfKR9rbg069pm+NkROS8CQQDlWf+lW49chAx/u22h2JXP72L38f4nCnC+HImhe6h9cUCps6gFbo62TFsmkWh8ouJtBrOrQ8FP+GnYEQ7ZtzhXAkBbUYf0BebCNrXmfy0t+A42TwKhMU3ZPwfRT2cgoFp5RODFqJYZwOTj34BLbVcwV7FfyDjmS9UybQhF37CHKZ3TAkAUabs4M3sCB/q4z6zFT+qdlDfhy5cMUICSWyXgyonHiHHx6LyWpy1qikc+moiLN1Ah1gvdvW0SOhDpmzMQK5+HAkEAkoe3/HeZDhzajK9rY0urAyr3Qf0/+GzwyyIYVaUdKhZbJxQ5WgL6ZXmzKn4seaRG7no4ZEAql82EaiJzjPWGSw=="
                , 1024,"RSA"));

        String publicKey = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIAZiJsFaadc0GHVd8h2fOmH9xQbbEWj63B8fstvN0NE+sEE01ZHB0t9MiBD1dxQtxUBOHx6FtRfWmpQDjU0fwkCAwEAAQ==";
        String privateKey = "MIIBUwIBADANBgkqhkiG9w0BAQEFAASCAT0wggE5AgEAAkEAgBmImwVpp1zQYdV3yHZ86Yf3FBtsRaPrcHx+y283Q0T6wQTTVkcHS30yIEPV3FC3FQE4fHoW1F9aalAONTR/CQIDAQABAkAaJJvjf1xLor6IaOd0SvI9OtlrChmzsLt5Dn9WB1GQ8bYMCz+2M816n1EnPDO/gaCj6zKmIoyjAP6r7EG8qW7tAiEAtqOKk0H9MTWxm72dBgPoOUOPVsNn8X1aNHT4LlldRP8CIQCzjdMkWwozhPciutLakcYQprU5pYZjej6/T6Xld+gT9wIgNd0FgWh0w46SUtwY8y0zz6+dFnmKri3J7K6SgxjX6QECIEDLc2BxQhMpbo2n4aNia5lir49n/kEzwiUDtxhB1wwtAiBPJdoNHogV5LZ88yw5nHA7SaAoNCR9QUiba9cCsjpvww==";
        String publicKeyEncryptBase64 = "NgTFONNwYwHj/SBT64KFx9D5N9pWm185o+j3LfX1H3a1+eM8FMN6d/kuv4DDVTKCAeE3X4DHKAgHW6wgiYeTYhusQm7T3K20t+O+7QV0dyfg2zuijKs0Su5TobSgwrF/+xhXJ6n5D18c9KdnMMiRRtPud9Lf0LlJOjSkjMfEas5P+zznR8wl12uOpZc/IePrB0mGPJRbZiZjsum0qzt+/T5AO027Q2OIFDWcFkRFE3OOeiQmjKK99RNMOTmK1uNBAXQ8+6Edo5WHacXH/rK6LOHeZB3vjwmWs6CtkJW5EctRXV4FeSCE3nBs156FdilCkGi5jmE1JtOoLcIpFaprjXVA2UOzFHG40/MsNAz8Iwab1VRWoGbSV3udLSu9AkI2KfXiPQ2wiHrTWiGm0CMFsU6UqG1QlFwgcQyuQmXAJvRDJcNwC2RlM4IfCvv9GvHaHJmPn67uobs/rQCF4LSZZYo2vLVgUGMuHSkwJ9hY5+Qvlt3XPWH2w228QpmR0Eajb75PExRwHy5CZBBw76csfnfOBCrPQIwxA9emPID4EdCBoJGbAlZh3Ej8P31AHlh4kPg7riE2+1suokrd9Sr/xyW/rnPPRHnQKYvQLkkq0fuDlAQ9r94XRQReSgLlTQ31QWzSb9rzCYfsgHvbA1oTcr6qJ4YFESC9sH33wV6l7zJF3r/y017vAA1jI1jVctNPAOmrqQZDVXuDo5l/4jUqTLS1bpfZudiWaZnwfH0GBIyROyJ2uXa0mBbLo0xYqsMNc10YeA0gAk765keZl/XicBetZryeTDPWYCIGQluM/5TnYsyPPMRnTD2Lx4+hVV9A4wHyVSbEXpYVv2wKiZO7s3Hv3g5kN8XttrUe4PPcQQnjy5cq80mZI1rxr+e0j8DfR1j0u3A3PgW1Mv6xvOEN40xckhk7EeuNOoHSI2T/NtwHlpkaxfKfTmrHbumOiWLksdfQ6n3JXXHSxCZtTJ8PMj/YjnwcmPJRbEDuZ8A19YelhQzhMub5spBLWbMWi5b2VZG9PpfzMJiAOnWPB2+4nEPRjSFieovg1hHIaUFQEIuWpKNO2X0OsK//ySCt83cpiJ8s3mAP0O+nyq6Af+Y0d2+6e+euWCRjNCdA+GvvaR9xR2hy6WEDAR4FENsiQtxBfnfoSn061Tk7rLwao/BcexvVlV1Jj/Nk/VJEeIxLZ4QsxsPlG3dhqBZNnB/fM3AbCGviRQ3UwjzAPyvQEV8xnkkUWsA6io22s4DfpyP9iJ+MwB07sU8oRbpL/2x83VmISLzNe0N5KGW8TB+rfVxCfh28zoBLeDqDPE7p1BlCrslggNhY9UcUWANm9sUoXsav70OKN0BG+M5OepqB90eVclLiUJlIXYYML2SzKu1VswQQaHiRBrg2ywfLH0VJYllCStQSyvwNZpIjbQs/5Fu/vcwUmkn9/gci9WCZL4rse4Efj5YQiyMv9Plo5QCt76RIQRrCzhvzwWMcv53HK9dmwH5LsFnnJtX1/cUkEpGrT8cMMQmJrQTCh30XrtrGa0Rz";
        System.out.println(MRSAHelper.privateKeyDecrypt(publicKeyEncryptBase64, privateKey, 512, "RSA"));

        /**
         * 1024 -> 公钥长度216, 私钥长度848
         *  base64 公钥长度288, 私钥长度1132
         * 512 -> 公钥长度128, 私钥长度460
         *  base64 公钥长度172, 私钥长度616
         *
         * 1024 公钥 + 512 私钥 ===> 密钥长度: 216 + 460
         * 密钥分别做base64编码 ===>           288 + 616
         * 密钥合并长度         ===>           904
         * 合并密钥base64编码   ===>           1208
         */
        MRSAKeyPair rsaKeyPairForServer = MRSAHelper.generateDefaultRSAKeyPair(1024);
        System.out.println("server: " + rsaKeyPairForServer.getPublicKeyBase64().length() + ", "
                + rsaKeyPairForServer.getPrivateKeyBase64().length());
        System.out.println("server: " + Base64.getEncoder().encodeToString(rsaKeyPairForServer.getPublicKeyBase64().getBytes(StandardCharsets.UTF_8)).length() + ", "
                + Base64.getEncoder().encodeToString(rsaKeyPairForServer.getPrivateKeyBase64().getBytes(StandardCharsets.UTF_8)).length());
        System.out.println("server: " + rsaKeyPairForServer.getPublicKeyPem().length() + ", "
                + rsaKeyPairForServer.getPrivateKeyPem().length());
        System.out.println("server: " + Base64.getEncoder().encodeToString(rsaKeyPairForServer.getPublicKeyPem().getBytes(StandardCharsets.UTF_8)).length() + ", "
                + Base64.getEncoder().encodeToString(rsaKeyPairForServer.getPrivateKeyPem().getBytes(StandardCharsets.UTF_8)).length());

        MRSAKeyPair rsaKeyPairForClient = MRSAHelper.generateDefaultRSAKeyPair(512);
        System.out.println("client: " + rsaKeyPairForClient.getPublicKeyBase64().length() + ", "
                + rsaKeyPairForClient.getPrivateKeyBase64().length());
        System.out.println("client: " + Base64.getEncoder().encodeToString(rsaKeyPairForClient.getPublicKeyBase64().getBytes(StandardCharsets.UTF_8)).length() + ", "
                + Base64.getEncoder().encodeToString(rsaKeyPairForClient.getPrivateKeyBase64().getBytes(StandardCharsets.UTF_8)).length());
        System.out.println("client: " + rsaKeyPairForClient.getPublicKeyPem().length() + ", "
                + rsaKeyPairForClient.getPrivateKeyPem().length());
        System.out.println("client: " + Base64.getEncoder().encodeToString(rsaKeyPairForClient.getPublicKeyPem().getBytes(StandardCharsets.UTF_8)).length() + ", "
                + Base64.getEncoder().encodeToString(rsaKeyPairForClient.getPrivateKeyPem().getBytes(StandardCharsets.UTF_8)).length());

        test2();
        test3();
    }

    private static void test3() {
        System.out.println("=============================== Pem转换测试");
        MRSAKeyPair keyPair = MRSAHelper.generateRSAKeyPair(2048, MRSAHelper.DEFAULT_KEY_ALGORITHM);
        System.out.println("公钥\n" + keyPair.getPublicKeyBase64());
        System.out.println("私钥\n" + keyPair.getPrivateKeyBase64());
        System.out.println("公钥PEM\n" + keyPair.getPublicKeyPem());
        System.out.println("私钥PEM\n" + keyPair.getPrivateKeyPem());
        System.out.println("公钥PEM转换:\n" + MRSAHelper.getPublicKeyBase64(MRSAHelper.readPublicKeyFromPem(MRSAHelper.DEFAULT_KEY_ALGORITHM, keyPair.getPublicKeyPem())));
        System.out.println("私钥PEM转换:\n" + MRSAHelper.getPrivateKeyBase64(MRSAHelper.readPrivateKeyFromPem(MRSAHelper.DEFAULT_KEY_ALGORITHM, keyPair.getPrivateKeyPem())));
    }

    private static void test2() {
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx 前后端加解密测试");
        MRSAKeyPair rsaKeyPair = test(MRSAHelper.DEFAULT_KEY_ALGORITHM, "SHA256withRSA");
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("publicKey", rsaKeyPair.getPublicKeyBase64());
        keyMap.put("publicKeyPem", rsaKeyPair.getPublicKeyPem());
        keyMap.put("privateKey", rsaKeyPair.getPrivateKeyBase64());
        keyMap.put("privateKeyPem", rsaKeyPair.getPrivateKeyPem());
        // http://127.0.0.1:10015/demo/test-rsa.html
        new MRestServer(10015)
            .context("/demo")
                .addDefaultClasspathResource()
                .get("/key", (request, response) -> {
                    response.write(MRestSerializer.objectToJsonBytes(keyMap));
                })
            .getRestServer()
            .start();
    }
}
