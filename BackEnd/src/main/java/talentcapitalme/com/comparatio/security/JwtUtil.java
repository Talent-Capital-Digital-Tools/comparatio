package talentcapitalme.com.comparatio.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import talentcapitalme.com.comparatio.enumeration.UserRole;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
	@Value("${app.jwt.secret:change_me_super_secret}") private String secret;
	@Value("${app.jwt.ttlMillis:86400000}") private long ttlMillis; // 1 day
	private Key key;

	@PostConstruct void init(){ key = Keys.hmacShaKeyFor(secret.getBytes()); }

	public String generateToken(String userId, UserRole role, String clientId){
		long now = System.currentTimeMillis();
		return Jwts.builder()
				.subject(userId)
				.claim("role", role.name())
				.claim("clientId", clientId)
				.issuedAt(new Date(now))
				.expiration(new Date(now + ttlMillis))
				.signWith(key)
				.compact();
	}

	public RequestContext.Ctx parse(String token){
		Claims claims = Jwts.parser()
				.verifyWith(Keys.hmacShaKeyFor(key.getEncoded()))
				.build()
				.parseSignedClaims(token)
				.getPayload();
		String sub = claims.getSubject();
		String roleString = (String) claims.get("role");
		UserRole role = UserRole.valueOf(roleString);
		String clientId = (String) claims.get("clientId");
		return new RequestContext.Ctx(sub, role, clientId);
	}
}


