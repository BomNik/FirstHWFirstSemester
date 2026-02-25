package com.mipt.nikitabumagin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.mipt.nikitabumagin.service.AppInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TodoListManagerApplicationTests {

    @Autowired
    private AppInfoService appInfoService;

    @Test
    void contextLoads() {
        assertNotNull(appInfoService);
    }

    @Test
    void customPropertiesAreInjected() {
        assertEquals("Todo List Manager", appInfoService.getAppName());
        assertEquals("1.0.0", appInfoService.getAppVersion());
    }
}
