package talentcapitalme.com.comparatio.security;

public final class RequestContext {
	private static final ThreadLocal<Ctx> CTX = new ThreadLocal<>();

	public record Ctx(String userId, String role, String clientId) {}

	public static void set(Ctx c) { CTX.set(c); }

	public static Ctx get() { return CTX.get(); }

	public static void clear() { CTX.remove(); }
}


