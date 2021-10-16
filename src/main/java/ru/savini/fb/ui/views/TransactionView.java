package ru.savini.fb.ui.views;

import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import ru.savini.fb.domain.entity.Transaction;
import ru.savini.fb.repo.filters.TransactionFilter;
import ru.savini.fb.ui.components.FBGrid;
import ru.savini.fb.ui.editors.TransactionEditor;
import ru.savini.fb.controller.TransactionController;
import ru.savini.fb.ui.helpers.CurrencyHelper;

@Route(value = "transactions", layout = MainView.class)
@PageTitle("Transactions")
public class TransactionView extends VerticalLayout {

    final FBGrid<Transaction> grid;
    private final Button addNewBtn;
    private final TransactionEditor editor;
    private final transient TransactionController transactionController;
    private final TextField filter = new TextField();
    private final TransactionFilter transactionFilter = new TransactionFilter();

    public TransactionView(TransactionEditor editor,
                           TransactionController transactionController) {
        this.editor = editor;
        this.transactionController = transactionController;
        this.grid = new FBGrid<>(Transaction.class);
        this.addNewBtn = new Button("New transaction", VaadinIcon.PLUS.create());

        // build layout
        HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);
        add(actions, grid, editor);

        // Instantiate and edit new Category the new button is clicked
        addNewBtn.addClickListener(e -> editor.addTransaction());

        // Listen changes made by the editor, refresh data from backend
        editor.setChangeHandler(isNeededNewTransaction -> {
            if (isNeededNewTransaction) {
                editor.addTransaction();
            } else {
                editor.setVisible(false);
            }
            setListOfTransactions();
        });

        initFilter();
        initGrid();
        setListOfTransactions();
    }

    private void initFilter() {
        filter.setClearButtonVisible(true);
        filter.setPlaceholder("Filter by account");
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> {
            transactionFilter.setAccountName(e.getValue());
            setListOfTransactions();
        });
    }

    void setListOfTransactions() {
        grid.setItems(transactionController.getFiltered(transactionFilter));
    }

    private void initGrid() {
        grid.setHeight("300px");
        setGridColumns();
        grid.asSingleSelect()
                .addValueChangeListener(e -> editor.editTransaction(transactionController.getEventFromTransaction(e.getValue())));
        grid.addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS,GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
    }

    private void setGridColumns() {
        grid.addColumn(Transaction::getDate).setHeader("Date");
        grid.addColumn(transaction -> transaction.getCategory().getName()).setHeader("Category");
        grid.addColumn(new NumberRenderer<>(Transaction::getAmount, CurrencyHelper.format)).setHeader("Amount");
        grid.addColumn(transaction -> transaction.getAccount().getName()).setHeader("Account");
        grid.addColumn(Transaction::getType).setHeader("Type");
        grid.addColumn(Transaction::getComment).setHeader("Comment");
    }
}