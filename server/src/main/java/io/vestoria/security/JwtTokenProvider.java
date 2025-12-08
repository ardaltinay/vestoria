package io.vestoria.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private final Key key;
    private final long validityInMillis;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long validityInMillis) {
        // secret should be at least 256 bits for HS256
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.validityInMillis = validityInMillis;
    }

    public String generateToken(String username, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMillis);

        return Jwts.builder().setSubject(username).claim("roles", roles).setIssuedAt(now).setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getUsername(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    // JwtUtils class'ının içi
    public List<SimpleGrantedAuthority> getRolesFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

        // Token oluştururken rolleri "roles" key'i ile List<String> olarak koyduğunu
        // varsayıyorum
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);

        if (roles == null)
            return Collections.emptyList();

        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
}
