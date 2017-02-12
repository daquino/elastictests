package com.refactify.integration.people

import com.refactify.utils.ElasticsearchInstance
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.Client
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Paths

class PeopleITCase extends Specification {
    @Shared
    ElasticsearchInstance instance

    def setupSpec() {
        instance = new ElasticsearchInstance()
        instance.createIndex("people", "people", Paths.get(System.getProperty("es.mapping.path"), "people.json"))
        instance.createIndex("games", "games", Paths.get(System.getProperty("es.mapping.path"), "games.json"))
        instance.seedTestData("people", "people", Paths.get(System.getProperty("es.data.path"), "test-people.json"))
        instance.seedTestData("games", "games", Paths.get(System.getProperty("es.data.path"), "test-games.json"))
    }

    def cleanupSpec() {
        instance.cleanup()
    }

    def "can fetch from people"() {
        given:
        Client client = instance.getClient()

        when:
        SearchResponse response = client.prepareSearch("people")
                .setTypes("people")
                .execute()
                .actionGet()

        then:
        println response
        response.getHits().totalHits() == 1
    }
}
