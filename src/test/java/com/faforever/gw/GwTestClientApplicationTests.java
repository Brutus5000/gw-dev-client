package com.faforever.gw;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class GwTestClientApplicationTests {

    @Test
    public void contextLoads() throws IOException {
        ObjectMapper om = new ObjectMapper();
    }

}
