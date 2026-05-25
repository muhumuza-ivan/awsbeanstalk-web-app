package com.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.*;

@Service
public class DynamoService {

    private final DynamoDbClient client;
    private final String tableName;
    private final boolean enabled;

    public DynamoService(
            @Value("${DYNAMO_REGION:us-west-1}") String region,
            @Value("${DYNAMO_TABLE:AppVisits}") String tableName,
            @Value("${DYNAMO_ENABLED:false}") boolean enabled) {

        this.tableName = tableName;
        this.enabled = enabled;
        this.client = DynamoDbClient.builder()
                .region(Region.of(region))
                .build();
    }

    public String recordVisit(String user) {
        if (!enabled) return "dynamo-disabled";

        String id = UUID.randomUUID().toString();
        client.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(Map.of(
                        "id",        AttributeValue.fromS(id),
                        "user",      AttributeValue.fromS(user),
                        "timestamp", AttributeValue.fromS(Instant.now().toString())
                ))
                .build());
        return id;
    }

    public List<Map<String, String>> listVisits() {
        if (!enabled) return List.of(Map.of("note", "DynamoDB integration disabled"));

        ScanResponse resp = client.scan(ScanRequest.builder()
                .tableName(tableName)
                .limit(20)
                .build());

        return resp.items().stream().map(item -> Map.of(
                "id",        item.getOrDefault("id",        AttributeValue.fromS("")).s(),
                "user",      item.getOrDefault("user",      AttributeValue.fromS("")).s(),
                "timestamp", item.getOrDefault("timestamp", AttributeValue.fromS("")).s()
        )).toList();
    }
}