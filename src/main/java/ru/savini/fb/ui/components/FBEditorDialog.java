package ru.savini.fb.ui.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public abstract class FBEditorDialog extends Dialog {
    protected Button cancel;
    protected Button save;
    protected Button delete;
    private HorizontalLayout saveAndCancel;
    protected HorizontalLayout customButtons;
    protected VerticalLayout fields;

    protected FBEditorDialog() {
        initFieldsLayout();
        initCustomButtons();
        initSaveAndCancelLayout();
        initDeleteButton();
        add(fields, customButtons, saveAndCancel, delete);
    }

    private void initFieldsLayout() {
        fields = new VerticalLayout();
        fields.setSpacing(false);
    }

    private void initCustomButtons() {
        customButtons = new HorizontalLayout();
    }

    private void initSaveAndCancelLayout() {
        initCancelButton();
        initSaveButton();
        saveAndCancel = new HorizontalLayout(save, cancel);
    }

    private void initCancelButton() {
        cancel = new Button("Cancel");
        cancel.addClickListener(event -> this.close());
        cancel.setWidthFull();
    }

    private void initSaveButton() {
        save = new Button("Save", VaadinIcon.CHECK.create());
        save.getElement().getThemeList().add("primary");
        save.setWidthFull();
        save.setEnabled(false);
    }

    private void initDeleteButton() {
        delete = new Button("Delete", VaadinIcon.TRASH.create());
        delete.getElement().getThemeList().add("error");
        delete.setWidthFull();
    }
}
