package com.sistema_de_inventarios_v02;

import com.sistema_de_inventarios_v02.Config.TestSecurityConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
@Import({
        TestSecurityConfig.class
})
public class TestE2EConfiguration {

}