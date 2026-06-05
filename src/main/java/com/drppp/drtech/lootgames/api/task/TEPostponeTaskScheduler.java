package com.drppp.drtech.lootgames.api.task;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TEPostponeTaskScheduler implements INBTSerializable<NBTTagList> {
    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger("LootGames");

    private final List<TaskWrapper> tasks = new ArrayList<>();
    private final TileEntity tileEntity;

    public TEPostponeTaskScheduler(TileEntity tileEntity) {
        this.tileEntity = tileEntity;
    }

    public void addTask(ITask task, int timeBeforeStart) {
        synchronized (tasks) {
            tasks.add(new TaskWrapper(timeBeforeStart, task));
        }
    }

    public void onUpdate() {
        synchronized (tasks) {
            Iterator<TaskWrapper> iterator = tasks.iterator();
            while (iterator.hasNext()) {
                TaskWrapper task = iterator.next();
                if (task.timeBeforeStart <= 0) {
                    task.run(tileEntity.getWorld());
                    iterator.remove();
                } else {
                    task.decreaseTimer();
                }
            }
        }
    }

    @Override
    public NBTTagList serializeNBT() {
        NBTTagList out = new NBTTagList();
        synchronized (tasks) {
            for (TaskWrapper wrapper : tasks) {
                NBTTagCompound element = new NBTTagCompound();
                element.setTag("task", wrapper.task.serializeNBT());
                element.setInteger("time", wrapper.timeBeforeStart);
                element.setString("name", wrapper.task.getClass().getName());
                out.appendTag(element);
            }
        }
        return out;
    }

    @Override
    public void deserializeNBT(NBTTagList nbt) {
        for (int i = 0; i < nbt.tagCount(); i++) {
            NBTTagCompound c = nbt.getCompoundTagAt(i);
            int time = c.getInteger("time");
            Class<? extends ITask> taskClass = TaskRegistry.getTaskClass(c.getString("name"));
            if (taskClass == null) {
                LOGGER.error("Task class {} not registered, skipping.", c.getString("name"));
                continue;
            }
            try {
                ITask task = taskClass.newInstance();
                task.deserializeNBT(c.getCompoundTag("task"));
                tasks.add(new TaskWrapper(time, task));
            } catch (InstantiationException | IllegalAccessException e) {
                LOGGER.error("Failed to create task {} from save.", taskClass, e);
            }
        }
    }

    private static class TaskWrapper {
        private int timeBeforeStart;
        private final ITask task;

        private TaskWrapper(int timeBeforeStart, ITask task) {
            this.timeBeforeStart = timeBeforeStart;
            this.task = task;
        }

        private void decreaseTimer() {
            timeBeforeStart--;
        }

        private void run(World world) {
            task.run(world);
        }
    }
}
