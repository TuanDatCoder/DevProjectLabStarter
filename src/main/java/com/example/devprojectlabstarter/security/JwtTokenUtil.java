package com.example.devprojectlabstarter.security;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.devprojectlabstarter.exception.Token.InvalidToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = -2550185165626007488L;
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60; // 5 hours
    public static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60; // 7 days

    @Value("${jwt.secret}")
    private String secret;

    // Lấy email từ JWT token
    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // Lấy ngày hết hạn của JWT token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    // Lấy một giá trị cụ thể từ JWT token
    private <T> T getClaimFromToken(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    // Lấy tất cả các thông tin từ JWT token bằng cách sử dụng secret key
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            Instant expiredOn = e.getClaims().getExpiration().toInstant();
            throw new TokenExpiredException("Token has expired", expiredOn);
        } catch (JwtException e) {
            throw new InvalidToken("Invalid token");
        } catch (Exception e) {
            throw new InvalidToken("Error parsing token: " + e.getMessage());
        }
    }
    public String extractEmail(String token) {
        return getEmailFromToken(token);
    }

    // Kiểm tra nếu JWT token đã hết hạn
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // Tạo JWT token từ thông tin người dùng
    public String generateToken(UserDetails userDetails) {
        return doGenerateToken(userDetails.getUsername(), JWT_TOKEN_VALIDITY);
    }

    // Tạo Refresh Token
    public String generateRefreshToken(UserDetails userDetails) {
        return doGenerateToken(userDetails.getUsername(), REFRESH_TOKEN_VALIDITY);
    }

    private String doGenerateToken(String subject, long validityInSeconds) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + validityInSeconds * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    // Xác thực JWT token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String email = getEmailFromToken(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

}
