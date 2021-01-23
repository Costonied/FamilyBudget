package ru.savini.fb.ui.views;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import ru.savini.fb.domain.entity.Account;
import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.exceptions.NoSuchAccountingUnitIdException;
import ru.savini.fb.exceptions.NoSuchCategoryIdException;
import ru.savini.fb.repo.AccountRepo;
import ru.savini.fb.repo.CategoryRepo;
import ru.savini.fb.repo.TransactionRepo;
import ru.savini.fb.domain.entity.Transaction;
import ru.savini.fb.ui.editors.TransactionEditor;

import java.util.Optional;

@Route(value = "transactions", layout = MainView.class)
@PageTitle("Transactions")
public class TransactionView extends VerticalLayout {

    private final AccountRepo accountRepo;
    private final CategoryRepo categoryRepo;
    private final TransactionRepo transactionRepo;
    private final TransactionEditor editor;
    final Grid<Transaction> grid;
    private final Button addNewBtn;

    public TransactionView(AccountRepo accountRepo,
                           TransactionEditor editor,
                           CategoryRepo categoryRepo,
                           TransactionRepo transactionRepo) {
        this.editor = editor;
        this.accountRepo = accountRepo;
        this.categoryRepo = categoryRepo;
        this.transactionRepo = transactionRepo;
        this.grid = new Grid<>(Transaction.class, false);
        this.addNewBtn = new Button("New transaction", VaadinIcon.PLUS.create());

        // build layout
        HorizontalLayout actions = new HorizontalLayout(addNewBtn);
        add(actions, grid, editor);

        grid.setHeight("300px");
        setGridColumns();

        // Hook logic to components

        // Connect selected Transaction to editor or hide if none is selected
        grid.asSingleSelect()
                .addValueChangeListener(e -> editor.editTransaction(e.getValue()));

        // Instantiate and edit new Category the new button is clicked
        addNewBtn.addClickListener(e -> editor.editTransaction(new Transaction()));

        // Listen changes made by the editor, refresh data from backend
        editor.setChangeHandler(() -> {
            editor.setVisible(false);
            setListOfTransactions();
        });

        // Initialize listing
        setListOfTransactions();
    }

    void setListOfTransactions() {
        grid.setItems(transactionRepo.findAll());
    }

    private void setGridColumns() {
        grid.addColumn(this::getCategoryNameFromTrans).setHeader("Category");
        grid.addColumns("date", "amount");
        grid.addColumn(this::getAccountNameFromTrans).setHeader("Account");
        grid.addColumn("comment");
    }

    private String getCategoryNameFromTrans(Transaction transaction) {
        Optional<Category> optionalCategory = categoryRepo.findById(transaction.getCategoryId());
        Category category = optionalCategory.orElseThrow(NoSuchCategoryIdException::new);
        return category.getName();
    }

    private String getAccountNameFromTrans(Transaction transaction) {
        Optional<Account> optionalAccount = accountRepo.findById(transaction.getAccountId());
        Account account = optionalAccount.orElseThrow(NoSuchAccountingUnitIdException::new);
        return account.getName();
    }
}