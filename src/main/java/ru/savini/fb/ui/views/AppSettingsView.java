package ru.savini.fb.ui.views;

import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import org.springframework.util.StringUtils;

import ru.savini.fb.ui.components.FBGrid;
import ru.savini.fb.ui.editors.AppSettingsEditor;
import ru.savini.fb.controller.AppSettingsController;
import ru.savini.fb.domain.entity.settings.AppSettings;


@Route(value = "settings", layout = MainView.class)
@PageTitle("Settings")
public class AppSettingsView extends VerticalLayout {

    final FBGrid<AppSettings> grid;
    final TextField filter;
    private final Button addNewBtn;
    private final AppSettingsEditor editor;
    private final transient AppSettingsController controller;

    public AppSettingsView(AppSettingsController categoryController, AppSettingsEditor editor) {
        this.editor = editor;
        this.controller = categoryController;
        this.grid = new FBGrid<>(AppSettings.class);
        this.filter = new TextField();
        this.addNewBtn = new Button("New settings", VaadinIcon.PLUS.create());

        HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);
        add(actions, grid, editor);

        grid.setHeight("300px");
        grid.setColumns("key", "value");

        filter.setPlaceholder("Filter by key");
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> listAppSettings(e.getValue()));

        grid.asSingleSelect().addValueChangeListener(e -> editor.editAppSettings(e.getValue()));
        initComponents();
        listAppSettings(null);
    }

    private void initComponents() {
        initNewButton();
        initEditor();
    }

    private void initNewButton() {
        addNewBtn.addClickListener(e -> addEntityAndDeselectGrid());
    }

    private void addEntityAndDeselectGrid() {
        editor.addAppSettings();
        grid.select(null);
    }

    private void initEditor() {
        editor.setChangeHandler(() -> listAppSettings(null));
    }

    void listAppSettings(String filterText) {
        if (StringUtils.isEmpty(filterText)) {
            grid.setItems(controller.getAll());
        }
        else {
            grid.setItems(controller.getByKeyStartsWithIgnoreCase(filterText));
        }
    }

}