package com.unimi.lim.hmi.entity;

public class Timbre {

    private String id;
    private boolean checked = false;

    private String content;
    private String details;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    @Override
    public String toString() {
        return "Timbre{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", details='" + details + '\'' +
                '}';
    }
}
