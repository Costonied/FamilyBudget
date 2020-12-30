package ru.savini.fb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ru.savini.fb.domain.entity.Account;
import ru.savini.fb.repo.AccountRepository;

@SpringBootApplication
public class FamilyBudgetApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(FamilyBudgetApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(FamilyBudgetApplication.class);
    }

    @Bean
    public CommandLineRunner loadData(AccountRepository repository) {
        return (args) -> {
            // save a couple of customers
            repository.save(new Account("Наличные", 10.0));
            repository.save(new Account("Карта Сбербанк", 2.34));

            // fetch all customers
            LOGGER.info("Accounts found with findAll():");
            LOGGER.info("-------------------------------");
            for (Account account : repository.findAll()) {
                LOGGER.info(account.toString());
            }
            LOGGER.info("");

            // fetch an individual account by ID
            Account account = repository.findById(1L).get();
            LOGGER.info("Account found with findOne(1L):");
            LOGGER.info("--------------------------------");
            LOGGER.info(account.toString());
            LOGGER.info("");

            // fetch accounts by last name
            LOGGER.info("Account found with findByNameStartsWithIgnoreCase('Карта'):");
            LOGGER.info("--------------------------------------------");
            for (Account card : repository
                    .findByNameStartsWithIgnoreCase("Карта")) {
                LOGGER.info(card.toString());
            }
            LOGGER.info("");
        };
    }
}
