package com.company;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

import static com.company.Main.*;

public class Main {
public static boolean beepSet = false;
public static boolean powerOffFlag = false;

static int hours = 0;
static int minutes = 0;
static int seconds = 0;
static final float VER = 1.06f;

static boolean permitRun = false;
static int timerSwitchID; //Selector

static public JLabel time; //Main timer
static public JLabel selected;
static public LocalDateTime mainClock;
static public LocalDateTime selClock;
static public Thread sound;
static public DateTimeFormatter sdfF = DateTimeFormatter.ofPattern("HH:mm:ss").withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault()); //format standard
public static Font font2;
public static Font font1;

    static {
        try {
            InputStream is = Main.class.getResourceAsStream("pixRectv2.ttf");
            font1 = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(is)).deriveFont(Font.PLAIN,64);
            font2 = font1.deriveFont(Font.PLAIN,14);
        } catch (FontFormatException | IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        uiSetup();
    }

    public static synchronized void timeStart() {

        Thread timerAction = new Thread(() -> {
        time.setForeground(Color.WHITE); //Because it could be red is timer gone off previously
        long cooldown = System.currentTimeMillis();

        while (mainClock.isBefore(selClock) && permitRun){
            mainClock = LocalDateTime.now(); //Time from which we count
            while (System.currentTimeMillis() - cooldown > 1000){
                cooldown = System.currentTimeMillis();
                try{ //Sometimes too much time passes after while loop check
                    LocalTime remains = LocalTime.ofSecondOfDay(Duration.between(mainClock,selClock).toSeconds());
                time.setText(sdfF.format(remains));
                time.updateUI();}
                catch (Exception ignore){} //Donut care
            }
            try {Thread.sleep(100);} catch (InterruptedException ignored){} //Busy wait for less CPU time
        }
            if(powerOffFlag){powerOff();}
            if (beepSet && permitRun){playSound();}
            time.setForeground(Color.RED);
        });
        timerAction.start();
    }

    public static synchronized void playSound() {
        sound = new Thread(() -> {
            try {
                Clip clip = AudioSystem.getClip();
                URL soundURL = Main.class.getResource("beep2.wav");
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(Objects.requireNonNull(soundURL));
                clip.open(inputStream);
                clip.start();
                clip.drain();
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
                    if (hours < 23) {hours++;}
                }
                //UPDATE CLOCKS DIFF
            }
            case (51) -> { //MINUTES SELECT
                if (minutes < 59) {
                    minutes++;
                } else {
                    minutes = 0;
                    if (hours < 23) {hours++;}
                }
                //UPDATE CLOCKS DIFF
            }
            case (52) -> { //HOURS SELECT
                if (hours < 23) {
                    hours++;
                }
                //UPDATE CLOCKS DIFF
            }
        }
    updateClockUI();
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
        updateClockUI();
    }

    public static void uiSetup() {
        JFrame main = new JFrame("TIMER");
        main.setSize(450,300);
        main.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JPanel mainPan = new JPanel();
        JPanel timerContrl = new JPanel();
        JPanel selector = new JPanel();

        main.add(mainPan);
        main.add(timerContrl);
        main.add(selector);
        main.setIconImage((Toolkit.getDefaultToolkit().getImage(Main.class.getResource("timericon.png")))); //Setting icon from resources

        mainClock = LocalDateTime.now();
        main.setResizable(false);
        time = new JLabel();
        time.setFont(font1);
        time.setForeground(Color.WHITE);
        time.setHorizontalAlignment(SwingConstants.CENTER);

        JCheckBox beeper = new JCheckBox("BEEP");
        beeper.setFont(font2);
        MyListener beepListener = new MyListener("SET BEEP");
        beeper.addActionListener(beepListener);

        JCheckBox powerOff = new JCheckBox("POWEROFF");
        powerOff.setFont(font2);
        MyListener powerOffListener = new MyListener("POWER_OFF");
        powerOff.addActionListener(powerOffListener);

        JPanel checkBoxPanel = new JPanel(new GridLayout(2,1));
        checkBoxPanel.add(powerOff);
        checkBoxPanel.add(beeper);

        JButton starter = new JButton("START");
        starter.setFont(font2);
        MyListener startListener = new MyListener("START");
        starter.addActionListener(startListener);

        JButton stop = new JButton("STOP");
        stop.setFont(font2);
        MyListener stopListener = new MyListener("STOP");
        stop.addActionListener(stopListener);

        JButton add = new JButton("+");
        add.setFont(font2);
        MyListener addListener = new MyListener("PLUS");
        add.addActionListener(addListener);

        JButton decr = new JButton("-");
        decr.setFont(font2);
        MyListener decrListener = new MyListener("MINUS");
        decr.addActionListener(decrListener);

        JButton secSel = new JButton("S SEL");
        secSel.setFont(font2);
        MyListener secSelListener = new MyListener("SECONDS"); //SEC
        secSel.addActionListener(secSelListener);

        JButton minSel = new JButton("M SEL");
        minSel.setFont(font2);
        MyListener minSelListener = new MyListener("MINUTES");//MIN
        minSel.addActionListener(minSelListener);

        JButton hrSel = new JButton("HR SEL");
        hrSel.setFont(font2);
        MyListener hrSelListener = new MyListener("HOURS");//HR
        hrSel.addActionListener(hrSelListener);

        JButton resetSel = new JButton("RESET");
        resetSel.setFont(font2);
        MyListener resetSelListener = new MyListener("RESET");//HR
        resetSel.addActionListener(resetSelListener);

        selected = new JLabel();
        selected.setFont(font2);
        selected.setHorizontalAlignment(SwingConstants.CENTER); //Just looks pretty

        selector.add(hrSel);
        selector.add(minSel);
        selector.add(secSel);
        selector.add(resetSel);
        selector.add(selected);

        mainPan.add(time);
        mainPan.setBackground(Color.BLACK);

        timerContrl.add(starter);
        timerContrl.add(stop);
        timerContrl.add(add);
        timerContrl.add(decr);
        timerContrl.add(checkBoxPanel);

        selector.setLayout(new GridLayout(1,5));
        timerContrl.setLayout(new GridLayout(1,4));
        mainPan.setLayout(new GridLayout(1,2));
        main.setLayout(new GridLayout(3,1));

        updateClockUI();
        minSel.doClick();

        main.setVisible(true);

        main.addKeyListener(new KeyListener() {
            @Override public void keyTyped(KeyEvent e) {}
            @Override public void keyPressed(KeyEvent e) {
                System.out.println(e.getKeyCode());
                if (e.getKeyCode() == 112){constructInfoUI();}}
            @Override public void keyReleased(KeyEvent e) {}
        });
        main.setFocusable(true);
        main.requestFocus();
    }

    private static void constructInfoUI() {
        JFrame info = new JFrame("INFO");
        info.setLayout(new GridLayout(4,1));

        JLabel ver = new JLabel("Year of 2021, " + VER);
        ver.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel authorInfo = new JLabel("This little programm is made by Lemandog. You can find my page on GitHub");
        JLabel feedback = new JLabel("Also, you can send your feedback and bugs to alexo98@yandex.ru");
        JLabel thankyou = new JLabel("Thank you for downloading, I care more than I can show");

        info.add(ver);
        info.add(authorInfo);
        info.add(feedback);
        info.add(thankyou);

        info.pack();
        info.setResizable(false);
        info.setVisible(true);
    }

    static void updateClockUI() {
        mainClock = LocalDateTime.now(); //Reference
        selClock = LocalDateTime.now(); //Time to which we count

        selClock = selClock.plusHours(hours);
        selClock = selClock.plusMinutes(minutes);
        selClock = selClock.plusSeconds(seconds);

        LocalTime remains = LocalTime.ofSecondOfDay(Duration.between(mainClock,selClock).toSeconds());
        time.setText(sdfF.format(remains));
    }
    static private void powerOff(){
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("shutdown -s -t 0");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
class MyListener implements ActionListener {
    private final String id;
    public MyListener(String SetId){
        id = SetId;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (id) {
            case ("POWER_OFF") -> //BEEP SET
                    beepSet = !beepSet;
            case ("SET BEEP") -> //BEEP SET
                beepSet = !beepSet;
            case ("START") -> {  //TIME START
                permitRun = true;
                Main.timeStart();
                updateClockUI();
            }
            case ("STOP") ->{ //TIME STOP
                permitRun = false;
                try{sound.stop();}
                catch (Exception ignored){}
            }
            case ("PLUS") -> //TIME ADD
                Main.addtime();
            case ("MINUS") -> //TIME DECREASE
                Main.mintime();
            case ("SECONDS") -> { //SECONDS SELECT
                selected.setText("SECONDS");
                timerSwitchID = 50;
            }
            case ("MINUTES") -> { //MINUTES SELECT
                selected.setText("MINUTES");
                timerSwitchID = 51;
            }
            case ("HOURS") -> { //HOURS SELECT
                selected.setText("HOURS");
                timerSwitchID = 52;
            }
            case ("RESET") -> { //RESET
                permitRun = false;
                seconds = 0;
                minutes = 0;
                hours = 0;
                Main.updateClockUI();
            }
        }
    }

}