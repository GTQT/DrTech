package com.drppp.drtech.Client.render.Items.lightsaber;

import com.drppp.drtech.Client.render.Items.lightsaber.model.*;
import com.drppp.drtech.Tags;
import com.drppp.drtech.common.Items.lightsaber.LightsaberHilt;
import com.drppp.drtech.common.Items.lightsaber.LightsaberPartType;
import net.minecraft.client.model.ModelBase;
import net.minecraft.util.ResourceLocation;

import java.util.EnumMap;
import java.util.Map;

public final class LightsaberModelRegistry {
    private static final Map<LightsaberHilt, ModelBase[]> MODELS = new EnumMap<>(LightsaberHilt.class);

    static {
        register(LightsaberHilt.GRAFLEX, new ModelEmitterGraflex(), new ModelSwitchSectionGraflex(),
                new ModelBodyGraflex(), new ModelPommelGraflex());
        register(LightsaberHilt.REDEEMER, new ModelEmitterRedeemer(), new ModelSwitchSectionRedeemer(),
                new ModelBodyRedeemer(), new ModelPommelRedeemer());
        register(LightsaberHilt.MAULER, new ModelEmitterMauler(), new ModelSwitchSectionMauler(),
                new ModelBodyMauler(), new ModelPommelMauler());
        register(LightsaberHilt.PRODIGAL_SON, new ModelEmitterProdigalSon(), new ModelSwitchSectionProdigalSon(),
                new ModelBodyProdigalSon(), new ModelPommelProdigalSon());
        register(LightsaberHilt.KNIGHTED, new ModelEmitterKnighted(), new ModelSwitchSectionKnighted(),
                new ModelBodyKnighted(), new ModelPommelKnighted());
        register(LightsaberHilt.VAID_ANCIENT, new ModelEmitterVaid(), new ModelSwitchSectionVaid(),
                new ModelBodyVaid(), new ModelPommelVaid());
        register(LightsaberHilt.VAID_MODERN, new ModelEmitterVaid(), new ModelSwitchSectionVaid(),
                new ModelBodyVaid(), new ModelPommelVaid());
        register(LightsaberHilt.DROIDEKA, new ModelEmitterDroideka(), new ModelSwitchSectionDroideka(),
                new ModelBodyDroideka(), new ModelPommelDroideka());
        register(LightsaberHilt.FULCRUM, new ModelEmitterFulcrum(), new ModelSwitchSectionFulcrum(),
                new ModelBodyFulcrum(), new ModelPommelFulcrum());
        register(LightsaberHilt.JUGGERNAUT, new ModelEmitterJuggernaut(), new ModelSwitchSectionJuggernaut(),
                new ModelBodyJuggernaut(), new ModelPommelJuggernaut());
        register(LightsaberHilt.MECHANICAL, new ModelEmitterMechanical(), new ModelSwitchSectionMechanical(),
                new ModelBodyMechanical(), new ModelPommelMechanical());
        register(LightsaberHilt.MANDALORIAN, new ModelEmitterMandalorian(), new ModelSwitchSectionMandalorian(),
                new ModelBodyMandalorian(), new ModelPommelMandalorian());
        register(LightsaberHilt.FURY, new ModelEmitterFury(), new ModelSwitchSectionFury(),
                new ModelBodyFury(), new ModelPommelFury());
        register(LightsaberHilt.REBEL, new ModelEmitterRebel(), new ModelSwitchSectionRebel(),
                new ModelBodyRebel(), new ModelPommelRebel());
        register(LightsaberHilt.IMPERIAL, new ModelEmitterImperial(), new ModelSwitchSectionImperial(),
                new ModelBodyImperial(), new ModelPommelImperial());
        register(LightsaberHilt.REBORN, new ModelEmitterReborn(), new ModelSwitchSectionReborn(),
                new ModelBodyReborn(), new ModelPommelReborn());
    }

    private LightsaberModelRegistry() {
    }

    public static ModelBase getModel(LightsaberHilt hilt, LightsaberPartType type) {
        return MODELS.get(hilt)[type.ordinal()];
    }

    public static ResourceLocation getTexture(LightsaberHilt hilt, LightsaberPartType type) {
        return new ResourceLocation(Tags.MODID,
                "textures/models/lightsaber/" + hilt.getTextureName(type) + ".png");
    }

    private static void register(LightsaberHilt hilt, ModelBase emitter, ModelBase switchSection,
                                 ModelBase body, ModelBase pommel) {
        MODELS.put(hilt, new ModelBase[] { emitter, switchSection, body, pommel });
    }
}
