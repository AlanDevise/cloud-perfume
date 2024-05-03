package com.alandevise;

import com.alandevise.GeneralServer.Task.TimerTask;

import java.util.Timer;

public class TimerTest {

    public static void main(String[] args) {
        Timer timer = new Timer();
        TimerTask noticeTask = new TimerTask();
        timer.schedule(noticeTask,0,1000);
        // timer.cancel();
        // System.out.println("结束");
    }

}