package ru.savini.fb.ui.editors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.beans.factory.annotation.Autowired;

import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.controller.CategoryController;

@UIScope
@SpringComponent
public class CategoryEditor extends VerticalLayout implements KeyNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryEditor.class);

    private Category category;
    private final CategoryController categoryController;

    TextField name = new TextField("Category name");
    ComboBox<String> type;

    Button save = new Button("Save", VaadinIcon.CHECK.create());
    Button cancel = new Button("Cancel");
    Button delete = new Button("Delete", VaadinIcon.TRASH.create());
    HorizontalLayout actions = new HorizontalLayout(save, cancel, delete);

    Binder<Category> binder = new Binder<>(Category.class);

    private ChangeHandler changeHandler;

    @Autowired
    public CategoryEditor(CategoryController categoryController) {
        this.categoryController = categoryController;
        initBinder();
        add(name, type, actions);
        setSpacing(true);
        save.getElement().getThemeList().add("primary");
        delete.getElement().getThemeList().add("error");
        addKeyPressListener(Key.ENTER, e -> save());
        save.addClickListener(e -> save());
        delete.addClickListener(e -> delete());
        cancel.addClickListener(e -> editCategory(category));
        setVisible(false);
    }

    private void initBinder() {
        initCategoryCodes();
        binder.forField(type).bind(Category::getName, (t, a) -> t.setType(type.getValue()));
        binder.bindInstanceFields(this);
    }

    private void initCategoryCodes() {
        type = new ComboBox<>("Category type");
        type.setItems(categoryController.getCategoryCodes());
    }

    void save() {
        categoryController.save(category);
        changeHandler.onChange();
    }

    void delete() {
        categoryController.delete(category);
        changeHandler.onChange();
    }

    public final void editCategory(Category category) {
        if (category == null) {
            setVisible(false);
            return;
        }
        final boolean persisted = category.getId() != null;
        if (persisted) {
            this.category = categoryController.getById(category.getId());
        }
        else {
            this.category = category;
        }
        cancel.setVisible(persisted);

        // Bind category properties to similarly named fields
        // Could also use annotation or "manual binding" or programmatically
        // moving values from fields to entities before saving
        binder.setBean(this.category);
        type.setValue(category.getType());

        setVisible(true);

        // Focus name initially
        name.focus();
        LOGGER.info("Edit category [{}]", category);
    }

    public interface ChangeHandler {
        void onChange();
    }

    public void setChangeHandler(ChangeHandler h) {
        // ChangeHandler is notified when either save or delete
        // is clicked
        changeHandler = h;
    }
}
