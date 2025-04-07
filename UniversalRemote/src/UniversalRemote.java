package org.example.universalremote;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class UniversalRemote extends JFrame implements ActionListener {

    private JButton powerBtn, homeBtn, backBtn, playBtn, muteBtn, volUpBtn, volDownBtn, centerBtn;
    private JPanel touchScreen;
    private JLabel touchBarDisplay;
    private boolean isPowerOn = false;
    private Timer visualizerTimer, homeAnimationTimer, homeDelayTimer, powerOffFadeTimer;
    private int volumeLevel = 50;
    private boolean isMuted = false;
    private long lastMuteClickTime = 0;
    private int crawlStep = 0;
    private boolean isMediaControlShown = false;
    private float fadeOpacity = 1.0f;

    public UniversalRemote() {
        setTitle("Universal Remote Control");
        setSize(300, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);
        getContentPane().setBackground(Color.LIGHT_GRAY);

        // Mini Touch Bar (Simulated Touchscreen with Display)
        touchScreen = new JPanel();
        touchScreen.setBounds(50, 450, 200, 50);
        touchScreen.setBackground(Color.BLACK);
        touchScreen.setLayout(new BorderLayout());

        touchBarDisplay = new JLabel("", SwingConstants.CENTER);
        touchBarDisplay.setForeground(Color.PINK);
        touchBarDisplay.setFont(new Font("Monospaced", Font.BOLD, 12));
        touchBarDisplay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isPowerOn || !isMediaControlShown) return;
                stopSnailCrawl();
                int x = e.getX();
                int width = touchBarDisplay.getWidth();
                if (x < width / 3) {
                    touchBarDisplay.setText("Reverse ⏪");
                } else if (x > 2 * width / 3) {
                    touchBarDisplay.setText("Skip ⏩");
                }
            }
        });
        touchScreen.add(touchBarDisplay, BorderLayout.CENTER);
        add(touchScreen);

        // Power Button
        powerBtn = createRoundButton("⏻");
        powerBtn.setBounds(250, 20, 30, 30);
        add(powerBtn);

        // Record Voice Area
        JPanel recordVoice = new JPanel();
        recordVoice.setBounds(140, 45, 30, 10);
        recordVoice.setBackground(Color.BLACK);
        add(recordVoice);

        // Center Button
        centerBtn = createLargeRoundButton("   ⋆˚\uD835\uDF17\uD835\uDF1A˚⋆ ");
        centerBtn.setBounds(75, 80, 150, 150);
        add(centerBtn);

        // Bottom Control Buttons
        backBtn = createRoundButton("↩");
        homeBtn = createRoundButton("🏠");
        playBtn = createRoundButton("▶");
        volUpBtn = createRoundButton("➕");
        muteBtn = createRoundButton("🔇");
        volDownBtn = createRoundButton("➖");

        backBtn.setBounds(75, 250, 50, 50);
        homeBtn.setBounds(170, 250, 50, 50);
        playBtn.setBounds(75, 310, 50, 50);
        volUpBtn.setBounds(170, 310, 50, 50);
        muteBtn.setBounds(75, 370, 50, 50);
        volDownBtn.setBounds(170, 370, 50, 50);

        add(backBtn);
        add(homeBtn);
        add(playBtn);
        add(volUpBtn);
        add(muteBtn);
        add(volDownBtn);

        powerBtn.addActionListener(this);
        centerBtn.addActionListener(this);
        backBtn.addActionListener(this);
        homeBtn.addActionListener(this);
        playBtn.addActionListener(this);
        volUpBtn.addActionListener(this);
        volDownBtn.addActionListener(this);

        muteBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isPowerOn) return;
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastMuteClickTime < 300) {
                    isMuted = !isMuted;
                    touchBarDisplay.setText(isMuted ? "🔇" : "Unmuted 🔉");
                } else {
                    isMuted = true;
                    touchBarDisplay.setText("🔇");
                }
                lastMuteClickTime = currentTime;
            }
        });

        setButtonsEnabled(false);
        setVisible(true);
    }

    private JButton createRoundButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(50, 50));
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        button.setBackground(Color.PINK);
        button.setOpaque(true);
        return button;
    }

    private JButton createLargeRoundButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isArmed()) {
                    g.setColor(Color.DARK_GRAY);
                } else {
                    g.setColor(getBackground());
                }
                g.fillOval(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                g.setColor(Color.BLACK);
                g.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
            }

            @Override
            public boolean contains(int x, int y) {
                double radius = getWidth() / 2.0;
                double centerX = getWidth() / 2.0;
                double centerY = getHeight() / 2.0;
                return Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2) <= Math.pow(radius, 2);
            }
        };

        button.setPreferredSize(new Dimension(150, 150));
        button.setBackground(Color.BLACK);
        button.setForeground(Color.PINK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setFont(new Font("Dialog", Font.BOLD, 40));
        return button;
    }

    private void setButtonsEnabled(boolean enabled) {
        centerBtn.setEnabled(enabled);
        backBtn.setEnabled(enabled);
        homeBtn.setEnabled(enabled);
        playBtn.setEnabled(enabled);
        volUpBtn.setEnabled(enabled);
        muteBtn.setEnabled(enabled);
        volDownBtn.setEnabled(enabled);
    }

    private void startSnailCrawl() {
        if (homeAnimationTimer != null && homeAnimationTimer.isRunning()) {
            homeAnimationTimer.stop();
        }
        crawlStep = 0;
        homeAnimationTimer = new Timer(200, e -> {
            StringBuilder space = new StringBuilder();
            for (int i = 0; i < crawlStep; i++) space.append(" ");
            touchBarDisplay.setText(space + " ✧˚ ༘ 🐌");
            crawlStep = (crawlStep + 1) % 16;
        });
        homeAnimationTimer.start();
    }

    private void stopSnailCrawl() {
        if (homeAnimationTimer != null && homeAnimationTimer.isRunning()) {
            homeAnimationTimer.stop();
        }
    }

    private void fadeOutPowerOff() {
        touchBarDisplay.setText("Power Off ⋆⑅˚₊ ");
        Timer fadeTimer = new Timer(2000, e -> touchBarDisplay.setText(""));
        fadeTimer.setRepeats(false);
        fadeTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton source = (JButton) e.getSource();

        if (source == powerBtn) {
            isPowerOn = !isPowerOn;
            if (isPowerOn) {
                touchBarDisplay.setText("Power On");
            } else {
                fadeOutPowerOff();
            }
            setButtonsEnabled(isPowerOn);

            if (!isPowerOn && visualizerTimer != null && visualizerTimer.isRunning()) {
                visualizerTimer.stop();
            }
            stopSnailCrawl();

        } else if (isPowerOn) {
            if (source == centerBtn) {
                stopSnailCrawl();
                touchBarDisplay.setText("⏪ | ▶ | ⏩");
                isMediaControlShown = true;

            } else if (source == backBtn) {
                stopSnailCrawl();
                touchBarDisplay.setText("Back");

            } else if (source == homeBtn) {
                touchBarDisplay.setText("Home 🏡");
                homeDelayTimer = new Timer(1000, evt -> startSnailCrawl());
                homeDelayTimer.setRepeats(false);
                homeDelayTimer.start();

            } else if (source == playBtn) {
                stopSnailCrawl();
                touchBarDisplay.setText("Playing Media");
                if (visualizerTimer != null && visualizerTimer.isRunning()) {
                    visualizerTimer.stop();
                }
                visualizerTimer = new Timer(1000, evt
                        -> touchBarDisplay.setText("✩♬ ₊̊.☁⋆☾⋆⁺₊✧"));
                visualizerTimer.setRepeats(false);
                visualizerTimer.start();

            } else if (source == volUpBtn) {
                stopSnailCrawl();
                if (volumeLevel < 100) {
                    if (volumeLevel < 20) volumeLevel = 20;
                    else if (volumeLevel < 50) volumeLevel = 50;
                    else if (volumeLevel < 80) volumeLevel = 80;
                    else volumeLevel = 100;
                    touchBarDisplay.setText("Volume: " + volumeLevel + "%");
                }
            } else if (source == volDownBtn) {
                stopSnailCrawl();
                if (volumeLevel > 0) {
                    if (volumeLevel > 80) volumeLevel = 80;
                    else if (volumeLevel > 50) volumeLevel = 50;
                    else if (volumeLevel > 20) volumeLevel = 20;
                    else volumeLevel = 0;
                    touchBarDisplay.setText("Volume: " + volumeLevel + "%");
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UniversalRemote::new);
    }
}


