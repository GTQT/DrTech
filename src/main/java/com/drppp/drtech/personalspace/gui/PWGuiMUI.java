package com.drppp.drtech.personalspace.gui;

import com.drppp.drtech.Tags;
import static com.drppp.drtech.personalspace.world.DimensionConfig.DaylightCycle.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IStringValue;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.factory.ClientGUI;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.DoubleValue;
import com.cleanroommc.modularui.value.StringValue;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.SliderWidget;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;
import com.github.bsideup.jabel.Desugar;

import com.drppp.drtech.personalspace.PWConfig;
import com.drppp.drtech.personalspace.packet.Packets;
import com.drppp.drtech.personalspace.world.DimensionConfig;

public class PWGuiMUI {

    private enum Page {
        GENERAL,
        LAYERS,
        LAYOUT,
        MARKER
    }

    private final int targetDim;
    private final int dimID;
    private final BlockPos blockPos;
    private final IDrawable checkmark = UITexture.builder().imageSize(16, 16).location("drtech", "checkmark")
            .build();
    private final IDrawable crossmark = UITexture.builder().imageSize(16, 16).location("drtech", "crossmark")
            .build();
    private final IDrawable moon = UITexture.builder().imageSize(16, 16)
            .uv((float) 80 / 256, (float) 32 / 256, (float) (80 + 16) / 256, (float) (32 + 16) / 256)
            .location("drtech", "widgets")
            .build();
    private final IDrawable sun = UITexture.builder().imageSize(16, 16)
            .uv((float) 96 / 256, (float) 32 / 256, (float) (96 + 16) / 256, (float) (32 + 16) / 256)
            .location("drtech", "widgets")
            .build();
    private final IDrawable sunMoon = UITexture.builder().imageSize(16, 16)
            .uv((float) 112 / 256, (float) 32 / 256, (float) (112 + 16) / 256, (float) (32 + 16) / 256)
            .location("drtech", "widgets")
            .build();
    private final IStringValue<String> name;
    private final StringValue boundaryXValue = new StringValue("0");
    private final StringValue boundaryZValue = new StringValue("0");
    private final StringValue gapWidthValue = new StringValue("0");
    private final List<String> layers = new ArrayList<>();
    private final ListWidget<IWidget, ?> layerListWidget = new ListWidget<>().top(58).left(306).size(22, 128);

    private DimensionConfig dimensionConfig;
    private Page page = Page.GENERAL;
    private int skyR;
    private int skyG;
    private int skyB;

    public PWGuiMUI(int targetDim, int dimID, int x, int y, int z, String name) {
        this.targetDim = targetDim;
        this.dimID = dimID;
        this.blockPos = new BlockPos(x, y, z);
        this.name = new StringValue(name);
    }

    public ModularScreen createGUI() {
        initDimensionConfig();
        ModularPanel panel = ModularPanel.defaultPanel("PWGUI_" + page.toString().toUpperCase(Locale.ROOT));
        panel.size(340, 230);

        panel.child(IKey.str(I18n.format("gui.personalWorld.name")).asWidget().top(7).left(7));
        panel.child(new TextFieldWidget().top(18).left(7).size(118, 18).value(name));

        panel.child(createTabButton(Page.GENERAL, I18n.format("gui.personalWorld.tab.general"), 132, true));
        panel.child(createTabButton(Page.LAYERS, I18n.format("gui.personalWorld.tab.layers"), 182,
                dimensionConfig.allowGenerationChanges()));
        panel.child(createTabButton(Page.LAYOUT, I18n.format("gui.personalWorld.tab.layout"), 232,
                dimensionConfig.allowGenerationChanges()));
        panel.child(createTabButton(Page.MARKER, I18n.format("gui.personalWorld.tab.marker"), 282,
                dimensionConfig.allowGenerationChanges()));

        panel.child(new ButtonWidget<>()
                .overlay(IKey.str(I18n.format("gui.personalWorld.done")))
                .size(60, 20)
                .bottom(9).right(9)
                .onMousePressed(i -> {
                    applyGridTextValues();
                    Packets.INSTANCE.sendChangeWorldSettings(dimID, blockPos, name.getStringValue(), dimensionConfig)
                            .sendToServer();
                    panel.closeIfOpen();
                    return true;
                }));
        panel.child(new ButtonWidget<>()
                .overlay(IKey.str(I18n.format("gui.personalWorld.cancel")))
                .size(60, 20)
                .bottom(9).left(9)
                .onMousePressed(i -> {
                    panel.closeIfOpen();
                    return true;
                }));

        switch (page) {
            case GENERAL -> buildGeneralPage(panel);
            case LAYERS -> buildLayersPage(panel);
            case LAYOUT -> buildLayoutPage(panel);
            case MARKER -> buildMarkerPage(panel);
        }
        return new ModularScreen(Tags.MODID, panel);
    }

    private void initDimensionConfig() {
        if (dimensionConfig != null) {
            return;
        }
        dimensionConfig = new DimensionConfig(0);
        if (targetDim == 0) {
            if (dimID != 0) {
                dimensionConfig = DimensionConfig.getConfig(dimID, true).copy();
            }
        } else {
            dimensionConfig = DimensionConfig.getConfig(targetDim, true).copy();
        }
        layers.clear();
        layers.addAll(fromPreset(dimensionConfig.getLayersAsString()));
        boundaryXValue.setValue(Integer.toString(dimensionConfig.getBoundaryChunkIntervalX()));
        boundaryZValue.setValue(Integer.toString(dimensionConfig.getBoundaryChunkIntervalZ()));
        gapWidthValue.setValue(Integer.toString(dimensionConfig.getGapWidth()));
        skyR = (dimensionConfig.getSkyColor() >> 16) & 0xFF;
        skyG = (dimensionConfig.getSkyColor() >> 8) & 0xFF;
        skyB = dimensionConfig.getSkyColor() & 0xFF;
    }

    private IWidget createTabButton(Page targetPage, String label, int left, boolean isEnabled) {
        return new ButtonWidget<>()
                .top(18).left(left)
                .size(48, 16)
                .background(isEnabled ? ((targetPage == page ? GuiTextures.MC_BUTTON_HOVERED : GuiTextures.MC_BUTTON)) :
                        GuiTextures.MC_BUTTON_DISABLED)
                .overlay(IKey.str(label).color(0xFFFFFF))
                .onMousePressed(mouse -> {
                    if (!isEnabled) return true;
                    if (targetPage != page) {
                        applyGridTextValues();
                        page = targetPage;
                        ClientGUI.open(createGUI());
                    }
                    return true;
                });
    }

    private void buildGeneralPage(ModularPanel panel) {
        panel.child(new SliderWidget()
                .size(116, 16)
                .top(46).left(8)
                .value(new DoubleValue(dimensionConfig.getStarsVisibility()))
                .onUpdateListener(widget -> {
                    dimensionConfig.setStarVisibility((float) widget.getSliderValue());
                    widget.overlay(IKey.str(String.format(I18n.format("gui.personalWorld.starBrightness") + " %.0f",
                            dimensionConfig.getStarsVisibility() * 100) + "%").color(0xFFFFFF).shadow(true));
                })
                .bounds(0F, 1F)
                .background(GuiTextures.MC_BUTTON_DISABLED));

        panel.child(new ButtonWidget<>()
                .size(18, 18)
                .top(45).left(130)
                .overlay(sun)
                .onMousePressed(i -> {
                    switch (dimensionConfig.getDaylightCycle()) {
                        case SUN -> dimensionConfig.setDaylightCycle(MOON);
                        case MOON -> dimensionConfig.setDaylightCycle(CYCLE);
                        case CYCLE -> dimensionConfig.setDaylightCycle(SUN);
                    }
                    return true;
                })
                .onUpdateListener(widget -> {
                    switch (dimensionConfig.getDaylightCycle()) {
                        case SUN -> widget.overlay(sun);
                        case MOON -> widget.overlay(moon);
                        case CYCLE -> widget.overlay(sunMoon);
                    }
                }));

        panel.child(createColorSlider(68, skyR, value -> skyR = value, "gui.personalWorld.skyColor.red"));
        panel.child(createColorSlider(90, skyG, value -> skyG = value, "gui.personalWorld.skyColor.green"));
        panel.child(createColorSlider(112, skyB, value -> skyB = value, "gui.personalWorld.skyColor.blue"));

        panel.child(new ParentWidget<>()
                .size(60, 60)
                .top(68).left(98)
                .onUpdateListener(widget -> {
                    dimensionConfig.setSkyColor((skyR << 16) | (skyG << 8) | skyB);
                    widget.overlay(new Rectangle().color(0xFF000000 | dimensionConfig.getSkyColor()));
                })
                .child(new Widget<>()
                        .leftRel(Alignment.TopRight.x)
                        .anchorLeft(Alignment.TopRight.x)
                        .topRel(Alignment.TopRight.y)
                        .anchorTop(Alignment.TopRight.y)
                        .size(14, 14)
                        .onUpdateListener(widget -> widget.overlay(new Star(dimensionConfig.getStarsVisibility()))))
                .child(new Widget<>()
                        .leftRel(Alignment.TopLeft.x)
                        .anchorLeft(Alignment.TopLeft.x)
                        .topRel(Alignment.TopLeft.y)
                        .anchorTop(Alignment.TopLeft.y)
                        .size(14, 14)
                        .onUpdateListener(widget -> widget.overlay(new Star(dimensionConfig.getStarsVisibility()))))
                .child(new Widget<>()
                        .leftRel(Alignment.BottomCenter.x)
                        .anchorLeft(Alignment.BottomCenter.x)
                        .topRel(Alignment.BottomCenter.y)
                        .anchorTop(Alignment.BottomCenter.y)
                        .size(14, 14)
                        .onUpdateListener(widget -> widget.overlay(new Star(dimensionConfig.getStarsVisibility())))));

        addLabeledToggle(panel, 180, 44, "gui.personalWorld.trees", () -> dimensionConfig.generateTrees(),
                () -> dimensionConfig.setGeneratingTrees(!dimensionConfig.generateTrees()),
                dimensionConfig.allowGenerationChanges());
        addLabeledToggle(panel, 180, 68, "gui.personalWorld.clouds", () -> dimensionConfig.cloudsEnabled(),
                () -> dimensionConfig.enableClouds(!dimensionConfig.cloudsEnabled()), true);
        addLabeledToggle(panel, 180, 92, "gui.personalWorld.peaceful_mobs", () -> dimensionConfig.spawnPassiveMobs(),
                () -> dimensionConfig.setSpawnPassiveMobs(!dimensionConfig.spawnPassiveMobs()), true);
        addLabeledToggle(panel, 180, 116, "gui.personalWorld.vegetation", () -> dimensionConfig.vegetationEnabled(),
                () -> dimensionConfig.setGeneratingVegetation(!dimensionConfig.vegetationEnabled()),
                dimensionConfig.allowGenerationChanges());
        addLabeledToggle(panel, 180, 140, "gui.personalWorld.weather", () -> dimensionConfig.weatherEnabled(),
                () -> dimensionConfig.enableWeather(!dimensionConfig.weatherEnabled()), true);
        addLabeledToggle(panel, 180, 164, "gui.personalWorld.hostile_mobs", () -> dimensionConfig.spawnMonsters(),
                () -> dimensionConfig.setSpawnMonsters(!dimensionConfig.spawnMonsters()), true);

        panel.child(IKey.str(I18n.format("gui.personalWorld.biome")).asWidget().top(156).left(8));
        panel.child(createBiomeSelector(8, 170, 150));
    }

    private void buildLayersPage(ModularPanel panel) {
        List<IBlockState> layerStates = PWConfig.getAllowedBlocks();
        ArrayList<IWidget> paletteButtons = new ArrayList<>();
        for (IBlockState blockState : layerStates) {
            Block block = blockState.getBlock();
            int itemMeta = block.damageDropped(blockState);
            int meta = block.getMetaFromState(blockState);
            ItemStack stack = new ItemStack(block, 1, itemMeta);
            paletteButtons.add(new ButtonWidget<>().size(20, 20)
                    .overlay(new ItemDrawable(stack).asIcon().size(20, 20))
                    .addTooltipLine(stack.getDisplayName())
                    .onMousePressed(i -> {
                        layers.add(new FlatLayerInfo(3, 1, block, meta).toString());
                        refreshLayerList();
                        return true;
                    }));
        }

        panel.child(IKey.str(I18n.format("gui.personalWorld.palette")).asWidget().top(44).left(8));
        panel.child(new ListWidget<>().children(paletteButtons).top(58).left(8).size(24, 128));
        panel.child(IKey.str(I18n.format("gui.personalWorld.presets")).asWidget().top(44).left(42));
        panel.child(createPresetSelector(42, 58, 118));
        panel.child(IKey.str(I18n.format("gui.personalWorld.layers")).asWidget().top(44).left(300));
        refreshLayerList();
        panel.child(layerListWidget);
    }

    private void buildLayoutPage(ModularPanel panel) {
        List<IBlockState> boundaryStates = PWConfig.getAllowedBoundaryBlocks();
        List<IBlockState> gapStates = PWConfig.getAllowedGapBlocks();

        panel.child(IKey.str(I18n.format("gui.personalWorld.boundary")).asWidget().top(44).left(8));
        panel.child(IKey.str("X").asWidget().top(62).left(8));
        panel.child(new TextFieldWidget().top(58).left(20).size(26, 18).value(boundaryXValue)
                .onUpdateListener(widget -> applyGridTextValues())
                .addTooltipLine(I18n.format("gui.personalWorld.layout.boundary.x.desc")));
        panel.child(IKey.str("Z").asWidget().top(62).left(56));
        panel.child(new TextFieldWidget().top(58).left(68).size(26, 18).value(boundaryZValue)
                .onUpdateListener(widget -> applyGridTextValues())
                .addTooltipLine(I18n.format("gui.personalWorld.layout.boundary.z.desc")));

        panel.child(IKey.str("A").asWidget().top(82).left(104));
        panel.child(createBlockStateSelector(boundaryStates,
                () -> dimensionConfig.stateFromSelection(dimensionConfig.getBoundaryBlockA(),
                        dimensionConfig.getBoundaryMetaA()),
                state -> applyBlockSelection(state, dimensionConfig::setBoundaryBlockA,
                        dimensionConfig::setBoundaryMetaA),
                104, 94, 108, "gui.personalWorld.layout.boundary.a.desc"));
        panel.child(IKey.str("B").asWidget().top(82).left(220));
        panel.child(createBlockStateSelector(boundaryStates,
                () -> dimensionConfig.stateFromSelection(dimensionConfig.getBoundaryBlockB(),
                        dimensionConfig.getBoundaryMetaB()),
                state -> applyBlockSelection(state, dimensionConfig::setBoundaryBlockB,
                        dimensionConfig::setBoundaryMetaB),
                220, 94, 108, "gui.personalWorld.layout.boundary.b.desc"));

        panel.child(IKey.str(I18n.format("gui.personalWorld.gap")).asWidget().top(124).left(8));
        panel.child(IKey.str(I18n.format("gui.personalWorld.gap.width")).asWidget().top(142).left(8));
        panel.child(new TextFieldWidget().top(138).left(48).size(28, 18).value(gapWidthValue)
                .onUpdateListener(widget -> applyGridTextValues())
                .addTooltipLine(I18n.format("gui.personalWorld.layout.gap.width.desc")));
        panel.child(new ButtonWidget<>()
                .top(138).left(84)
                .size(80, 18)
                .addTooltipLine(I18n.format("gui.personalWorld.layout.gap.preset.desc"))
                .onMousePressed(mouse -> {
                    DimensionConfig.GapPreset next = dimensionConfig.getGapPreset() == DimensionConfig.GapPreset.ROAD ?
                            DimensionConfig.GapPreset.SOLID : DimensionConfig.GapPreset.ROAD;
                    dimensionConfig.setGapPreset(next);
                    return true;
                })
                .onUpdateListener(widget -> widget.overlay(IKey.str(
                        I18n.format("gui.personalWorld.gap.preset." + dimensionConfig.getGapPreset().name()))
                        .color(0xFFFFFF))));

        panel.child(IKey.str("A").asWidget().top(166).left(8));
        panel.child(createBlockStateSelector(gapStates,
                () -> dimensionConfig.stateFromSelection(dimensionConfig.getGapBlockA(), dimensionConfig.getGapMetaA()),
                state -> applyBlockSelection(state, dimensionConfig::setGapBlockA, dimensionConfig::setGapMetaA),
                8, 178, 104, "gui.personalWorld.layout.gap.a.desc"));
        panel.child(IKey.str("B").asWidget().top(166).left(116));
        panel.child(createBlockStateSelector(gapStates,
                () -> dimensionConfig.stateFromSelection(dimensionConfig.getGapBlockB(), dimensionConfig.getGapMetaB()),
                state -> applyBlockSelection(state, dimensionConfig::setGapBlockB, dimensionConfig::setGapMetaB),
                116, 178, 104, "gui.personalWorld.layout.gap.b.desc"));
        panel.child(IKey.str("C").asWidget().top(166).left(224));
        panel.child(createBlockStateSelector(gapStates,
                () -> dimensionConfig.stateFromSelection(dimensionConfig.getGapBlockC(), dimensionConfig.getGapMetaC()),
                state -> applyBlockSelection(state, dimensionConfig::setGapBlockC, dimensionConfig::setGapMetaC),
                224, 178, 104, "gui.personalWorld.layout.gap.c.desc"));
    }

    private void buildMarkerPage(ModularPanel panel) {
        List<IBlockState> centerStates = PWConfig.getAllowedCenterBlocks();

        panel.child(IKey.str(I18n.format("gui.personalWorld.center")).asWidget().top(44).left(8));
        panel.child(new ButtonWidget<>()
                .top(58).left(8)
                .size(90, 18)
                .addTooltipLine(I18n.format("gui.personalWorld.layout.center.enabled.desc"))
                .onMousePressed(mouse -> {
                    dimensionConfig.setCenterEnabled(!dimensionConfig.isCenterEnabled());
                    return true;
                })
                .onUpdateListener(widget -> widget.overlay(IKey.str(
                        I18n.format(dimensionConfig.isCenterEnabled() ? "gui.personalWorld.center.enabled" :
                                "gui.personalWorld.center.disabled"))
                        .color(0xFFFFFF))));
        panel.child(new ButtonWidget<>()
                .top(58).left(106)
                .size(76, 18)
                .addTooltipLine(I18n.format("gui.personalWorld.layout.center.dir.desc"))
                .onMousePressed(mouse -> {
                    DimensionConfig.CenterDirection[] values = DimensionConfig.CenterDirection.values();
                    int next = (dimensionConfig.getCenterDirection().ordinal() + 1) % values.length;
                    dimensionConfig.setCenterDirection(values[next]);
                    return true;
                })
                .onUpdateListener(widget -> widget.overlay(IKey.str(
                        I18n.format("gui.personalWorld.center.dir." + dimensionConfig.getCenterDirection().name()))
                        .color(0xFFFFFF))));
        panel.child(createBlockStateSelector(centerStates,
                () -> dimensionConfig.stateFromSelection(dimensionConfig.getCenterBlock(),
                        dimensionConfig.getCenterMeta()),
                state -> applyBlockSelection(state, dimensionConfig::setCenterBlock, dimensionConfig::setCenterMeta),
                190, 58, 138, "gui.personalWorld.layout.center.block.desc"));

        panel.child(IKey.str(I18n.format("gui.personalWorld.grid")).asWidget().top(84).left(190));
        panel.child(new WorldPreviewWidget(dimensionConfig).size(130, 100).top(96).left(190));
    }

    private void addLabeledToggle(ModularPanel panel, int left, int top, String labelKey, ToggleValue getter,
                                  Runnable action, boolean isEnabled) {
        panel.child(createToggleButton(left, top, getter, action, isEnabled));
        panel.child(IKey.str(I18n.format(labelKey)).asWidget().top(top + 4).left(left + 24));
    }

    private IWidget createToggleButton(int left, int top, ToggleValue getter, Runnable action, boolean isEnabled) {
        return new ButtonWidget<>()
                .size(18, 18)
                .top(top).left(left)
                .overlay(crossmark)
                .background(isEnabled ? GuiTextures.MC_BUTTON :
                        GuiTextures.MC_BUTTON_DISABLED)
                .hoverBackground(isEnabled ? GuiTextures.MC_BUTTON_HOVERED :
                        GuiTextures.MC_BUTTON_DISABLED)
                .onMousePressed(i -> {
                    if (isEnabled) {
                        action.run();
                    }
                    return true;
                })
                .onUpdateListener(widget -> widget.overlay(getter.get() ? checkmark : crossmark));
    }

    private IWidget createColorSlider(int top, int initialValue, IntConsumer consumer, String translationKey) {
        return new SliderWidget()
                .size(82, 15)
                .top(top).left(8)
                .background(GuiTextures.MC_BUTTON_DISABLED)
                .value(new DoubleValue(initialValue))
                .bounds(0, 255)
                .onUpdateListener(widget -> {
                    int value = (int) widget.getSliderValue();
                    consumer.accept(value);
                    widget.overlay(IKey.str(String.format(I18n.format(translationKey) + " %s", value))
                            .color(0xFFFFFF).shadow(true));
                });
    }

    private IWidget createBiomeSelector(int left, int top, int width) {
        List<Biome> biomes = PWConfig.getAllowedBiomes();
        return createTextSelector(left, top, width,
                () -> dimensionConfig.getBiome().getBiomeName(),
                () -> cycleBiome(-1, biomes),
                () -> cycleBiome(1, biomes),
                "gui.personalWorld.biome.desc", dimensionConfig.allowGenerationChanges());
    }

    private IWidget createPresetSelector(int left, int top, int width) {
        return createTextSelector(left, top, width,
                this::currentPresetLabel,
                () -> cyclePreset(-1),
                () -> cyclePreset(1),
                "gui.personalWorld.presets.desc", dimensionConfig.allowGenerationChanges());
    }

    private String currentPresetLabel() {
        if (layers.isEmpty()) {
            return I18n.format("gui.personalWorld.voidWorld");
        }
        String currentLayers = toPreset(layers);
        String fullPreset = dimensionConfig.getFullPresetString();
        for (Map.Entry<String, String> entry : PWConfig.getPresets().entrySet()) {
            boolean matches = DimensionConfig.hasExtendedSettings(entry.getValue()) ?
                    entry.getValue().equals(fullPreset) :
                    DimensionConfig.extractLayersPart(entry.getValue()).equals(currentLayers);
            if (matches) {
                return getPresetDisplayName(entry.getKey());
            }
        }
        return I18n.format("gui.personalWorld.customPreset");
    }

    private String getPresetDisplayName(String presetName) {
        String translationKey = "gui.personalWorld.preset." + presetName.toLowerCase();
        String translated = I18n.format(translationKey);
        return translated.equals(translationKey) ? presetName : translated;
    }

    private void applyPreset(String preset) {
        layers.clear();
        layers.addAll(fromPreset(DimensionConfig.extractLayersPart(preset)));
        dimensionConfig.setLayers(toPreset(layers));
        if (DimensionConfig.hasExtendedSettings(preset)) {
            dimensionConfig.applyExtendedSettings(preset);
        }
        boundaryXValue.setValue(Integer.toString(dimensionConfig.getBoundaryChunkIntervalX()));
        boundaryZValue.setValue(Integer.toString(dimensionConfig.getBoundaryChunkIntervalZ()));
        gapWidthValue.setValue(Integer.toString(dimensionConfig.getGapWidth()));
    }

    private IWidget createTextSelector(int left, int top, int width, Supplier<String> labelSupplier,
                                       Runnable prevAction,
                                       Runnable nextAction, String tooltipKey, boolean isEnabled) {
        ParentWidget<?> container = new ParentWidget<>().top(top).left(left).size(width, 20);
        String[] tooltipLines = new String[] {
                I18n.format(tooltipKey),
                I18n.format("gui.personalWorld.selector.cycle")
        };
        if (isEnabled) {
            container.child(createArrowButton(0, 0, -1, prevAction, tooltipLines));
            container.child(createArrowButton(width - 12, 0, 1, nextAction, tooltipLines));
        }
        container.child(addTooltipLines(new ButtonWidget<>()
                .size(width - 24, 20)
                .top(0).left(12)
                .background(isEnabled ? GuiTextures.MC_BUTTON : GuiTextures.MC_BUTTON_DISABLED)
                .onMousePressed(mouse -> {
                    if (isEnabled) {
                        nextAction.run();
                    }
                    return true;
                })
                .onUpdateListener(
                        widget -> widget.overlay(IKey.str(shortenLabel(labelSupplier.get(), 18)).color(0xFFFFFF))),
                tooltipLines));
        return container;
    }

    private IWidget createBlockStateSelector(List<IBlockState> states, Supplier<IBlockState> selectedSupplier,
                                             Consumer<IBlockState> consumer, int left, int top, int width,
                                             String tooltipKey) {
        ParentWidget<?> container = new ParentWidget<>().top(top).left(left).size(width, 20);
        String[] tooltipLines = new String[] {
                I18n.format(tooltipKey),
                I18n.format("gui.personalWorld.selector.cycle")
        };
        container.child(addTooltipLines(new ButtonWidget<>()
                .size(18, 18)
                .top(1).left(0)
                .background(GuiTextures.MC_BUTTON_DISABLED)
                .onUpdateListener(widget -> {
                    ItemStack stack = toDisplayStack(selectedSupplier.get());
                    if (stack.isEmpty()) {
                        widget.overlay(IKey.str("-").color(0xFFFFFF));
                    } else {
                        widget.overlay(new ItemDrawable(stack).asIcon().size(16, 16));
                    }
                }), tooltipLines));
        container.child(createArrowButton(20, 0, -1,
                () -> cycleBlockState(states, selectedSupplier.get(), consumer, -1), tooltipLines));
        container.child(addTooltipLines(new ButtonWidget<>()
                .size(width - 44, 20)
                .top(0).left(32)
                .background(GuiTextures.MC_BUTTON)
                .onMousePressed(mouse -> {
                    cycleBlockState(states, selectedSupplier.get(), consumer, 1);
                    return true;
                })
                .onUpdateListener(
                        widget -> widget.overlay(IKey.str(shortenLabel(getStateLabel(selectedSupplier.get()), 10))
                                .color(0xFFFFFF))),
                tooltipLines));
        container.child(createArrowButton(width - 12, 0, 1,
                () -> cycleBlockState(states, selectedSupplier.get(), consumer, 1), tooltipLines));
        return container;
    }

    private ButtonWidget<?> createArrowButton(int left, int top, int direction, Runnable action,
                                              String... tooltipLines) {
        return addTooltipLines(new ButtonWidget<>()
                .size(12, 20)
                .top(top).left(left)
                .background(GuiTextures.MC_BUTTON)
                .overlay(IKey.str(direction < 0 ? "<" : ">").color(0xFFFFFF))
                .onMousePressed(mouse -> {
                    action.run();
                    return true;
                }), tooltipLines);
    }

    private void cycleBiome(int direction, List<Biome> biomes) {
        if (biomes.isEmpty()) {
            return;
        }
        int currentIndex = biomes.indexOf(dimensionConfig.getBiome());
        if (currentIndex < 0) {
            currentIndex = 0;
        }
        dimensionConfig.setBiome(biomes.get(wrapIndex(currentIndex + direction, biomes.size())));
    }

    private void cyclePreset(int direction) {
        ArrayList<Map.Entry<String, String>> entries = new ArrayList<>(PWConfig.getPresets().entrySet());
        int optionCount = entries.size() + 1;
        int currentIndex = getCurrentPresetIndex(entries);
        if (currentIndex < 0) {
            currentIndex = direction > 0 ? -1 : 0;
        }
        int nextIndex = wrapIndex(currentIndex + direction, optionCount);
        if (nextIndex == entries.size()) {
            layers.clear();
            dimensionConfig.getLayers().clear();
        } else {
            applyPreset(entries.get(nextIndex).getValue());
        }
        refreshLayerList();
    }

    private int getCurrentPresetIndex(List<Map.Entry<String, String>> entries) {
        if (layers.isEmpty()) {
            return entries.size();
        }
        String currentLayers = toPreset(layers);
        String fullPreset = dimensionConfig.getFullPresetString();
        for (int i = 0; i < entries.size(); i++) {
            String preset = entries.get(i).getValue();
            boolean matches = DimensionConfig.hasExtendedSettings(preset) ? preset.equals(fullPreset) :
                    DimensionConfig.extractLayersPart(preset).equals(currentLayers);
            if (matches) {
                return i;
            }
        }
        return -1;
    }

    private void cycleBlockState(List<IBlockState> states, IBlockState selected, Consumer<IBlockState> consumer,
                                 int direction) {
        if (states.isEmpty()) {
            return;
        }
        int currentIndex = findStateIndex(states, selected);
        if (currentIndex < 0) {
            currentIndex = 0;
        }
        consumer.accept(states.get(wrapIndex(currentIndex + direction, states.size())));
    }

    private int findStateIndex(List<IBlockState> states, IBlockState target) {
        if (target == null) {
            return -1;
        }
        Block targetBlock = target.getBlock();
        int targetMeta = targetBlock.getMetaFromState(target);
        for (int i = 0; i < states.size(); i++) {
            IBlockState state = states.get(i);
            Block block = state.getBlock();
            if (block == targetBlock && block.getMetaFromState(state) == targetMeta) {
                return i;
            }
        }
        return -1;
    }

    private ItemStack toDisplayStack(IBlockState state) {
        if (state == null || state.getBlock() == null || state.getBlock() == Block.getBlockFromName("minecraft:air")) {
            return ItemStack.EMPTY;
        }
        Block block = state.getBlock();
        return new ItemStack(block, 1, block.damageDropped(state));
    }

    private int wrapIndex(int value, int size) {
        int wrapped = value % size;
        return wrapped < 0 ? wrapped + size : wrapped;
    }

    private String shortenLabel(String label, int maxChars) {
        if (label == null || label.isEmpty()) {
            return "";
        }
        if (label.length() <= maxChars) {
            return label;
        }
        return label.substring(0, Math.max(1, maxChars - 3)) + "...";
    }

    private <T extends ButtonWidget<?>> T addTooltipLines(T widget, String... lines) {
        for (String line : lines) {
            widget.addTooltipLine(line);
        }
        return widget;
    }

    private void refreshLayerList() {
        layerListWidget.removeAll();
        layerListWidget.children(buildLayerWidgets());
    }

    private ArrayList<IWidget> buildLayerWidgets() {
        ArrayList<IWidget> layerWidgets = new ArrayList<>();
        for (int i = 0; i < layers.size(); i++) {
            FlatLayerInfo layerInfo = DimensionConfig.LayerFromString(layers.get(i));
            if (layerInfo == null) {
                continue;
            }
            AtomicInteger layerCount = new AtomicInteger(layerInfo.getLayerCount());
            IBlockState blockState = layerInfo.getLayerMaterial();
            Block block = blockState.getBlock();
            int itemMeta = block.damageDropped(blockState);
            int meta = block.getMetaFromState(blockState);
            ItemStack stack = new ItemStack(block, 1, itemMeta);
            boolean arrowDown = i != layers.size() - 1 && layers.size() != 1;
            boolean arrowUp = i > 0;
            int index = i;
            layerWidgets.add(new ParentWidget<>().size(22, 22)
                    .overlay(new ItemDrawable(stack).asIcon().size(20, 20))
                    .addTooltipLine(stack.getDisplayName())
                    .child(IKey.str(Integer.toString(layerCount.get())).color(0xFFFFF).asWidget()
                            .leftRel(Alignment.BottomCenter.x)
                            .anchorLeft(Alignment.BottomCenter.x)
                            .topRel(Alignment.BottomCenter.y)
                            .anchorTop(Alignment.BottomCenter.y))
                    .child(new ButtonWidget<>().size(6, 6)
                            .leftRel(Alignment.TopLeft.x)
                            .anchorLeft(Alignment.TopLeft.x)
                            .topRel(Alignment.TopLeft.y)
                            .anchorTop(Alignment.TopLeft.y)
                            .overlay(GuiTextures.ADD)
                            .addTooltipLine(I18n.format("gui.personalWorld.layers.increase"))
                            .onMousePressed(mouse -> {
                                FlatLayerInfo newLayer = new FlatLayerInfo(3, layerCount.incrementAndGet(), block,
                                        meta);
                                layers.set(index, newLayer.toString());
                                refreshLayerList();
                                return true;
                            }))
                    .child(new ButtonWidget<>().size(6, 6)
                            .leftRel(Alignment.BottomLeft.x)
                            .anchorLeft(Alignment.BottomLeft.x)
                            .topRel(Alignment.BottomLeft.y)
                            .anchorTop(Alignment.BottomLeft.y)
                            .overlay(GuiTextures.REMOVE)
                            .addTooltipLine(I18n.format("gui.personalWorld.layers.decrease"))
                            .onMousePressed(mouse -> {
                                if (layerCount.get() == 1) {
                                    return true;
                                }
                                FlatLayerInfo newLayer = new FlatLayerInfo(3, layerCount.decrementAndGet(), block,
                                        meta);
                                layers.set(index, newLayer.toString());
                                refreshLayerList();
                                return true;
                            }))
                    .child(new ButtonWidget<>().size(6, 6)
                            .leftRel(Alignment.CenterLeft.x)
                            .anchorLeft(Alignment.CenterLeft.x)
                            .topRel(Alignment.CenterLeft.y)
                            .anchorTop(Alignment.CenterLeft.y)
                            .overlay(GuiTextures.CROSS_TINY)
                            .addTooltipLine(I18n.format("gui.personalWorld.layers.remove"))
                            .onMousePressed(mouse -> {
                                layers.remove(index);
                                refreshLayerList();
                                return true;
                            }))
                    .childIf(arrowUp, () -> new ButtonWidget<>().size(6, 6)
                            .leftRel(Alignment.TopRight.x)
                            .anchorLeft(Alignment.TopRight.x)
                            .topRel(Alignment.TopRight.y)
                            .anchorTop(Alignment.TopRight.y)
                            .overlay(GuiTextures.MOVE_UP)
                            .addTooltipLine(I18n.format("gui.personalWorld.layers.moveUp"))
                            .onMousePressed(mouse -> {
                                Collections.swap(layers, index - 1, index);
                                refreshLayerList();
                                return true;
                            }))
                    .childIf(arrowDown, () -> new ButtonWidget<>().size(6, 6)
                            .leftRel(Alignment.BottomRight.x)
                            .anchorLeft(Alignment.BottomRight.x)
                            .topRel(Alignment.BottomRight.y)
                            .anchorTop(Alignment.BottomRight.y)
                            .overlay(GuiTextures.MOVE_DOWN)
                            .addTooltipLine(I18n.format("gui.personalWorld.layers.moveDown"))
                            .onMousePressed(mouse -> {
                                Collections.swap(layers, index, index + 1);
                                refreshLayerList();
                                return true;
                            })));
        }
        dimensionConfig.setLayers(toPreset(layers));
        return layerWidgets;
    }

    private String getStateLabel(IBlockState state) {
        if (state == null || state.getBlock() == null) {
            return I18n.format("tile.air.name");
        }
        Block block = state.getBlock();
        if (block == Block.getBlockFromName("minecraft:air")) {
            return I18n.format("tile.air.name");
        }
        ItemStack stack = new ItemStack(block, 1, block.damageDropped(state));
        return stack.getDisplayName();
    }

    private void applyBlockSelection(IBlockState state, Consumer<String> blockSetter, IntConsumer metaSetter) {
        if (state == null || state.getBlock() == null) {
            blockSetter.accept("minecraft:air");
            metaSetter.accept(0);
            return;
        }
        Block block = state.getBlock();
        String registryName = block.getRegistryName() == null ? "minecraft:air" : block.getRegistryName().toString();
        blockSetter.accept(registryName);
        metaSetter.accept(block.getMetaFromState(state));
    }

    private void applyGridTextValues() {
        dimensionConfig.setBoundaryChunkIntervalX(parseClampedInt(boundaryXValue.getStringValue(), 0, 512));
        dimensionConfig.setBoundaryChunkIntervalZ(parseClampedInt(boundaryZValue.getStringValue(), 0, 512));
        dimensionConfig.setGapWidth(parseClampedInt(gapWidthValue.getStringValue(), 0, 31));
    }

    private int parseClampedInt(String value, int min, int max) {
        try {
            return Math.max(min, Math.min(max, Integer.parseInt(value.trim())));
        } catch (Exception ignored) {
            return min;
        }
    }

    private String toPreset(List<String> layerList) {
        StringBuilder sb = new StringBuilder();
        for (int i = layerList.size() - 1; i >= 0; i--) {
            sb.append(layerList.get(i));
            if (i != 0) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public static List<String> fromPreset(String preset) {
        ArrayList<String> layerEntries = new ArrayList<>();
        if (preset == null || preset.isEmpty()) {
            return layerEntries;
        }
        if (preset.contains(",")) {
            String[] stringArray = preset.split(",");
            for (int i = stringArray.length - 1; i > -1; --i) {
                layerEntries.add(stringArray[i]);
            }
        } else {
            layerEntries.add(preset);
        }
        return layerEntries;
    }

    @Desugar
    private record Star(float brightness) implements IDrawable {

        @SideOnly(Side.CLIENT)
        @Override
        public void draw(GuiContext context, int x0, int y0, int width, int height, WidgetTheme widgetTheme) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, brightness);
            UITexture.builder().imageSize(16, 16)
                    .uv((float) 32 / 256, (float) 32 / 256, (float) (32 + 16) / 256, (float) (32 + 16) / 256)
                    .location("drtech", "widgets")
                    .build().draw(x0, y0, width, height);
        }
    }

    @FunctionalInterface
    private interface ToggleValue {

        boolean get();
    }
}
