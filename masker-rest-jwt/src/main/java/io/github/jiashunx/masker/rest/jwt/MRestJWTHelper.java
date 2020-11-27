package io.github.jiashunx.masker.rest.jwt;

import com.alibaba.fastjson.JSON;
import io.github.jiashunx.masker.rest.framework.exception.MRestJWTException;
import io.github.jiashunx.masker.rest.framework.util.StringUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final String secretKey;
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
        this.secretKey = secretKey;
        this.timeoutSeconds = timeoutSeconds;
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
                    .setIssuedAt(new Date(currentTimeMillis))
                    .setExpiration(new Date(currentTimeMillis + timeoutMillis))
                    .signWith(SignatureAlgorithm.HS256, secretKey)
                    .compact();
        } catch (Throwable throwable) {
            throw new MRestJWTException(String.format("create jwt token failed, claims: %s, headers: %s", claims, headers), throwable);
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

    public boolean isTokenTimeout(String jwtToken) {
        long currentTimeMillis = System.currentTimeMillis();
        try {
            String token = removeTokenHeader(jwtToken);
            Jws<Claims> jws = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            Map<String, Object> claims = jws.getBody();
            int createTimeSeconds = Integer.parseInt(claims.get(CLAIM_KEY_IAT).toString());
            int expireTimeSeconds = Integer.parseInt(claims.get(CLAIM_KEY_EXP).toString());
            return !(expireTimeSeconds*1000L >= currentTimeMillis && expireTimeSeconds - createTimeSeconds <= timeoutSeconds);
        } catch (Throwable throwable) {
            throw new MRestJWTException(String.format("verify jwt token failed: %s", jwtToken), throwable);
        }
    }

    public boolean isTokenValid(String jwtToken) {
        try {
            String token = removeTokenHeader(jwtToken);
            // header={alg=HS256, typ=JWT, yourHeaderKey=yourHeaderValue}
            // body={exp=1605420304, iat=1605419704, yourBodyKey=yourBodyValue}
            // signature=_DQ2CTmS3rELlbst6P_jEE_LBhJOo-4JJ_8NkyGsR4k
            Jws<Claims> jws = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            Map<String, Object> claims = jws.getBody();
            Map<String, Object> headers = (Map<String, Object>) jws.getHeader();
            String newToken = Jwts.builder()
                    .setPayload(JSON.toJSONString(claims))
                    .setHeader(headers)
                    .setHeaderParam(HEADER_KEY_TYP, HEADER_VALUE_TYP)
                    .signWith(SignatureAlgorithm.HS256, secretKey).compact();
            // 对比签名, 从token中获取payload, 再根据payload重新生成新的token, 新旧token进行对比
            // header, payload一致, 得到的signature肯定一致
            return token.split("\\.")[2].equals(newToken.split("\\.")[2]);
        } catch (Throwable throwable) {
            throw new MRestJWTException(String.format("verify jwt token failed: %s", jwtToken), throwable);
        }
    }

    public String updateToken(String jwtToken) {
        try {
            String token = removeTokenHeader(jwtToken);
            Jws<Claims> jws = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            Map<String, Object> claims = getTokenClaims(jws);
            Map<String, Object> headers = getTokenHeaders(jws);
            return newToken(claims, headers);
        } catch (Throwable throwable) {
            throw new MRestJWTException(String.format("update jwt token failed: %s", jwtToken), throwable);
        }
    }

    private Map<String, Object> getTokenClaims(Jws<Claims> jws) {
        Map<String, Object> claims = new HashMap<>(jws.getBody());
        claims.remove(CLAIM_KEY_IAT);
        claims.remove(CLAIM_KEY_EXP);
        return claims;
    }

    private Map<String, Object> getTokenHeaders(Jws<Claims> jws) {
        Map<String, Object> headers = new HashMap<>((Map<String, Object>) jws.getHeader());
        headers.remove(HEADER_KEY_ALG);
        headers.remove(HEADER_KEY_TYP);
        return headers;
    }

}
