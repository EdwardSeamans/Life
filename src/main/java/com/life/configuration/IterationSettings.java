package com.life.configuration;

public class IterationSettings {
    public static final int COLUMNS = 100;                      //Width - Width in Cells
    public static final int ROWS = 100;                         //Height - Height in Cells
    public static final int INITIAL_POPULATION_PERCENT = 40;    //Density - Percentage of cells initially alive
    public static final int[] SURVIVE = {2,3};                  //Survival - Number of live neighbors for a live cell to survive
    public static final int[] BIRTH = {3};                      //Birth - Number of live neighbors for a dead cell to become alive
    public static final int MAX_HISTORY = 10000;                 //Number of generations saved in history and/or checked for cycles
    public static final int SCALING_FACTOR = 8;                 //Size - A live cell appears as a square with this height and width

    public static final byte[] GREEN = {0, -1, 0};              //Will be live cell color, need to work out
    public static final byte[] BLACK = {0,0,0};                 //Will be dead cell color, need to work out

    public static final long TARGET_FRAME_RATE = 106;           //Target Frame Rate, need to find formula, should automate to fastest unless set

    public static final long MILLISECONDS_PER_SECOND = 1000L;
    public static final int BYTES_PER_PIXEL = 3;                //Current rendering uses three-byte color, constant
    public static final long TARGET_FRAME_INTERVAL = MILLISECONDS_PER_SECOND/TARGET_FRAME_RATE;
}

//TODO: Create threaded Logger
//TODO: Interface for settings
//TODO: Repeatable Runs Without Terminating
//TODO: Preview of World
//TODO: Cell Color (Brightness)? Shows Age, configurable

//TODO: History and Cycle-checks independently set CONTROL REQUIRED
//TODO: History can be turned on or off CONTROL REQUIRED
//TODO: Cycle-checks can be turned on or off CONTROL REQUIRED
//TODO: Save and name seed CONTROL REQUIRED
//TODO: Save run seed and all settings CONTROL REQUIRED
