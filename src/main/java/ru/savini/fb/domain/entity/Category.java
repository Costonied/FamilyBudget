package ru.savini.fb.domain.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;

@Entity
@Getter
@Setter
public class Category {
    @Id
    @GeneratedValue(generator="CATEGORY_GENERATOR")
    private Integer id;
    private String name;
    private String type;

    public Category() {
    }

    public Category(String name, String type) {
    }

    @Override
    public String toString() {
        return String.format("Category[id=%d, name='%s', type='%s']",
                id, name, type);
    }
}
