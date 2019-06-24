package com.github.evgeniymelnikov.piro.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Widget {

    private String id;
    private Long positionX;
    private Long positionY;
    private Long width;
    private Long height;
    private Long indexZ;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDate lastUpdate;

    public static Widget copy(Widget that) {
        return new Widget(that.getId(), that.getPositionX(), that.getPositionY(),
                that.getWidth(), that.getHeight(), that.getIndexZ(), that.getLastUpdate());
    }

    public synchronized void updateWidget(Widget widget) {
            this.setPositionX(widget.getPositionX());
            this.setPositionY(widget.getPositionY());
            this.setWidth(widget.getWidth());
            this.setHeight(widget.getHeight());
            this.setIndexZ(widget.getIndexZ());
            this.setLastUpdate(widget.getLastUpdate());
    }

    public synchronized void checkIndexZAndIfNeedIncreaseIt(Long markIndex, Long valueForSummarizeWithIndexZ) {
        if (markIndex == null) {return;}
        if (this.getIndexZ() != null && this.getIndexZ() >= markIndex) {
            this.setIndexZ(this.getIndexZ() + valueForSummarizeWithIndexZ);
        }
    }

    public synchronized String getId() {
        return id;
    }

    public synchronized void setId(String id) {
        this.id = id;
    }

    public synchronized Long getPositionX() {
        return positionX;
    }

    public synchronized void setPositionX(Long positionX) {
        this.positionX = positionX;
    }

    public synchronized Long getPositionY() {
        return positionY;
    }

    public synchronized void setPositionY(Long positionY) {
        this.positionY = positionY;
    }

    public synchronized Long getWidth() {
        return width;
    }

    public synchronized void setWidth(Long width) {
        this.width = width;
    }

    public synchronized Long getHeight() {
        return height;
    }

    public synchronized void setHeight(Long height) {
        this.height = height;
    }

    public synchronized Long getIndexZ() {
        return indexZ;
    }

    public synchronized void setIndexZ(Long indexZ) {
        this.indexZ = indexZ;
    }

    public synchronized LocalDate getLastUpdate() {
        return lastUpdate;
    }

    public synchronized void setLastUpdate(LocalDate lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
