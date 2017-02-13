package com.refactify.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Person {
    private String name;
    private Integer age;
    private List<String> favoriteGames;

    @JsonProperty(value = "name")
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @JsonProperty(value = "age")
    public Integer getAge() {
        return age;
    }

    public void setAge(final Integer age) {
        this.age = age;
    }

    @JsonProperty(value = "favorite_games")
    public List<String> getFavoriteGames() {
        return favoriteGames;
    }

    public void setFavoriteGames(final List<String> favoriteGames) {
        this.favoriteGames = favoriteGames;
    }
}
