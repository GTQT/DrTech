package com.drppp.drtech.common.drone.entity;

import com.meowmel.cropQT.api.CropRegistry;
import com.meowmel.cropQT.api.CropStats;
import com.meowmel.cropQT.api.CropType;
import com.meowmel.cropQT.tile.TileCropStick;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CropStickHarvestContractTest {

    @Test
    void matureHybridHarvestResetsGrowthButPreservesIdentityAndInheritedStats() {
        String cropId = "drtech_drone_harvest_contract";
        CropRegistry.register(new CropType.Builder(cropId).maxGrowthStage(6).harvestStage(5).build());
        TileCropStick crop = new TileCropStick();
        NBTTagCompound saved = new NBTTagCompound();
        saved.setString("cropId", cropId);
        saved.setInteger("growthStage", 6);
        saved.setInteger("growthProgress", 12);
        new CropStats(17, 23, 29).writeToNBT(saved);
        crop.readFromNBT(saved);

        assertTrue(crop.isMature());
        assertTrue(crop.harvest());

        assertFalse(crop.isMature());
        assertEquals(cropId, crop.getCropId());
        assertEquals(0, crop.getGrowthStage());
        assertEquals(0, crop.getGrowthProgress());
        assertEquals(17, crop.getStats().getGrowth());
        assertEquals(23, crop.getStats().getGain());
        assertEquals(29, crop.getStats().getResistance());
    }
}
