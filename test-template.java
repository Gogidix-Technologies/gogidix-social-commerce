package com.gogidix.socialcommerce.PACKAGE_NAME;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = APPLICATION_CLASS.class)
@ActiveProfiles("test")
class APPLICATION_CLASSTest {

    @Test
    void contextLoads() {
        // Test that the Spring context loads successfully
    }
}
EOF < /dev/null
