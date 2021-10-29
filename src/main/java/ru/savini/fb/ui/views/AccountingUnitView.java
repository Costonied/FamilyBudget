package ru.savini.fb.ui.views;

import java.time.Month;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import ru.savini.fb.domain.entity.AccountingUnit;
import ru.savini.fb.ui.components.FBGrid;
import ru.savini.fb.controller.AccountingUnitController;
import ru.savini.fb.ui.editors.AccountingUnitEditorDialog;
import ru.savini.fb.ui.helpers.CurrencyHelper;
import ru.savini.fb.ui.models.AccountingCategory;

@PageTitle("Accounting")
@Route(value = "accounting", layout = MainView.class)
public class AccountingUnitView extends VerticalLayout {

    private final FBGrid<AccountingCategory> accountingGrid = new FBGrid<>(AccountingCategory.class);
    private final Button addNewBtn;
    private final AccountingUnitEditorDialog editor;
    private final ComboBox<Integer> selectedYear = new ComboBox<>();
    private final ComboBox<Month> selectedMonth = new ComboBox<>();
    private final Button find = new Button("Find");
    private final HorizontalLayout filter = new HorizontalLayout();
    private final transient AccountingUnitController accountingUnitController;

    private final Map<Integer, AccountingUnit> accountingUnits = new HashMap<>();
    private final List<AccountingCategory> accountingCategories = new ArrayList<>();

    public AccountingUnitView(AccountingUnitController accountingUnitController, AccountingUnitEditorDialog editor) {
        this.editor = editor;
        this.accountingUnitController = accountingUnitController;
        this.addNewBtn = new Button("New accounting unit", VaadinIcon.PLUS.create());
        // build layout
        HorizontalLayout actions = new HorizontalLayout(addNewBtn);
        add(actions, filter, accountingGrid);

        addNewBtn.addClickListener(e -> editor.open());

        initFind();
        initEditor();
        initFilter();
        initSelectedYear();
        initSelectedMonth();
        initAccountingGrid();

        refreshAccountingGrid();
    }

    private void initEditor() {
        editor.setChangeHandler(() -> {
            refreshAccountingGrid();
            editor.close();
        });
    }

    private void initFilter() {
        filter.setAlignItems(Alignment.BASELINE);
        filter.add(selectedYear, selectedMonth, find);
    }

    private void initAccountingGrid() {
        accountingGrid.addColumn(AccountingCategory::getCategoryName).setHeader("Category");
        accountingGrid.addColumn(new NumberRenderer<>(AccountingCategory::getPlanAmount, CurrencyHelper.format)).setHeader("Plan amount");
        accountingGrid.addColumn(new NumberRenderer<>(AccountingCategory::getFactAmount, CurrencyHelper.format)).setHeader("Fact amount");
        accountingGrid.asSingleSelect()
                .addValueChangeListener(event -> {
                    if (event.getValue() == null) {
                        return;
                    }
                    AccountingUnit accountingUnit = event.getValue().getAccountingUnit();
                    if (accountingUnit == null) {
                        accountingUnit = new AccountingUnit();
                        accountingUnit.setCategory(event.getValue().getCategory());
                        accountingUnit.setYear(selectedYear.getValue());
                        accountingUnit.setMonth(selectedMonth.getValue().getValue());
                    }
                    editor.open(accountingUnit);
                });
    }

    private void refreshAccountingGrid() {
        setupAccountingCategories();
        accountingGrid.setItems(accountingCategories);
    }

    private void initSelectedMonth() {
        selectedMonth.setLabel("Selected month");
        selectedMonth.setItems(Month.values());
        selectedMonth.setValue(LocalDate.now().getMonth());
    }

    private void initSelectedYear() {
        selectedYear.setLabel("Selected year");
        selectedYear.setItems(2021, 2022);
        selectedYear.setValue(2021);
    }

    private void initFind() {
        find.addClickListener(event -> refreshAccountingGrid());
    }

    private void setupAccountingUnits() {
        accountingUnits.clear();
        accountingUnitController.getAllByYearAndMonth(
                selectedYear.getValue(), selectedMonth.getValue().getValue())
                .forEach(accountingUnit -> accountingUnits.put(
                            accountingUnit.getCategory().getId(),
                            accountingUnit));
    }

    private void setupAccountingCategories() {
        setupAccountingUnits();
        accountingCategories.clear();
        accountingUnitController.getAllCategory().forEach(category -> {
            AccountingUnit accountingUnit = accountingUnits.get(category.getId());
            AccountingCategory accountingCategory = new AccountingCategory(category, accountingUnit);
            accountingCategories.add(accountingCategory);
        });
    }
}