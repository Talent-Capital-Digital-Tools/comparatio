package talentcapitalme.com.comparatio.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import talentcapitalme.com.comparatio.security.JwtUtil;
import talentcapitalme.com.comparatio.security.RequestContext;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
	private final JwtUtil jwtUtil;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		try {
			if (header != null && header.startsWith("Bearer ")) {
				String token = header.substring(7);
				var ctx = jwtUtil.parse(token);
				RequestContext.set(ctx);
				var auth = new UsernamePasswordAuthenticationToken(
						ctx.userId(), null,
						List.of(new SimpleGrantedAuthority("ROLE_" + ctx.role()))
				);
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
			chain.doFilter(request, response);
		} finally {
			SecurityContextHolder.clearContext();
			RequestContext.clear();
		}
	}
}


