package com.study.task.controller;

import com.study.task.util.MsgUtils;
import com.study.task.util.XMLUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
public class IndexController {
    private MsgUtils msgUtils;

    @Autowired
    private void setMsgUtils(MsgUtils msgUtils) {
        this.msgUtils = msgUtils;
    }

    @RequestMapping(value = "/", method = {RequestMethod.GET, RequestMethod.POST})
    public String index(HttpServletRequest request, @RequestParam Map<String, String> data) {
        if (Objects.equals(request.getMethod(), "GET")) {
            return this.msgUtils.verify(data);
        } else if (Objects.equals(request.getMethod(), "POST")) {
            return this.msgUtils.replyToMessage(XMLUtils.parseXML(request));
        } else {
            return null;
        }
    }
}
