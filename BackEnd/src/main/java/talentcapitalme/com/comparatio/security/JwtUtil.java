package talentcapitalme.com.comparatio.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
	@Value("${app.jwt.secret:change_me_super_secret}") private String secret;
	@Value("${app.jwt.ttlMillis:86400000}") private long ttlMillis; // 1 day
	private Key key;

	@PostConstruct void init(){ key = Keys.hmacShaKeyFor(secret.getBytes()); }

	public String generateToken(String userId, String role, String clientId){
		long now = System.currentTimeMillis();
		return Jwts.builder()
				.setSubject(userId)
				.claim("role", role)
				.claim("clientId", clientId)
				.setIssuedAt(new Date(now))
				.setExpiration(new Date(now + ttlMillis))
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}

	public RequestContext.Ctx parse(String token){
		var claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
		String sub = claims.getSubject();
		String role = (String) claims.get("role");
		String clientId = (String) claims.get("clientId");
		return new RequestContext.Ctx(sub, role, clientId);
	}
}


