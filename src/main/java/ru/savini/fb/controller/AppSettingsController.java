package ru.savini.fb.controller;

import ru.savini.fb.domain.entity.settings.AppSettings;

import java.util.List;

public interface AppSettingsController {
    void save(AppSettings category);
    void deleteById(Long id);

    String getValue(String key);

    List<AppSettings> getAll();
    List<AppSettings> getByKeyStartsWithIgnoreCase(String name);
}
