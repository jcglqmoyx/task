package com.study.task.util;

import com.study.task.entity.Record;
import com.study.task.entity.User;
import com.study.task.service.RecordService;
import com.study.task.service.UserService;
import io.micrometer.common.util.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class MsgUtils {

    @Value("${wechat.token}")
    public String token;
    private UserService userService;
    private RecordService recordService;
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    private void setRecordService(RecordService recordService) {
        this.recordService = recordService;
    }

    @Autowired
    private void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String verify(Map<String, String> data) {
        String signature = data.get("signature");
        String echoStr = data.get("echostr");
        String timestamp = data.get("timestamp");
        String nonce = data.get("nonce");
        if (ObjectUtils.isEmpty(signature) || ObjectUtils.isEmpty(echoStr) || ObjectUtils.isEmpty(timestamp) || ObjectUtils.isEmpty(nonce)) {
            return null;
        }
        String[] array = {token, timestamp, nonce};
        Arrays.sort(array);
        String encoded = DigestUtils.sha1Hex(String.join("", array));
        if (encoded.equals(signature)) {
            return echoStr;
        } else {
            return null;
        }
    }

    @Transactional
    public String replyToMessage(Map<String, String> map) {
        if (Objects.equals(map.get("MsgType"), "text")) {
            return replyToTextMessage(map, true);
        } else if (Objects.equals(map.get("MsgType"), "image")) {
            return replyToImageMessage(map);
        } else if (Objects.equals(map.get("MsgType"), "voice")) {
            return replyToVoiceMessage(map);
        } else if (Objects.equals(map.get("MsgType"), "video")) {
            return replyToVideoMessage(map);
        } else {
            return replyToTextMessage(map, false);
        }
    }

    private String replyToTextMessage(Map<String, String> map, boolean messageTypeSupported) {
        String reply;
        if (!messageTypeSupported) {
            reply = "不支持此类型的消息";
        } else if (map.get("Content").startsWith("注册 ")) {
            String username = map.get("Content").substring(3);
            if (StringUtils.isEmpty(username)) {
                reply = "用户名不能为空";
            } else if (username.length() > 32) {
                reply = "用户名长度不能超过32";
            } else if (userService.getByUsername(username) != null) {
                reply = "用户名已存在，请重新输入";
            } else if (userService.getByWechatId(map.get("FromUserName")) != null) {
                reply = "用户已注册, 无需重复注册";
            } else {
                User user = new User();
                user.setUsername(username);
                user.setWechatId(map.get("FromUserName"));
                user.setJoinTime(new Date());
                user.setAdmin(false);
                user.setHasQuited(true);
                userService.save(user);
                reply = "注册成功, 请联系管理员(微信号2992860292)进行激活";
            }
        } else if (map.get("Content").startsWith("admin ")) {
            String username = map.get("FromUserName");
            User user = userService.getByWechatId(username);
            if (!user.isAdmin()) {
                reply = "你不是管理员";
            } else {
                String command = map.get("Content").substring(6);
                if (command.startsWith("activate ")) {
                    String wechatId = command.substring(9);
                    User userToActivate = userService.getByWechatId(wechatId);
                    if (userToActivate == null) {
                        reply = "用户不存在";
                    } else if (!userToActivate.isHasQuited()) {
                        reply = "用户已激活";
                    } else {
                        userToActivate.setHasQuited(false);
                        userService.updateById(user);
                        reply = "用户激活成功";
                    }
                } else if (command.startsWith("delete ")) {
                    String wechatId = command.substring(7);
                    User userToDelete = userService.getByWechatId(wechatId);
                    if (userToDelete == null) {
                        reply = "用户不存在";
                    } else {
                        userService.removeById(userToDelete.getId());
                        reply = "用户删除成功";
                    }
                } else if (command.startsWith("list")) {
                    List<User> users = userService.list();
                    StringBuilder builder = new StringBuilder();
                    for (User u : users) {
                        builder.append(u.getUsername()).append("\t\t").append(u.getWechatId()).append("\t\t").append(u.isAdmin() ? "管理员" : "普通用户").append("\t\t").append(u.isHasQuited() ? "已注销" : "已激活").append("\n");
                    }
                    reply = builder.toString();
                } else {
                    reply = "尚未开发此功能";
                }
            }
        } else {
            if (isInvalidUser(map.get("FromUserName"))) {
                reply = "用户尚未注册或尚未激活";
            } else {
                reply = "管理员尚未开发此功能";
            }
        }
        return messageToXML(map, reply);
    }

    private String replyToImageMessage(Map<String, String> map) {
        String wechatId = map.get("FromUserName"), reply;
        if (isInvalidUser(wechatId)) {
            reply = "用户没有注册或者用户尚未激活，请联系管理员进行注册";
        } else {
            long msgId = Long.parseLong(map.get("MsgId"));
            String key = wechatId + '_' + msgId;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                reply = "";
            } else {
                redisTemplate.opsForValue().set(wechatId + msgId, true);
                redisTemplate.expire(key, 60, TimeUnit.SECONDS);
                Date time = new Date(Long.parseLong(map.get("CreateTime")) * 1000);
                User user = userService.getByWechatId(wechatId);
                String url = map.get("PicUrl");
                Record record = new Record();
                record.setUserId(user.getId());
                record.setUrl(url);
                record.setTime(time);
                recordService.save(record);

                List<Record> recordsToday = recordService.getUserRecordsToday(user.getId());
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i <= recordsToday.size(); i++) {
                    builder.append("<a href=\"").append(recordsToday.get(i - 1).getUrl()).append("\">").append("图片").append(i).append("</a>").append("\t\t");
                }
                reply = "图片上传成功, 你今天已经上传了" + (recordsToday.size()) + "张图片。 链接为: " + builder;
            }
        }
        return messageToXML(map, reply);
    }

    private String replyToVoiceMessage(Map<String, String> map) {
        return messageToXML(map, "暂时不支持语音消息");
    }

    private String replyToVideoMessage(Map<String, String> map) {
        return messageToXML(map, "暂时不支持视频消息");
    }

    private String messageToXML(Map<String, String> map, String reply) {
        return "<xml>\n" + "<ToUserName><![CDATA[" + map.get("FromUserName") + "]]></ToUserName>\n" + "<FromUserName><![CDATA[" + map.get("ToUserName") + "]]></FromUserName>\n" + "<CreateTime>" + map.get("CreateTime") + "</CreateTime>\n" + "<MsgType><![CDATA[text]]></MsgType>\n" + "<Content><![CDATA[" + reply + "]]></Content>\n" + "</xml>";
    }

    private boolean isInvalidUser(String wechatId) {
        User user = userService.getByWechatId(wechatId);
        return user == null || user.isHasQuited();
    }
}
