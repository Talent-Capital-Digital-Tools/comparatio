package talentcapitalme.com.comparatio.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import talentcapitalme.com.comparatio.entity.User;
import talentcapitalme.com.comparatio.enumeration.Currency;
import talentcapitalme.com.comparatio.repository.UserRepository;
import talentcapitalme.com.comparatio.security.Authz;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Service for handling currency operations and formatting
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final UserRepository userRepository;

    /**
     * Get the currency for the current user/client
     */
    public Currency getUserCurrency() {
        String clientId = Authz.getCurrentUserClientId();
        return userRepository.findById(clientId)
                .map(User::getCurrency)
                .orElse(Currency.USD); // Default to USD
    }

    /**
     * Format amount with user's currency
     * @param amount the amount to format
     * @return formatted string with currency symbol
     */
    public String formatAmount(BigDecimal amount) {
        Currency currency = getUserCurrency();
        return formatAmount(amount, currency);
    }

    /**
     * Format amount with specific currency
     * @param amount the amount to format
     * @param currency the currency to use
     * @return formatted string with currency symbol
     */
    public String formatAmount(BigDecimal amount, Currency currency) {
        if (amount == null) return currency.getSymbol() + "0.00";
        
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        return currency.getSymbol() + formatter.format(amount);
    }

    /**
     * Format amount with user's currency (double version)
     */
    public String formatAmount(double amount) {
        return formatAmount(BigDecimal.valueOf(amount));
    }

    /**
     * Format amount with specific currency (double version)
     */
    public String formatAmount(double amount, Currency currency) {
        return formatAmount(BigDecimal.valueOf(amount), currency);
    }

    /**
     * Get currency symbol for current user
     */
    public String getUserCurrencySymbol() {
        return getUserCurrency().getSymbol();
    }

    /**
     * Get currency code for current user (e.g., "USD")
     */
    public String getUserCurrencyCode() {
        return getUserCurrency().getCode();
    }

    /**
     * Validate currency code
     */
    public boolean isValidCurrencyCode(String code) {
        return Currency.isValidCode(code);
    }

    /**
     * Get currency by code
     */
    public Currency getCurrencyByCode(String code) {
        return Currency.fromCode(code);
    }

    /**
     * Format amount for display in tables/lists
     * @param amount the amount
     * @param showSymbol whether to show currency symbol
     * @return formatted amount
     */
    public String formatForDisplay(BigDecimal amount, boolean showSymbol) {
        Currency currency = getUserCurrency();
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        String formattedAmount = formatter.format(amount != null ? amount : BigDecimal.ZERO);
        
        return showSymbol ? currency.getSymbol() + formattedAmount : formattedAmount;
    }

    /**
     * Format amount for Excel export
     * @param amount the amount
     * @return formatted amount suitable for Excel
     */
    public String formatForExcel(BigDecimal amount) {
        // Excel format without currency symbol for better compatibility
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        return formatter.format(amount != null ? amount : BigDecimal.ZERO);
    }

    /**
     * Get locale-specific number formatter for currency
     */
    public NumberFormat getCurrencyFormatter() {
        Currency currency = getUserCurrency();
        
        // Map currencies to appropriate locales
        Locale locale = switch (currency) {
            case USD, CAD, AUD -> Locale.US;
            case EUR -> Locale.GERMANY;
            case GBP -> Locale.UK;
            case JPY -> Locale.JAPAN;
            case CNY -> Locale.CHINA;
            case INR -> Locale.forLanguageTag("en-IN");
            case AED, SAR -> Locale.forLanguageTag("ar-AE");
            default -> Locale.US;
        };
        
        return NumberFormat.getCurrencyInstance(locale);
    }
}
