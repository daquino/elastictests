package com.refactify.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.refactify.model.Person;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;

import java.io.IOException;
import java.util.Map;

public class PeopleRepository {
    private final Client client;
    private final ObjectMapper mapper;
    private final String INDEX = "people";
    private final String TYPE = "people";

    public PeopleRepository(final Client client) {
        this.client = client;
        this.mapper = new ObjectMapper();
    }

    public Person find(final String id) throws IOException {
        GetResponse response = client.prepareGet(INDEX, TYPE, id)
                .get();
        return mapper.readValue(response.getSourceAsString(), Person.class);
    }

}
