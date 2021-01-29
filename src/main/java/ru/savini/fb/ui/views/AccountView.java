package ru.savini.fb.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.springframework.util.StringUtils;

import ru.savini.fb.domain.entity.Account;
import ru.savini.fb.ui.editors.AccountEditor;
import ru.savini.fb.controller.AccountController;

@Route(value = "accounts", layout = MainView.class)
@PageTitle("Accounts")
@RouteAlias(value = "", layout = MainView.class)
public class AccountView extends VerticalLayout {

    private final AccountController accountController;
    private final AccountEditor editor;
    final Grid<Account> grid;
    final TextField filter;
    private final Button addNewBtn;

    public AccountView(AccountController accountController, AccountEditor editor) {
        this.editor = editor;
        this.filter = new TextField();
        this.grid = new Grid<>(Account.class);
        this.accountController = accountController;
        this.addNewBtn = new Button("New account", VaadinIcon.PLUS.create());

        // build layout
        HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);
        add(actions, grid, editor);

        grid.setHeight("300px");
        grid.setColumns("id", "name", "amount", "currency");
        grid.getColumnByKey("id").setWidth("50px").setFlexGrow(0);

        filter.setPlaceholder("Filter by name");

        // Hook logic to components

        // Replace listing with filtered content when user changes filter
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> listAccounts(e.getValue()));

        // Connect selected Account to editor or hide if none is selected
        grid.asSingleSelect().addValueChangeListener(e -> {
            editor.editAccount(e.getValue());
        });

        // Instantiate and edit new Account the new button is clicked
        addNewBtn.addClickListener(e -> editor.editAccount(new Account("", 0.00, "RUB")));

        // Listen changes made by the editor, refresh data from backend
        editor.setChangeHandler(() -> {
            editor.setVisible(false);
            listAccounts(filter.getValue());
        });

        // Initialize listing
        listAccounts(null);
    }

    // tag::listCustomers[]
    void listAccounts(String filterText) {
        if (StringUtils.isEmpty(filterText)) {
            grid.setItems(accountController.getAll());
        }
        else {
            grid.setItems(accountController.getByNameStartsWithIgnoreCase(filterText));
        }
    }
    // end::listCustomers[]

}