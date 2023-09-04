package com.study.task.util;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class XMLUtils {
    private static final SAXReader saxReader = new SAXReader();

    public static Map<String, String> parseXML(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        try {
            ServletInputStream inputStream = request.getInputStream();
            Document document = saxReader.read(inputStream);
            Element root = document.getRootElement();
            List<Element> childrenElement = root.elements();
            for (Element element : childrenElement) {
                map.put(element.getName(), element.getStringValue());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return map;
    }
}
