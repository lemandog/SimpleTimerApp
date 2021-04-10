package com.company;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

import static com.company.Main.*;

public class Main {
public static boolean beepSet = false;

static int hours = 0;
static int minutes = 0;
static int seconds = 0;

static boolean permitRun = false;
static int timerSwitchID; //Selector

static public JLabel time; //Main timer
static public JLabel selected;
static public LocalTime mainClock;
static public LocalTime selClock;
static public Thread sound;
static public DateTimeFormatter sdfF = DateTimeFormatter.ofPattern("HH:mm:ss").withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault()); //format standard

    public static void main(String[] args) {
        JFrame main = new JFrame("TIMER");
        main.setSize(400,300);
        main.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel mainPan = new JPanel();
        JPanel timerContrl = new JPanel();
        JPanel selector = new JPanel();

        main.add(mainPan);
        main.add(timerContrl);
        main.add(selector);

        mainClock = LocalTime.now();
        main.setResizable(false);
        time = new JLabel();
        time.setFont(new Font("Times New Roman",Font.BOLD, 48));

        JCheckBox beeper = new JCheckBox("Play sound when time is up");
        MyListener beepListener = new MyListener(0);
        beeper.addActionListener(beepListener);

        JButton starter = new JButton("START");
        MyListener startListener = new MyListener(1);
        starter.addActionListener(startListener);

        JButton stop = new JButton("STOP");
        MyListener stopListener = new MyListener(2);
        stop.addActionListener(stopListener);

        JButton add = new JButton("+");
        MyListener addListener = new MyListener(3);
        add.addActionListener(addListener);

        JButton decr = new JButton("-");
        MyListener decrListener = new MyListener(4);
        decr.addActionListener(decrListener);

        JButton hrSel = new JButton("S SEL");
        MyListener hrSelListener = new MyListener(50); //SEC
        hrSel.addActionListener(hrSelListener);

        JButton minSel = new JButton("M SEL");
        MyListener minSelListener = new MyListener(51);//MIN
        minSel.addActionListener(minSelListener);

        JButton secSel = new JButton("HR SEL");
        MyListener secSelListener = new MyListener(52);//HR
        secSel.addActionListener(secSelListener);

        selected = new JLabel();

        selector.add(hrSel);
        selector.add(minSel);
        selector.add(secSel);
        selector.add(selected);

        mainPan.add(time);

        mainPan.add(beeper);
        timerContrl.add(starter);
        timerContrl.add(stop);
        timerContrl.add(add);
        timerContrl.add(decr);

        selector.setLayout(new GridLayout(1,4));
        timerContrl.setLayout(new GridLayout(1,4));
        mainPan.setLayout(new GridLayout(1,2));
        main.setLayout(new GridLayout(3,1));

        mainClock = LocalTime.now(); //Reference
        selClock = mainClock = LocalTime.now(); //Time to which we count

        selClock = selClock.plusHours(hours);
        selClock = selClock.plusMinutes(minutes);
        selClock = selClock.plusSeconds(seconds);

        hrSel.doClick();

        LocalTime remains = LocalTime.ofNanoOfDay(Duration.between(mainClock,selClock).toNanos());
        time.setText(sdfF.format(remains));
        main.setVisible(true);
    }

    public static synchronized void timeStart() {
        Thread timerAction = new Thread(() -> {
        time.setForeground(Color.BLACK);
        mainClock = LocalTime.now(); //Reference
        selClock = mainClock = LocalTime.now(); //Time to which we count

        selClock = selClock.plusHours(hours);
        selClock = selClock.plusMinutes(minutes);
        selClock = selClock.plusSeconds(seconds);

        long refreshCooldown = System.currentTimeMillis();

        while (mainClock.isBefore(selClock) && permitRun){
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored){}
            mainClock = LocalTime.now(); //Time from which we count
            while (System.currentTimeMillis() - refreshCooldown > 1000){
                refreshCooldown = System.currentTimeMillis();
                try{ //Sometimes too much time passes after while loop check
                LocalTime remains = LocalTime.ofNanoOfDay(Duration.between(mainClock,selClock).toNanos());
                time.setText(sdfF.format(remains));
                time.updateUI();}
                catch (Exception ignore){} //Donut care
            }
        }
            if (beepSet && permitRun){playSound();}
            time.setForeground(Color.RED);
        });
        timerAction.start();
    }

    public static synchronized void playSound() {
        sound = new Thread(() -> {
            try {
                Clip clip = AudioSystem.getClip();
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                        Objects.requireNonNull(Main.class.getResourceAsStream("beep2.wav")));
                clip.open(inputStream);
                clip.start();
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        });
        sound.start();
    }

    public static void addtime() {
        switch (timerSwitchID) {
            case (50) -> { //SECONDS SELECT
                if (seconds < 59) {
                    seconds++;
                } else {
                    seconds = 0;
                    minutes++;
                }
                //UPDATE CLOCKS DIFF
            }
            case (51) -> { //MINUTES SELECT
                if (minutes < 59) {
                    minutes++;
                } else {
                    minutes = 0;
                    hours++;
                }
                //UPDATE CLOCKS DIFF
            }
            case (52) -> { //HOURS SELECT
                if (hours < 100) {
                    hours++;
                }
                //UPDATE CLOCKS DIFF
            }
        }
        mainClock = LocalTime.now(); //Reference
        selClock = mainClock = LocalTime.now(); //Time to which we count

        selClock = selClock.plusHours(hours);
        selClock = selClock.plusMinutes(minutes);
        selClock = selClock.plusSeconds(seconds);
        LocalTime remains = LocalTime.ofNanoOfDay(Duration.between(mainClock,selClock).toNanos());
        time.setText(sdfF.format(remains));
    }

    public static void mintime() {
        switch (timerSwitchID) {
            case (50) -> { //SECONDS SELECT
                if (seconds > 0) {
                    seconds--;
                }
            }
            case (51) -> { //MINUTES SELECT
                if (minutes > 0) {
                    minutes--;
                }
            }
            case (52) -> { //HOURS SELECT
                if (hours > 0) {
                    hours--;
                }
            }
        }
        mainClock = LocalTime.now(); //Reference
        selClock = mainClock = LocalTime.now(); //Time to which we count

        selClock = selClock.plusHours(hours);
        selClock = selClock.plusMinutes(minutes);
        selClock = selClock.plusSeconds(seconds);
        LocalTime remains = LocalTime.ofNanoOfDay(Duration.between(mainClock,selClock).toNanos());
        time.setText(sdfF.format(remains));
    }
}

class MyListener implements ActionListener {
    private final int id;
    public MyListener(int SetId){
        id = SetId;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (id) {
            case (0) -> //BEEP SET
                    beepSet = !beepSet;
            case (1) -> {  //TIME START
                permitRun = true;
                Main.timeStart();
            }
            case (2) ->{ //TIME STOP
                    permitRun = false;
                    try{sound.stop();}
                    catch (Exception E){}
            }
            case (3) -> //TIME ADD
                    Main.addtime();
            case (4) -> //TIME DECREASE
                    Main.mintime();
            case (50) -> { //SECONDS SELECT
                selected.setText("SECONDS");
                timerSwitchID = 50;
            }
            case (51) -> { //MINUTES SELECT
                selected.setText("MINUTES");
                timerSwitchID = 51;
            }
            case (52) -> { //HOURS SELECT
                selected.setText("HOURS");
                timerSwitchID = 52;
            }
        }
    }

}