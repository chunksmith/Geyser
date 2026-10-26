package org.geysermc.geyser.entity.type;

import org.geysermc.geyser.entity.type.living.animal.farm.PigEntity;
import org.geysermc.geyser.entity.type.player.PlayerEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LivingEntityLerpTest {

    @Test
    void entitiesWithoutOwnTickOnlyLerp() {
        assertTrue(LivingEntity.onlyTicksLerp(PlayerEntity.class));
    }

    @Test
    void entitiesWithOwnTickKeepTicking() {
        assertFalse(LivingEntity.onlyTicksLerp(PigEntity.class));
    }
}
