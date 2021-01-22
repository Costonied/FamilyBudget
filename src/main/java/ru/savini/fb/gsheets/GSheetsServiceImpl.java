package ru.savini.fb.gsheets;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.savini.fb.domain.entity.Account;
import ru.savini.fb.domain.entity.Category;
import ru.savini.fb.domain.entity.Transaction;
import ru.savini.fb.exceptions.NoSuchAccountIdException;
import ru.savini.fb.exceptions.NoSuchCategoryIdException;
import ru.savini.fb.exceptions.ReadingValueRangeException;
import ru.savini.fb.exceptions.WritingValueRangeException;
import ru.savini.fb.repo.AccountRepo;
import ru.savini.fb.repo.CategoryRepo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static ru.savini.fb.gsheets.GSheetsUtils.getLongFromObj;
import static ru.savini.fb.gsheets.GSheetsUtils.getStringFromObj;
import static ru.savini.fb.gsheets.GSheetsUtils.getDoubleFromObj;

@Service
public class GSheetsServiceImpl implements GSheetsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GSheetsServiceImpl.class);

    private static final String APPLICATION_NAME = "Google Sheets API Family Budget";
    private static final String SPREADSHEET_ID = "1BB-MFczIhtcZdYPUS9htPEpMsu-vxCDaFcQ4qsjWqgY";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    Sheets service;
    private final CategoryRepo categoryRepo;
    private final AccountRepo accountRepo;

    @Autowired
    public GSheetsServiceImpl(CategoryRepo categoryRepo, AccountRepo accountRepo)
            throws GeneralSecurityException, IOException {
        this.accountRepo = accountRepo;
        this.categoryRepo = categoryRepo;
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        this.service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Override
    public void addAccount(Account account) throws IOException {
        ValueRange accountValueRange = getNewAccountValueRange(account);
        sendValueRange(accountValueRange);
    }

    private ValueRange getNewAccountValueRange(Account account) {
        ValueRange valueRange = new ValueRange();
        valueRange.setRange(GSheetsInfo.ACCOUNTS_RANGE);
        valueRange.setValues(Collections.singletonList(
                Arrays.asList(
                        account.getId(), account.getName(),
                        account.getAmount(), account.getCurrency())));
        return valueRange;
    }

    @Override
    public List<Account> getAccounts() {
        List<Account> accounts = new ArrayList<>();
        List<List<Object>> accountsValue = getValuesOfDomain(GSheetsInfo.ACCOUNTS_RANGE);
        for (List<Object> row : accountsValue) {
            accounts.add(createAccFromRow(row));
        }
        return accounts;
    }

    @Override
    public void addCategory(Category category) {
        ValueRange categoryValueRange = getNewCategoryValueRange(category);
        sendValueRange(categoryValueRange);
    }

    @Override
    public void addTransaction(Transaction transaction) {
        ValueRange transactionValueRange = getNewTransactionValueRange(transaction);
        sendValueRange(transactionValueRange);
    }

    private ValueRange getNewTransactionValueRange(Transaction transaction) {
        ValueRange valueRange = new ValueRange();
        valueRange.setRange(GSheetsInfo.TRANSACTIONS_RANGE);
        valueRange.setValues(Collections.singletonList(
                Arrays.asList(
                        transaction.getId(),
                        transaction.getCategoryId(),
                        getCategoryNameForTransaction(transaction),
                        transaction.getDate().toString(),
                        transaction.getAmount(),
                        transaction.getAccountId(),
                        getAccountNameForTransaction(transaction))));
        return valueRange;
    }

    private String getCategoryNameForTransaction(Transaction transaction) {
        int categoryId = transaction.getCategoryId();
        Optional<Category> categoryResult = categoryRepo.findById(categoryId);
        if (categoryResult.isPresent()) {
            return categoryResult.get().getName();
        } else {
            throw new NoSuchCategoryIdException("Category ID: " + categoryId);
        }
    }

    private String getAccountNameForTransaction(Transaction transaction) {
        Long accountId = transaction.getAccountId();
        Optional<Account> accountOptional = accountRepo.findById(accountId);
        if (accountOptional.isPresent()) {
            return accountOptional.get().getName();
        } else {
            throw new NoSuchAccountIdException("Account ID: " + accountId);
        }
    }

    private List<List<Object>> getValuesOfDomain(String domain) {
        try {
            ValueRange domainValueRange = this.service
                    .spreadsheets().values()
                    .get(GSheetsInfo.SPREADSHEET_ID, domain)
                    .execute();
            List<List<Object>> result = domainValueRange.getValues();
            return result != null ? result : Collections.singletonList(Collections.emptyList());
        } catch (IOException e) {
            throw new ReadingValueRangeException(String.format("Problem while get '%s'", domain));
        }
    }

    private void sendValueRange(ValueRange valueRange) {
        try {
            service.spreadsheets().values()
                    .append(GSheetsInfo.SPREADSHEET_ID, valueRange.getRange(), valueRange)
                    .setValueInputOption("USER_ENTERED")
                    .execute();
        } catch (GoogleJsonResponseException e) {
            LOGGER.error("Error while sending information to range {} because {}", valueRange.getRange(), e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            throw new WritingValueRangeException("Range is:" + valueRange.getRange());
        }
    }

    private Account createAccFromRow(List<Object> row) {
        return new Account(
                getLongFromObj(row.get(GSheetsInfo.ACCOUNT_ID_IDX)),
                getStringFromObj(row.get(GSheetsInfo.ACCOUNT_NAME_IDX)),
                getDoubleFromObj(row.get(GSheetsInfo.ACCOUNT_AMOUNT_IDX)),
                getStringFromObj(GSheetsInfo.ACCOUNT_CURRENCY_IDX)
        );
    }

    private ValueRange getNewCategoryValueRange(Category category) {
        ValueRange valueRange = new ValueRange();
        valueRange.setMajorDimension("ROWS");
        valueRange.setRange("Categories!A2:C");
        valueRange.setValues(Collections.singletonList(
                Arrays.asList(
                        category.getId(), category.getName(), category.getType())));
        return valueRange;
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GSheetsServiceImpl.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("costonied");
    }
}
