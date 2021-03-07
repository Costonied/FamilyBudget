package ru.savini.fb.ui.editors;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;

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
    TextField amount = new TextField("Amount");
    TextField comment = new TextField("Comment");
    DatePicker valueDatePicker = new DatePicker("Transaction date");
    ComboBox<Category> category;
    ComboBox<Account> account;
    ComboBox<Account> creditAccount;

    Button save = new Button("Save", VaadinIcon.CHECK.create());
    Button cancel = new Button("Cancel");
    Button delete = new Button("Delete", VaadinIcon.TRASH.create());
    HorizontalLayout actions = new HorizontalLayout(save, cancel, delete);

    private ChangeHandler changeHandler;

    @Autowired
    public TransactionEditor(AccountController accountController,
                             CategoryController categoryController,
                             TransactionController transactionController) {
        this.accountController = accountController;
        this.categoryController = categoryController;
        this.transactionController = transactionController;
        initAccountBehaviour();
        initCreditAccountBehaviour();
        initCategoryBehaviour();
        add(category, valueDatePicker, amount, account, creditAccount, comment, actions);
        setSpacing(true);
        save.getElement().getThemeList().add("primary");
        delete.getElement().getThemeList().add("error");
        save.addClickListener(e -> save());
        delete.addClickListener(e -> delete());
        cancel.addClickListener(e -> cancel());
        addKeyPressListener(Key.ENTER, e -> save());

        valueDatePicker.setValue(LocalDate.now());

        setVisible(false);
    }

    void save() {
        bindUiElementsWithTransaction();
        transactionController.save(transaction, creditAccount.getValue());
        changeHandler.onChange();
    }

    void delete() {
        transactionController.delete(transaction);
        changeHandler.onChange();
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
        creditAccount.setVisible(false);
        setVisible(true);
    }

    public interface ChangeHandler {
        void onChange();
    }

    public void setChangeHandler(ChangeHandler h) {
        // ChangeHandler is notified when either save or delete
        // is clicked
        changeHandler = h;
    }

    private void refreshComboBoxData() {
        accounts = accountController.getAll();
        account.setItems(accounts);
        creditAccount.setItems(accounts);
        category.setItems(categoryController.getAll());
    }

    private void initAccountBehaviour() {
        account = new ComboBox<>("Account");
        account.setItemLabelGenerator(Account::getName);
        account.setClearButtonVisible(true);
        account.addFocusListener(event -> {
            updateAccountsComboBoxData(account, creditAccount);
        });
    }

    private void initCreditAccountBehaviour() {
        creditAccount = new ComboBox<>("Credit account");
        creditAccount.setItemLabelGenerator(Account::getName);
        creditAccount.setClearButtonVisible(true);
        creditAccount.addFocusListener(event -> {
            updateAccountsComboBoxData(creditAccount, account);
        });
    }

    private void initCategoryBehaviour() {
        category = new ComboBox<>("Category");
        category.setItemLabelGenerator(Category::getName);
        category.addValueChangeListener(event -> {
            if (event.getValue() != null && (
                    CategoryCode.isGoalsCategory(event.getValue()) ||
                    CategoryCode.isTransferCategory(event.getValue()))) {
                account.setLabel("Debit account");
                creditAccount.setVisible(true);
            }
        });
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
        account.setValue(transaction.getAccount());
        creditAccount.setValue(null);
        valueDatePicker.setValue(transaction.getDate());
        amount.setValue(String.valueOf(transaction.getAmount()));
        comment.setValue(transaction.getComment());
    }

    private void bindUiElementsWithTransaction() {
        transaction.setCategory(category.getValue());
        transaction.setAccount(account.getValue());
        transaction.setDate(valueDatePicker.getValue());
        transaction.setAmount(Double.parseDouble(amount.getValue()));
        transaction.setComment(comment.getValue());
    }
}
