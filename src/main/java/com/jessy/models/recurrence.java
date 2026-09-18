package com.jessy.models;
import java.time.LocalDate;

public class recurrence {
    private String type;
    private int interval;
    private LocalDate endDate;

    public recurrence(String type, int interval, LocalDate endDate) {
        this.type = type;
        this.interval = interval;
        this.endDate = endDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
