package com.mgstudio.imitationnettyclient.Broadcast;

import java.io.Serializable;
import java.util.Arrays;

public class Info implements Serializable{

    private byte[] bytes;

    private String content;

    private int function;

    private int state;

    public Info() {
    }

    public Info(int state) {
        this.state = state;
    }

    public Info(byte[] bytes, String content, int function, int state) {
        this.bytes = bytes;
        this.content = content;
        this.function = function;
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getFunction() {
        return function;
    }

    public void setFunction(int function) {
        this.function = function;
    }

    @Override
    public String toString() {
        return "Info{" +
                "bytes=" + Arrays.toString(bytes) +
                ", content='" + content + '\'' +
                ", function='" + function + '\'' +
                '}';
    }
}
