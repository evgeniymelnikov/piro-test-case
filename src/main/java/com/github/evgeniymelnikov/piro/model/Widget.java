package com.github.evgeniymelnikov.piro.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

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

    public void updateWidget(Widget widget) {
        synchronized (this) {
            this.setPositionX(widget.getPositionX());
            this.setPositionY(widget.getPositionY());
            this.setWidth(widget.getWidth());
            this.setHeight(widget.getHeight());
            this.setIndexZ(widget.getIndexZ());
            this.setLastUpdate(widget.getLastUpdate());
        }
    }

    public void checkIndexZAndIfNeedUpdateIt(Long markIndex, Long valueForSummarizeWithIndexZ) {
        if (markIndex == null) {return;}
        synchronized (this) {
            if (this.getIndexZ() != null && this.getIndexZ() >= markIndex) {
                this.setIndexZ(this.getIndexZ() + valueForSummarizeWithIndexZ);
            }
        }
    }
}
