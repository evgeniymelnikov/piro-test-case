package com.github.evgeniymelnikov.piro.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.concurrent.locks.ReentrantLock;

@Data
@AllArgsConstructor
public class Widget {

    private String id;
    private volatile Long positionX;
    private volatile Long positionY;
    private volatile Long width;
    private volatile Long height;
    private volatile Long indexZ;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private volatile LocalDate lastUpdate;

    @JsonIgnore
    private final ReentrantLock lock = new ReentrantLock();

    public void updateWidget(Widget widget) {
        this.lock.lock();
        this.setPositionX(widget.getPositionX());
        this.setPositionY(widget.getPositionY());
        this.setWidth(widget.getWidth());
        this.setHeight(widget.getHeight());
        this.setIndexZ(widget.getIndexZ());
        this.setLastUpdate(widget.getLastUpdate());
        this.lock.unlock();
    }
}
