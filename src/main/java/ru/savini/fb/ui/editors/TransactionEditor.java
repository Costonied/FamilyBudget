package ru.savini.fb.ui.editors;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.converter.StringToLongConverter;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.savini.fb.domain.entity.Transaction;
import ru.savini.fb.exceptions.NoSuchTransactionIdException;
import ru.savini.fb.gsheets.GSheetsService;
import ru.savini.fb.repo.TransactionRepo;

import java.io.IOException;
import java.time.LocalDate;

@UIScope
@SpringComponent
public class TransactionEditor extends VerticalLayout implements KeyNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionEditor.class);

    private Transaction transaction;
    private final TransactionRepo repo;

    TextField amount = new TextField("Amount");
    TextField accountId = new TextField("Account ID");
    IntegerField categoryId = new IntegerField("Category ID");
    DatePicker valueDatePicker = new DatePicker("Transaction date");

    Button save = new Button("Save", VaadinIcon.CHECK.create());
    Button cancel = new Button("Cancel");
    Button delete = new Button("Delete", VaadinIcon.TRASH.create());
    HorizontalLayout actions = new HorizontalLayout(save, cancel, delete);

    Binder<Transaction> binder = new Binder<>(Transaction.class);

    private GSheetsService gSheets;
    private ChangeHandler changeHandler;

    @Autowired
    public TransactionEditor(TransactionRepo repo, GSheetsService gSheets) {
        this.repo = repo;
        this.gSheets = gSheets;
        initBinder();
        add(categoryId, valueDatePicker, amount, accountId, actions);
        setSpacing(true);
        save.getElement().getThemeList().add("primary");
        delete.getElement().getThemeList().add("error");
        addKeyPressListener(Key.ENTER, e -> save());
        save.addClickListener(e -> save());
        delete.addClickListener(e -> delete());
        cancel.addClickListener(e -> editTransaction(transaction));

        valueDatePicker.setValue(LocalDate.now());

        setVisible(false);
    }

    private void initBinder() {
        binder.forField(amount)
                .withConverter(new StringToDoubleConverter("Must enter a double"))
                .bind(Transaction::getAmount, Transaction::setAmount);
        binder.forField(accountId)
                .withConverter(new StringToLongConverter("Must enter a long"))
                .bind(Transaction::getAccountId, Transaction::setAccountId);
        binder.bind(valueDatePicker, Transaction::getDate, Transaction::setDate);
        binder.bindInstanceFields(this);
    }

    void save() {
        repo.save(transaction);
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
        repo.delete(transaction);
        changeHandler.onChange();
    }

    public final void editTransaction(Transaction transaction) {
        if (transaction == null) {
            setVisible(false);
            return;
        }
        final boolean persisted = transaction.getId() != null;
        if (persisted) {
            this.transaction = repo.findById(transaction.getId())
                    .orElseThrow(NoSuchTransactionIdException::new);
        } else {
            this.transaction = transaction;
        }
        cancel.setVisible(persisted);

        // Bind transaction properties to similarly named fields
        // Could also use annotation or "manual binding" or programmatically
        // moving values from fields to entities before saving
        binder.setBean(this.transaction);

        setVisible(true);

        // Focus name initially
        amount.focus();
        LOGGER.debug("Selected transaction [{}]", transaction);
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
