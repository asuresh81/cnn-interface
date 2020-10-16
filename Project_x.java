import ij.plugin.*;
import ij.*;
import ij.io.*;
import ij.gui.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.*;

public class Project_X implements PlugIn {

    static File dir;
    final String[][] image = new String[1][1];
    final String[][] model = new String[1][1];
    JFrame jFrame;
    JPanel leftPanel, rightPanel;

    public void run(String arg) {
        startPane();
        IJ.register(Project_X.class);
    }

    public void startPane() {

        JFrame jFrame = new JFrame();
        jFrame.setTitle("Start");
        jFrame.setResizable(false);

        JPanel jPanel = new JPanel();
        jPanel.setPreferredSize(new Dimension(300, 400));
        jPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        jPanel.setLayout(new GridLayout(5, 1, 0, 20));

        JLabel selectImage = new JLabel();
        selectImage.setText("Choose an Image");
        selectImage.setVerticalAlignment(SwingConstants.BOTTOM);
        jPanel.add(selectImage);

        JButton singleImage = new JButton();
        singleImage.setText("Upload an image");
        singleImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                image[0] = openFiles("image");
                selectImage.setText("Image Chosen: " + image[0][1]);
                singleImage.setText("Select different image");
            }
        });
        jPanel.add(singleImage);

        JLabel selectCNN = new JLabel();
        selectCNN.setText("Choose CNN Model Directory");
        selectCNN.setVerticalAlignment(SwingConstants.BOTTOM);
        jPanel.add(selectCNN);

        JButton cnnModel = new JButton();
        cnnModel.setText("Upload model directory");
        cnnModel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model[0] = openFiles("model");
                selectCNN.setText("CNN Model Chosen: " + model[0][1]);
                cnnModel.setText("Select different CNN directory");
            }
        });
        jPanel.add(cnnModel);

        JPanel subPanel = new JPanel();
        subPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        subPanel.setLayout(new GridLayout(1, 2, 20, 0));
        jPanel.add(subPanel);

        JButton reset = new JButton();
        reset.setText("Reset");
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jFrame.dispose();
                startPane();
            }
        });
        subPanel.add(reset);

        JButton start = new JButton();
        start.setText("Continue");
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                boolean imageNotChosen = selectImage.getText().contains("Choose");
                boolean modelNotChosen = selectCNN.getText().contains("Choose");

                JFrame errorFrame = new JFrame();
                errorFrame.setTitle("Error");
                errorFrame.setPreferredSize(new Dimension(300, 200));
                JLabel errorLabel = new JLabel();
                errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
                errorLabel.setVerticalAlignment(SwingConstants.CENTER);
                errorFrame.add(errorLabel);

                if (imageNotChosen || modelNotChosen) {
                    if (imageNotChosen) errorLabel.setText("Please choose an image!");
                    if (modelNotChosen) errorLabel.setText("Please choose a directory for the model!");
                    if (imageNotChosen && modelNotChosen) errorLabel.setText("" +
                            "<html><center>Please choose an image and<br>" +
                            "a directory for the model!</center></html>");
                    errorFrame.pack();
                    errorFrame.setLocationRelativeTo(null);
                    errorFrame.setVisible(true);
                    errorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                }
                else {
                    jFrame.dispose();
                    mainPane();
                }
            }
        });
        subPanel.add(start);

        jFrame.add(jPanel);
        jFrame.pack();
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

    public void mainPane() {

        // MAIN FRAME
        jFrame = new JFrame();
        jFrame.setTitle("Project X");
        jFrame.setLayout(new GridLayout(1, 1));
        jFrame.setPreferredSize(new Dimension(450, 700));
        jFrame.setResizable(false);

        // INPUT PANEL
        leftPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        jFrame.add(leftPanel);

        // INPUT PANEL - TITLE
        JLabel inputLabel = new JLabel();
        inputLabel.setText("Input Image");
        inputLabel.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 20, 0);
        leftPanel.add(inputLabel, c);

        // INPUT PANEL - IMAGE
        Opener opener = new Opener();
        ImagePlus imp = opener.openImage(image[0][0], image[0][1]);
        BufferedImage myPicture = resize(imp.getBufferedImage(), 350, 350);
        JLabel picLabel = new JLabel(new ImageIcon(myPicture));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 20, 0);
        leftPanel.add(picLabel, c);

        // INPUT PANEL - SLIDER PANEL
        JPanel sliders = new JPanel();
        sliders.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        sliders.setPreferredSize(new Dimension(350, 150));
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(0, 0, 20, 0);
        leftPanel.add(sliders, c);
        JSlider zSlider = new JSlider(JSlider.HORIZONTAL,1, imp.getNSlices(), imp.getSlice());
        JSlider timeSlider = new JSlider(JSlider.HORIZONTAL,1, imp.getNFrames(), imp.getFrame());
        JSlider channelSlider = new JSlider(JSlider.HORIZONTAL,1, imp.getNChannels(), imp.getChannel());

        // INPUT PANEL - SLIDER PANEL - Z SLIDER
        JLabel zLabel = new JLabel("<html><center>z-stack<br>" + imp.getSlice() + "</center></html>");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 10, 20);
        sliders.add(zLabel, c);
        zSlider.setMajorTickSpacing(imp.getNSlices()-1);
        zSlider.setMinorTickSpacing(1);
        zSlider.setPaintTicks(true);
        zSlider.setPaintLabels(true);
        zSlider.setSnapToTicks(true);
        zSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = zSlider.getValue();
                zLabel.setText("<html><center>z-stack<br>" + value + "</center></html>");
                imp.setZ(value);
                imp.setPosition(channelSlider.getValue(), zSlider.getValue(), timeSlider.getValue());
                imp.updateImage();
                BufferedImage image = resize(imp.getBufferedImage(), 350, 350);
                picLabel.setIcon(new ImageIcon(image));
                picLabel.repaint();
            }
        });
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.ipadx = 225;
        c.insets = new Insets(0, 0, 10, 0);
        sliders.add(zSlider, c);

        // INPUT PANEL - SLIDER PANEL - TIME SLIDER
        JLabel timeLabel = new JLabel("<html><center>time-course<br>" + imp.getFrame() + "</center></html>");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 10, 20);
        sliders.add(timeLabel, c);
        timeSlider.setMajorTickSpacing(imp.getNFrames()-1);
        timeSlider.setMinorTickSpacing(1);
        timeSlider.setPaintTicks(true);
        timeSlider.setPaintLabels(true);
        timeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = timeSlider.getValue();
                timeLabel.setText("<html><center>time-course<br>" + value + "</center></html>");
                imp.setT(value);
                imp.setPosition(channelSlider.getValue(), zSlider.getValue(), timeSlider.getValue());
                imp.updateImage();
                BufferedImage image = resize(imp.getBufferedImage(), 350, 350);
                picLabel.setIcon(new ImageIcon(image));
                picLabel.repaint();
            }
        });
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.ipadx = 225;
        c.insets = new Insets(0, 0, 10, 0);
        sliders.add(timeSlider, c);

        // INPUT PANEL - SLIDER PANEL - CHANNEL SLIDER
        JLabel channelLabel = new JLabel("<html><center>channel<br>" + imp.getChannel() + "</center></html>");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 20);
        sliders.add(channelLabel, c);
        channelSlider.setMajorTickSpacing(imp.getNChannels()-1);
        channelSlider.setMinorTickSpacing(1);
        channelSlider.setPaintTicks(true);
        channelSlider.setPaintLabels(true);
        channelSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = channelSlider.getValue();
                channelLabel.setText("<html><center>channel<br>" + value + "</center></html>");
                imp.setT(value);
                imp.setPosition(channelSlider.getValue(), zSlider.getValue(), timeSlider.getValue());
                imp.updateImage();
                BufferedImage image = resize(imp.getBufferedImage(), 350, 350);
                picLabel.setIcon(new ImageIcon(image));
                picLabel.repaint();
            }
        });
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.ipadx = 225;
        sliders.add(channelSlider, c);

        // INPUT PANEL - CHOICES PANEL
        JPanel inputButtons = new JPanel();
        inputButtons.setLayout(new GridBagLayout());
        inputButtons.setPreferredSize(new Dimension(350, 100));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.insets = new Insets(0, 0, 0, 0);
        leftPanel.add(inputButtons, c);

        // INPUT PANEL - CHOICES PANEL - RESET BUTTON
        JButton reset = new JButton();
        reset.setText("Reset");
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jFrame.dispose();
                startPane();
            }
        });
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 25;
        c.ipady = 25;
        c.insets = new Insets(0, 0, 0, 75);
        inputButtons.add(reset, c);

        // INPUT PANEL - CHOICES PANEL - CNN BUTTON
        JButton update = new JButton();
        update.setText("Run CNN");
        update.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jFrame.getWidth() == 900) {
                    jFrame.remove(rightPanel);
                }
                addOutputPane();
            }
        });
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.ipadx = 75;
        c.ipady = 25;
        inputButtons.add(update, c);

        // UPDATING MAIN FRAME
        jFrame.pack();
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

    public void addOutputPane() {

        // MAIN FRAME
        jFrame.setResizable(true);
        jFrame.setLayout(new GridLayout(1, 2));
        jFrame.setPreferredSize(new Dimension(900, 700));
        jFrame.setResizable(false);

        // RIGHT PANEL
        rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        jFrame.add(rightPanel);

        // RIGHT PANEL - TITLE
        JLabel outputLabel = new JLabel();
        outputLabel.setText("CNN Output");
        outputLabel.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 20, 0);
        rightPanel.add(outputLabel, c);

        // RIGHT PANEL - OUTPUT IMAGE
        Opener opener = new Opener();
        ImagePlus imp_out = opener.openImage(image[0][0], image[0][1]);
        BufferedImage bufferedImage_out = resize(imp_out.getBufferedImage(), 350, 350);
        JLabel imLabel_out = new JLabel(new ImageIcon(bufferedImage_out));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 20, 0);
        rightPanel.add(imLabel_out, c);

        // RIGHT PANEL - SLIDER PANEL
        JPanel sliderPanel_out = new JPanel();
        sliderPanel_out.setLayout(new GridBagLayout());
        sliderPanel_out.setPreferredSize(new Dimension(350, 150));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(0, 0, 20, 0);
        rightPanel.add(sliderPanel_out, c);
        JSlider zSlider_out = new JSlider(JSlider.HORIZONTAL,1, imp_out.getNSlices(), imp_out.getSlice());
        JSlider timeSlider_out = new JSlider(JSlider.HORIZONTAL,1, imp_out.getNFrames(), imp_out.getFrame());
        JSlider channelSlider_out = new JSlider(JSlider.HORIZONTAL,1, imp_out.getNChannels(), imp_out.getChannel());

        // RIGHT PANEL - SLIDER PANEL - Z SLIDER
        JLabel zLabel_out = new JLabel("<html><center>z-stack<br>" + imp_out.getSlice() + "</center></html>");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 10, 20);
        sliderPanel_out.add(zLabel_out, c);
        zSlider_out.setMajorTickSpacing(imp_out.getNSlices()-1);
        zSlider_out.setMinorTickSpacing(1);
        zSlider_out.setPaintTicks(true);
        zSlider_out.setPaintLabels(true);
        zSlider_out.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = zSlider_out.getValue();
                zLabel_out.setText("<html><center>z-stack<br>" + value + "</center></html>");
                imp_out.setZ(value);
                imp_out.setPosition(channelSlider_out.getValue(), zSlider_out.getValue(), timeSlider_out.getValue());
                imp_out.updateImage();
                BufferedImage newIm_out = resize(imp_out.getBufferedImage(), 350, 350);
                imLabel_out.setIcon(new ImageIcon(newIm_out));
                imLabel_out.repaint();
            }
        });
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.ipadx = 225;
        c.insets = new Insets(0, 0, 10, 0);
        sliderPanel_out.add(zSlider_out, c);

        // RIGHT PANEL - SLIDER PANEL - TIME SLIDER
        JLabel timeLabel_out = new JLabel("<html><center>time-course<br>" + imp_out.getFrame() + "</center></html>");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 10, 20);
        sliderPanel_out.add(timeLabel_out, c);
        timeSlider_out.setMajorTickSpacing(imp_out.getNFrames()-1);
        timeSlider_out.setMinorTickSpacing(1);
        timeSlider_out.setPaintTicks(true);
        timeSlider_out.setPaintLabels(true);
        timeSlider_out.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = timeSlider_out.getValue();
                timeLabel_out.setText("<html><center>time-course<br>" + value + "</center></html>");
                imp_out.setT(value);
                imp_out.setPosition(channelSlider_out.getValue(), zSlider_out.getValue(), timeSlider_out.getValue());
                imp_out.updateImage();
                BufferedImage newIm_out = resize(imp_out.getBufferedImage(), 350, 350);
                imLabel_out.setIcon(new ImageIcon(newIm_out));
                imLabel_out.repaint();
            }
        });
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.ipadx = 225;
        c.insets = new Insets(0, 0, 10, 0);
        sliderPanel_out.add(timeSlider_out, c);

        // RIGHT PANEL - SLIDER PANEL - CHANNEL SLIDER
        JLabel channelLabel_out = new JLabel("<html><center>channel<br>" + imp_out.getChannel() + "</center></html>");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 20);
        sliderPanel_out.add(channelLabel_out, c);
        channelSlider_out.setMajorTickSpacing(imp_out.getNChannels()-1);
        channelSlider_out.setMinorTickSpacing(1);
        channelSlider_out.setPaintTicks(true);
        channelSlider_out.setPaintLabels(true);
        channelSlider_out.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = channelSlider_out.getValue();
                channelLabel_out.setText("<html><center>channel<br>" + value + "</center></html>");
                imp_out.setT(value);
                imp_out.setPosition(channelSlider_out.getValue(), zSlider_out.getValue(), timeSlider_out.getValue());
                imp_out.updateImage();
                BufferedImage newIm_out = resize(imp_out.getBufferedImage(), 350, 350);
                imLabel_out.setIcon(new ImageIcon(newIm_out));
                imLabel_out.repaint();
            }
        });
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.ipadx = 225;
        sliderPanel_out.add(channelSlider_out, c);

        // RIGHT PANEL - CHOICES PANEL
        JPanel choicePanel_out = new JPanel();
        choicePanel_out.setLayout(new GridBagLayout());
        choicePanel_out.setPreferredSize(new Dimension(350, 100));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.insets = new Insets(0, 0, 0, 0);
        rightPanel.add(choicePanel_out, c);

        // RIGHT PANEL - CHOICES PANEL - DENY BUTTON
        JButton denyResults = new JButton();
        denyResults.setText("<html><center>Deny and<br>Edit Results</center><html>");
        denyResults.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jFrame.remove(rightPanel);
                denyResults(imp_out);
            }
        });
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 64;
        c.ipady = 9;
        c.insets = new Insets(0, 0, 0, 40);
        choicePanel_out.add(denyResults, c);

        // RIGHT PANEL - CHOICES PANEL - ACCEPT BUTTON
        JButton acceptResults = new JButton();
        acceptResults.setText("Accept Results");
        acceptResults.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.ipadx = 25;
        c.ipady = 25;
        c.insets = new Insets(0, 0, 0, 0);
        choicePanel_out.add(acceptResults, c);

        // UPDATE MAIN FRAME
        jFrame.pack();
        jFrame.setLocation(jFrame.getLocation());
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

    public void denyResults(ImagePlus imp_out) {

        // RIGHT PANEL
        rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        jFrame.add(rightPanel);

        // RIGHT PANEL - TITLE
        JLabel outputLabel = new JLabel();
        outputLabel.setText("CNN Output");
        outputLabel.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 20, 0);
        rightPanel.add(outputLabel, c);

        // RIGHT PANEL - OUTPUT IMAGE
        BufferedImage bufferedImage_out = resize(imp_out.getBufferedImage(), 350, 350);
        JLabel imLabel_out = new JLabel(new ImageIcon(bufferedImage_out));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 20, 0);
        rightPanel.add(imLabel_out, c);

        JPanel choicePanel_deny = new JPanel();
        choicePanel_deny.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(0, 0, 1, 0);
        rightPanel.add(choicePanel_deny, c);

        JButton editCNNRegions, freehand, segmented, back;
        JLabel regionTitle;
        JPanel checkboxPanel;

        editCNNRegions = new JButton();
        editCNNRegions.setText("Edit CNN Results");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 25;
        c.ipady = 25;
        c.insets = new Insets(0, 0, 5, 50);
        choicePanel_deny.add(editCNNRegions, c);

        freehand = new JButton();
        freehand.setText("Freehand Selection");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.ipadx = 0;
        c.ipady = 25;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 5, 50);
        choicePanel_deny.add(freehand, c);

        segmented = new JButton();
        segmented.setText("Segmented Lines");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.ipadx = 0;
        c.ipady = 25;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 5, 50);
        choicePanel_deny.add(segmented, c);

        back = new JButton();
        back.setText("Return");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.ipady = 25;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 50);
        choicePanel_deny.add(back, c);

        regionTitle = new JLabel();
        regionTitle.setText("<html><center><u>Regions</u></center></html>");
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.ipady = 25;
        c.fill = GridBagConstraints.HORIZONTAL;
        choicePanel_deny.add(regionTitle, c);

        checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.gridheight = 3;
        c.fill = GridBagConstraints.VERTICAL;
        c.insets = new Insets(-50, -10, 0, 0);
        choicePanel_deny.add(checkboxPanel, c);

        JCheckBox region_1 = new JCheckBox("1");
        region_1.setMnemonic(KeyEvent.VK_C);
        region_1.setSelected(true);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        checkboxPanel.add(region_1, c);

        JCheckBox region_2 = new JCheckBox("2");
        region_2.setMnemonic(KeyEvent.VK_C);
        region_2.setSelected(true);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        checkboxPanel.add(region_2, c);

        JCheckBox region_3 = new JCheckBox("3");
        region_3.setMnemonic(KeyEvent.VK_C);
        region_3.setSelected(true);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        checkboxPanel.add(region_3, c);

        JCheckBox region_4 = new JCheckBox("4");
        region_4.setMnemonic(KeyEvent.VK_C);
        region_4.setSelected(true);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        checkboxPanel.add(region_4, c);

        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jFrame.remove(rightPanel);
                addOutputPane();
            }
        });

        // UPDATE MAIN FRAME
        jFrame.pack();
        jFrame.setLocation(jFrame.getLocation());
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

    public void acceptResults(ImagePlus imp_out) {

    }

    public String[] openFiles(String fileType) {

        JFileChooser fileChooser = new JFileChooser();

        if (dir==null) {
            String sdir = OpenDialog.getDefaultDirectory();
            if (sdir!=null)
                dir = new File(sdir);
        }
        if (dir!=null)
            fileChooser.setCurrentDirectory(dir);

        if (fileType == "image") {
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Image (.jpg, .gif., .tif, .tiff, .bmp, .dicom, .fits, .pgm)",
                    "jpg", "gif", "tif", "tiff", "bmp", "dicom", "fits", "pgm");
            fileChooser.setFileFilter(filter);
            fileChooser.setMultiSelectionEnabled(false);
            int option = fileChooser.showOpenDialog(IJ.getInstance());
            if(option == JFileChooser.APPROVE_OPTION){
                File file = fileChooser.getSelectedFile();
                String path = fileChooser.getCurrentDirectory().getPath() + Prefs.getFileSeparator();
                String[] output = new String[2];
                output[0] = path;
                output[1] = file.getName();
                return output;
            }
        }

        if (fileType == "model") {
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = fileChooser.showOpenDialog(IJ.getInstance());
            if(option == JFileChooser.APPROVE_OPTION){
                File file = fileChooser.getSelectedFile();
                String path = fileChooser.getCurrentDirectory().getPath() + Prefs.getFileSeparator();
                String[] output = new String[2];
                output[0] = path;
                output[1] = file.getName();
                return output;
            }
        }

        return new String[0];
    }

    public BufferedImage resize(BufferedImage img, int width, int height) {

        Image temp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = output.createGraphics();
        g2d.drawImage(temp, 0, 0, null);
        g2d.dispose();

        return output;

    }

}
