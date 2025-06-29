package com.exalt.socialcommerce.${SERVICE}.service;

import com.exalt.socialcommerce.${SERVICE}.entity.${SERVICE_CLASS};
import com.exalt.socialcommerce.${SERVICE}.repository.${SERVICE_CLASS}Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ${SERVICE_CLASS}ServiceTest {

    @Mock
    private ${SERVICE_CLASS}Repository ${SERVICE_VAR}Repository;

    @InjectMocks
    private ${SERVICE_CLASS}ServiceImpl ${SERVICE_VAR}Service;

    private ${SERVICE_CLASS} test${SERVICE_CLASS};

    @BeforeEach
    void setUp() {
        test${SERVICE_CLASS} = new ${SERVICE_CLASS}();
        test${SERVICE_CLASS}.setId(1L);
    }

    @Test
    void testFindAll() {
        List<${SERVICE_CLASS}> expected = Arrays.asList(test${SERVICE_CLASS});
        when(${SERVICE_VAR}Repository.findAll()).thenReturn(expected);

        List<${SERVICE_CLASS}> result = ${SERVICE_VAR}Service.findAll();

        assertEquals(expected.size(), result.size());
        verify(${SERVICE_VAR}Repository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        when(${SERVICE_VAR}Repository.findById(anyLong())).thenReturn(Optional.of(test${SERVICE_CLASS}));

        ${SERVICE_CLASS} result = ${SERVICE_VAR}Service.findById(1L);

        assertNotNull(result);
        assertEquals(test${SERVICE_CLASS}.getId(), result.getId());
        verify(${SERVICE_VAR}Repository, times(1)).findById(1L);
    }

    @Test
    void testSave() {
        when(${SERVICE_VAR}Repository.save(any(${SERVICE_CLASS}.class))).thenReturn(test${SERVICE_CLASS});

        ${SERVICE_CLASS} result = ${SERVICE_VAR}Service.save(test${SERVICE_CLASS});

        assertNotNull(result);
        assertEquals(test${SERVICE_CLASS}.getId(), result.getId());
        verify(${SERVICE_VAR}Repository, times(1)).save(test${SERVICE_CLASS});
    }

    @Test
    void testDeleteById() {
        doNothing().when(${SERVICE_VAR}Repository).deleteById(anyLong());

        ${SERVICE_VAR}Service.deleteById(1L);

        verify(${SERVICE_VAR}Repository, times(1)).deleteById(1L);
    }

    @Test
    void testUpdate() {
        when(${SERVICE_VAR}Repository.existsById(anyLong())).thenReturn(true);
        when(${SERVICE_VAR}Repository.save(any(${SERVICE_CLASS}.class))).thenReturn(test${SERVICE_CLASS});

        ${SERVICE_CLASS} result = ${SERVICE_VAR}Service.update(1L, test${SERVICE_CLASS});

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(${SERVICE_VAR}Repository, times(1)).save(test${SERVICE_CLASS});
    }
}
