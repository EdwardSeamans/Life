package com.life.configuration;

public class IterationSettings {
    public static final int COLUMNS = 1500;                      //Width - Width in Cells
    public static final int ROWS = 750;                         //Height - Height in Cells
    public static final int INITIAL_POPULATION_PERCENT = 47;    //Density - Percentage of cells initially alive
    public static final int[] SURVIVE = {2, 3, 6};                  //Survival - Number of live neighbors for a live cell to survive
    public static final int[] BIRTH = {3, 7};                      //Birth - Number of live neighbors for a dead cell to become alive
    public static final int SCALING_FACTOR = 1;                 //Size - A live cell appears as a square with this height and width
    public static final int BYTES_PER_PIXEL = 3;                //Current rendering uses three-byte color, constant
    public static final int SPEED_DIVISOR = 1;
}

//TODO: Explore functional approach to processing game array
//TODO: Quartz Exception seems to happen more often/sooner when scaled. Catch Exception and report iteration to confirm.
//TODO: Interface for settings
//TODO: Repeatable Runs Without Terminating
//TODO: Preview of World
//TODO: Cell Color (Brightness)? Shows Age, configurable

//TODO: History and Cycle-checks independently set CONTROL REQUIRED
//TODO: Cycle-checks can be turned on or off CONTROL REQUIRED
//TODO: Save and name seed CONTROL REQUIRED
//TODO: Save run seed and all settings CONTROL REQUIRED
