package com.refactify

import com.refactify.utils.ElasticsearchInstance
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.Client
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Paths

class GamesITCase extends Specification {
    @Shared
    ElasticsearchInstance instance

    def setupSpec() {
        instance = new ElasticsearchInstance()
        instance.seedTestData("people", "people", Paths.get(System.getProperty("es.data.path"), "test-people.json"))
        instance.seedTestData("games", "games", Paths.get(System.getProperty("es.data.path"), "test-games.json"))
    }

    def cleanupSpec() {
        instance.cleanup()
    }

    def "can fetch from games"() {
        given:
        Client client = instance.getClient()

        when:
        SearchResponse response = client.prepareSearch("games")
                .setTypes("games")
                .execute()
                .actionGet()

        then:
        println response
        response.getHits().totalHits() == 3
    }
}
