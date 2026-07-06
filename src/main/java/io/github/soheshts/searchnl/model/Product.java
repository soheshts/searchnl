package io.github.soheshts.searchnl.model;

import org.springframework.ai.document.Document;

import java.util.Map;

public record Product(String id, String name, String imageLink, Double price, String gender, String masterCategory,
                      String subCategory, String articleType, String baseColour, String season, Integer year,
                      String usage) {

    public static Product fromDocument(Document document) {
        Map<String, Object> meta = document.getMetadata();

        return new Product(asString(meta.get("_id")), asString(meta.get("productDisplayName")), asString(meta.get("imageLink")), asDouble(meta.get("price")), asString(meta.get("gender")), asString(meta.get("masterCategory")), asString(meta.get("subCategory")), asString(meta.get("articleType")), asString(meta.get("baseColour")), asString(meta.get("season")), asInteger(meta.get("year")), asString(meta.get("usage")));
    }

    private static String asString(Object val) {
        return val == null ? null : val.toString();
    }

    private static Double asDouble(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(val.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer asInteger(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}