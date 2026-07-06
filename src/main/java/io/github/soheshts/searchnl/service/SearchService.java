package io.github.soheshts.searchnl.service;

import io.github.soheshts.searchnl.model.Product;
import io.github.soheshts.searchnl.model.SearchCriteria;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    @Value("${app.search.response.count:20}")
    private int count;

    VectorStore vectorStore;

    public SearchService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Product> findBySimilarity(final String query, final SearchCriteria searchCriteria) {
        Filter.Expression expression = buildExpression(searchCriteria);
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(count).filterExpression(expression).build());

        return documents.stream()
                .map(Product::fromDocument)
                .toList();
    }

    public static Filter.Expression buildExpression(SearchCriteria criteria) {
        if (criteria == null) {
            return null;
        }

        FilterExpressionBuilder b = new FilterExpressionBuilder();
        List<Filter.Expression> expressions = new ArrayList<>();

        /*// String Equality Checks
        // String Equality Checks - Add .build() at the end
        if (criteria.getGender() != null) {
            expressions.add(b.eq("gender", criteria.getGender()).build());
        }
        if (criteria.getBaseColour() != null) {
            expressions.add(b.eq("baseColour", criteria.getBaseColour()).build());
        }
        if (criteria.getArticleType() != null) {
            expressions.add(b.eq("articleType", criteria.getArticleType()).build());
        }
        if (criteria.getSeason() != null) {
            expressions.add(b.eq("season", criteria.getSeason()).build());
        }
        if (criteria.getUsage() != null) {
            expressions.add(b.eq("usage", criteria.getUsage()).build());
        }*/

// Numeric Range Checks - Add .build() at the end
        if (criteria.getPriceMin() != null) {
            expressions.add(b.gte("price", criteria.getPriceMin()).build());
        }
        if (criteria.getPriceMax() != null) {
            expressions.add(b.lte("price", criteria.getPriceMax()).build());
        }

        // Combine all expressions using AND logic
        if (expressions.isEmpty()) {
            return null;
        }

        Filter.Expression combinedExpression = expressions.get(0);
        for (int i = 1; i < expressions.size(); i++) {
            combinedExpression = new Filter.Expression(
                    Filter.ExpressionType.AND,
                    combinedExpression,
                    expressions.get(i)
            );
        }

        return combinedExpression;
    }
}
