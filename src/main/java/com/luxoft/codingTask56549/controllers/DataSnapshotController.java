package com.luxoft.codingTask56549.controllers;

import com.luxoft.codingTask56549.exceptions.InvalidCsvFormatException;
import com.luxoft.codingTask56549.dtos.ErrorDto;
import com.luxoft.codingTask56549.models.DataSnapshot;
import com.luxoft.codingTask56549.services.DataSnapshotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("data-snapshots")
public class DataSnapshotController {
    private static final Logger LOG = LoggerFactory.getLogger(DataSnapshotController.class);

    private final DataSnapshotService dataSnapshotService;

    @Autowired
    public DataSnapshotController(DataSnapshotService dataSnapshotService) {
        this.dataSnapshotService = dataSnapshotService;
    }

    @PostMapping("upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        Path path = dataSnapshotService.saveOnDisk(file.getInputStream());
        try {
            dataSnapshotService.loadOnDb(path);
        } catch (InvalidCsvFormatException e) {
            LOG.error(e.getMessage(), e);
            var error = new ErrorDto(e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{primaryKey}")
    public ResponseEntity<DataSnapshot> get(@PathVariable("primaryKey") String primaryKey) {
        try {
            var data = dataSnapshotService.get(primaryKey);
            return ResponseEntity.ok(data);
        } catch (EntityNotFoundException e) {
            LOG.error(e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{primaryKey}")
    public ResponseEntity<Void> delete(@PathVariable("primaryKey") String primaryKey) {
        try {
            dataSnapshotService.delete(primaryKey);
        } catch (EmptyResultDataAccessException e) {
            LOG.error(e.getMessage(), e);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }
}
