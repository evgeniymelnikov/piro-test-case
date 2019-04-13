package com.github.evgeniymelnikov.piro.model.validator;

import com.github.evgeniymelnikov.piro.model.Widget;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class WidgetValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return Widget.class.equals(aClass);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Widget widget = (Widget) o;
        if (widget.getId() == null) {
            errors.rejectValue("id", "id должен быть определён");
        }

        if (widget.getPositionX() == null) {
            errors.rejectValue("positionX", "координата по оси X должна быть определена");
        }

        if (widget.getPositionY() == null) {
            errors.rejectValue("positionY", "координата по оси Y должна быть определена");
        }

        if (widget.getHeight() == null) {
            errors.rejectValue("height", "ширина должна быть определена");
        }

        if (widget.getIndexZ() == null) {
            errors.rejectValue("indexZ", "индекс Z должен быть определён");
        }

        if (widget.getLastUpdate() == null) {
            errors.rejectValue("lastUpdate", "дата модификации должна быть определена");
        }
    }

}
