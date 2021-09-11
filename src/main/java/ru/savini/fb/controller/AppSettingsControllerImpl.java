package ru.savini.fb.controller;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import ru.savini.fb.domain.entity.settings.AppSettings;
import ru.savini.fb.repo.AppSettingsRepo;


@Component
public class AppSettingsControllerImpl implements AppSettingsController {
    private final AppSettingsRepo repo;

    @Autowired
    public AppSettingsControllerImpl(AppSettingsRepo appSettingsRepo) {
        this.repo = appSettingsRepo;
    }

    @Override
    public void save(AppSettings category) {
        repo.save(category);
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    @Override
    public List<AppSettings> getAll() {
        return repo.findAll();
    }

    @Override
    public List<AppSettings> getByKeyStartsWithIgnoreCase(String key) {
        return repo.findByKeyStartsWithIgnoreCase(key);
    }

    @Override
    public String getValue(String key) {
        AppSettings appSettings = repo.findByKey(key);
        if (appSettings != null) {
            return appSettings.getValue();
        }
        else {
            return null;
        }
    }
}
