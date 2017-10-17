package com.faforever.gw;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GwTestClientApplicationTests {

    @Test
    public void contextLoads() throws IOException {
        ObjectMapper om = new ObjectMapper();
    }

}
