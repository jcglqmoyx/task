package com.study.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.task.entity.Record;

import java.util.List;

public interface RecordService extends IService<Record> {
    List<Record> getUserRecordsToday(Integer userId);
    List<Record> getUserRecordsYesterday(Integer userId);
    List<Record> getUserRecordsTheDayBeforeYesterday(Integer userId);
}
