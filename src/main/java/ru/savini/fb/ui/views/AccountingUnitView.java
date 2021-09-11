package ru.savini.fb.ui.views;

import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import ru.savini.fb.domain.entity.AccountingUnit;
import ru.savini.fb.ui.editors.AccountingUnitEditor;
import ru.savini.fb.controller.AccountingUnitController;
import ru.savini.fb.ui.helpers.CurrencyHelper;

@PageTitle("Accounting")
@Route(value = "accounting", layout = MainView.class)
public class AccountingUnitView extends VerticalLayout {

    private final AccountingUnitEditor editor;
    final Grid<AccountingUnit> grid;
    private final Button addNewBtn;
    private final transient AccountingUnitController accountingUnitController;

    public AccountingUnitView(AccountingUnitController accountingUnitController, AccountingUnitEditor editor) {
        this.accountingUnitController = accountingUnitController;
        this.editor = editor;
        this.grid = new Grid<>(AccountingUnit.class, false);
        this.addNewBtn = new Button("New accounting unit", VaadinIcon.PLUS.create());

        // build layout
        HorizontalLayout actions = new HorizontalLayout(addNewBtn);
        add(actions, grid, editor);

        addNewBtn.addClickListener(e -> editor.editAccountingUnit(new AccountingUnit()));

        // Listen changes made by the editor, refresh data from backend
        editor.setChangeHandler(() -> {
            editor.setVisible(false);
            listAccountingUnits();
        });

        initGrid();
        listAccountingUnits();
    }

    private void initGrid() {
        grid.setHeight("300px");
        grid.addColumn(AccountingUnit::getYear).setHeader("Year");
        grid.addColumn(AccountingUnit::getMonth).setHeader("Month");
        grid.addColumn(accountingUnit -> accountingUnit.getCategory().getName()).setHeader("Category");
        grid.addColumn(new NumberRenderer<>(AccountingUnit::getPlanAmount, CurrencyHelper.format)).setHeader("Plant amount");
        grid.addColumn(new NumberRenderer<>(AccountingUnit::getFactAmount, CurrencyHelper.format)).setHeader("Fact amount");
        grid.asSingleSelect()
                .addValueChangeListener(e -> editor.editAccountingUnit(e.getValue()));
    }

    void listAccountingUnits() {
        grid.setItems(accountingUnitController.getAll());
    }
}