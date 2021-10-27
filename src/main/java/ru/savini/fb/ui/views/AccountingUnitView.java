package ru.savini.fb.ui.views;

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

@PageTitle("Accounting")
@Route(value = "accounting", layout = MainView.class)
public class AccountingUnitView extends VerticalLayout {

    final FBGrid<AccountingUnit> grid;
    private final Button addNewBtn;
    private final AccountingUnitEditorDialog editor;
    private final transient AccountingUnitController accountingUnitController;

    public AccountingUnitView(AccountingUnitController accountingUnitController, AccountingUnitEditorDialog editor) {
        this.accountingUnitController = accountingUnitController;
        this.editor = editor;
        this.grid = new FBGrid<>(AccountingUnit.class);
        this.addNewBtn = new Button("New accounting unit", VaadinIcon.PLUS.create());
        // build layout
        HorizontalLayout actions = new HorizontalLayout(addNewBtn);
        add(actions, grid);

        addNewBtn.addClickListener(e -> editor.open());

        editor.setChangeHandler(() -> {
            listAccountingUnits();
            editor.close();
        });

        initGrid();
        listAccountingUnits();
    }

    private void initGrid() {
        grid.addColumn(AccountingUnit::getYear).setHeader("Year");
        grid.addColumn(AccountingUnit::getMonth).setHeader("Month");
        grid.addColumn(accountingUnit -> accountingUnit.getCategory().getName()).setHeader("Category");
        grid.addColumn(new NumberRenderer<>(AccountingUnit::getPlanAmount, CurrencyHelper.format)).setHeader("Plant amount");
        grid.addColumn(new NumberRenderer<>(AccountingUnit::getFactAmount, CurrencyHelper.format)).setHeader("Fact amount");
        grid.asSingleSelect()
                .addValueChangeListener(e -> editor.open(e.getValue()));
    }

    void listAccountingUnits() {
        grid.setItems(accountingUnitController.getAll());
    }
}