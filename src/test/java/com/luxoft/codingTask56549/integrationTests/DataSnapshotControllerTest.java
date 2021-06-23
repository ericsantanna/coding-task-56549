package com.luxoft.codingTask56549.integrationTests;

import com.luxoft.codingTask56549.DataSnapshotRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DataSnapshotControllerTest {
    @LocalServerPort
    private int port;

    private String dataSnapshotBaseUrl;
    private static final Path sample1 = Paths.get("src", "test", "resources", "sample.1.csv");
    private static final Path sample100 = Paths.get("src", "test", "resources", "sample.100.csv");
    private static final Path sampleWithInvalidLine = Paths.get("src", "test", "resources", "sample.withInvalidLine.csv");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DataSnapshotRepository dataSnapshotRepository;

    @BeforeEach
    void setUp() {
        dataSnapshotBaseUrl = String.format("http://localhost:%d/data-snapshots", port);
    }

    @AfterEach
    void tearDown() {
        dataSnapshotRepository.deleteAll();
    }

    @Test
    void upload() {
        var post = loadSample(sample100);

        assertEquals(HttpStatus.OK, post.getStatusCode(), "Unexpected http status code");

        var count = dataSnapshotRepository.count();
        assertEquals(count, 100, "Unexpected number of records saved");
    }

    @Test
    void upload_shouldRollbackOnInvalidLine() {
        var post = loadSample(sampleWithInvalidLine);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, post.getStatusCode(), "Unexpected http status code");

        var expectedMessage = "{\"message\":\"Could not parse a DataSnapshot from csv line 101\"}";
        assertEquals(expectedMessage, post.getBody(), "Unexpected returned message");

        var count = dataSnapshotRepository.count();
        assertEquals(count, 0, "Rollback was not completed");
    }

    @Test
    void get() {
        loadSample(sample1);

        var count = dataSnapshotRepository.count();
        assertEquals(count, 1, "Test record was not loaded");

        var get = restTemplate.getForEntity(dataSnapshotBaseUrl + "/1", String.class);

        assertEquals(HttpStatus.OK, get.getStatusCode(), "Unexpected http status code");

        var expectedMessage = "{\"primaryKey\":\"1\",\"name\":\"name 1\",\"description\":\"description 1\",\"updatedTimestamp\":\"2021-06-23T16:50:55.2+02:00\"}";
        assertEquals(expectedMessage, get.getBody(), "Unexpected returned message");
    }

    @Test
    void delete() {
        loadSample(sample1);

        var count = dataSnapshotRepository.count();
        assertEquals(count, 1, "Test record was not loaded");

        restTemplate.delete(dataSnapshotBaseUrl + "/1");

        count = dataSnapshotRepository.count();
        assertEquals(count, 0, "Record was not deleted");
    }

    private ResponseEntity<String> loadSample(Path sample) {
        var multipart = new LinkedMultiValueMap<>();
        multipart.add("file", new FileSystemResource(sample));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        var request = new HttpEntity<>(multipart, headers);
        return restTemplate.postForEntity(dataSnapshotBaseUrl + "/upload", request, String.class);
    }
}