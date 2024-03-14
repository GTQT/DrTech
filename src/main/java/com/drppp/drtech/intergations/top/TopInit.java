package com.drppp.drtech.intergations.top;

import com.drppp.drtech.intergations.top.provider.NuclearInfoProvider;
import com.drppp.drtech.intergations.top.provider.TopCommonProvider;
import com.drppp.drtech.intergations.top.provider.YotTankProvider;
import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.api.ITheOneProbe;



public class TopInit {
    public static void init() {
        ITheOneProbe oneProbe = TheOneProbe.theOneProbeImp;
        oneProbe.registerProvider(new NuclearInfoProvider());
        oneProbe.registerProvider(new YotTankProvider());
        oneProbe.registerProvider(new TopCommonProvider());

    }
}
