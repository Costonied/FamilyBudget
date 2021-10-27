package ru.savini.fb.ui.editors;

import java.time.Month;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import ru.savini.fb.controller.AccountingUnitController;
import ru.savini.fb.controller.CategoryController;
import ru.savini.fb.domain.entity.AccountingUnit;
import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.ui.components.FBEditorDialog;

@UIScope
@SpringComponent
public class AccountingUnitEditorDialog extends FBEditorDialog implements KeyNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountingUnitEditorDialog.class);

    private transient ChangeHandler changeHandler;
    private transient AccountingUnit accountingUnit;
    private final transient CategoryController categoryController;
    private final transient AccountingUnitController accountingUnitController;

    IntegerField year = new IntegerField("Year");
    private final ComboBox<Month> month = new ComboBox("Month");
    BigDecimalField planAmount = new BigDecimalField("Plan amount");
    BigDecimalField factAmount = new BigDecimalField("Fact amount");
    ComboBox<Category> category = new ComboBox<>("Category");

    private Notification duplicateNotification;

    @Autowired
    public AccountingUnitEditorDialog(AccountingUnitController accountingUnitController,
                                      CategoryController categoryController) {
        this.accountingUnitController = accountingUnitController;
        this.categoryController = categoryController;
        initMonth();
        initCategory();
        initFactAmount();
        initDelete();
        initSave();
        initDuplicateNotification();
        fields.add(category, year, month, planAmount, factAmount);
        cancel.addClickListener(e -> editAccountingUnit(accountingUnit));
        planAmount.setPlaceholder("0.00");
    }

    private void initCategory() {
        category.setItemLabelGenerator(Category::getName);
        category.addValueChangeListener(event -> switchEnablerSaveButton());
    }

    private void initMonth() {
        month.setItemLabelGenerator(Month::name);
        month.setItems(Month.values());
    }

    private void initFactAmount() {
        factAmount.setPlaceholder("0");
        factAmount.setEnabled(false);
    }

    private void initDelete() {
        delete.addClickListener(event -> delete());
        delete.setVisible(false);
    }

    private void initSave() {
        save.addClickListener(e -> save());
        addKeyPressListener(Key.ENTER, e -> save());
    }

    private void initDuplicateNotification() {
        duplicateNotification = new Notification("Error! Duplicate accounting unit!", 3000,
                Notification.Position.MIDDLE);
        duplicateNotification.getElement().getThemeList().add("error");
    }

    void save() {
        bindUIDataWithAccountingUnit();
        try {
            accountingUnitController.save(accountingUnit);
            changeHandler.onChange();
        } catch (DataIntegrityViolationException e) {
            duplicateNotification.open();
        }
    }

    void delete() {
        accountingUnitController.delete(accountingUnit);
        changeHandler.onChange();
    }

    @Override
    public void open() {
        editAccountingUnit(new AccountingUnit());
        category.setEnabled(true);
        month.setEnabled(true);
        year.setEnabled(true);
        super.open();
    }

    public void open(AccountingUnit accountingUnit) {
        editAccountingUnit(accountingUnit);
        category.setEnabled(false);
        month.setEnabled(false);
        year.setEnabled(false);
        super.open();
    }

    public final void editAccountingUnit(AccountingUnit accountingUnit) {
        if (accountingUnit == null) {
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
        bindAccountingUnitWithUIFields();
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

    private void switchEnablerSaveButton() {
        save.setEnabled(category.getValue() != null && !category.getValue().getName().isEmpty());
    }

    private void bindUIDataWithAccountingUnit() {
        accountingUnit.setCategory(category.getValue());
        accountingUnit.setYear(year.getValue());
        accountingUnit.setMonth(month.getValue().getValue());
        accountingUnit.setFactAmount(factAmount.getValue());
        accountingUnit.setPlanAmount(planAmount.getValue());
    }

    private void bindAccountingUnitWithUIFields() {
        category.setValue(accountingUnit.getCategory());
        year.setValue(accountingUnit.getYear());
        month.setValue(Month.of(accountingUnit.getMonth()));
        factAmount.setValue(accountingUnit.getFactAmount());
        planAmount.setValue(accountingUnit.getPlanAmount());
    }
}
