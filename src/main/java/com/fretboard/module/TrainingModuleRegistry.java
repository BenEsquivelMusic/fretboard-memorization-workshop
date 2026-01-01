package com.fretboard.module;

import java.util.*;

/**
 * Registry for managing training modules in the application.
 * Training modules can be registered and retrieved from this central registry.
 */
public class TrainingModuleRegistry {

    private static TrainingModuleRegistry instance;

    private final Map<String, TrainingModule> modules;
    private final List<String> moduleOrder;

    private TrainingModuleRegistry() {
        this.modules = new LinkedHashMap<>();
        this.moduleOrder = new ArrayList<>();
    }

    /**
     * Gets the singleton instance of the TrainingModuleRegistry.
     *
     * @return the TrainingModuleRegistry instance
     */
    public static synchronized TrainingModuleRegistry getInstance() {
        if (instance == null) {
            instance = new TrainingModuleRegistry();
        }
        return instance;
    }

    /**
     * Registers a training module.
     *
     * @param module the module to register
     * @throws IllegalArgumentException if a module with the same ID is already registered
     */
    public void registerModule(TrainingModule module) {
        String moduleId = module.getModuleId();
        if (modules.containsKey(moduleId)) {
            throw new IllegalArgumentException(
                    "Module with ID '" + moduleId + "' is already registered");
        }
        modules.put(moduleId, module);
        moduleOrder.add(moduleId);
    }

    /**
     * Unregisters a training module.
     *
     * @param moduleId the ID of the module to unregister
     * @return the unregistered module, or null if not found
     */
    public TrainingModule unregisterModule(String moduleId) {
        moduleOrder.remove(moduleId);
        TrainingModule module = modules.remove(moduleId);
        if (module != null) {
            module.cleanup();
        }
        return module;
    }

    /**
     * Gets a training module by ID.
     *
     * @param moduleId the module ID
     * @return an Optional containing the module, or empty if not found
     */
    public Optional<TrainingModule> getModule(String moduleId) {
        return Optional.ofNullable(modules.get(moduleId));
    }

    /**
     * Gets all registered modules in registration order.
     *
     * @return an unmodifiable list of modules
     */
    public List<TrainingModule> getAllModules() {
        List<TrainingModule> result = new ArrayList<>();
        for (String id : moduleOrder) {
            result.add(modules.get(id));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Gets the IDs of all registered modules.
     *
     * @return an unmodifiable list of module IDs
     */
    public List<String> getModuleIds() {
        return Collections.unmodifiableList(new ArrayList<>(moduleOrder));
    }

    /**
     * Gets the number of registered modules.
     *
     * @return the module count
     */
    public int getModuleCount() {
        return modules.size();
    }

    /**
     * Checks if a module is registered.
     *
     * @param moduleId the module ID
     * @return true if the module is registered
     */
    public boolean isModuleRegistered(String moduleId) {
        return modules.containsKey(moduleId);
    }

    /**
     * Clears all registered modules.
     */
    public void clear() {
        for (TrainingModule module : modules.values()) {
            module.cleanup();
        }
        modules.clear();
        moduleOrder.clear();
    }
}
