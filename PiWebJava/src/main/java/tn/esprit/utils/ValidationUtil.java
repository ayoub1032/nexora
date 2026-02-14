package tn.esprit.utils;

import java.util.function.UnaryOperator;

import javafx.scene.control.TextFormatter;

public class ValidationUtil {

    // digits only (for IDs)
    public static TextFormatter<String> numericLongFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            return newText.matches("\\d*") ? change : null;
        };
        return new TextFormatter<>(filter);
    }

    // positive decimal formatter (for values, prices)
    public static TextFormatter<String> positiveDecimalFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String t = change.getControlNewText();
            if (t.isEmpty()) return change;
            return t.matches("\\d{0,10}([\\.]\\d{0,2})?") ? change : null;
        };
        return new TextFormatter<>(filter);
    }

    // positive integer formatter (for quantity)
    public static TextFormatter<String> positiveIntegerFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            return newText.matches("\\d*") ? change : null;
        };
        return new TextFormatter<>(filter);
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
