package com.rj.wisp.bean;

/**
 * 作者：志文 on 2015/12/17 0017 15:11
 * 邮箱：594485991@qq.com
 * EventBus 消息
 */
public class MessageEvent {
    private static final String TAG = MessageEvent.class.getName();
    private int eventType;
    private Object eventContent;

    public static final int RESOURCE_GET_FAIL = 1;
    public static final int RESOURCE_DOWN_START = 2;
    public static final int RESOURCE_DOWN_SUCC = 3;
    public static final int RESOURCE_DOWN_FAIL = 4;

    public MessageEvent(int eventType, Object eventContent) {
        setEventType(eventType);
        setEventContent(eventContent);
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public Object getEventContent() {
        return eventContent;
    }

    public void setEventContent(Object eventContent) {
        this.eventContent = eventContent;
    }
}
