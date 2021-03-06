package org.zimmob.zimlx.config;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.view.View;

import org.zimmob.zimlx.R;
import org.zimmob.zimlx.util.AppSettings;

/**
 * Created by saul on 05-06-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */
public class Config {
    //App Drawer Mode
    public static final int DRAWER_HORIZONTAL = 0;
    public static final int DRAWER_VERTICAL = 1;

    //Folder Shape
    public static final int FOLDER_SHAPE_CIRCLE = 0;
    public static final int FOLDER_SHAPE_CIRCLE_SHADOW = 1;
    public static final int FOLDER_SHAPE_SQUARE = 2;
    public static final int FOLDER_SHAPE_SQUARE_SHADOW = 3;

    //SORT MODE
    public static final String APP_SORT_AZ="az";
    public static final String APP_SORT_ZA="za";
    public static final String APP_SORT_LI="li";//last installer
    public static final String APP_SORT_MU="mu";//most used

    //INDICATOR MODE
    public static final int INDICATOR_DOTS= 0;
    public static final int INDICATOR_ARROW = 1;
    public static final int INDICATOR_LINE = 2;

    public static final int ACTION_LAUNCHER = 8;
    public static final int NO_SCALE = -1;

    // separates a list of integers
    public static final String INT_SEP = "#";

    // don't change the order, index is saved into db!
    public enum ItemPosition {
        Dock,
        Desktop
    }

    // don't change the order, index is saved into db!
    public enum ItemState {
        Hidden,
        Visible
    }

    public enum PeekDirection {
        UP, LEFT, RIGHT, DOWN
    }

    // doesn't work reliably yet
    public static final boolean ENABLE_ITEM_TOUCH_LISTENER = false;


    public static int boundToRange(int value, int lowerBound, int upperBound) {
        return Math.max(lowerBound, Math.min(value, upperBound));
    }

}
