package ru.savini.fb.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.springframework.util.StringUtils;
import ru.savini.fb.domain.entity.Account;
import ru.savini.fb.repo.AccountRepository;
import ru.savini.fb.ui.editors.AccountEditor;

@Route("accounts")
public class AccountView extends VerticalLayout {

    private final AccountRepository repo;

    private final AccountEditor editor;

    final Grid<Account> grid;

    final TextField filter;

    private final Button addNewBtn;

    public AccountView(AccountRepository repo, AccountEditor editor) {
        this.repo = repo;
        this.editor = editor;
        this.grid = new Grid<>(Account.class);
        this.filter = new TextField();
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
        filter.addValueChangeListener(e -> listCustomers(e.getValue()));

        // Connect selected Customer to editor or hide if none is selected
        grid.asSingleSelect().addValueChangeListener(e -> {
            editor.editAccount(e.getValue());
        });

        // Instantiate and edit new Customer the new button is clicked
        addNewBtn.addClickListener(e -> editor.editAccount(new Account("", 0.00)));

        // Listen changes made by the editor, refresh data from backend
        editor.setChangeHandler(() -> {
            editor.setVisible(false);
            listCustomers(filter.getValue());
        });

        // Initialize listing
        listCustomers(null);
    }

    // tag::listCustomers[]
    void listCustomers(String filterText) {
        if (StringUtils.isEmpty(filterText)) {
            grid.setItems(repo.findAll());
        }
        else {
            grid.setItems(repo.findByNameStartsWithIgnoreCase(filterText));
        }
    }
    // end::listCustomers[]

}