package com.study.task.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.task.entity.Record;
import com.study.task.mapper.RecordMapper;
import com.study.task.schedule.ScheduledTask;
import com.study.task.service.RecordService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record> implements RecordService {
    @Override
    public List<Record> getUserRecordsToday(Integer userId) {
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("time", new Date(ScheduledTask.startOfToday)).eq("user_id", userId);
        return list(queryWrapper);
    }

    @Override
    public List<Record> getUserRecordsYesterday(Integer userId) {
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("time", new Date(ScheduledTask.startOfYesterday))
                .lt("time", new Date(ScheduledTask.startOfToday))
                .eq("user_id", userId);
        return list(queryWrapper);
    }

    @Override
    public List<Record> getUserRecordsTheDayBeforeYesterday(Integer userId) {
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("time", new Date(ScheduledTask.startOfTheDayBeforeYesterday))
                .lt("time", new Date(ScheduledTask.startOfYesterday))
                .eq("user_id", userId);
        return list(queryWrapper);
    }
}
