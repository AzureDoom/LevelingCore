package com.azuredoom.hyleveling;

import com.azuredoom.hyleveling.database.H2LevelRepository;
import com.azuredoom.hyleveling.level.ExponentialLevelFormula;
import com.azuredoom.hyleveling.level.LevelServiceImpl;

import java.util.UUID;

public class Main {

    private static final System.Logger LOGGER = System.getLogger(Main.class.getName());

    static void main() {
        var formula = new ExponentialLevelFormula(100, 1.7);
        var repository = new H2LevelRepository("./data/hyleveling");
        var levelService = new LevelServiceImpl(formula, repository);
        var testId = UUID.fromString("d3804858-4bb8-4026-ae21-386255ed467d");

        levelService.addXp(testId, 500);

        LOGGER.log(System.Logger.Level.INFO, String.format("XP: %d", levelService.getXp(testId)));
        LOGGER.log(System.Logger.Level.INFO, String.format("Level: %d", levelService.getLevel(testId)));

        Runtime.getRuntime().addShutdownHook(new Thread(repository::close));
    }
}
