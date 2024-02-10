package edu.ucsd.cse110.successorator.lib.domain;

import java.util.Calendar;
import java.util.Date;

public class MockCalendar extends Calendar {
    private long time;
    public MockCalendar(long time) {
        this.time = time;
    }

    @Override
    public long getTimeInMillis() {
        return time;
    }

    public void advanceTime(long time) {
        this.time += time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void reverseTime(long time) {
        this.time -= time;
    }

    // Stub methods do nothing
    @Override
    protected void computeTime() {

    }

    @Override
    protected void computeFields() {

    }

    @Override
    public void add(int i, int i1) {

    }

    @Override
    public void roll(int i, boolean b) {

    }

    @Override
    public int getMinimum(int i) {
        return 0;
    }

    @Override
    public int getMaximum(int i) {
        return 0;
    }

    @Override
    public int getGreatestMinimum(int i) {
        return 0;
    }

    @Override
    public int getLeastMaximum(int i) {
        return 0;
    }
}
