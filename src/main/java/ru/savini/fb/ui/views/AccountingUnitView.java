package ru.savini.fb.ui.views;

import java.time.Month;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import ru.savini.fb.domain.entity.AccountingUnit;
import ru.savini.fb.domain.enums.CategoryCode;
import ru.savini.fb.ui.components.FBGrid;
import ru.savini.fb.controller.AccountingUnitController;
import ru.savini.fb.ui.editors.AccountingUnitEditorDialog;
import ru.savini.fb.ui.helpers.CurrencyHelper;
import ru.savini.fb.ui.models.AccountingCategory;

@PageTitle("Accounting")
@Route(value = "accounting", layout = MainView.class)
public class AccountingUnitView extends VerticalLayout {
    private static final String GOALS_VIEW_CODE = "goals";
    private static final String INCOMES_VIEW_CODE = "incomes";
    private static final String SPENDING_VIEW_CODE = "spending";

    private String selectedView;
    private final FBGrid<AccountingCategory> goalsView = new FBGrid<>(AccountingCategory.class);
    private final FBGrid<AccountingCategory> incomesView = new FBGrid<>(AccountingCategory.class);
    private final FBGrid<AccountingCategory> spendingView = new FBGrid<>(AccountingCategory.class);
    private final AccountingUnitEditorDialog editor;
    private final ComboBox<Integer> selectedYear = new ComboBox<>();
    private final ComboBox<Month> selectedMonth = new ComboBox<>();
    private final Button find = new Button("Find");
    private final HorizontalLayout filter = new HorizontalLayout();
    private final transient AccountingUnitController accountingUnitController;

    private final Map<Integer, AccountingUnit> accountingUnits = new HashMap<>();
    private final List<AccountingCategory> goals = new ArrayList<>();
    private final List<AccountingCategory> incomes = new ArrayList<>();
    private final List<AccountingCategory> spending = new ArrayList<>();

    public AccountingUnitView(AccountingUnitController accountingUnitController, AccountingUnitEditorDialog editor) {
        this.editor = editor;
        this.accountingUnitController = accountingUnitController;
        Button addNewBtn = new Button("New accounting unit", VaadinIcon.PLUS.create());
        // build layout
        HorizontalLayout actions = new HorizontalLayout(addNewBtn);
        H2 goalsViewHeader = new H2("Goals");
        H2 incomesViewHeader = new H2("Incomes");
        H2 spendingViewHeader = new H2("Spending");
        this.setSpacing(false);
        add(actions, filter, incomesViewHeader, incomesView, goalsViewHeader, goalsView, spendingViewHeader, spendingView);

        addNewBtn.addClickListener(e -> editor.open());

        initFind();
        initEditor();
        initFilter();
        initSelectedYear();
        initSelectedMonth();
        initSpendingGrid();
        initIncomesGrid();

        initGoalsGrid();
        refreshGoalsGrid();
        refreshIncomesGrid();
        refreshSpendingGrid();
    }

    private void initEditor() {
        editor.setChangeHandler(() -> {
            refreshSelectedGrid();
            editor.close();
        });
    }

    private void initFilter() {
        filter.setAlignItems(Alignment.BASELINE);
        filter.add(selectedYear, selectedMonth, find);
    }

    private void initSpendingGrid() {
        setupGridColumns(spendingView);
        spendingView.asSingleSelect().addValueChangeListener(event -> {
            goalsView.deselectAll();
            incomesView.deselectAll();
            selectedView = SPENDING_VIEW_CODE;
            doGridSingleSelectAction(event);
        });
    }

    private void initIncomesGrid() {
        setupGridColumns(incomesView);
        incomesView.asSingleSelect().addValueChangeListener(event -> {
            goalsView.deselectAll();
            spendingView.deselectAll();
            selectedView = INCOMES_VIEW_CODE;
            doGridSingleSelectAction(event);
        });
    }

    private void initGoalsGrid() {
        setupGridColumns(goalsView);
        goalsView.asSingleSelect().addValueChangeListener(event -> {
            incomesView.deselectAll();
            spendingView.deselectAll();
            selectedView = GOALS_VIEW_CODE;
            doGridSingleSelectAction(event);
        });
    }

    private void refreshSpendingGrid() {
        refreshSpending();
        spendingView.setItems(spending);
    }

    private void refreshIncomesGrid() {
        refreshIncomes();
        incomesView.setItems(incomes);
    }

    private void refreshGoalsGrid() {
        refreshGoals();
        goalsView.setItems(goals);
    }

    private void refreshSelectedGrid() {
        switch (selectedView) {
            case GOALS_VIEW_CODE:
                refreshGoalsGrid();
                break;
            case INCOMES_VIEW_CODE:
                refreshIncomesGrid();
                break;
            case SPENDING_VIEW_CODE:
                refreshSpendingGrid();
                break;
            default:
                break;
        }
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
        find.addClickListener(event -> {
            refreshGoalsGrid();
            refreshIncomesGrid();
            refreshSpendingGrid();
        });
    }

    private void refreshAccountingUnits() {
        accountingUnits.clear();
        accountingUnitController.getAllByYearAndMonth(
                selectedYear.getValue(), selectedMonth.getValue().getValue())
                .forEach(accountingUnit -> accountingUnits.put(
                            accountingUnit.getCategory().getId(),
                            accountingUnit));
    }

    private void refreshSpending() {
        refreshAccountingUnits();
        spending.clear();
        accountingUnitController.getAllCategory().stream()
                .filter(CategoryCode::isOutgoingCategory)
                .forEach(category -> {
                    AccountingUnit accountingUnit = accountingUnits.get(category.getId());
                    AccountingCategory accountingCategory = new AccountingCategory(category, accountingUnit);
                    spending.add(accountingCategory);
        });
    }

    private void refreshIncomes() {
        refreshAccountingUnits();
        incomes.clear();
        accountingUnitController.getAllCategory().stream()
                .filter(CategoryCode::isIncomeCategory)
                .forEach(category -> {
                    AccountingUnit accountingUnit = accountingUnits.get(category.getId());
                    AccountingCategory accountingCategory = new AccountingCategory(category, accountingUnit);
                    incomes.add(accountingCategory);
                });
    }

    private void refreshGoals() {
        refreshAccountingUnits();
        goals.clear();
        accountingUnitController.getAllCategory().stream()
                .filter(CategoryCode::isGoalsCategory)
                .forEach(category -> {
                    AccountingUnit accountingUnit = accountingUnits.get(category.getId());
                    AccountingCategory accountingCategory = new AccountingCategory(category, accountingUnit);
                    goals.add(accountingCategory);
                });
    }

    private void setupGridColumns(FBGrid<AccountingCategory> grid) {
        grid.addColumn(AccountingCategory::getCategoryName).setHeader("Category");
        grid.addColumn(new NumberRenderer<>(AccountingCategory::getPlanAmount, CurrencyHelper.format)).setHeader("Plan amount");
        grid.addColumn(new NumberRenderer<>(AccountingCategory::getFactAmount, CurrencyHelper.format)).setHeader("Fact amount");
    }

    private void doGridSingleSelectAction(HasValue.ValueChangeEvent<AccountingCategory> event) {
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
    }
}