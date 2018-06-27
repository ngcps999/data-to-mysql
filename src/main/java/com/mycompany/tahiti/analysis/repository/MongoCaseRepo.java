package com.mycompany.tahiti.analysis.repository;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import static com.mongodb.client.model.Filters.in;

@Repository
public class MongoCaseRepo {

    private final MongoCollection<Document> collection;

    public MongoCaseRepo(
            MongoClient mongoClient,
            @Value("${miami.mongocase.database}") String database,
            @Value("${miami.mongocase.collection.case}") String collectionName
    ) {
        collection = mongoClient.getDatabase(database).getCollection(collectionName);
    }

    public Iterable<String> getCaseList(Iterable<String> ids) {
        return collection.find(in("_id", ids)).map(Document::toJson);
    }

    public Iterable<String> getCaseList() {
        return collection.find().map(Document::toJson);
    }

    public String getCase(String caseId)
    {
        Iterable<String> cases = collection.find(in("AJBH", caseId)).map(Document::toJson);
        if(cases.iterator().hasNext())
            return cases.iterator().next();
        else
            return "";
    }
}
