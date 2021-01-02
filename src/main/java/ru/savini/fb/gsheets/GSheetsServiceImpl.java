package ru.savini.fb.gsheets;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.stereotype.Service;
import ru.savini.fb.domain.entity.Account;
import ru.savini.fb.domain.entity.Category;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class GSheetsServiceImpl implements GSheetsService {
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

    public GSheetsServiceImpl() throws GeneralSecurityException, IOException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        this.service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @Override
    public void addAccount(Account account) throws IOException {
        ValueRange valueRange = getNewAccountValueRange(account);
        service.spreadsheets().values()
                .append(SPREADSHEET_ID, valueRange.getRange(), valueRange)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    private ValueRange getNewAccountValueRange(Account account) {
        ValueRange valueRange = new ValueRange();
        valueRange.setMajorDimension("ROWS");
        valueRange.setRange("Accounts!A2:D");
        valueRange.setValues(Collections.singletonList(
                Arrays.asList(
                        account.getId(), account.getName(),
                        account.getAmount(), account.getCurrency())));
        return valueRange;
    }

    @Override
    // TODO: рефакторинг - разбить ответственность функции
    public List<Account> getAccounts() throws IOException {
        final int idIndex = 0;
        final int nameIndex = 1;
        final int amountIndex = 2;
        final int accountCurrencyIndex = 3;
        final String range = "Accounts!A2:D";
        List<Account> accounts = new ArrayList<>();
        ValueRange response = this.service.spreadsheets().values()
                .get(SPREADSHEET_ID, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            // nothing
        } else {
            for (List<Object> row : values) {
                accounts.add(
                        new Account(
                                Long.parseLong(row.get(idIndex).toString()),
                                row.get(nameIndex).toString(),
                                GSheetsUtils.getDoubleFromString(row.get(amountIndex).toString()),
                                row.get(accountCurrencyIndex).toString()
                        )
                );
            }
        }
        return accounts;
    }

    @Override
    public void addCategory(Category category) throws IOException {
        ValueRange valueRange = getNewCategoryValueRange(category);
        service.spreadsheets().values()
                .append(SPREADSHEET_ID, valueRange.getRange(), valueRange)
                .setValueInputOption("USER_ENTERED")
                .execute();
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
