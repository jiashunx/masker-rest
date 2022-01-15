
// const JSEncrypt = JSEncrypt;
const JSRSASign_CryptoJS = CryptoJS;
const JSRSASign_KJUR = KJUR;
const JSRSASign_KEYUTIL = KEYUTIL;
const JSRSASign_b64tohex = b64tohex;
const JSRSASign_hex2b64 = hex2b64;

/**
 * 基于JSEncrypt实现公钥加密私钥解密
 * 需注意: JSEncrypt不支持私钥加密公钥解密
 */
const MCrypto = {
    publicKeyEncrypt: function (publicKey, content) {
        try {
            let jsencrypt = new JSEncrypt()
            jsencrypt.setPublicKey(publicKey)
            return this.doEncrypt(jsencrypt, content)
        } catch (ex) {
            console.error(`RSA publicKey encrypt failed, content=[${content}]`, ex)
        }
        return ""
    },
    privateKeyDecrypt: function (privateKey, encryptBase64) {
        try {
            let jsencrypt = new JSEncrypt()
            jsencrypt.setPrivateKey(privateKey)
            return this.doDecrypt(jsencrypt, encryptBase64)
        } catch (ex) {
            console.error(`RSA privateKey decrypt failed, encryptBase64=[${encryptBase64}]`, ex)
        }
        return ""
    },
    /*Deprecated*/
    sign: function(privateKey, encryptBase64) {
        return this.signWithDigest(privateKey, encryptBase64, JSRSASign_CryptoJS.SHA256, "sha256")
    },
    /*Deprecated*/
    signWithDigest: function(privateKey, encryptBase64, digestMethod, digestName) {
        try {
            let jsencrypt = new JSEncrypt()
            jsencrypt.setPrivateKey(privateKey)
            return jsencrypt.sign(encryptBase64, digestMethod, digestName)
        } catch (ex) {
            console.error(`RSA privateKey sign failed, encryptBase64=[${encryptBase64}], algorithm=[${digestAlgorithm}]`, ex)
        }
        return false
    },
    /*Deprecated*/
    verify: function(publicKey, encryptBase64, signature) {
        return this.verifyWithDigest(publicKey, encryptBase64, signature, JSRSASign_CryptoJS.SHA256)
    },
    /*Deprecated*/
    verifyWithDigest: function(publicKey, encryptBase64, signature, digestMethod) {
        try {
            var jsencrypt = new JSEncrypt()
            jsencrypt.setPublicKey(publicKey)
            return jsencrypt.verify(encryptBase64, signature, digestMethod)
        } catch (ex) {
            console.error(`RSA publicKey verify signature failed, encryptBase64=[${encryptBase64}], sign=[${sign}]`, ex)
        }
        return false
    },
    doEncrypt: function (jsencrypt, content) {
        try {
            // 兼容中文
            var urlEncodedContent = encodeURI(content)
            // 分段加密长度
            let maxLength = ((jsencrypt.getKey().n.bitLength() + 7) >> 3) - 11
            let subStr = "", encryptStr = ""
            let subBgnIndex = 0, subEndIndex = 0
            let bitLen = 0, tmpPoint = 0
            let contentLen = urlEncodedContent.length
            for(let index = 0; index < contentLen; index++) {
                //js 是使用 Unicode 编码的，每个字符所占用的字节数不同
                let charCode = urlEncodedContent.charCodeAt(index)
                if (charCode <= 0x007f) {
                    bitLen += 1
                } else if (charCode <= 0x07ff) {
                    bitLen += 2
                } else if (charCode <= 0xffff) {
                    bitLen += 3
                } else {
                    bitLen += 4
                }
                //字节数到达上限，获取子字符串加密并追加到总字符串后。更新下一个字符串起始位置及字节计算。
                if(bitLen === maxLength || bitLen % maxLength >= 114) {
                    subStr = urlEncodedContent.substring(subBgnIndex, subEndIndex)
                    encryptStr += jsencrypt.getKey().encrypt(subStr)
                    subBgnIndex = subEndIndex
                    bitLen = bitLen - tmpPoint
                } else {
                    subEndIndex = index
                    tmpPoint = bitLen
                }
            }
            if (subBgnIndex < contentLen) {
                subStr = urlEncodedContent.substring(subBgnIndex, contentLen)
                encryptStr += jsencrypt.getKey().encrypt(subStr)
            }
            // hex to base64
            return JSRSASign_hex2b64(encryptStr)
        } catch (ex) {
            throw ex
        }
    },
    doDecrypt: function (jsencrypt, encryptBase64) {
        try {
            // 分段解密长度
            let maxOffset = ((jsencrypt.getKey().n.bitLength() + 7) >> 3) * 2
            // base64 to hex
            let encryptHexStr = JSRSASign_b64tohex(encryptBase64)
            let decryptStr = ""
            let encryptHexArr = encryptHexStr.match(new RegExp(".{1," + maxOffset  + "}", 'g'))
            encryptHexArr.forEach((hexStr) => {
                decryptStr += jsencrypt.getKey().decrypt(hexStr)
            })
            // 兼容中文
            return decodeURI(decryptStr)
        } catch (ex) {
            throw ex
        }
    }
}

/**
 * 基于jsrsasign实现前端签名及验签, 可用于前后端交互验签处理(私钥签名, 公钥验签)
 */
const MSign = {
    sign: function (privateKeyPem, encryptBase64, algorithm = "SHA256withRSA") {
        try {
            let sign = new JSRSASign_KJUR.crypto.Signature({ "alg": algorithm })
            sign.init(JSRSASign_KEYUTIL.getKeyFromPlainPrivatePKCS8PEM(privateKeyPem))
            sign.updateHex(JSRSASign_b64tohex(encryptBase64))
            return JSRSASign_hex2b64(sign.sign())
        } catch (ex) {
            console.error(`RSA privakeKeyPem signature failed, encryptBase64=${encryptBase64}`, ex)
        }
        return false
    },
    verify: function (publicKeyPem, encryptBase64, signatureBase64, algorithm= "SHA256withRSA") {
        try {
            let sign = new JSRSASign_KJUR.crypto.Signature({ "alg": algorithm })
            sign.init(JSRSASign_KEYUTIL.getKey(publicKeyPem))
            sign.updateHex(JSRSASign_b64tohex(encryptBase64))
            return sign.verify(JSRSASign_b64tohex(signatureBase64))
        } catch (ex) {
            console.error(`RSA publicKeyPem verify failed, signatureBase64=${signatureBase64}, encryptBase64=${encryptBase64}`, ex)
        }
        return false
    }
}

