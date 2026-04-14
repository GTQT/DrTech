package com.drppp.drtech.api.vein;


import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.capabilities.Capability.IStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 将 {@link ChunkVeinData} 以 Capability 形式挂载到 Chunk。
 *
 * <h3>注册</h3>
 * 在 FMLPreInitializationEvent 中调用：
 * <pre>
 *   CapabilityManager.INSTANCE.register(
 *       IVeinDataCapability.class,
 *       new VeinDataCapability.Storage(),
 *       VeinDataCapability.Implementation::new
 *   );
 * </pre>
 *
 * <h3>绑定到 Chunk</h3>
 * 在 {@link net.minecraftforge.event.AttachCapabilitiesEvent} 监听器（泛型为 Chunk）中：
 * <pre>
 *   event.addCapability(KEY, new VeinDataCapability.Provider());
 * </pre>
 */
public final class VeinDataCapability {

    /** Capability 的唯一 ResourceLocation，用于 AttachCapabilitiesEvent */
    public static final ResourceLocation KEY =
            new ResourceLocation("yourmod", "vein_data");

    /** Forge 注册句柄，通过 @CapabilityInject 注入 */
    @CapabilityInject(IVeinDataCapability.class)
    public static Capability<IVeinDataCapability> CAP = null;

    private VeinDataCapability() {}

    // ── 接口 ────────────────────────────────────────────────────

    public interface IVeinDataCapability {
        ChunkVeinData getData();
    }

    // ── 默认实现 ─────────────────────────────────────────────────

    public static final class Implementation implements IVeinDataCapability {
        private final ChunkVeinData data = new ChunkVeinData();

        @Override
        public ChunkVeinData getData() { return data; }
    }

    // ── NBT Storage ──────────────────────────────────────────────

    public static final class Storage implements IStorage<IVeinDataCapability> {

        @Nullable
        @Override
        public NBTBase writeNBT(Capability<IVeinDataCapability> capability,
                                IVeinDataCapability instance, EnumFacing side) {
            NBTTagCompound tag = new NBTTagCompound();
            instance.getData().writeToNBT(tag);
            return tag;
        }

        @Override
        public void readNBT(Capability<IVeinDataCapability> capability,
                            IVeinDataCapability instance, EnumFacing side, NBTBase nbt) {
            if (nbt instanceof NBTTagCompound) {
                instance.getData().readFromNBT((NBTTagCompound) nbt);
            }
        }
    }

    // ── Provider (挂载到 Chunk) ──────────────────────────────────

    public static final class Provider implements ICapabilitySerializable<NBTTagCompound> {

        private final IVeinDataCapability impl = new Implementation();

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == CAP;
        }

        @Override
        @Nullable
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == CAP ? CAP.cast(impl) : null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            impl.getData().writeToNBT(tag);
            return tag;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            impl.getData().readFromNBT(nbt);
        }
    }
}
