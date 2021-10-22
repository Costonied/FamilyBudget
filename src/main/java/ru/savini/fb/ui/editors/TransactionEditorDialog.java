package ru.savini.fb.ui.editors;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.combobox.ComboBox;

import ru.savini.fb.controller.AccountController;
import ru.savini.fb.controller.TransactionController;
import ru.savini.fb.domain.entity.Account;
import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.controller.CategoryController;
import ru.savini.fb.domain.entity.TransactionEvent;
import ru.savini.fb.domain.enums.CategoryCode;
import ru.savini.fb.exceptions.InvalidCategoryCodeException;
import ru.savini.fb.settings.Settings;
import ru.savini.fb.ui.components.FBEditorDialog;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@UIScope
@SpringComponent
public class TransactionEditorDialog extends FBEditorDialog {
    private final transient Settings settings;
    private final transient AccountController accountController;
    private final transient CategoryController categoryController;
    private final transient TransactionController transactionController;

    private transient DatePicker date;
    private transient TextField comment;
    private transient Button saveAndNew;
    private transient BigDecimalField amount;
    private transient ComboBox<Category> category;
    private transient ComboBox<Account> debitAccount;
    private transient ComboBox<Account> creditAccount;
    private transient TransactionEvent transactionEvent;

    private transient LocalDate lastDate;
    private transient SaveHandler saveHandler;
    private transient DeleteHandler deleteHandler;

    @Autowired
    public TransactionEditorDialog(Settings settings,
                                   AccountController accountController,
                                   CategoryController categoryController,
                                   TransactionController transactionController) {
        this.settings = settings;
        this.accountController = accountController;
        this.categoryController = categoryController;
        this.transactionController = transactionController;
        initFields();
        initCustomButtonsLayout();
    }

    @Override
    public void open() {
        this.transactionEvent = new TransactionEvent();
        refreshComboBoxData();
        clearFields();
        category.focus();
        this.delete.setVisible(false);
        super.open();
    }

    public void open(TransactionEvent transactionEvent) {
        this.transactionEvent = transactionEvent;
        refreshComboBoxData();
        setFields();
        this.delete.setVisible(true);
        switchEnablerSaveButton();
        super.open();
    }

    private void refreshComboBoxData() {
        category.setItems(categoryController.getAll());
        debitAccount.setItems(accountController.getAllByNeedAccountingIsTrue());
        creditAccount.setItems(accountController.getAll());
    }

    private void setFields() {
        if (transactionEvent == null) {
            return;
        }
        category.setValue(transactionEvent.getCategory());
        date.setValue(transactionEvent.getDate());
        amount.setValue(transactionEvent.getAmount());
        comment.setValue(transactionEvent.getComment());
        setDebitAccount();
        setCreditAccount();
    }

    private void setDebitAccount() {
        if (transactionEvent.getDebitAccount() != null) {
            this.debitAccount.setValue(transactionEvent.getDebitAccount());
        } else {
            this.debitAccount.setVisible(false);
        }
    }

    private void setCreditAccount() {
        if (transactionEvent.getCreditAccount() != null) {
            this.creditAccount.setValue(transactionEvent.getCreditAccount());
        } else {
            this.creditAccount.setVisible(false);
        }
    }

    private void clearFields() {
        date.setValue(lastDate == null ? LocalDate.now() : lastDate);
        amount.clear();
        comment.clear();
    }

    private void initFields() {
        initCategory();
        initDate();
        initAmount();
        initDebitAccount();
        initCreditAccount();
        initComment();
        initSave();
        initCancel();
        initDelete();
        fields.add(category, date, amount, debitAccount, creditAccount, comment);
    }

    private void initCustomButtonsLayout() {
        initSaveAndNew();
        customButtons.add(saveAndNew);
    }

    private void initCategory() {
        category = new ComboBox<>("Category");
        category.setItemLabelGenerator(Category::getName);
        category.addValueChangeListener(this::initCategoryValueChangeListener);
    }

    private void initDate() {
        date = new DatePicker("Transaction date");
    }

    private void initAmount() {
        amount = new BigDecimalField("Amount");
        amount.setLocale(settings.getLocale());
        amount.setPlaceholder("0");
        amount.addValueChangeListener(event -> switchEnablerSaveButton());
    }

    private void initDebitAccount() {
        debitAccount = new ComboBox<>("Debit account");
        debitAccount.setClearButtonVisible(true);
        debitAccount.setItemLabelGenerator(Account::getName);
        debitAccount.addValueChangeListener(event -> switchEnablerSaveButton());
    }

    private void initCreditAccount() {
        creditAccount = new ComboBox<>("Credit account");
        creditAccount.setClearButtonVisible(true);
        creditAccount.setItemLabelGenerator(Account::getName);
        creditAccount.setVisible(false);
        creditAccount.addFocusListener(event -> updateAccountsComboBoxData(creditAccount, debitAccount));
        creditAccount.addValueChangeListener(event -> switchEnablerSaveButton());
    }

    private void initComment() {
        comment = new TextField("Comment");
    }

    private void initSaveAndNew() {
        saveAndNew = new Button("Save & New", VaadinIcon.CHECK_SQUARE_O.create());
        saveAndNew.setWidthFull();
        saveAndNew.setEnabled(false);
        saveAndNew.getElement().getThemeList().add("primary");
        saveAndNew.addClickListener(e -> {
            saveAndNew.setEnabled(false);
            lastDate = date.getValue();
            saveTransaction(true);
        });
    }

    private void initSave() {
        save.addClickListener(event -> {
            save.setEnabled(false);
            lastDate = null;
            saveTransaction(false);
        });
    }

    private void initDelete() {
        // TODO: Прежде чем убирать признак disabled необходимо
        //  реализовать логику корректного удаления транзакции
        //  с логикой пересчета денег на счету и изменения раздела бюджетирования
        delete.setEnabled(false);
        delete.addClickListener(event -> deleteTransaction());
    }

    private void initCancel() {
        cancel.addClickListener(event -> {
            transactionEvent = null;
            lastDate = null;
        });
    }

    void saveTransaction(boolean isNeededNewTransaction) {
        setupTransactionEventFromFieldsValues();
        transactionController.save(transactionEvent);
        saveHandler.onSave(isNeededNewTransaction);
    }

    void deleteTransaction() {
        transactionController.delete(transactionEvent);
        deleteHandler.onDelete();
    }

    private void setupTransactionEventFromFieldsValues() {
        transactionEvent.setCategory(category.getValue());
        transactionEvent.setDebitAccount(debitAccount.getValue());
        transactionEvent.setCreditAccount(creditAccount.getValue());
        transactionEvent.setDate(date.getValue());
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

    private void initCategoryValueChangeListener(AbstractField.ComponentValueChangeEvent<ComboBox<Category>, Category> event) {
        Category selectedCategory = event.getValue();
        if (selectedCategory == null) return;

        if (CategoryCode.isGoalsCategory(selectedCategory) ||
                CategoryCode.isTransferCategory(selectedCategory)) {
            debitAccount.setItems(accountController.getAll());
            debitAccount.setVisible(true);
            creditAccount.setVisible(true);
        }
        else if (CategoryCode.isIncomeCategory(selectedCategory)) {
            debitAccount.setVisible(false);
            creditAccount.setVisible(true);
        }
        else if (CategoryCode.isOutgoingCategory(selectedCategory) ||
                CategoryCode.isWithdrawalCategory(selectedCategory)) {
            debitAccount.setVisible(true);
            initDebitAccounts();
            creditAccount.setVisible(false);
        }
        else {
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

    private void initAccountsNotForAccounting(ComboBox<Account> accounts) {
        accounts.setItems(accountController.getAllByNeedAccountingIsFalse());
    }

    private void initAccountsForAccounting(ComboBox<Account> accounts) {
        accounts.setItems(accountController.getAllByNeedAccountingIsTrue());
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

    private void updateAccountsComboBoxData(ComboBox<Account> selectedAccount, ComboBox<Account> anotherAccount) {
        if (!anotherAccount.isVisible() || anotherAccount.getValue() == null) {
            return;
        }
        List<Account> correctedAccounts = accountController.getAll();
        correctedAccounts.remove(anotherAccount.getValue());
        correctedAccounts.removeIf(account -> account.getId().equals(anotherAccount.getValue().getId()));
        Account currentAcc = selectedAccount.getValue();
        selectedAccount.setItems(correctedAccounts);
        selectedAccount.setValue(currentAcc);
    }

    public void setSaveHandler(SaveHandler saveHandler) {
        this.saveHandler = saveHandler;
    }

    public void setDeleteHandler(DeleteHandler deleteHandler) {
        this.deleteHandler = deleteHandler;
    }

    public interface SaveHandler {
        void onSave(boolean isNeededNewTransaction);
    }

    public interface DeleteHandler {
        void onDelete();
    }
}
