package tn.esprit.utils;

import javafx.scene.control.TextFormatter;
import java.util.function.UnaryOperator;

public class ValidationUtil {

    // digits only (for IDs)
    public static TextFormatter<String> numericLongFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            return newText.matches("\\d*") ? change : null;
        };
        return new TextFormatter<>(filter);
    }

    // DECIMAL(18,8) like "123", "123.45"
    public static TextFormatter<String> positiveDecimalFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String t = change.getControlNewText();
            if (t.isEmpty()) return change;
            return t.matches("\\d{0,18}([\\.]\\d{0,8})?") ? change : null;
        };
        return new TextFormatter<>(filter);
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}