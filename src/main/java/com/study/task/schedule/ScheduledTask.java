package com.study.task.schedule;

import com.study.task.entity.Record;
import com.study.task.entity.Report;
import com.study.task.entity.User;
import com.study.task.service.RecordService;
import com.study.task.service.UserService;
import com.study.task.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Component
public class ScheduledTask {
    private UserService userService;
    private RecordService recordService;
    public static long startOfToday;
    public static long startOfYesterday;
    public static long startOfTheDayBeforeYesterday;

    @Autowired
    private void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private void setRecordService(RecordService recordService) {
        this.recordService = recordService;
    }

    private List<Report> dbQuery() {
        List<User> users = userService.list();
        List<Report> reports = new LinkedList<>();
        for (User user : users) {
            boolean isFinished = false;
            List<Record> theDayBeforeYesterdayRecords = recordService.getUserRecordsTheDayBeforeYesterday(user.getId());
            if (theDayBeforeYesterdayRecords.size() >= 3) {
                isFinished = true;
            }
            List<Record> yesterdayRecords = recordService.getUserRecordsYesterday(user.getId());
            if (yesterdayRecords.size() >= 3) {
                isFinished = true;
            }
            reports.add(new Report(user, isFinished, theDayBeforeYesterdayRecords, yesterdayRecords));
        }
        return reports;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @EventListener(ApplicationReadyEvent.class)
    private void updateTime() {
        long timestamp = new Date().getTime();
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        timestamp -= hour * 60 * 60 * 1000 + minute * 60 * 1000 + second * 1000;
        startOfToday = timestamp;
        startOfYesterday = timestamp - 24 * 60 * 60 * 1000;
        startOfTheDayBeforeYesterday = timestamp - 2 * 24 * 60 * 60 * 1000;
    }

    @Scheduled(cron = "0 1 0 * * *")
    private void generateReport() throws IOException {
        List<Report> reports = dbQuery();

        String userHome = System.getProperty("user.home");
        String reportDirectory = userHome + "/report/";
        String reportPath = reportDirectory + DateUtils.getNowDate() + ".md";
        File reportFile = new File(reportPath);
        if (reportFile.exists() && !reportFile.delete()) {
            return;
        }

        BufferedWriter out = new BufferedWriter(new FileWriter(reportPath));
        out.write(String.format("# %s\n\n", DateUtils.getNowDate()));
        out.write("## 前天打卡情况\n\n");
        for (Report report : reports) {
            out.write(report.getUser().getUsername());
            out.write(": ");
            for (int i = 0; i < report.getTheDayBeforeYesterdayRecords().size(); i++) {
                out.write(String.format("[链接%d](%s)\t", i + 1, report.getTheDayBeforeYesterdayRecords().get(i).getUrl()));
            }
            out.write("<br />\n");
        }

        out.write("## 昨天打卡情况\n\n");
        for (Report report : reports) {
            out.write(report.getUser().getUsername());
            out.write(": ");
            for (int i = 0; i < report.getYesterdayRecords().size(); i++) {
                out.write(String.format("[链接%d](%s)\t", i + 1, report.getYesterdayRecords().get(i).getUrl()));
            }
            out.write("<br />\n");
        }

        out.write("## 完成打卡的朋友\n\n");
        for (Report report : reports) {
            if (report.isFinished()) {
                out.write(report.getUser().getUsername());
                out.write("<br />\n");
            }
        }
        out.write("## 未完成打卡的朋友\n\n");
        for (Report report : reports) {
            if (!report.isFinished()) {
                out.write(report.getUser().getUsername());
                out.write("<br />\n");
            }
        }
        out.close();

        out = new BufferedWriter(new FileWriter(reportDirectory + "index.md"));
        out.write(String.format("# [%s](%s.md)\n\n", DateUtils.getNowDate(), DateUtils.getNowDate()));
        out.close();

        String[] command = {"sh", "-c", "cd " + reportDirectory + " && git add . && git commit -m \"Daily report " + DateUtils.getNowDate() + "\" && git push"};
        Runtime.getRuntime().exec(command);
    }
}
