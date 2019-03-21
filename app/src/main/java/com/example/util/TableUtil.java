package com.example.util;


import com.example.pojo.FishInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TableUtil {
    private String[][] data;

    public TableUtil() {
        data = new String[9][11];
        for (String[] line : data) {
            Arrays.fill(line, "");
        }
    }

    public void addChar(int x, int width, int row, String str) {
        int col = getCol(x, width);
        if (col == -1) {
            return;
        }
        data[row][col] += str;
    }

    public List<FishInfo> getFishInfoList() {
        List<FishInfo> fishInfoList = new ArrayList<>();
        for (String[] line : data) {
            FishInfo fishInfo = getFishInfo(line);
            fishInfoList.add(fishInfo);
        }
        return fishInfoList;
    }


    private FishInfo getFishInfo(String[] line) {
        FishInfo fishInfo = new FishInfo();
        fishInfo.setDate(new Date());
        fishInfo.setPoolId(line[0]);
        fishInfo.setFeedQuantity1(line[1]);
        fishInfo.setFeedQuantity2(line[2]);
        fishInfo.setFeedQuantity3(line[3]);
        fishInfo.setFeedQuantity4(line[4]);
        fishInfo.setWaterQuantity(line[5]);
        fishInfo.setPhAM(line[6]);
        fishInfo.setPhPM(line[7]);
        fishInfo.setNh4n(line[8]);
        fishInfo.setNano2(line[9]);
        fishInfo.setMedicationRecord(line[10]);
        return fishInfo;
    }

    private int getCol(int x, int width) {
        if (x >= 0 && x + width < 60) {
            return 0;
        } else if (x >= 60 && x + width < 146) {
            return 1;
        } else if (x >= 146 && x + width < 233) {
            return 2;
        } else if (x >= 233 && x + width < 319) {
            return 3;
        } else if (x >= 319 && x + width < 406) {
            return 4;
        } else if (x >= 406 && x + width < 492) {
            return 5;
        } else if (x >= 492 && x + width < 578) {
            return 6;
        } else if (x >= 578 && x + width < 665) {
            return 7;
        } else if (x >= 665 && x + width < 751) {
            return 8;
        } else if (x >= 751 && x + width < 838) {
            return 9;
        } else if (x >= 838 && x + width < 1500) {
            return 10;
        }
        return -1;
    }


}