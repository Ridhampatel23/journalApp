package net.ridham.journalApp.utils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JWTUtilTest {

    private static final String SECRET = "test-secret-that-is-at-least-32-bytes-long!";

    private JWTUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JWTUtil();
        // stands in for @Value("${jwt.secret.key}") without starting Spring
        ReflectionTestUtils.setField(jwtUtil, "SECRET_KEY", SECRET);
    }

    @Test
    void tokenCarriesTheUsername() {
        String token = jwtUtil.generateToken("ridham");

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("ridham");
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void expiredTokenIsRejectedWhileParsing() {
        String expired = Jwts.builder()
                .subject("ridham")
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();

        // parsing throws before validateToken() ever runs, which is why JWTFilter must catch it
        assertThatThrownBy(() -> jwtUtil.extractUsername(expired)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String forged = Jwts.builder()
                .subject("admin")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor("a-completely-different-secret-key-32b!!".getBytes()))
                .compact();

        assertThatThrownBy(() -> jwtUtil.extractUsername(forged)).isInstanceOf(JwtException.class);
    }
}
