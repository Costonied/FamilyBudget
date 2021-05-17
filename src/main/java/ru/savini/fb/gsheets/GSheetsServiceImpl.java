package ru.savini.fb.gsheets;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.savini.fb.domain.entity.Transaction;
import ru.savini.fb.exceptions.WritingValueRangeException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class GSheetsServiceImpl implements GSheetsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GSheetsServiceImpl.class);

    private static final String APPLICATION_NAME = "Google Sheets API Family Budget";
    private static final String SERVICE_ACCOUNT_CREDENTIALS = "/google-service-account-credentials.json";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);

    private final Sheets service;

    @Autowired
    public GSheetsServiceImpl() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        this.service = new Sheets.Builder(httpTransport, JSON_FACTORY, getGoogleServiceAccountCredential())
                .setApplicationName(APPLICATION_NAME)
                .build();
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
                        transaction.getCategory().getName(),
                        transaction.getDate().toString(),
                        transaction.getAmount(),
                        transaction.getAccount().getName(),
                        transaction.getType(),
                        transaction.getComment())));
        return valueRange;
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

    private static Credential getGoogleServiceAccountCredential() throws IOException {
        InputStream in = getCredentialsInputStream(SERVICE_ACCOUNT_CREDENTIALS);
        GoogleCredential credential = GoogleCredential.fromStream(in).createScoped(SCOPES);
        in.close();
        return credential;
    }

    private static InputStream getCredentialsInputStream(String credentialsFileName) throws FileNotFoundException {
        InputStream in = GSheetsServiceImpl.class.getResourceAsStream(credentialsFileName);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + credentialsFileName);
        } else {
            return in;
        }
    }
}
