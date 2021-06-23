package com.luxoft.codingTask56549.services;

import com.luxoft.codingTask56549.repositories.DataSnapshotRepository;
import com.luxoft.codingTask56549.exceptions.InvalidCsvFormatException;
import com.luxoft.codingTask56549.models.DataSnapshot;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class DataSnapshotService {
    private final Path uploadedFiles;
    private final DataSnapshotRepository dataSnapshotRepository;

    public DataSnapshotService(
        @Value("${dataSnapshot.uploadedFiles.path:#{systemProperties['java.io.tmpdir']}}") Path uploadedFiles,
        @Autowired DataSnapshotRepository dataSnapshotRepository
    ) {
        this.uploadedFiles = uploadedFiles;
        this.dataSnapshotRepository = dataSnapshotRepository;
    }

    public Path saveOnDisk(InputStream inputStream) throws IOException {
        Path path = uploadedFiles.resolve(UUID.randomUUID().toString());
        Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        return path;
    }

    @Transactional
    public void loadOnDb(Path path) throws IOException, InvalidCsvFormatException {
        int count = 0;
        int batchSize = 10;
        var dataSnapshots = new ArrayList<DataSnapshot>();
        try (var reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (++count == 1) {
                    continue;
                }

                var dataSnapshot = fromCsv(line, count);
                dataSnapshots.add(dataSnapshot);

                if(count % batchSize == 0) {
                    dataSnapshotRepository.saveAll(dataSnapshots);
                    dataSnapshots.clear();
                }
            }

            if (!dataSnapshots.isEmpty()) {
                dataSnapshotRepository.saveAll(dataSnapshots);
            }
        }
    }

    public DataSnapshot get(String primaryKey) {
        var proxy = dataSnapshotRepository.getById(primaryKey);
        return Hibernate.unproxy(proxy, DataSnapshot.class);
    }

    public void delete(String primaryKey) {
        dataSnapshotRepository.deleteById(primaryKey);
    }

    public static DataSnapshot fromCsv(String csv, int lineNumber) throws InvalidCsvFormatException {
        try {
            var fields = getCsvFields(csv);

            DataSnapshot dataSnapshot = new DataSnapshot();
            dataSnapshot.setPrimaryKey(fields[0]);
            dataSnapshot.setName(fields[1]);
            dataSnapshot.setDescription(fields[2]);

            Instant instant = Instant.ofEpochMilli(Long.parseLong(fields[3]));
            dataSnapshot.setUpdatedTimestamp(ZonedDateTime.ofInstant(instant, ZoneId.of("UTC")));
            return dataSnapshot;
        } catch (Exception e) {
            throw new InvalidCsvFormatException(
                String.format("Could not parse a DataSnapshot from csv line %d", lineNumber),
                e
            );
        }
    }

    private static String[] getCsvFields(String csv) throws Exception {
        var fields = csv.split(",");

        if(fields.length != 4) {
            throw new Exception("Line does not have the required number of fields");
        }

        if(fields[0].length() > 255) {
            throw new Exception("Field 1 is too big");
        }

        if(fields[1].length() > 255) {
            throw new Exception("Field 2 is too big");
        }

        if(fields[2].length() > 255) {
            throw new Exception("Field 3 is too big");
        }

        if(fields[3].length() != 13) {
            throw new Exception("Not a valid timestamp");
        }

        return fields;
    }
}
