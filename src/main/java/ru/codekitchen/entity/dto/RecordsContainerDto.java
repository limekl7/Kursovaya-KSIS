    package ru.codekitchen.entity.dto;

    import ru.codekitchen.entity.Record;

    import java.util.List;

    public class RecordsContainerDto {
        private List<Record> records;

        public RecordsContainerDto(List<Record> records) {
            this.records = records;
        }

        public List<Record> getRecords() {
            return records;
        }

        public void setRecords(List<Record> records) {
            this.records = records;
        }
    }