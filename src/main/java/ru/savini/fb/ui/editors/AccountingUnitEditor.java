package ru.savini.fb.ui.editors;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import org.springframework.beans.factory.annotation.Autowired;

import ru.savini.fb.controller.AccountingUnitController;
import ru.savini.fb.controller.CategoryController;
import ru.savini.fb.domain.entity.AccountingUnit;
import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.gsheets.GSheetsService;

@UIScope
@SpringComponent
public class AccountingUnitEditor extends VerticalLayout implements KeyNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountingUnitEditor.class);

    private AccountingUnit accountingUnit;
    private final CategoryController categoryController;
    private final AccountingUnitController accountingUnitController;

    IntegerField year = new IntegerField("Year");
    IntegerField month = new IntegerField("Month");
    NumberField planAmount = new NumberField("Plan amount");
    NumberField factAmount = new NumberField("Fact amount");
    ComboBox<Category> category = new ComboBox<>("Category");

    Button save = new Button("Save", VaadinIcon.CHECK.create());
    Button cancel = new Button("Cancel");
    Button delete = new Button("Delete", VaadinIcon.TRASH.create());
    HorizontalLayout actions = new HorizontalLayout(save, cancel, delete);

    Binder<AccountingUnit> binder = new Binder<>(AccountingUnit.class);

    private GSheetsService gSheets;
    private ChangeHandler changeHandler;

    @Autowired
    public AccountingUnitEditor(AccountingUnitController accountingUnitController,
                                CategoryController categoryController,
                                GSheetsService gSheets) {
        this.accountingUnitController = accountingUnitController;
        this.categoryController = categoryController;
        this.gSheets = gSheets;
        initBinder();
        add(category, year, month, planAmount, factAmount, actions);
        setSpacing(true);
        save.getElement().getThemeList().add("primary");
        delete.getElement().getThemeList().add("error");
        addKeyPressListener(Key.ENTER, e -> save());
        save.addClickListener(e -> save());
        delete.addClickListener(e -> delete());
        cancel.addClickListener(e -> editAccountingUnit(accountingUnit));
        category.setItemLabelGenerator(Category::getName);
        setVisible(false);
    }

    private void initBinder() {
        binder.bindInstanceFields(this);
    }

    void save() {
        accountingUnitController.save(accountingUnit);
        changeHandler.onChange();
        try {
            gSheets.addAccountingUnit(accountingUnit);
        } catch (IOException e) {
            LOGGER.error("Problem save accounting unit to Google Sheets");
        }
    }

    void delete() {
        accountingUnitController.delete(accountingUnit);
        changeHandler.onChange();
    }

    public final void editAccountingUnit(AccountingUnit accountingUnit) {
        if (accountingUnit == null) {
            setVisible(false);
            return;
        }
        this.refreshComboBox();
        final boolean persisted = accountingUnit.getId() != null;
        if (persisted) {
            this.accountingUnit = accountingUnitController.getById(accountingUnit.getId());
            category.setValue(this.accountingUnit.getCategory());
        } else {
            this.accountingUnit = accountingUnit;
        }
        cancel.setVisible(persisted);

        // Bind transaction properties to similarly named fields
        // Could also use annotation or "manual binding" or programmatically
        // moving values from fields to entities before saving
        binder.setBean(this.accountingUnit);

        setVisible(true);

        // Focus name initially
        planAmount.focus();
        LOGGER.info("Edit accounting unit [{}]", accountingUnit);
    }

    public interface ChangeHandler {
        void onChange();
    }

    public void setChangeHandler(ChangeHandler h) {
        // ChangeHandler is notified when either save or delete
        // is clicked
        changeHandler = h;
    }

    private void refreshComboBox() {
        category.setItems(categoryController.getAll());
    }
}
