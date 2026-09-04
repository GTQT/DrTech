package com.drppp.drtech.lootgames.api.task;

import java.util.HashMap;
import java.util.Map;

public class TaskRegistry {
    private static final Map<String, Class<? extends ITask>> TASKS = new HashMap<>();

    public static void registerTask(Class<? extends ITask> taskClass) {
        TASKS.put(taskClass.getName(), taskClass);
    }

    public static Class<? extends ITask> getTaskClass(String name) {
        return TASKS.get(name);
    }
}
