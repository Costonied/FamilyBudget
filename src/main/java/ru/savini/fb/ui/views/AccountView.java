package ru.savini.fb.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.util.StringUtils;

import ru.savini.fb.domain.entity.Account;
import ru.savini.fb.ui.editors.AccountEditor;
import ru.savini.fb.controller.AccountController;
import ru.savini.fb.ui.helpers.CurrencyHelper;

import java.math.BigDecimal;

@Route(value = "accounts", layout = MainView.class)
@PageTitle("Accounts")
public class AccountView extends VerticalLayout {

    private final AccountController accountController;
    private final AccountEditor editor;
    final Grid<Account> grid;
    final TextField filter;
    private final Button addNewBtn;

    public AccountView(AccountController accountController, AccountEditor editor) {
        this.editor = editor;
        this.filter = new TextField();
        this.grid = new Grid<>(Account.class, false);
        this.accountController = accountController;
        this.addNewBtn = new Button("New account", VaadinIcon.PLUS.create());

        // build layout
        HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);
        add(actions, grid, editor);

        filter.setPlaceholder("Filter by name");

        // Hook logic to components

        // Replace listing with filtered content when user changes filter
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> listAccounts(e.getValue()));

        // Instantiate and edit new Account the new button is clicked
        addNewBtn.addClickListener(e -> editor.editAccount(new Account("", BigDecimal.valueOf(0).setScale(2, BigDecimal.ROUND_DOWN), "RUB")));

        // Listen changes made by the editor, refresh data from backend
        editor.setChangeHandler(() -> {
            editor.setVisible(false);
            listAccounts(filter.getValue());
        });
        initGrid();
        listAccounts(null);
    }

    private void initGrid() {
        grid.setHeight("300px");
        grid.addColumn(Account::getName).setHeader("Name");
        grid.addColumn(new NumberRenderer<>(Account::getAmount, CurrencyHelper.format)).setHeader("Amount");
        grid.addColumn(Account::getCurrency).setHeader("Currency");
        grid.addColumn(new ComponentRenderer<>(account -> {
            Checkbox checkbox = new Checkbox();
            checkbox.setValue(account.isNeedAccounting());
            checkbox.setReadOnly(true);
            return checkbox;})
        ).setHeader("Need accounting");
        grid.asSingleSelect().addValueChangeListener(e -> {
            editor.editAccount(e.getValue());
        });
    }

    void listAccounts(String filterText) {
        if (StringUtils.isEmpty(filterText)) {
            grid.setItems(accountController.getAll());
        }
        else {
            grid.setItems(accountController.getByNameStartsWithIgnoreCase(filterText));
        }
    }

}