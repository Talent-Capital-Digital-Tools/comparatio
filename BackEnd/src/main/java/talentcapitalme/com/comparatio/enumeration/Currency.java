package talentcapitalme.com.comparatio.enumeration;

/**
 * Currency enumeration based on ISO 4217 standard
 * Provides currency codes, symbols, and display names for international support
 */
public enum Currency {
    // Major currencies
    USD("USD", "$", "US Dollar", "United States Dollar"),
    EUR("EUR", "€", "Euro", "Euro"),
    GBP("GBP", "£", "British Pound", "British Pound Sterling"),
    JPY("JPY", "¥", "Japanese Yen", "Japanese Yen"),
    CAD("CAD", "C$", "Canadian Dollar", "Canadian Dollar"),
    AUD("AUD", "A$", "Australian Dollar", "Australian Dollar"),
    CHF("CHF", "CHF", "Swiss Franc", "Swiss Franc"),
    CNY("CNY", "¥", "Chinese Yuan", "Chinese Yuan"),
    
    // Middle East & Africa
    AED("AED", "د.إ", "UAE Dirham", "United Arab Emirates Dirham"),
    SAR("SAR", "﷼", "Saudi Riyal", "Saudi Arabian Riyal"),
    EGP("EGP", "£", "Egyptian Pound", "Egyptian Pound"),
    ZAR("ZAR", "R", "South African Rand", "South African Rand"),
    
    // Asia Pacific
    INR("INR", "₹", "Indian Rupee", "Indian Rupee"),
    SGD("SGD", "S$", "Singapore Dollar", "Singapore Dollar"),
    HKD("HKD", "HK$", "Hong Kong Dollar", "Hong Kong Dollar"),
    KRW("KRW", "₩", "South Korean Won", "South Korean Won"),
    
    // Europe
    SEK("SEK", "kr", "Swedish Krona", "Swedish Krona"),
    NOK("NOK", "kr", "Norwegian Krone", "Norwegian Krone"),
    DKK("DKK", "kr", "Danish Krone", "Danish Krone"),
    PLN("PLN", "zł", "Polish Zloty", "Polish Zloty"),
    
    // Americas
    BRL("BRL", "R$", "Brazilian Real", "Brazilian Real"),
    MXN("MXN", "$", "Mexican Peso", "Mexican Peso"),
    CLP("CLP", "$", "Chilean Peso", "Chilean Peso"),
    ARS("ARS", "$", "Argentine Peso", "Argentine Peso");

    private final String code;          // ISO 4217 code (e.g., "USD")
    private final String symbol;        // Currency symbol (e.g., "$")
    private final String shortName;     // Short display name (e.g., "US Dollar")
    private final String fullName;      // Full official name

    Currency(String code, String symbol, String shortName, String fullName) {
        this.code = code;
        this.symbol = symbol;
        this.shortName = shortName;
        this.fullName = fullName;
    }

    public String getCode() {
        return code;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getShortName() {
        return shortName;
    }

    public String getFullName() {
        return fullName;
    }

    /**
     * Get default currency (USD for international compatibility)
     */
    public static Currency getDefault() {
        return USD;
    }

    /**
     * Find currency by ISO code (case-insensitive)
     */
    public static Currency fromCode(String code) {
        if (code == null) return null;
        
        for (Currency currency : values()) {
            if (currency.code.equalsIgnoreCase(code.trim())) {
                return currency;
            }
        }
        return null;
    }

    /**
     * Check if currency code is valid
     */
    public static boolean isValidCode(String code) {
        return fromCode(code) != null;
    }

    /**
     * Format amount with currency symbol
     * @param amount the amount to format
     * @return formatted string (e.g., "$1,234.56")
     */
    public String formatAmount(double amount) {
        return String.format("%s%.2f", symbol, amount);
    }

    /**
     * Get display name for UI (code + short name)
     */
    public String getDisplayName() {
        return String.format("%s - %s", code, shortName);
    }

    @Override
    public String toString() {
        return code;
    }
}
