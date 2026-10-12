package io.github.jiashunx.masker.rest.jjwt;

import io.github.jiashunx.masker.rest.framework.util.StringUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * jwt util.
 *
 * JWT生成验证流程:
 * 1. 服务端接受登录请求, 生成jwt token返回客户端(响应内容或header或cookie)
 * 2. 客户端读取jwt token, 下次请求添加到请求头Autoorization: Bearer token (以Bearer开头)
 * 3. 服务端接受请求, 对token进行验证签发及过期时间, 对权限会话等进行验证.
 *
 * JWT结构：A.B.C
 *  A: header, 存放描述jwt元数据的json对象字符串, A=Base64URLEncode(header)
 *  B: payload, 存放用户信息, B=Base64URLEncode(payload)
 *  C:signature, 签名, C=HMACSHA256(A+"."+B, secret)
 *
 * Base64URL算法与Base64算法区别: Base64中用到的"+", "=", "/"由于在URL中有特殊含义，因此在Base64URL算法中进行替换: ="取消，"+"使用"-"替换，"/"使用"_"替换
 * @author jiashunx
 */
public class MRestJWTHelper {

    private static final Logger logger = LoggerFactory.getLogger(MRestJWTHelper.class);

    public static final int DEFAULT_TIMEOUT_MILLIS = 10 * 60;
    public static final String JWT_TOKEN_PREFIX = "Bearer ";

    private static final String CLAIM_KEY_IAT = "iat";
    private static final String CLAIM_KEY_EXP = "exp";
    private static final String HEADER_KEY_ALG = "alg";
    private static final String HEADER_KEY_TYP = "typ";
    private static final String HEADER_VALUE_TYP = "JWT";

    /**
     * 签名密钥(基于原始密钥的SHA-256摘要派生, 保证长度满足HMAC-SHA256要求).
     */
    private final SecretKey secretKey;
    /**
     * token失效时间, 为0永不过期.
     */
    private final int timeoutSeconds;

    public MRestJWTHelper() {
        this(DEFAULT_TIMEOUT_MILLIS);
    }

    public MRestJWTHelper(String secretKey) {
        this(secretKey, DEFAULT_TIMEOUT_MILLIS);
    }

    public MRestJWTHelper(int timeoutSeconds) {
        this(UUID.randomUUID().toString(), timeoutSeconds);
    }

    public MRestJWTHelper(String secretKey, int timeoutSeconds) {
        if (StringUtils.isEmpty(secretKey) || timeoutSeconds < 0) {
            throw new IllegalArgumentException();
        }
        this.secretKey = Keys.hmacShaKeyFor(sha256(secretKey));
        this.timeoutSeconds = timeoutSeconds;
    }

    private static byte[] sha256(String text) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 not supported", exception);
        }
    }

    public String newToken() {
        return newToken(null);
    }

    public String newToken(Map<String, Object> claims) {
        return newToken(claims, null);
    }

    public String newToken(Map<String, Object> claims, Map<String, Object> headers) {
        long currentTimeMillis = System.currentTimeMillis();
        long timeoutMillis = (this.timeoutSeconds == 0L ? DEFAULT_TIMEOUT_MILLIS : this.timeoutSeconds) * 1000L;
        Map<String, Object> _claims = new HashMap<>();
        Map<String, Object> _headers = new HashMap<>();
        Optional.ofNullable(claims).ifPresent(_claims::putAll);
        Optional.ofNullable(headers).ifPresent(_headers::putAll);
        try {
            return Jwts.builder()
                    .setClaims(_claims)
                    .setHeader(_headers)
                    .setHeaderParam(HEADER_KEY_TYP, HEADER_VALUE_TYP)
                    .issuedAt(new Date(currentTimeMillis))
                    .expiration(new Date(currentTimeMillis + timeoutMillis))
                    .signWith(secretKey)
                    .compact();
        } catch (Throwable throwable) {
            logger.error("create jwt token failed, claims: {}, headers: {}", claims, headers, throwable);
            return StringUtils.EMPTY;
        }
    }

    public String addTokenHeader(String jwtToken) {
        String token = Objects.requireNonNull(jwtToken);
        if (!jwtToken.trim().startsWith(JWT_TOKEN_PREFIX)) {
            token = JWT_TOKEN_PREFIX + token;
        }
        return token;
    }

    public String removeTokenHeader(String jwtToken) {
        String token = Objects.requireNonNull(jwtToken);
        if (jwtToken.trim().startsWith(JWT_TOKEN_PREFIX)) {
            token = jwtToken.substring(JWT_TOKEN_PREFIX.length());
        }
        return token;
    }

    private Jws<Claims> parse(String jwtToken) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(removeTokenHeader(jwtToken));
    }

    public boolean isTokenTimeout(String jwtToken) {
        long currentTimeMillis = System.currentTimeMillis();
        try {
            Claims claims = parse(jwtToken).getPayload();
            int createTimeSeconds = Integer.parseInt(claims.get(CLAIM_KEY_IAT).toString());
            int expireTimeSeconds = Integer.parseInt(claims.get(CLAIM_KEY_EXP).toString());
            return !(expireTimeSeconds*1000L >= currentTimeMillis && expireTimeSeconds - createTimeSeconds <= timeoutSeconds);
        } catch (Throwable throwable) {
            logger.error("verify jwt token [timeout] failed: {}", jwtToken, throwable);
            return true;
        }
    }

    public boolean isTokenValid(String jwtToken) {
        try {
            // parseSignedClaims 内部会校验签名合法性, 校验通过即视为有效.
            parse(jwtToken);
            return true;
        } catch (Throwable throwable) {
            logger.error("verify jwt token [valid] failed: {}", jwtToken, throwable);
            return false;
        }
    }

    public String updateToken(String jwtToken) {
        try {
            Jws<Claims> jws = parse(jwtToken);
            Map<String, Object> claims = getTokenClaims(jws);
            Map<String, Object> headers = getTokenHeaders(jws);
            return newToken(claims, headers);
        } catch (Throwable throwable) {
            logger.error("update jwt token failed: {}", jwtToken, throwable);
            return StringUtils.EMPTY;
        }
    }

    private Map<String, Object> getTokenClaims(Jws<Claims> jws) {
        Map<String, Object> claims = new HashMap<>(jws.getPayload());
        claims.remove(CLAIM_KEY_IAT);
        claims.remove(CLAIM_KEY_EXP);
        return claims;
    }

    private Map<String, Object> getTokenHeaders(Jws<Claims> jws) {
        Map<String, Object> headers = new HashMap<>(jws.getHeader());
        headers.remove(HEADER_KEY_ALG);
        headers.remove(HEADER_KEY_TYP);
        return headers;
    }

}
