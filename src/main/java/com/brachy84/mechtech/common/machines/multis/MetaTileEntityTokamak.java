package com.brachy84.mechtech.common.machines.multis;
/*
import gregicadditions.item.GAMetaBlocks;
import gregicadditions.item.fusion.GAFusionCasing;
import gregicadditions.machines.multi.advance.MetaTileEntityAdvFusionReactor;
import gregicadditions.machines.multi.multiblockpart.GAMetaTileEntityEnergyHatch;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.multiblock.BlockPattern;
import gregtech.api.multiblock.FactoryBlockPattern;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

public class MetaTileEntityTokamak extends MetaTileEntityAdvFusionReactor {

    private static final MultiblockAbility<?>[] ALLOWED_ABILITIES = {
            MultiblockAbility.IMPORT_FLUIDS,
            MultiblockAbility.EXPORT_FLUIDS
    };

    public MetaTileEntityTokamak(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("#####################", "#####################", "#####################", "#####################", "#####################", "#######cccCccc#######", "#####################", "#####################", "#####################", "#####################", "#####################")
                .aisle("#####################", "#####################", "#####################", "#######cccCccc#######", "#######cccCccc#######", "#####ccVVVVVVVcc#####", "#######cccCccc#######", "#######cccCccc#######", "#####################", "#####################", "#####################")
                .aisle("#####################", "#####################", "#######cccCccc#######", "#####ccVVVVVVVcc#####", "#####ccVVVVVVVcc#####", "####cVV#######VVc####", "#####ccVVVVVVVcc#####", "#####ccVVVVVVVcc#####", "#######cccCccc#######", "#####################", "#####################")
                .aisle("#####################", "#######cccCccc#######", "#####ccVVVVVVVcc#####", "###CcVV#######VVcC###", "###CcVV#######VVcC###", "###CV###########VC###", "###CcVV#######VVcC###", "###CcVV#######VVcC###", "#####ccVVVVVVVcc#####", "#######cccCccc#######", "#####################")
                .aisle("##########C##########", "####CccDDDDDDDccC####", "####CVV#######VVC####", "###cV###########Vc###", "###cV###########Vc###", "##cV#############Vc##", "###cV###########Vc###", "###cV###########Vc###", "####CVV#######VVC####", "####CccBBBBBBBccC####", "##########C##########")
                .aisle("#####C####C####C#####", "####cDDDDDDDDDDDc####", "###cV###########Vc###", "##cV#############Vc##", "##cV#############Vc##", "#cV###############Vc#", "##cV#############Vc##", "##cV#############Vc##", "###cV###########Vc###", "####cBBBBBBBBBBBc####", "#####C####C####C#####")
                .aisle("######C#######C######", "####cDDD##C##DDDc####", "###cV###VVVVV###Vc###", "##cV#############Vc##", "##cV#############Vc##", "#cV###############Vc#", "##cV#############Vc##", "##cV#############Vc##", "###cV###VVVVV###Vc###", "####cBBB##C##BBBc####", "######C#######C######")
                .aisle("#####################", "###cDDDC#####CDDDc###", "##cV###V#####V###Vc##", "#cV#####VVVVV#####Vc#", "#cV######VVV######Vc#", "cV#######VVV#######Vc", "#cV######VVV######Vc#", "#cV#####VVVVV#####Vc#", "##cV###V#####V###Vc##", "###cBBBC#####CBBBc###", "#####################")
                .aisle("#####################", "###cDD#########DDc###", "##cV##V#C###C#V##Vc##", "#cV####VC###CV####Vc#", "#cV#####V###V#####Vc#", "cV######V###V######Vc", "#cV#####V###V#####Vc#", "#cV####VC###CV####Vc#", "##cV##V#C###C#V##Vc##", "###cBB#########BBc###", "#####################")
                .aisle("#####################", "###cDD#########DDc###", "##cV##V###X###V##Vc##", "#cV####V#XCX#V####Vc#", "#cV####V#CCC#V####Vc#", "cV#####V#CCC#V#####Vc", "#cV####V#CCC#V####Vc#", "#cV####V#XCX#V####Vc#", "##cV##V###X###V##Vc##", "###cBB#########BBc###", "#####################")
                .aisle("####CC#########CC####", "###CDDC###X###CDDC###", "##CV##V##XCX##V##VC##", "#CV####V#CCC#V####VC#", "#CV####V#CCC#V####VC#", "CV#####V#CCC#V#####VC", "#CV####V#CCC#V####VC#", "#CV####V#CCC#V####VC#", "##CV##V##XCX##V##VC##", "###CBBC###X###CBBC###", "####CC#########CC####")
                .aisle("#####################", "###cDD#########DDc###", "##cV##V###X###V##Vc##", "#cV####V#XCX#V####Vc#", "#cV####V#CCC#V####Vc#", "cV#####V#CCC#V#####Vc", "#cV####V#CCC#V####Vc#", "#cV####V#XCX#V####Vc#", "##cV##V###X###V##Vc##", "###cBB#########BBc###", "#####################")
                .aisle("#####################", "###cDD#########DDc###", "##cV##V#C###C#V##Vc##", "#cV####VC###CV####Vc#", "#cV#####V###V#####Vc#", "cV######V###V######Vc", "#cV#####V###V#####Vc#", "#cV####VC###CV####Vc#", "##cV##V#C###C#V##Vc##", "###cBB#########BBc###", "#####################")
                .aisle("#####################", "###cDDDC#####CDDDc###", "##cV###V#####V###Vc##", "#cV#####VVVVV#####Vc#", "#cV######VVV######Vc#", "cV#######VVV#######Vc", "#cV######VVV######Vc#", "#cV#####VVVVV#####Vc#", "##cV###V#####V###Vc##", "###cBBBC#####CBBBc###", "#####################")
                .aisle("######C#######C######", "####cDDD##C##DDDc####", "###cV###VVVVV###Vc###", "##cV#############Vc##", "##cV#############Vc##", "#cV###############Vc#", "##cV#############Vc##", "##cV#############Vc##", "###cV###VVVVV###Vc###", "####cBBB##C##BBBc####", "######C#######C######")
                .aisle("#####C####C####C#####", "####cDDDDDDDDDDDc####", "###cV###########Vc###", "##cV#############Vc##", "##cV#############Vc##", "#cV###############Vc#", "##cV#############Vc##", "##cV#############Vc##", "###cV###########Vc###", "####cBBBBBBBBBBBc####", "#####C####C####C#####")
                .aisle("##########C##########", "####CccDDDDDDDccC####", "####CVV#######VVC####", "###cV###########Vc###", "###cV###########Vc###", "##cV#############Vc##", "###cV###########Vc###", "###cV###########Vc###", "####CVV#######VVC####", "####CccBBBBBBBccC####", "##########C##########")
                .aisle("#####################", "#######cccCccc#######", "#####ccVVVVVVVcc#####", "###CcVV#######VVcC###", "###CcVV#######VVcC###", "###CV###########VC###", "###CcVV#######VVcC###", "###CcVV#######VVcC###", "#####ccVVVVVVVcc#####", "#######cccCccc#######", "#####################")
                .aisle("#####################", "#####################", "#######cccCccc#######", "#####ccVVVVVVVcc#####", "#####ccVVVVVVVcc#####", "####cVV#######VVc####", "#####ccVVVVVVVcc#####", "#####ccVVVVVVVcc#####", "#######cccCccc#######", "#####################", "#####################")
                .aisle("#####################", "#####################", "#####################", "#######cccCccc#######", "#######cccCccc#######", "#####ccVVVVVVVcc#####", "#######cccCccc#######", "#######cccCccc#######", "#####################", "#####################", "#####################")
                .aisle("#####################", "#####################", "#####################", "#####################", "##########S##########", "#######cccCccc#######", "#####################", "#####################", "#####################", "#####################", "#####################")
                .where('S', this.selfPredicate()).where('#', (tile) -> true)
                .where('C', coilPredicate())
                .where('X', statePredicate(this.getCasingState()))
                .where('D', divertorPredicate().or(tilePredicate((state, tile) -> tile instanceof GAMetaTileEntityEnergyHatch)).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('V', vacuumPredicate().or(tilePredicate((state, tile) -> tile instanceof GAMetaTileEntityEnergyHatch)).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('c', cryostatPredicate().or(tilePredicate((state, tile) -> tile instanceof GAMetaTileEntityEnergyHatch)).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .where('B', statePredicate(GAMetaBlocks.FUSION_CASING.getState(GAFusionCasing.CasingType.FUSION_BLANKET)).or(tilePredicate((state, tile) -> tile instanceof GAMetaTileEntityEnergyHatch)).or(abilityPartPredicate(ALLOWED_ABILITIES)))
                .setAmountAtMost('E', 16)
                .where('E', tilePredicate((state, tile) -> tile instanceof GAMetaTileEntityEnergyHatch))
                .setAmountAtMost('I', 3)
                .where('I', abilityPartPredicate(MultiblockAbility.IMPORT_FLUIDS)).setAmountAtMost('i', 2)
                .where('i', abilityPartPredicate(MultiblockAbility.EXPORT_FLUIDS))
                .build();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(MetaTileEntityHolder metaTileEntityHolder) {
        return new MetaTileEntityTokamak(metaTileEntityId);
    }

    private IBlockState getCasingState() {
        return GAMetaBlocks.FUSION_CASING.getState(GAFusionCasing.CasingType.ADV_FUSION_CASING);
    }
}*/
