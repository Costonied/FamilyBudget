package ru.savini.fb.ui.editors;

import java.util.List;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Optional;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;

import ru.savini.fb.settings.Settings;
import ru.savini.fb.domain.entity.Account;
import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.domain.entity.TransactionEvent;
import ru.savini.fb.domain.enums.CategoryCode;
import ru.savini.fb.controller.AccountController;
import ru.savini.fb.controller.CategoryController;
import ru.savini.fb.controller.TransactionController;
import ru.savini.fb.exceptions.InvalidCategoryCodeException;

@UIScope
@SpringComponent
public class TransactionEditor extends VerticalLayout implements KeyNotifier {
    private transient TransactionEvent transactionEvent;
    private final transient AccountController accountController;
    private final transient CategoryController categoryController;
    private final transient TransactionController transactionController;
    private final transient Settings settings;

    BigDecimalField amount = new BigDecimalField("Amount");
    TextField comment = new TextField("Comment");
    DatePicker valueDatePicker = new DatePicker("Transaction date");

    ComboBox<Category> category;
    ComboBox<Account> debitAccount;
    ComboBox<Account> creditAccount;

    Button cancel = new Button("Cancel");
    Button save = new Button("Save", VaadinIcon.CHECK.create());
    Button delete = new Button("Delete", VaadinIcon.TRASH.create());
    Button saveAndNew = new Button("Save & New", VaadinIcon.CHECK_SQUARE_O.create());

    HorizontalLayout actions = new HorizontalLayout(save, cancel, saveAndNew, delete);

    private transient ChangeHandler changeHandler;

    @Autowired
    public TransactionEditor(Settings settings,
                             AccountController accountController,
                             CategoryController categoryController,
                             TransactionController transactionController) {
        this.settings = settings;
        this.accountController = accountController;
        this.categoryController = categoryController;
        this.transactionController = transactionController;

        transactionEvent = new TransactionEvent();

        initDebitAccountBehaviour();
        initButtonsBehaviourAndState();
        initCategoryBehaviour();
        initCreditAccountBehaviour();
        initAmountBehaviour();

        setSpacing(true);
        add(category, valueDatePicker, amount, debitAccount, creditAccount, comment, actions);
        addKeyPressListener(Key.ENTER, e -> saveTransaction(false));

        valueDatePicker.setValue(LocalDate.now());
        amount.setLocale(settings.getLocale());

        setVisible(false);
    }

    void saveTransaction(boolean isNeededNewTransaction) {
        bindUiElementsWithTransactionEvent();
        transactionController.save(transactionEvent);
        changeHandler.onChange(isNeededNewTransaction);
    }

    void deleteTransaction() {
        transactionController.delete(transactionEvent);
        changeHandler.onChange(false);
    }

    void cancelAction() {
        setVisible(false);
    }

    public final void editTransaction(TransactionEvent transactionEvent) {
        // Transaction event could be null after refresh
        if (transactionEvent == null) {
            setVisible(false);
            return;
        }
        this.refreshComboBoxData();
        this.transactionEvent = transactionEvent;
        transactionEvent.setId(transactionEvent.getId());
        bindTransactionEventWithUiElements();
        amount.focus();
        cancel.setVisible(true);
        delete.setVisible(true);
        if (creditAccount.getValue() != null) {
            creditAccount.setVisible(true);
            debitAccount.setVisible(false);
        } else {
            creditAccount.setVisible(false);
            debitAccount.setVisible(true);
        }
        setVisible(true);
    }

    public final void addTransaction() {
        this.clearTransactionEvent();
        this.refreshComboBoxData();
        bindTransactionEventWithUiElements();
        cancel.setVisible(true);
        delete.setVisible(false);
        debitAccount.setVisible(false);
        creditAccount.setVisible(false);
        setVisible(true);
    }

    private void clearTransactionEvent() {
        if (transactionEvent != null) {
            transactionEvent = new TransactionEvent(transactionEvent.getDate());
        }
        else {
            transactionEvent = new TransactionEvent();
        }
    }

    public interface ChangeHandler {
        void onChange(boolean isNeededNewTransaction);
    }

    public void setChangeHandler(ChangeHandler h) {
        // ChangeHandler is notified when either save or delete
        // is clicked
        changeHandler = h;
    }

    private void refreshComboBoxData() {
        debitAccount.setItems(accountController.getAllByNeedAccountingIsTrue());
        creditAccount.setItems(accountController.getAllByNeedAccountingIsTrue());
        category.setItems(categoryController.getAll());
    }

    private void initDebitAccountBehaviour() {
        debitAccount = new ComboBox<>("Debit account");
        debitAccount.setClearButtonVisible(true);
        debitAccount.setItemLabelGenerator(Account::getName);
        debitAccount.addValueChangeListener(event -> switchEnablerSaveButton());
    }

    private void initCreditAccountBehaviour() {
        creditAccount = new ComboBox<>("Credit account");
        creditAccount.setClearButtonVisible(true);
        creditAccount.setItemLabelGenerator(Account::getName);
        creditAccount.addFocusListener(event -> updateAccountsComboBoxData(creditAccount, debitAccount));
        creditAccount.addValueChangeListener(event -> switchEnablerSaveButton());
    }

    private void initCategoryBehaviour() {
        category = new ComboBox<>("Category");
        category.setItemLabelGenerator(Category::getName);
        category.addValueChangeListener(this::initCategoryValueChangeListener);
    }

    private void initCategoryValueChangeListener(AbstractField.ComponentValueChangeEvent<ComboBox<Category>, Category> event) {
        Category selectedCategory = event.getValue();
        if (selectedCategory == null) return;
        if (CategoryCode.isGoalsCategory(selectedCategory) ||
                CategoryCode.isTransferCategory(selectedCategory)) {
            debitAccount.setVisible(true);
            creditAccount.setVisible(true);
        } else if (CategoryCode.isIncomeCategory(selectedCategory)) {
            debitAccount.setVisible(false);
            creditAccount.setVisible(true);
        } else if (CategoryCode.isOutgoingCategory(selectedCategory) ||
                CategoryCode.isWithdrawalCategory(selectedCategory)) {
            debitAccount.setVisible(true);
            initDebitAccounts();
            creditAccount.setVisible(false);
        } else {
            throw new InvalidCategoryCodeException();
        }
        switchEnablerSaveButton();
    }

    private void initDebitAccounts() {
        if (CategoryCode.isWithdrawalCategory(category.getValue())) {
            initAccountsNotForAccounting(debitAccount);
        }
        else if (CategoryCode.isOutgoingCategory(category.getValue())) {
            initAccountsForAccounting(debitAccount);
            debitAccount.setValue(getDefaultAccountForOutgoing());
        }
        else if (CategoryCode.isTransferCategory(category.getValue())) {
            updateAccountsComboBoxData(debitAccount, creditAccount);
        }
        else {
            throw new InvalidCategoryCodeException();
        }
    }

    private void initButtonsBehaviourAndState() {
        initSaveButtonBehaviourAndState();
        initSaveAndNewButtonBehaviourAndState();
        delete.getElement().getThemeList().add("error");
        cancel.addClickListener(e -> cancelAction());
        delete.addClickListener(e -> deleteTransaction());
    }

    private void initSaveButtonBehaviourAndState() {
        save.setEnabled(false);
        save.getElement().getThemeList().add("primary");
        save.addClickListener(e -> saveTransaction(false));
    }

    private void initSaveAndNewButtonBehaviourAndState() {
        saveAndNew.setEnabled(false);
        saveAndNew.getElement().getThemeList().add("primary");
        saveAndNew.addClickListener(e -> saveTransaction(true));
    }

    private void initAmountBehaviour() {
        amount.addValueChangeListener(event -> switchEnablerSaveButton());
    }

    private void updateAccountsComboBoxData(ComboBox<Account> selectedAccount, ComboBox<Account> anotherAccount) {
        List<Account> correctedAccounts = accountController.getAll();
        correctedAccounts.remove(anotherAccount.getValue());
        Account currentAcc = selectedAccount.getValue();
        selectedAccount.setItems(correctedAccounts);
        selectedAccount.setValue(currentAcc);
    }

    private void initAccountsForAccounting(ComboBox<Account> accounts) {
        accounts.setItems(accountController.getAllByNeedAccountingIsTrue());
    }

    private void initAccountsNotForAccounting(ComboBox<Account> accounts) {
        accounts.setItems(accountController.getAllByNeedAccountingIsFalse());
    }

    private void bindTransactionEventWithUiElements() {
        category.setValue(transactionEvent.getCategory());
        valueDatePicker.setValue(transactionEvent.getDate());
        comment.setValue(transactionEvent.getComment());
        this.debitAccount.setValue(transactionEvent.getDebitAccount());
        this.creditAccount.setValue(transactionEvent.getCreditAccount());
        if (transactionEvent.getAmount().equals(BigDecimal.ZERO)) {
            amount.setValue(null);
            amount.setPlaceholder("0.00");
        } else {
            amount.setValue(transactionEvent.getAmount());
        }
    }

    private void bindUiElementsWithTransactionEvent() {
        transactionEvent.setCategory(category.getValue());
        transactionEvent.setDebitAccount(debitAccount.getValue());
        transactionEvent.setCreditAccount(creditAccount.getValue());
        transactionEvent.setDate(valueDatePicker.getValue());
        transactionEvent.setAmount(amount.getValue());
        transactionEvent.setComment(comment.getValue());
    }

    private void switchEnablerSaveButton() {
        if (isAllFieldValidForSaveButton()) {
            save.setEnabled(true);
            saveAndNew.setEnabled(true);
        } else {
            save.setEnabled(false);
            saveAndNew.setEnabled(false);
        }
    }

    private boolean isAllFieldValidForSaveButton() {
        try {
            return (category.isVisible() && !category.getValue().getName().isEmpty()) &&
                    (!debitAccount.isVisible() || (debitAccount.isVisible() && !debitAccount.getValue().getName().isEmpty())) &&
                    (!creditAccount.isVisible() || (creditAccount.isVisible() && !creditAccount.getValue().getName().isEmpty())) &&
                    !amount.getValue().equals(new BigDecimal(0));
        } catch (NullPointerException e) {
            return false;
        }
    }

    private Account getDefaultAccountForOutgoing() {
        Long defaultAccountId = settings.getDefaultIncomingAccountId();
        if (defaultAccountId != null) {
            List<Account> accounts = accountController.getAllByNeedAccountingIsTrue();
            Optional<Account> result = accounts.stream().filter(account -> account.getId().equals(defaultAccountId)).findFirst();
            return result.orElse(null);
        }
        else {
            return null;
        }
    }
}
