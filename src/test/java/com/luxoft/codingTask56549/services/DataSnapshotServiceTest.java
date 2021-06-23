package com.luxoft.codingTask56549.services;

import com.luxoft.codingTask56549.DataSnapshotRepository;
import com.luxoft.codingTask56549.exceptions.InvalidCsvFormatException;
import com.luxoft.codingTask56549.models.DataSnapshot;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DataSnapshotServiceTest {

    @Mock
    private DataSnapshotRepository repository;
    private final Path uploadFolderPath = Paths.get(System.getProperty("java.io.tmpdir"));

    @Test
    void saveOnDisk_shouldSaveOnDisk() throws IOException {
        var message = "test";
        var inputStream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
        var dataSnapshotService = new DataSnapshotService(uploadFolderPath, repository);
        var path = dataSnapshotService.saveOnDisk(inputStream);
        assertTrue(Files.exists(path), "Uploaded file does not exists");
        assertEquals(message, Files.readString(path), "Unexpected uploaded content");
    }

    @Test
    void fromCsv_shouldParseSuccessfully() {
        var csv = "1,name 1,description 1,1624459855200";
        var dataSnapshot = DataSnapshotService.fromCsv(csv, 1);
        var expected = new DataSnapshot("1", "name 1", "description 1", ZonedDateTime.ofInstant(Instant.ofEpochMilli(1624459855200L), ZoneId.of("UTC")));
        assertEquals(expected, dataSnapshot, "Object returned should be equal to " + expected);
    }

    @Test
    void fromCsv_shouldFailOnInvalidLengthTimestamp() {
        var csv = "1,name 1,description 1,9999999999999999";
        var exception = assertThrows(
            InvalidCsvFormatException.class,
            () -> DataSnapshotService.fromCsv(csv, 1),
            "A InvalidCsvFormatException was expected"
        );

        var expectedMessage = "Could not parse a DataSnapshot from csv line 1";
        assertEquals(expectedMessage, exception.getMessage(), "Unexpected exception message");
    }

    @Test
    void fromCsv_shouldFailOnInvalidCharTimestamp() {
        var csv = "1,name 1,description 1,162445985520z";
        var exception = assertThrows(
            InvalidCsvFormatException.class,
            () -> DataSnapshotService.fromCsv(csv, 1),
            "A InvalidCsvFormatException was expected"
        );

        var expectedMessage = "Could not parse a DataSnapshot from csv line 1";
        assertEquals(expectedMessage, exception.getMessage(), "Unexpected exception message");
    }

    @Test
    void fromCsv_shouldFailOnEmptyLine() {
        var csv = "";
        var exception = assertThrows(
            InvalidCsvFormatException.class,
            () -> DataSnapshotService.fromCsv(csv, 1),
            "A InvalidCsvFormatException was expected"
        );

        var expectedMessage = "Could not parse a DataSnapshot from csv line 1";
        assertEquals(expectedMessage, exception.getMessage(), "Unexpected exception message");
    }

    @Test
    void fromCsv_shouldFailOnTooFewFields() {
        var csv = "name 1,description 1,1624459855200";
        var exception = assertThrows(
            InvalidCsvFormatException.class,
            () -> DataSnapshotService.fromCsv(csv, 1),
            "A InvalidCsvFormatException was expected"
        );

        var expectedMessage = "Could not parse a DataSnapshot from csv line 1";
        assertEquals(expectedMessage, exception.getMessage(), "Unexpected exception message");
    }

    @Test
    void fromCsv_shouldFailOnTooManyFields() {
        var csv = "1,name 1,description 1,1624459855200,EXTRA FIELD";
        var exception = assertThrows(
            InvalidCsvFormatException.class,
            () -> DataSnapshotService.fromCsv(csv, 1),
            "A InvalidCsvFormatException was expected"
        );

        var expectedMessage = "Could not parse a DataSnapshot from csv line 1";
        assertEquals(expectedMessage, exception.getMessage(), "Unexpected exception message");
    }
}