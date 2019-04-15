package com.github.evgeniymelnikov.piro.model.filter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.evgeniymelnikov.piro.model.Widget;
import com.github.evgeniymelnikov.piro.model.metamodel.Widget_;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Getter
@Setter
public class WidgetFilter extends AbstractFilter<Widget> {

    private Point leftTopPoint;
    private Point rightBottomPoint;

    @JsonCreator
    public WidgetFilter(@JsonProperty("page") Long page, @JsonProperty("limit") Long limit,
                        @JsonProperty("sort") String sort, @JsonProperty("direction") String direction,
                        @JsonProperty("leftTopPoint") Point leftTopPoint, @JsonProperty("rightBottomPoint") Point rightBottomPoint) {
        super(page, limit, sort, direction);
        this.leftTopPoint = leftTopPoint;
        this.rightBottomPoint = rightBottomPoint;
    }

    @Override
    protected String getDefaultSort() {
        return Widget_.INDEX_Z;
    }

    @Override
    protected String getIdFieldName() {
        return Widget_.ID;
    }

    @Override
    protected String[] getFieldsFromMetamodel() {
        return Widget_.getFieldNames();
    }

    @Override
    public Predicate<Widget> toPredicate() {

        List<Predicate<Widget>> predicates = new ArrayList<>();

        if (leftTopPoint != null && leftTopPoint.getPositionX() != null && leftTopPoint.getPositionY() != null) {
            predicates.add(widget -> {
                if ((widget.getPositionX() + widget.getWidth()) <= leftTopPoint.getPositionX()) {
                    return false;
                }

                if ((widget.getPositionY() + widget.getHeight()) <= leftTopPoint.getPositionY()) {
                    return false;
                }

                return true;
            });
        }

        if (rightBottomPoint != null && rightBottomPoint.getPositionX() != null && rightBottomPoint.getPositionY() != null) {
            predicates.add(widget -> {
                if (widget.getPositionX() >= rightBottomPoint.getPositionX()) {
                    return false;
                }

                if (widget.getPositionY() >= rightBottomPoint.getPositionY()) {
                    return false;
                }

                return true;
            });
        }

        if (predicates.size() == 1) {
            return predicates.get(0);
        } else if (predicates.size() > 1) {
            Predicate<Widget> predicate = predicates.get(0);
            for (int i = 1; i < predicates.size(); i++) {
                predicate = predicate.and(predicates.get(i));
            }

            return predicate;
        } else {
            return widget -> true;
        }
    }
}
