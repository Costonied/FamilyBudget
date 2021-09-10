package ru.savini.fb.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.savini.fb.domain.entity.settings.AppSettings;


public interface AppSettingsRepo extends JpaRepository<AppSettings, Long> {
    List<AppSettings> findByKeyStartsWithIgnoreCase(String key);

}
