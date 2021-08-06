package ru.savini.fb.ui.editors;

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
import ru.savini.fb.domain.enums.CategoryCode;
import ru.savini.fb.domain.entity.Transaction;
import ru.savini.fb.controller.AccountController;
import ru.savini.fb.controller.CategoryController;
import ru.savini.fb.controller.TransactionController;
import ru.savini.fb.exceptions.InvalidCategoryCodeException;

@UIScope
@SpringComponent
public class TransactionEditor extends VerticalLayout implements KeyNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionEditor.class);

    private Transaction transaction;
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

        initDebitAccountBehaviour();
        initButtonsBehaviour();
        initCategoryBehaviour();
        initCreditAccountBehaviour();

        setSpacing(true);
        add(category, valueDatePicker, amount, debitAccount, creditAccount, comment, actions);
        addKeyPressListener(Key.ENTER, e -> save(false));

        valueDatePicker.setValue(LocalDate.now());

        setVisible(false);
    }

    void save(boolean isNeededNewTransaction) {
        bindUiElementsWithTransaction();
        transactionController.save(transaction, creditAccount.getValue());
        changeHandler.onChange(isNeededNewTransaction);
    }

    void delete() {
        transactionController.delete(transaction);
        changeHandler.onChange(false);
    }

    void cancel() {
        setVisible(false);
    }

    public final void editTransaction(Transaction transaction) {
        // Transaction could be null after refresh
        if (transaction == null) {
            setVisible(false);
            return;
        }
        this.refreshComboBoxData();
        this.transaction = transaction;
        bindTransactionWithUiElements();
        amount.focus();
        cancel.setVisible(true);
        delete.setVisible(true);
        creditAccount.setVisible(false);
        setVisible(true);
        LOGGER.debug("Selected transaction [{}]", transaction);
    }

    public final void addTransaction(Transaction transaction) {
        this.refreshComboBoxData();
        this.transaction = transaction;
        bindTransactionWithUiElements();
        cancel.setVisible(true);
        delete.setVisible(false);
        debitAccount.setVisible(false);
        creditAccount.setVisible(false);
        setVisible(true);
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
        debitAccount.setItemLabelGenerator(Account::getName);
        debitAccount.setClearButtonVisible(true);
        debitAccount.addFocusListener(event -> {
            updateAccountsComboBoxData(debitAccount, creditAccount);
        });
    }

    private void initCreditAccountBehaviour() {
        creditAccount = new ComboBox<>("Credit account");
        creditAccount.setItemLabelGenerator(Account::getName);
        creditAccount.setClearButtonVisible(true);
        creditAccount.addFocusListener(event -> {
            updateAccountsComboBoxData(creditAccount, debitAccount);
        });
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
        });
    }

    private void initButtonsBehaviour() {
        save.getElement().getThemeList().add("primary");
        delete.getElement().getThemeList().add("error");
        saveAndNew.getElement().getThemeList().add("primary");
        cancel.addClickListener(e -> cancel());
        delete.addClickListener(e -> delete());
        save.addClickListener(e -> save(false));
        saveAndNew.addClickListener(e -> save(true));
    }

    private void updateAccountsComboBoxData(ComboBox<Account> selectedAccount, ComboBox<Account> anotherAccount) {
        List<Account> correctedAccounts = new ArrayList<>(accounts);
        correctedAccounts.remove(anotherAccount.getValue());
        Account currentAcc = selectedAccount.getValue();
        selectedAccount.setItems(correctedAccounts);
        selectedAccount.setValue(currentAcc);
    }

    private void bindTransactionWithUiElements() {
        category.setValue(transaction.getCategory());
        debitAccount.setValue(transaction.getAccount());
        creditAccount.setValue(null);
        valueDatePicker.setValue(transaction.getDate());
        amount.setValue(transaction.getAmount());
        comment.setValue(transaction.getComment());
    }

    private void bindUiElementsWithTransaction() {
        transaction.setCategory(category.getValue());
        transaction.setAccount(debitAccount.getValue());
        transaction.setDate(valueDatePicker.getValue());
        transaction.setAmount(amount.getValue());
        transaction.setComment(comment.getValue());
    }
}
