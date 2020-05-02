package com.lemonoflime.rpbackground.persistence;

import java.util.Objects;
import java.util.UUID;

public class SimpleObject {
    private String id = UUID.randomUUID().toString();
    private String value;

    public SimpleObject() {
    }

    public SimpleObject(String value) {
        this.value = value;
    }

    public SimpleObject(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleObject)) return false;
        SimpleObject that = (SimpleObject) o;
        return getId().equals(that.getId()) &&
            getValue().equals(that.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getValue());
    }
}
