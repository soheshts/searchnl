package io.github.soheshts.searchnl.service;

import io.github.soheshts.searchnl.model.Product;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    @Value("${app.search.response.count:20}")
    private int count;

    VectorStore vectorStore;

    public SearchService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Product> findBySimilarity(final String query) {
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(count).build());

        return documents.stream()
                .map(Product::fromDocument)
                .toList();
    }
}
