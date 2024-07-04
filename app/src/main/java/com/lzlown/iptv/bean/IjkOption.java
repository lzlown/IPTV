package com.lzlown.iptv.bean;

public class IjkOption {
    int category;
    String name;
    String value;

    public IjkOption() {
    }

    public IjkOption(int category, String name, String value) {
        this.category = category;
        this.name = name;
        this.value = value;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
