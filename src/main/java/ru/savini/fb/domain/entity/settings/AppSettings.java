package ru.savini.fb.domain.entity.settings;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AppSettings {
    @Id
    @GeneratedValue(generator="APP_SETTINGS_GENERATOR")
    private Long id;
    private String key;
    private String value;
}