package ru.savini.fb.ui.components;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

public class FBGrid<T> extends Grid<T> {

    public FBGrid(Class<T> beanType) {
        super(beanType, false);
        this.addThemeVariants(GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_ROW_STRIPES);
    }
}
