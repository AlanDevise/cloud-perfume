package com.alandevise.sse.model;

/**
 * WebSocket 客户端下发的简单命令。
 */
public class WebSocketCommand {

    private String action;
    private Integer points;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}
