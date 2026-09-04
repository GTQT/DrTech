package com.drppp.drtech.drone.visual;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable render state consumed by world, dock, drone, and logistics renderers. */
public final class DroneVisualState {
    public enum FlightMarker { NONE, TAKEOFF, LANDING }
    private final boolean areaWireframe, shapePreview, dockBeacon, guideLights, placementHologram, damageSmoke;
    private final float chargeProgress, cargoFill, rotorSpeed;
    private final FlightMarker flightMarker;
    private final ResourceLocation moduleAppearance;
    private final BlockPos placementTarget;
    private final List<Link> logisticsLinks;

    public DroneVisualState(boolean areaWireframe, boolean shapePreview, boolean dockBeacon, boolean guideLights,
            float chargeProgress, float cargoFill, float rotorSpeed, FlightMarker flightMarker,
            @Nullable ResourceLocation moduleAppearance, boolean placementHologram, @Nullable BlockPos placementTarget,
            boolean damageSmoke, List<Link> logisticsLinks) {
        this.areaWireframe=areaWireframe; this.shapePreview=shapePreview; this.dockBeacon=dockBeacon; this.guideLights=guideLights;
        this.chargeProgress=unit(chargeProgress); this.cargoFill=unit(cargoFill); this.rotorSpeed=unit(rotorSpeed);
        this.flightMarker=flightMarker==null?FlightMarker.NONE:flightMarker; this.moduleAppearance=moduleAppearance;
        this.placementHologram=placementHologram; this.placementTarget=placementTarget==null?null:placementTarget.toImmutable();
        this.damageSmoke=damageSmoke; ArrayList<Link> links=new ArrayList<>(); if(logisticsLinks!=null) links.addAll(logisticsLinks.subList(0,Math.min(128,logisticsLinks.size()))); this.logisticsLinks=Collections.unmodifiableList(links);
    }
    private static float unit(float value){return Math.max(0F,Math.min(1F,value));}
    public boolean isAreaWireframe(){return areaWireframe;} public boolean isShapePreview(){return shapePreview;} public boolean isDockBeacon(){return dockBeacon;} public boolean isGuideLights(){return guideLights;}
    public float getChargeProgress(){return chargeProgress;} public float getCargoFill(){return cargoFill;} public float getRotorSpeed(){return rotorSpeed;} public FlightMarker getFlightMarker(){return flightMarker;}
    @Nullable public ResourceLocation getModuleAppearance(){return moduleAppearance;} public boolean isPlacementHologram(){return placementHologram;} @Nullable public BlockPos getPlacementTarget(){return placementTarget;} public boolean isDamageSmoke(){return damageSmoke;} public List<Link> getLogisticsLinks(){return logisticsLinks;}
    public static final class Link { public final BlockPos source,target; public Link(BlockPos source,BlockPos target){this.source=source.toImmutable();this.target=target.toImmutable();} }
}
