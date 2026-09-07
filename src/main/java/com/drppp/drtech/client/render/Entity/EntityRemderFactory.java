package com.drppp.drtech.Client.render.Entity;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class EntityRemderFactory<E extends Entity> implements IRenderFactory<E>
{
    private final Class<? extends Render<E>> renderClass;

    public EntityRemderFactory(Class<? extends Render<E>> renderClass)
    {
        this.renderClass = renderClass;
    }

    @Override
    public Render<E> createRenderFor(RenderManager manager)
    {
        try
        {
            return renderClass.getConstructor(RenderManager.class).newInstance(manager);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}