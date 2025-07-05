package com.gogidix.socialcommerce.${SERVICE}.repository;

import com.gogidix.socialcommerce.${SERVICE}.entity.${SERVICE_CLASS};
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ${SERVICE_CLASS}RepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ${SERVICE_CLASS}Repository ${SERVICE_VAR}Repository;

    private ${SERVICE_CLASS} test${SERVICE_CLASS};

    @BeforeEach
    void setUp() {
        test${SERVICE_CLASS} = new ${SERVICE_CLASS}();
        // Set required fields
        entityManager.persistAndFlush(test${SERVICE_CLASS});
    }

    @Test
    void testFindById() {
        Optional<${SERVICE_CLASS}> found = ${SERVICE_VAR}Repository.findById(test${SERVICE_CLASS}.getId());
        
        assertTrue(found.isPresent());
        assertEquals(test${SERVICE_CLASS}.getId(), found.get().getId());
    }

    @Test
    void testFindAll() {
        List<${SERVICE_CLASS}> all = ${SERVICE_VAR}Repository.findAll();
        
        assertFalse(all.isEmpty());
        assertTrue(all.stream().anyMatch(item -> item.getId().equals(test${SERVICE_CLASS}.getId())));
    }

    @Test
    void testSave() {
        ${SERVICE_CLASS} new${SERVICE_CLASS} = new ${SERVICE_CLASS}();
        // Set required fields
        
        ${SERVICE_CLASS} saved = ${SERVICE_VAR}Repository.save(new${SERVICE_CLASS});
        
        assertNotNull(saved.getId());
    }

    @Test
    void testDelete() {
        ${SERVICE_VAR}Repository.deleteById(test${SERVICE_CLASS}.getId());
        
        Optional<${SERVICE_CLASS}> found = ${SERVICE_VAR}Repository.findById(test${SERVICE_CLASS}.getId());
        assertFalse(found.isPresent());
    }
}
