package ru.savini.fb.domain.entity;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;

@Entity
public class Category {
    @Id
    @GeneratedValue(generator="CATEGORY_GENERATOR")
    private Long id;
    private String name;
    private String type;

    public Category() {
    }

    public Category(String name, String type) {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("Category[id=%d, name='%s', type='%s']",
                id, name, type);
    }
}
