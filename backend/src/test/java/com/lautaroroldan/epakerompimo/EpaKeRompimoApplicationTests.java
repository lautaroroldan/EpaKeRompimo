package com.lautaroroldan.epakerompimo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Levanta el contexto completo contra una H2 en memoria: valida los mapeos de
 * las entidades nuevas (roles, reportes y overrides) y el cableado de beans.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:epakerompimo-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
class EpaKeRompimoApplicationTests {

    @Test
    void contextLoads() {
    }
}
