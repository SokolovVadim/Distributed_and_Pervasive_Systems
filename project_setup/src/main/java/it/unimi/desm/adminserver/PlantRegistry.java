package it.unimi.desm.adminserver;

import it.unimi.desm.common.MyLock;
import it.unimi.desm.common.PlantInfo;

import java.util.*;

public class PlantRegistry {

    private final MyLock lock = new MyLock();
    private final Map<String, PlantInfo> plants = new HashMap<>();


    // Registers a new plant if ID is unique
    // @return list of all plants, including the newly created, after registratiom
    public List<PlantInfo> registerPlant(PlantInfo plant) throws IllegalArgumentException {
        lock.lock();
        try {
            if (plants.containsKey(plant.getId())) {
                throw new IllegalArgumentException("Plant ID already exists: " + plant.getId());
            }
            plants.put(plant.getId(), plant);
            return new ArrayList<>(plants.values());
        } finally {
            lock.unlock();
        }
    }

    public List<PlantInfo> listPlants() {
        lock.lock();
        try {
            return new ArrayList<>(plants.values());
        } finally {
            lock.unlock();
        }
    }
}
