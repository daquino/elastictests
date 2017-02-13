package com.refactify.integration.repository

import com.refactify.model.Person
import com.refactify.repository.PeopleRepository
import com.refactify.utils.ElasticsearchInstance
import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Paths

class PeopleRepositoryTest extends Specification {
    @Shared
    PeopleRepository repository

    @Shared
    ElasticsearchInstance instance

    def setupSpec() {
        instance = new ElasticsearchInstance()
        instance.createIndex("people", "people", Paths.get(System.getProperty("es.mapping.path"), "people.json"))
        instance.createIndex("games", "games", Paths.get(System.getProperty("es.mapping.path"), "games.json"))
        instance.seedTestData("people", "people", Paths.get(System.getProperty("es.data.path"), "test-people.json"))
        instance.seedTestData("games", "games", Paths.get(System.getProperty("es.data.path"), "test-games.json"))
        repository = new PeopleRepository(instance.client)
    }

    def cleanupSpec() {
        instance.cleanup()
    }

    def "can fetch person by id"() {
        given:
        String id = "1"

        when:
        Person person = repository.find(id)

        then:
        person.name == "Daniel Aquino"
        person.age == 31
        person.favoriteGames.containsAll(["Rocket League", "Final Fantasy 6"])
    }


}
