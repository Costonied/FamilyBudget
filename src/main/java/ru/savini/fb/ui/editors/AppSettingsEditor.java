package ru.savini.fb.ui.editors;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import org.springframework.beans.factory.annotation.Autowired;
import ru.savini.fb.controller.AppSettingsController;
import ru.savini.fb.domain.entity.settings.AppSettings;


@UIScope
@SpringComponent
public class AppSettingsEditor extends VerticalLayout implements KeyNotifier {
    private final AppSettingsController controller;

    private Long appSettingsId;

    private final TextField key = new TextField("Key");
    private final TextField value = new TextField("Value");

    private final Button save = new Button("Save", VaadinIcon.CHECK.create());
    private final Button cancel = new Button("Cancel");
    private final Button delete = new Button("Delete", VaadinIcon.TRASH.create());

    HorizontalLayout actions = new HorizontalLayout(save, cancel, delete);

    private ChangeHandler changeHandler;

    @Autowired
    public AppSettingsEditor(AppSettingsController controller) {
        this.controller = controller;
        setVisible(false);
        initComponents();
        add(key, value, actions);
    }

    private void initComponents() {
        initSave();
        initCancel();
        initDelete();
    }

    private void initSave() {
        save.addClickListener(event -> saveEntityAndNotifyView());
    }

    private void saveEntityAndNotifyView() {
        controller.save(getEditorEntity());
        changeHandler.onChange();
        closeEditor();
    }

    private AppSettings getEditorEntity() {
        return new AppSettings(this.appSettingsId, this.key.getValue(), this.value.getValue());
    }

    private void initCancel() {
        cancel.addClickListener(event -> closeEditor());
    }

    private void closeEditor() {
        setVisible(false);
        clearComponentValues();
    }

    private void clearComponentValues() {
        key.setValue("");
        value.setValue("");
        appSettingsId = null;
    }

    private void initDelete() {
        delete.getElement().getThemeList().add("error");
        delete.addClickListener(event -> deleteEntityAndNotifyView());
    }

    private void deleteEntityAndNotifyView() {
        controller.deleteById(this.appSettingsId);
        changeHandler.onChange();
        closeEditor();
    }

    public void addAppSettings() {
        delete.setVisible(false);
        clearComponentValues();
        setVisible(true);
    }

    public void editAppSettings(AppSettings appSettings) {
        if (appSettings == null) {
            return;
        }
        delete.setVisible(true);
        setupComponentValuesFromEntity(appSettings);
        setVisible(true);
    }

    private void setupComponentValuesFromEntity(AppSettings appSettings) {
        this.appSettingsId = appSettings.getId();
        this.key.setValue(appSettings.getKey());
        this.value.setValue(appSettings.getValue());
    }

    public void setChangeHandler(ChangeHandler changeHandler) {
        this.changeHandler = changeHandler;
    }

    public interface ChangeHandler {
        void onChange();
    }
}
