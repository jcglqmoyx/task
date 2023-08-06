package com.study.task.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Report {
    private User user;
    private boolean isFinished;
    private List<Record> theDayBeforeYesterdayRecords;
    private List<Record> yesterdayRecords;
}
