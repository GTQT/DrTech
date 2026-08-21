package com.drppp.drtech.common.multiblock.mover;

import com.drppp.drtech.DrTechMain;
import com.drppp.drtech.DrtConfig;
import com.drppp.drtech.Tags;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;

/** One-time migrations for mover settings whose defaults changed after release. */
public final class MoverConfigMigration {
    static final int CURRENT_REVISION = 1;
    private static final String CATEGORY = "general.multiblockmover";
    private static final String REVISION_KEY = "configRevision";
    private static final String ROTATION_KEY = "enableRotation";

    private MoverConfigMigration() {
    }

    public static void apply(File configDirectory) {
        File configFile = new File(configDirectory, Tags.MODID + ".cfg");
        if (!configFile.isFile()) return;

        Configuration configuration = new Configuration(configFile);
        try {
            configuration.load();
            if (!configuration.hasCategory(CATEGORY)) return;

            ConfigCategory moverCategory = configuration.getCategory(CATEGORY);
            if (moverCategory.containsKey(REVISION_KEY)) return;

            Property rotation = moverCategory.get(ROTATION_KEY);
            if (rotation != null) {
                rotation.set(true);
            }
            configuration.get(CATEGORY, REVISION_KEY, CURRENT_REVISION)
                    .set(CURRENT_REVISION);
            configuration.save();

            // @Config has already populated its static fields by pre-init, so keep
            // the running integrated/dedicated server in sync with the migrated file.
            DrtConfig.MultiblockMover.enableRotation = true;
            DrtConfig.MultiblockMover.configRevision = CURRENT_REVISION;
            DrTechMain.LOGGER.info(
                    "Migrated legacy multiblock mover config: rotation is enabled by default");
        } catch (RuntimeException exception) {
            DrTechMain.LOGGER.error("Failed to migrate multiblock mover configuration", exception);
        }
    }
}
