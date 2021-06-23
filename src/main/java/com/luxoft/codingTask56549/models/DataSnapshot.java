package com.luxoft.codingTask56549.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.ZonedDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataSnapshot {
    @Id
    private String primaryKey;
    private String name;
    private String description;
    private ZonedDateTime updatedTimestamp;
}
