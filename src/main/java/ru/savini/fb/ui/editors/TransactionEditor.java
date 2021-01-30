package ru.savini.fb.ui.editors;

import java.io.IOException;
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
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;

import ru.savini.fb.gsheets.GSheetsService;
import ru.savini.fb.domain.entity.Account;
import ru.savini.fb.domain.entity.Category;
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

    TextField amount = new TextField("Amount");
    TextField comment = new TextField("Comment");
    DatePicker valueDatePicker = new DatePicker("Transaction date");
    ComboBox<Account> account = new ComboBox<>("Account");
    ComboBox<Category> category = new ComboBox<>("Category");

    Button save = new Button("Save", VaadinIcon.CHECK.create());
    Button cancel = new Button("Cancel");
    Button delete = new Button("Delete", VaadinIcon.TRASH.create());
    HorizontalLayout actions = new HorizontalLayout(save, cancel, delete);

    Binder<Transaction> binder = new Binder<>(Transaction.class);

    private GSheetsService gSheets;
    private ChangeHandler changeHandler;

    @Autowired
    public TransactionEditor(AccountController accountController,
                             CategoryController categoryController,
                             TransactionController transactionController,
                             GSheetsService gSheets) {
        this.gSheets = gSheets;
        this.accountController = accountController;
        this.categoryController = categoryController;
        this.transactionController = transactionController;
        initBinder();
        add(category, valueDatePicker, amount, account, comment, actions);
        setSpacing(true);
        save.getElement().getThemeList().add("primary");
        delete.getElement().getThemeList().add("error");
        save.addClickListener(e -> save());
        delete.addClickListener(e -> delete());
        cancel.addClickListener(e -> cancel());
        addKeyPressListener(Key.ENTER, e -> save());

        valueDatePicker.setValue(LocalDate.now());

        account.setItemLabelGenerator(Account::getName);
        account.setItems(accountController.getAll());

        category.setItemLabelGenerator(Category::getName);
        category.setItems(categoryController.getAll());

        setVisible(false);
    }

    private void initBinder() {
        binder.forField(amount)
                .withConverter(new StringToDoubleConverter("Must enter a double"))
                .bind(Transaction::getAmount, Transaction::setAmount);
        binder.bind(valueDatePicker, Transaction::getDate, Transaction::setDate);
        binder.bindInstanceFields(this);
    }

    void save() {
        transactionController.save(transaction);
        changeHandler.onChange();
        try {
            // TODO: Сейчас даже при edit добавляется новая транзакция в GSheets.
            //  Нужно сделать edit для GSheets
            gSheets.addTransaction(transaction);
        } catch (IOException e) {
            LOGGER.error("Problem save category to Google Sheets");
        }
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
        this.transaction = transaction;
        binder.setBean(this.transaction);
        category.setValue(transaction.getCategory());
        account.setValue(transaction.getAccount());
        amount.focus();
        cancel.setVisible(true);
        delete.setVisible(true);
        setVisible(true);
        LOGGER.debug("Selected transaction [{}]", transaction);
    }

    public final void addTransaction(Transaction transaction) {
        this.transaction = transaction;
        binder.setBean(this.transaction);
        cancel.setVisible(true);
        delete.setVisible(false);
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
}
