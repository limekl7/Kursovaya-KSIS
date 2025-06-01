package ru.codekitchen.dao;

import org.springframework.stereotype.Repository;
import ru.codekitchen.entity.Record;

import java.util.List;

@Repository
public class RecordDao {
    public List<Record> findAllRecords() {
        return List.of();
    }
}