package ru.savini.fb.ui.views;

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

@PageTitle("Accounting")
@Route(value = "accounting", layout = MainView.class)
public class AccountingUnitView extends VerticalLayout {

    private final AccountingUnitController accountingUnitController;
    private final AccountingUnitEditor editor;
    final Grid<AccountingUnit> grid;
    private final Button addNewBtn;

    public AccountingUnitView(AccountingUnitController accountingUnitController, AccountingUnitEditor editor) {
        this.accountingUnitController = accountingUnitController;
        this.editor = editor;
        this.grid = new Grid<>(AccountingUnit.class);
        this.addNewBtn = new Button("New accounting unit", VaadinIcon.PLUS.create());

        // build layout
        HorizontalLayout actions = new HorizontalLayout(addNewBtn);
        add(actions, grid, editor);

        grid.setHeight("300px");
        grid.setColumns("id", "year", "month", "category.name", "planAmount", "factAmount");
        grid.getColumnByKey("id").setWidth("50px").setFlexGrow(0);

        // Hook logic to components

        // Connect selected Transaction to editor or hide if none is selected
        grid.asSingleSelect()
                .addValueChangeListener(e -> editor.editAccountingUnit(e.getValue()));

        addNewBtn.addClickListener(e -> editor.editAccountingUnit(new AccountingUnit()));

        // Listen changes made by the editor, refresh data from backend
        editor.setChangeHandler(() -> {
            editor.setVisible(false);
            listAccountingUnits();
        });

        // Initialize listing
        listAccountingUnits();
    }

    void listAccountingUnits() {
        grid.setItems(accountingUnitController.getAll());
    }
}