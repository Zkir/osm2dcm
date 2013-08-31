/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.zkir.mp2mp.taskextsimp;

import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author freeExec
 */
public class LabelStat {
    public String text;
    public int count;


    public LabelStat(String text, int count) {
        this.text = text;
        this.count = count;
    }

    //Get label of road from stats, combinatory version
    //Flags: 0 - default
    public static String getLabelByStats2(ArrayList<LabelStat> LabelStats) {
        String _rtn = "";

        //no labels in stats
        if (LabelStats.isEmpty()) { return _rtn; }

        //combine all labels from stats
        _rtn = LabelStats.get(0).text;
        //for (i = 1; i <= LabelStatsNum - 1; i++) {
        Iterator<LabelStat> iLabel = LabelStats.iterator();
        iLabel.next();
        for (; iLabel.hasNext();) {
            _rtn += "," + iLabel.next().text;
        }

        return _rtn;
    }

    //Add label by parts (for combinatory calc and so on)
    public static void addLabelStat2(String text, ArrayList<LabelStat> LabelStats) {
        int i = 0;
        String[] marks;
        if (text.length() < 1) { return; }
        //split string by delimiter into set of strings
        marks = text.split(",");
        for (i = 0; i < marks.length; i++) {
            addLabelStat1(marks[i], LabelStats);
        }
    }

    //Add label completely (for majoritary calc and so on)
    public static void addLabelStat1(String text, ArrayList<LabelStat> LabelStats) {
        int i = 0;
        //skip empty strings
        if (text.length() < 1) { return; }
        //for (i = 0; i <= LabelStatsNum - 1; i++) {
        for (Iterator<LabelStat> iLabel = LabelStats.iterator(); iLabel.hasNext();) {
            LabelStat LS = iLabel.next();
            if (LS.text.equals(text)) {
                //already present - increment count
                LS.count ++;
                return;
            }
        }
        //not present - add
        LabelStats.add(new LabelStat(text, 1));
        /*
        LabelStats[LabelStatsNum].text = text;
        LabelStats[LabelStatsNum]..count = 1;
        LabelStatsNum = LabelStatsNum + 1;
        if (LabelStatsNum >= LabelStatsAlloc) {
            //realloc if needed
            LabelStatsAlloc = LabelStatsAlloc * 2;
            G.redimPreserve(LabelStats, LabelStatsAlloc);
        }*/
    }
}
