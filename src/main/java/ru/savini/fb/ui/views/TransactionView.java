package ru.savini.fb.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.util.StringUtils;
import ru.savini.fb.domain.entity.Transaction;
import ru.savini.fb.repo.TransactionRepo;
import ru.savini.fb.ui.editors.TransactionEditor;

@Route(value = "transactions", layout = MainView.class)
@PageTitle("Transactions")
public class TransactionView extends VerticalLayout {

    private final TransactionRepo repo;
    private final TransactionEditor editor;
    final Grid<Transaction> grid;
    private final Button addNewBtn;

    public TransactionView(TransactionRepo repo, TransactionEditor editor) {
        this.repo = repo;
        this.editor = editor;
        this.grid = new Grid<>(Transaction.class);
        this.addNewBtn = new Button("New transaction", VaadinIcon.PLUS.create());

        // build layout
        HorizontalLayout actions = new HorizontalLayout(addNewBtn);
        add(actions, grid, editor);

        grid.setHeight("300px");
        grid.setColumns("id", "categoryId", "date", "amount", "accountId", "comment");
        grid.getColumnByKey("id").setWidth("50px").setFlexGrow(0);

        // Hook logic to components

        // Connect selected Transaction to editor or hide if none is selected
        grid.asSingleSelect()
                .addValueChangeListener(e -> editor.editTransaction(e.getValue()));

        // Instantiate and edit new Category the new button is clicked
        addNewBtn.addClickListener(e -> editor.editTransaction(new Transaction()));

        // Listen changes made by the editor, refresh data from backend
        editor.setChangeHandler(() -> {
            editor.setVisible(false);
//            listTransactions(filter.getValue());
            listTransactions(null);
        });

        // Initialize listing
        listTransactions(null);
    }

    // tag::listCategories[]
    void listTransactions(String filterText) {
        if (StringUtils.isEmpty(filterText)) {
            grid.setItems(repo.findAll());
        }
        else {
//            grid.setItems(repo.findByNameStartsWithIgnoreCase(filterText));
        }
    }
    // end::listCategories[]

}