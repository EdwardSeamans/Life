package com.life;

public class GlobalSettings {
    public static final int COLUMNS = 300;                                                          //User Window, field
    public static final int ROWS = 300;                                                             //User Window, field
    public static final int INITIAL_POPULATION_PERCENT = 5;                                         //User Window, ?
    public static final int[] SURVIVE = {2,3};                                                      //User Window, use checkboxes
    public static final int[] BIRTH = {3,6};                                                        //User Window, use checkboxes
    public static final int MAX_HISTORY = 2000;                                                     //properties
    public static final int BYTES_PER_PIXEL = 3;                                                    //properties
    public static final long TARGET_FRAME_RATE = 110;                                               //properties
    public static final long MILLISECONDS_PER_SECOND = 1000L;                                       //properties
    public static final long TARGET_FRAME_INTERVAL = MILLISECONDS_PER_SECOND/TARGET_FRAME_RATE;     //calculate in config java file
}

//TODO: Refactor graphics to be scalable
//TODO: Customize colors
//TODO: Flatten 2D State Grid to Array
//TODO: Create threaded Logger
//TODO: Save and name seed
