package talentcapitalme.com.comparatio.security;

public final class Authz {
	public static boolean isSuperAdmin() {
		var ctx = RequestContext.get();
		return ctx != null && "SUPER_ADMIN".equals(ctx.role());
	}

	public static String requireClientScope(String requestedClientId) {
		var ctx = RequestContext.get();
		if (ctx == null) throw new RuntimeException("Unauthenticated");
		if (isSuperAdmin()) return requestedClientId != null ? requestedClientId : ctx.clientId();
		if (ctx.clientId() == null) throw new RuntimeException("No client scope");
		if (requestedClientId != null && !requestedClientId.equals(ctx.clientId()))
			throw new RuntimeException("Forbidden: not your client");
		return ctx.clientId();
	}
}


