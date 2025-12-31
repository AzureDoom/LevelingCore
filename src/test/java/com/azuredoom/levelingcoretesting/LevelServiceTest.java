package com.azuredoom.levelingcoretesting;

import com.azuredoom.levelingcore.Main;
import com.azuredoom.levelingcore.config.ConfigBootstrap;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelServiceTest {

    @Test
    void addXp_increasesXpAndLevel() {
        var service = ConfigBootstrap.bootstrap().service();
        var id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        var amount = 500;
        var beforeXp = service.getXp(id);
        var beforeLevel = service.getLevel(id);

        service.addXp(id, amount);
        Main.LOGGER.log(System.Logger.Level.INFO, String.format("XP: %d", service.getXp(id)));
        Main.LOGGER.log(System.Logger.Level.INFO, String.format("Level: %d", service.getLevel(id)));

        assertEquals(beforeXp + amount, service.getXp(id));
        assertTrue(service.getLevel(id) >= beforeLevel);
    }
}
