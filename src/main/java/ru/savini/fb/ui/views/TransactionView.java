package ru.savini.fb.ui.views;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import ru.savini.fb.domain.entity.Transaction;
import ru.savini.fb.ui.editors.TransactionEditor;
import ru.savini.fb.controller.TransactionController;

@Route(value = "transactions", layout = MainView.class)
@PageTitle("Transactions")
public class TransactionView extends VerticalLayout {

    final Grid<Transaction> grid;
    private final Button addNewBtn;
    private final TransactionEditor editor;
    private final TransactionController transactionController;

    public TransactionView(TransactionEditor editor,
                           TransactionController transactionController) {
        this.editor = editor;
        this.transactionController = transactionController;
        this.grid = new Grid<>(Transaction.class, false);
        this.addNewBtn = new Button("New transaction", VaadinIcon.PLUS.create());

        // build layout
        HorizontalLayout actions = new HorizontalLayout(addNewBtn);
        add(actions, grid, editor);

        grid.setHeight("300px");
        setGridColumns();

        // Hook logic to components

        // Connect selected Transaction to editor or hide if none is selected
//        grid.asSingleSelect()
//                .addValueChangeListener(e -> editor.editTransaction(e.getValue()));

        // Instantiate and edit new Category the new button is clicked
        addNewBtn.addClickListener(e -> editor.addTransaction(new Transaction()));

        // Listen changes made by the editor, refresh data from backend
        editor.setChangeHandler(isNeededNewTransaction -> {
            if (isNeededNewTransaction) {
                editor.addTransaction(new Transaction());
            } else {
                editor.setVisible(false);
            }
            setListOfTransactions();
        });

        // Initialize listing
        setListOfTransactions();
    }

    void setListOfTransactions() {
        grid.setItems(transactionController.getAll());
    }

    private void setGridColumns() {
        grid.addColumns("date", "category.name",
                "amount", "account.name", "type", "comment");
    }
}