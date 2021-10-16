package ru.savini.fb.ui.editors;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.savini.fb.controller.AccountController;
import ru.savini.fb.domain.entity.Account;
import ru.savini.fb.ui.helpers.CurrencyHelper;

import java.math.BigDecimal;

/**
 * A simple example to introduce building forms. As your real application is probably much
 * more complicated than this example, you could re-use this form in multiple places. This
 * example component is only used in MainView.
 * <p>
 * In a real world application you'll most likely using a common super class for all your
 * forms - less code, better UX.
 */
@SpringComponent
@UIScope
public class AccountEditor extends VerticalLayout implements KeyNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountEditor.class);
    private final transient AccountController accountController;

    /**
     * The currently edited customer
     */
    private transient Account account;

    /* Fields to edit properties in Account entity */
    TextField name = new TextField("Account name");
    BigDecimalField amount = new BigDecimalField("Amount");
    ComboBox<String> currency;
    Checkbox needAccounting = new Checkbox("Need accounting");

    Button save = new Button("Save", VaadinIcon.CHECK.create());
    Button cancel = new Button("Cancel");
    Button delete = new Button("Delete", VaadinIcon.TRASH.create());
    HorizontalLayout actions = new HorizontalLayout(save, cancel);

    Binder<Account> binder = new Binder<>(Account.class);

    private transient ChangeHandler changeHandler;

    @Autowired
    public AccountEditor(AccountController accountController) {
        this.accountController = accountController;
        initBinder();
        amount.setPlaceholder("0.00");

        add(name, amount, currency, needAccounting, actions, delete);
        // Configure and style components
        setSpacing(true);

        save.getElement().getThemeList().add("primary");
        delete.getElement().getThemeList().add("error");

        addKeyPressListener(Key.ENTER, e -> saveAccount());

        // wire action buttons to save, delete and reset
        save.addClickListener(e -> saveAccount());
        delete.addClickListener(e -> deleteAccount());
        cancel.addClickListener(e -> this.cancelEditing());
        initDeleteButton();
        setVisible(false);
    }

    private void initDeleteButton() {
        delete.setWidthFull();
    }

    private void initBinder() {
        initCurrencyCodes();
        binder.forField(amount)
                .bind(Account::getAmount, Account::setAmount);
        binder.forField(currency).bind(Account::getName, (c, a) -> c.setCurrency(currency.getValue()));
        binder.forField(needAccounting).bind(Account::isNeedAccounting, Account::setNeedAccounting);

        // bind using naming convention
        binder.bindInstanceFields(this);
    }

    private void initCurrencyCodes() {
        currency = new ComboBox<>("List of currency codes");
        currency.setItems(CurrencyHelper.getCurrencyCode());
    }

    void deleteAccount() {
        accountController.delete(account);
        changeHandler.onChange();
    }

    void saveAccount() {
        if (account.getAmount() == null) {
            account.setAmount(BigDecimal.ZERO);
        }
        accountController.save(account);
        changeHandler.onChange();
    }

    void cancelEditing() {
        this.account = null;
        changeHandler.onChange();
    }

    public interface ChangeHandler {
        void onChange();
    }

    public final void editAccount(Account account) {
        if (account == null) {
            setVisible(false);
            return;
        }
        final boolean persisted = account.getId() != null;
        if (persisted) {
            // Find fresh entity for editing
            this.account = accountController.getById(account.getId());
        }
        else {
            this.account = account;
        }

        // Bind account properties to similarly named fields
        // Could also use annotation or "manual binding" or programmatically
        // moving values from fields to entities before saving
        binder.setBean(this.account);
        // set currency from edited account because setBean not do it well for currency
        currency.setValue(account.getCurrency());

        setVisible(true);
        delete.setVisible(account.getId() != null);

        // Focus name initially
        name.focus();
        LOGGER.info("Edit account [{}]", account);
    }

    public void setChangeHandler(ChangeHandler h) {
        // ChangeHandler is notified when either save or delete
        // is clicked
        changeHandler = h;
    }

}