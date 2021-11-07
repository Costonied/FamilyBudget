package ru.savini.fb.settings;

import java.util.Locale;

import org.joda.money.CurrencyUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.savini.fb.controller.AppSettingsController;
import ru.savini.fb.globals.Default;


@Service
public class Settings {
    private static final Logger LOGGER = LoggerFactory.getLogger(Settings.class);
    private final AppSettingsController controller;

    private static final String APPLICATION_LOCALE = "app.locale";
    private static final String APPLICATION_ACCOUNTING_CURRENCY = "app.accounting.currency";
    private static final String DEFAULT_ACCOUNT_ID_FOR_OUTGOING = "default.account.id.for.outgoing";

    @Autowired
    public Settings(AppSettingsController controller) {
        this.controller = controller;
    }

    public Long getDefaultIncomingAccountId() {
        String value = controller.getValue(DEFAULT_ACCOUNT_ID_FOR_OUTGOING);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            LOGGER.error("Bad value [{}] for parameter [{}]", value, DEFAULT_ACCOUNT_ID_FOR_OUTGOING);
            return null;
        }
    }

    public Locale getLocale() {
        String locale = controller.getValue(APPLICATION_LOCALE);
        return locale != null ? new Locale(locale) : Locale.getDefault();
    }

    public CurrencyUnit getAccountingCurrencyUnit() {
        String currency = controller.getValue(APPLICATION_ACCOUNTING_CURRENCY);
        if (currency == null) {
            currency = Default.CURRENCY_CODE;
        }
        return CurrencyUnit.of(currency);
    }
}
