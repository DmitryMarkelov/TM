package com.tagmarshal.golf.manager;

import androidx.collection.ArrayMap;
import android.util.Log;

import com.tagmarshal.golf.R;
import com.tagmarshal.golf.rest.model.RestHoleModel;

import java.util.Arrays;
import java.util.List;

public class GameManager {

    private static final List<Integer> holesYellowIcons = Arrays.asList(
            R.drawable.flag1,
            R.drawable.flag2,
            R.drawable.flag3,
            R.drawable.flag4,
            R.drawable.flag5,
            R.drawable.flag6,
            R.drawable.flag7,
            R.drawable.flag8,
            R.drawable.flag9,
            R.drawable.flag10,
            R.drawable.flag11,
            R.drawable.flag12,
            R.drawable.flag13,
            R.drawable.flag14,
            R.drawable.flag15,
            R.drawable.flag16,
            R.drawable.flag17,
            R.drawable.flag18
    );
    private static final List<Integer> holesBlueIcons = Arrays.asList(
            R.drawable.flag1_blue,
            R.drawable.flag2_blue,
            R.drawable.flag3_blue,
            R.drawable.flag4_blue,
            R.drawable.flag5_blue,
            R.drawable.flag6_blue,
            R.drawable.flag7_blue,
            R.drawable.flag8_blue,
            R.drawable.flag9_blue,
            R.drawable.flag10_blue,
            R.drawable.flag11_blue,
            R.drawable.flag12_blue,
            R.drawable.flag13_blue,
            R.drawable.flag14_blue,
            R.drawable.flag15_blue,
            R.drawable.flag16_blue,
            R.drawable.flag17_blue,
            R.drawable.flag18_blue
    );
    private static final List<Integer> holesRedIcons = Arrays.asList(
            R.drawable.flag1_red,
            R.drawable.flag2_red,
            R.drawable.flag3_red,
            R.drawable.flag4_red,
            R.drawable.flag5_red,
            R.drawable.flag6_red,
            R.drawable.flag7_red,
            R.drawable.flag8_red,
            R.drawable.flag9_red,
            R.drawable.flag10_red,
            R.drawable.flag11_red,
            R.drawable.flag12_red,
            R.drawable.flag13_red,
            R.drawable.flag14_red,
            R.drawable.flag15_red,
            R.drawable.flag16_red,
            R.drawable.flag17_red,
            R.drawable.flag18_red
    );
    private static final List<Integer> holesGreenIcons = Arrays.asList(
            R.drawable.flag1_green,
            R.drawable.flag2_green,
            R.drawable.flag3_green,
            R.drawable.flag4_green,
            R.drawable.flag5_green,
            R.drawable.flag6_green,
            R.drawable.flag7_green,
            R.drawable.flag8_green,
            R.drawable.flag9_green,
            R.drawable.flag10_green,
            R.drawable.flag11_green,
            R.drawable.flag12_green,
            R.drawable.flag13_green,
            R.drawable.flag14_green,
            R.drawable.flag15_green,
            R.drawable.flag16_green,
            R.drawable.flag17_green,
            R.drawable.flag18_green
    );

    private static List<RestHoleModel> holes;
    private static int currentPlayHole = 0;
    public static int currentHole = 1;
    private static String TAG ="GAME MANAGER";

    public static List<Integer> getHolesYellowIcons() {
        return holesYellowIcons;
    }

    public static List<Integer> getHolesBlueIcons() {
        return holesBlueIcons;
    }

    public static List<Integer> getHolesRedIcons() {
        return holesRedIcons;
    }

    public static List<Integer> getHolesGreenIcons() {
        return holesGreenIcons;
    }

    private static ArrayMap<String, Integer> indexHolesMap = new ArrayMap<>();


    public static int getStartHole() {
        return PreferenceManager.getInstance().getStartHole();
    }

    public static void setCurrentPlayHole(int hole) {
        currentPlayHole = hole;
        if (hole > 0) {
            PreferenceManager.getInstance().setCurrentPlayHole(String.valueOf(hole));
        }
    }

    public static int getCurrentPlayHole() {
        return currentPlayHole == 0 ? 1 : currentPlayHole;
    }

    public static void setHoles(List<RestHoleModel> inHoles) {
        PreferenceManager.getInstance().setCourseHoles(inHoles);
        holes = inHoles;

        indexHolesMap.clear();

        for (int i = 0; i < holes.size(); i++) {
            String id = (holes.get(i).getColor() + holes.get(i).getHole()).trim().toLowerCase();
            indexHolesMap.put(id, i);
        }
    }

    public static int getHoleIndexByColor(String holeId) {
        holeId = holeId.trim().toLowerCase();
        Integer holeIndex = indexHolesMap.get(holeId);
        if (indexHolesMap != null && indexHolesMap.size() > 0 && holeIndex != null) {
            return holeIndex;
        } else {
            return 1;
        }

    }

    public static RestHoleModel getHole(int hole) {
        Log.i(TAG, "getHole: hole number = " + hole);
//        if(PreferenceManager.getInstance().getHoles() != null) {
//            for (RestHoleModel item:
//                    PreferenceManager.getInstance().getHoles()){
//                Log.i(TAG, "all Hole list" + item.toString());
//            }
//        }
        if (PreferenceManager.getInstance().getHoles() != null && !PreferenceManager.getInstance().getHoles().isEmpty() && PreferenceManager.getInstance().getHoles().size() >=  hole){

            Log.i(TAG, "getHole: = true");
            Log.i(TAG, "getHole: returning value"  + PreferenceManager.getInstance().getHoles().get(hole - 1));
            return PreferenceManager.getInstance().getHoles().get(hole - 1);
        }else{
            if(PreferenceManager.getInstance().getHoles() == null){

                Log.i(TAG, "getHole: holes==null");
            }else{
                Log.i(TAG, "getHole: holes size= " + PreferenceManager.getInstance().getHoles().size());
            }
            return null;
        }
    }

    public static boolean isExistHoles() {
        if (PreferenceManager.getInstance().getHoles() == null || PreferenceManager.getInstance().getHoles().isEmpty())
            return false;
        else
            return true;
    }

    public static List<RestHoleModel> getHoles() {
        return PreferenceManager.getInstance().getHoles();
    }

    public static String getStartGameTime() {
        return PreferenceManager.getInstance().getCourseTeeStartTime();
    }
}
