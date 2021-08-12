package ru.savini.fb.ui.editors;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;

import com.vaadin.flow.component.textfield.BigDecimalField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import ru.savini.fb.domain.entity.Account;
import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.domain.entity.TransactionEvent;
import ru.savini.fb.domain.enums.CategoryCode;
import ru.savini.fb.controller.AccountController;
import ru.savini.fb.controller.CategoryController;
import ru.savini.fb.controller.TransactionController;
import ru.savini.fb.domain.enums.TransactionType;
import ru.savini.fb.exceptions.InvalidCategoryCodeException;

@UIScope
@SpringComponent
public class TransactionEditor extends VerticalLayout implements KeyNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionEditor.class);

    private TransactionEvent transactionEvent;
    private final AccountController accountController;
    private final CategoryController categoryController;
    private final TransactionController transactionController;
    private List<Account> accounts = new ArrayList<>();

    /* UI elements */
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

    private ChangeHandler changeHandler;

    @Autowired
    public TransactionEditor(AccountController accountController,
                             CategoryController categoryController,
                             TransactionController transactionController) {
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
        addKeyPressListener(Key.ENTER, e -> save(false));

        valueDatePicker.setValue(LocalDate.now());

        setVisible(false);
    }

    void save(boolean isNeededNewTransaction) {
        bindUiElementsWithTransactionEvent();
        transactionController.save(transactionEvent);
        changeHandler.onChange(isNeededNewTransaction);
    }

    void delete() {
        transactionController.delete(transactionEvent);
        changeHandler.onChange(false);
    }

    void cancel() {
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
        this.transactionEvent = new TransactionEvent();
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
        accounts = accountController.getAll();
        debitAccount.setItems(accounts);
        creditAccount.setItems(accounts);
        category.setItems(categoryController.getAll());
    }

    private void initDebitAccountBehaviour() {
        debitAccount = new ComboBox<>("Debit account");
        debitAccount.setClearButtonVisible(true);
        debitAccount.setItemLabelGenerator(Account::getName);
        debitAccount.addFocusListener(event -> updateAccountsComboBoxData(debitAccount, creditAccount));
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
        category.addValueChangeListener(event -> {
            Category selectedCategory = event.getValue();
            if (selectedCategory == null) return;
            if (CategoryCode.isGoalsCategory(selectedCategory) ||
                CategoryCode.isTransferCategory(selectedCategory)) {
                debitAccount.setVisible(true);
                creditAccount.setVisible(true);
            } else if (CategoryCode.isIncomeCategory(selectedCategory)) {
                debitAccount.setVisible(false);
                creditAccount.setVisible(true);
            } else if (CategoryCode.isOutgoingCategory(selectedCategory)) {
                debitAccount.setVisible(true);
                creditAccount.setVisible(false);
            } else {
                throw new InvalidCategoryCodeException();
            }
            switchEnablerSaveButton();
        });
    }

    private void initButtonsBehaviourAndState() {
        initSaveButtonBehaviourAndState();
        initSaveAndNewButtonBehaviourAndState();
        delete.getElement().getThemeList().add("error");
        cancel.addClickListener(e -> cancel());
        delete.addClickListener(e -> delete());
    }

    private void initSaveButtonBehaviourAndState() {
        save.setEnabled(false);
        save.getElement().getThemeList().add("primary");
        save.addClickListener(e -> save(false));
    }

    private void initSaveAndNewButtonBehaviourAndState() {
        saveAndNew.setEnabled(false);
        saveAndNew.getElement().getThemeList().add("primary");
        saveAndNew.addClickListener(e -> save(true));
    }

    private void initAmountBehaviour() {
        amount.addValueChangeListener(event -> switchEnablerSaveButton());
    }

    private void updateAccountsComboBoxData(ComboBox<Account> selectedAccount, ComboBox<Account> anotherAccount) {
        List<Account> correctedAccounts = new ArrayList<>(accounts);
        correctedAccounts.remove(anotherAccount.getValue());
        Account currentAcc = selectedAccount.getValue();
        selectedAccount.setItems(correctedAccounts);
        selectedAccount.setValue(currentAcc);
    }

    private void bindTransactionEventWithUiElements() {
        category.setValue(transactionEvent.getCategory());
        valueDatePicker.setValue(transactionEvent.getDate());
        amount.setValue(transactionEvent.getAmount());
        comment.setValue(transactionEvent.getComment());
        this.debitAccount.setValue(transactionEvent.getDebitAccount());
        this.creditAccount.setValue(transactionEvent.getCreditAccount());
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
}
