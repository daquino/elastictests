package com.refactify.unit

import spock.lang.Specification

class TestUnit extends Specification {
    def "simple test"() {
        given:
        int a = 1
        int b = 2

        when:
        int c = a + b

        then:
        c == 3
    }
}
