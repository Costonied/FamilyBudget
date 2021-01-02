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
import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.repo.CategoryRepo;
import ru.savini.fb.ui.editors.CategoryEditor;

@Route("categories")
public class CategoryView extends VerticalLayout {

    private final CategoryRepo repo;
    private final CategoryEditor editor;
    final Grid<Category> grid;
    final TextField filter;
    private final Button addNewBtn;

    public CategoryView(CategoryRepo repo, CategoryEditor editor) {
        this.repo = repo;
        this.editor = editor;
        this.grid = new Grid<>(Category.class);
        this.filter = new TextField();
        this.addNewBtn = new Button("New category", VaadinIcon.PLUS.create());

        // build layout
        HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);
        add(actions, grid, editor);

        grid.setHeight("300px");
        grid.setColumns("id", "name", "type");
        grid.getColumnByKey("id").setWidth("50px").setFlexGrow(0);

        filter.setPlaceholder("Filter by name");

        // Hook logic to components

        // Replace listing with filtered content when user changes filter
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> listCategories(e.getValue()));

        // Connect selected Category to editor or hide if none is selected
        grid.asSingleSelect().addValueChangeListener(e -> editor.editCategory(e.getValue()));

        // Instantiate and edit new Category the new button is clicked
        addNewBtn.addClickListener(e -> editor.editCategory(new Category("", "")));

        // Listen changes made by the editor, refresh data from backend
        editor.setChangeHandler(() -> {
            editor.setVisible(false);
            listCategories(filter.getValue());
        });

        // Initialize listing
        listCategories(null);
    }

    // tag::listCategories[]
    void listCategories(String filterText) {
        if (StringUtils.isEmpty(filterText)) {
            grid.setItems(repo.findAll());
        }
        else {
            grid.setItems(repo.findByNameStartsWithIgnoreCase(filterText));
        }
    }
    // end::listCategories[]

}