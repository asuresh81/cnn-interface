
import ij.*;
import ij.io.*;
import ij.gui.*;
import ij.process.*;
import ij.plugin.*;
import ij.plugin.frame.RoiManager;
import ij.measure.*;
import ij.WindowManager;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import java.io.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.*;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.plaf.InsetsUIResource;

import java.util.stream.Collectors;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.imageio.*;
import java.lang.ProcessBuilder;

import java.util.Timer;
import java.util.TimerTask;

public class CNN_Annotation implements PlugIn {

    // VARIABLES - INPUTS
    final String[][] image = new String[1][1];
    final String[][] model = new String[1][1];

    // VARIABLES - IMAGES
    private ImagePlus imp;
    private ImagePlus cnnOut;
    private ImagePlus annotationImage;

    // VARIABLES - START PANE
    private JFrame startFrame;
    private JPanel jPanel, subPanel;
    private JButton singleImage, cnnModel, reset, start;
    private JLabel selectImage, selectCNN;

    // VARIABLES - MAIN PANE
    private JFrame jFrame;

    // VARIABLES - LEFT SIDE OF MAIN PANE
    private JPanel emptyInPanel, inChoicesPanel, zPanel, timePanel, channelPanel;
    private JLabel inputLabel, inputPicLabel, zLabel, timeLabel, channelLabel;
    private JSlider zSlider, timeSlider, channelSlider;
    private JButton resetButton, runButton, runAllButton;

    // VARIABLES - RIGHT SIDE OF MAIN PANE
    private JPanel emptyOutPanel, outChoicesPanel, regionsPanel, checkboxPanel;
    private JLabel outputLabel, outPicLabel, regionsLabel;
    private JButton denyButton, acceptButton, editButton, manualButton, backButton, exportButton;
    private JScrollPane scrollPane;

    // VARIABLES - HELPERS
    static File dir;
    final Opener opener = new Opener();

    private RoiManager rm;

    private Thread addRegionsThread;
    private Timer timer;
    private TimerTask checkForChanges;
    private volatile boolean running = true;

    // INITIAL FUNCTION
    public void run(String arg) {
        startPane();
        IJ.register(CNN_Annotation.class);
    }

    // CREATE START PANE
    public void startPane() {

        // MAIN FRAME
        startFrame = new JFrame();
        startFrame.setTitle("Start");
        startFrame.setResizable(false);

        // MAIN PANEL
        jPanel = new JPanel();
        jPanel.setPreferredSize(new Dimension(300, 400));
        jPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        jPanel.setLayout(new GridLayout(5, 1, 0, 20));
        startFrame.add(jPanel);

        // MAIN PANEL - SELECT IMAGE
        selectImage = new JLabel();
        selectImage.setText("Choose an Image");
        selectImage.setVerticalAlignment(SwingConstants.BOTTOM);
        jPanel.add(selectImage);
        singleImage = new JButton();
        singleImage.setText("Upload an image");
        jPanel.add(singleImage);

        // MAIN PANEL - SELECT CNN
        selectCNN = new JLabel();
        selectCNN.setText("Choose CNN Model Directory");
        selectCNN.setVerticalAlignment(SwingConstants.BOTTOM);
        jPanel.add(selectCNN);
        cnnModel = new JButton();
        cnnModel.setText("Upload model directory");
        jPanel.add(cnnModel);

        // MAIN PANEL - CHOICES PANEL
        subPanel = new JPanel();
        subPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        subPanel.setLayout(new GridLayout(1, 2, 20, 0));
        jPanel.add(subPanel);

        // MAIN PANEL - CHOICES PANEL - RESET
        reset = new JButton();
        reset.setText("Reset");
        subPanel.add(reset);

        // MAIN PANEL - CHOICES PANEL - START
        start = new JButton();
        start.setText("Continue");
        subPanel.add(start);

        // ACTION LISTENERS
        singleImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                image[0] = openFiles("image");
                selectImage.setText("Image Chosen: " + image[0][1]);
                singleImage.setText("Select different image");
            }
        });
        cnnModel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model[0] = openFiles("model");
                selectCNN.setText("CNN Model Chosen: " + model[0][1]);
                cnnModel.setText("Select different CNN directory");
            }
        });
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startFrame.dispose();
                startPane();
            }
        });
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
                    startFrame.dispose();
                    mainPane();
                }
            }
        });

        // UPDATE MAIN FRAME
        startFrame.pack();
        startFrame.setLocationRelativeTo(null);
        startFrame.setVisible(true);
        startFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    // LEFT SIDE OF MAIN PANE
    public void mainPane() {

        // FRAME INITIALIZATION
        jFrame = new JFrame();
        jFrame.setTitle("Cell Annotation with CNN");
        jFrame.setLayout(new GridBagLayout());
        jFrame.setPreferredSize(new Dimension(450, 700));
        jFrame.setResizable(false);

        // TITLE SECTION
        emptyInPanel = new JPanel();
        emptyInPanel.setPreferredSize(new DimensionUIResource(75, 25));
        inputLabel = new JLabel("Input Image");
        emptyInPanel.add(inputLabel);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new InsetsUIResource(20, 20, 20, 20);
        c.gridx = 0;
        c.gridy = 0;
        jFrame.add(emptyInPanel, c);

        // IMAGE SECTION
        imp = opener.openImage(image[0][0], image[0][1]);
        BufferedImage bufferedImage = resize(imp.getBufferedImage(), 350, 350);
        inputPicLabel = new JLabel(new ImageIcon(bufferedImage));
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new InsetsUIResource(0, 20, 20, 20);
        c.gridx = 0;
        c.gridy = 1;
        jFrame.add(inputPicLabel, c);

        // CHOICES SECTION
        inChoicesPanel = new JPanel();
        inChoicesPanel.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new InsetsUIResource(0, 20, 20, 20);
        c.gridx = 0;
        c.gridy = 2;
        c.weighty = 1;
        jFrame.add(inChoicesPanel, c);

        // CHOICES SECTION - Z-SLIDER LABEL
        zPanel = new JPanel();
        zPanel.setPreferredSize(new DimensionUIResource(60, 45));
        zLabel = new JLabel("<html><center>z-stack<br>" + imp.getSlice() + "</center></html>");
        zPanel.add(zLabel);
        c = new GridBagConstraints();
        c.insets = new InsetsUIResource(0, 0, 0, 15);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        inChoicesPanel.add(zPanel, c);

        // CHOICES SECTION - Z-SLIDER
        zSlider = makeSlider(imp.getSlice(), imp.getNSlices());
        zSlider.setPreferredSize(new DimensionUIResource(275, 45));
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 2;
        inChoicesPanel.add(zSlider, c);

        // CHOICES SECTION - TIME-SLIDER LABEL
        timePanel = new JPanel();
        timePanel.setPreferredSize(new DimensionUIResource(60, 45));
        timeLabel = new JLabel("<html><center>time<br>" + imp.getFrame() + "</center></html>");
        timePanel.add(timeLabel);
        c = new GridBagConstraints();
        c.insets = new InsetsUIResource(0, 0, 0, 15);
        c.gridx = 0;
        c.gridy = 1;
        inChoicesPanel.add(timePanel, c);

        // CHOICES SECTION - TIME-SLIDER
        timeSlider = makeSlider(imp.getFrame(), imp.getNFrames());
        timeSlider.setPreferredSize(new DimensionUIResource(275, 45));
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 2;
        inChoicesPanel.add(timeSlider, c);

        // CHOICES SECTION - CHANNEL-SLIDER LABEL
        channelPanel = new JPanel();
        channelPanel.setPreferredSize(new DimensionUIResource(60, 45));
        channelLabel = new JLabel("<html><center>channel<br>" + imp.getChannel() + "</center></html>");
        channelPanel.add(channelLabel);
        c = new GridBagConstraints();
        c.insets = new InsetsUIResource(0, 0, 0, 15);
        c.gridx = 0;
        c.gridy = 2;
        inChoicesPanel.add(channelPanel, c);

        // CHOICES SECTION - CHANNEL-SLIDER
        channelSlider = makeSlider(imp.getChannel(), imp.getNChannels());
        channelSlider.setPreferredSize(new DimensionUIResource(275, 45));
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 2;
        inChoicesPanel.add(channelSlider, c);

        // CHOICES SECTION - RESET BUTTON
        resetButton = new JButton("Reset");
        resetButton.setPreferredSize(new DimensionUIResource(75, 50));
        c = new GridBagConstraints();
        c.insets = new InsetsUIResource(15, 0, 0, 0);
        c.gridx = 0;
        c.gridy = 3;
        inChoicesPanel.add(resetButton, c);

        // CHOICES SECTION - RUN BUTTON
        runButton = new JButton("Run");
        runButton.setPreferredSize(new DimensionUIResource(122, 50));
        c = new GridBagConstraints();
        c.insets = new InsetsUIResource(15, 16, 0, 15);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 1;
        inChoicesPanel.add(runButton, c);

        // CHOICES SECTION - RUN ALL BUTTON
        runAllButton = new JButton("Run All");
        runAllButton.setPreferredSize(new DimensionUIResource(122, 50));
        c = new GridBagConstraints();
        c.insets = new InsetsUIResource(15, 0, 0, 0);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 2;
        c.gridy = 3;
        c.gridwidth = 1;
        inChoicesPanel.add(runAllButton, c);

        // ACTION LISTENERS
        zSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = zSlider.getValue();
                zLabel.setText("<html><center>z-stack<br>" + value + "</center></html>");
                updateImage(imp, channelSlider, zSlider, timeSlider, inputPicLabel);
            }
        });
        timeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = timeSlider.getValue();
                timeLabel.setText("<html><center>time<br>" + value + "</center></html>");
                updateImage(imp, channelSlider, zSlider, timeSlider, inputPicLabel);
            }
        });
        channelSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = channelSlider.getValue();
                channelLabel.setText("<html><center>channel<br>" + value + "</center></html>");
                updateImage(imp, channelSlider, zSlider, timeSlider, inputPicLabel);
            }
        });
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                running = false;
                timer.cancel();
                timer.purge();
                System.out.println("Cancel timer");
                jFrame.dispose();
                startPane();
            }
        });
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (jFrame.getWidth() == 850) {
                        jFrame.remove(emptyOutPanel);
                        jFrame.remove(outPicLabel);
                        jFrame.remove(outChoicesPanel);
                        jFrame.validate();
                        jFrame.repaint();
                    }
                    cnnOut = runCNN(imp);
                    addOutput();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        runAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        // FRAME FINALIZATION
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                timer.cancel();
                timer.purge();
                System.out.println("Cancel timer");
                running = false;
            }
        });
        jFrame.pack();
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

    // RIGHT SIDE OF MAIN PANE
    public void addOutput() throws IOException {

        // FRAME INITIALIZATION
        jFrame.setPreferredSize(new Dimension(850, 700));

        // TITLE SECTION
        emptyOutPanel = new JPanel();
        emptyOutPanel.setPreferredSize(new DimensionUIResource(75, 25));
        outputLabel = new JLabel("Output Image");
        emptyOutPanel.add(outputLabel);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new InsetsUIResource(20, 20, 20, 20);
        c.gridx = 1;
        c.gridy = 0;
        jFrame.add(emptyOutPanel, c);

        // IMAGE SECTION
        BufferedImage bufferedImage = resize(cnnOut.getBufferedImage(), 350, 350);
        outPicLabel = new JLabel(new ImageIcon(bufferedImage));
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new InsetsUIResource(0, 20, 20, 20);
        c.gridx = 1;
        c.gridy = 1;
        jFrame.add(outPicLabel, c);

        // CHOICES SECTION
        outChoicesPanel = new JPanel();
        outChoicesPanel.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new InsetsUIResource(0, 20, 20, 20);
        c.gridx = 1;
        c.gridy = 2;
        c.weighty = 1;
        jFrame.add(outChoicesPanel, c);

        // CHOICES SECTION - EDIT BUTTON
        editButton = new JButton("Edit CNN Results");
        editButton.setPreferredSize(new DimensionUIResource(167, 45));
        c = new GridBagConstraints();
        c.insets = new InsetsUIResource(0, 0, 5, 16);
        c.gridx = 0;
        c.gridy = 0;
        outChoicesPanel.add(editButton, c);

        // CHOICES SECTION - MANUAL BUTTON
        manualButton = new JButton("Manual Annotation");
        manualButton.setPreferredSize(new DimensionUIResource(167, 45));
        c = new GridBagConstraints();
        c.insets = new InsetsUIResource(0, 0, 5, 16);
        c.gridx = 0;
        c.gridy = 1;
        outChoicesPanel.add(manualButton, c);

        // CHOICES SECTION - REGION TITLE
        regionsPanel = new JPanel();
        regionsPanel.setLayout(new GridBagLayout());
        regionsPanel.setPreferredSize(new DimensionUIResource(167, 45));
        regionsLabel = new JLabel("<html><u>Regions</u></html>");
        regionsPanel.add(regionsLabel);
        c = new GridBagConstraints();
        c.insets = new InsetsUIResource(0, 0, 5, 0);
        c.gridx = 1;
        c.gridy = 0;
        outChoicesPanel.add(regionsPanel, c);

        // CHOICES SECTION - REGIONS
        checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new GridBagLayout());
        scrollPane = new JScrollPane(checkboxPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new DimensionUIResource(167, 95));
        scrollPane.setEnabled(true);
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 1;
        c.gridheight = 2;
        outChoicesPanel.add(scrollPane, c);

        // ADD REGIONS
        //addTempRegions();

        // CHOICES SECTION - EXPORT BUTTON
        exportButton = new JButton("Export");
        exportButton.setPreferredSize(new DimensionUIResource(75, 25));
        c = new GridBagConstraints();
        c.insets = new InsetsUIResource(5, 0, 0, 0);
        c.gridx = 1;
        c.gridy = 3;
        outChoicesPanel.add(exportButton, c);

        // ACTION LISTENERS
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        manualButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                running = true;
                annotationImage = imp.duplicate();
                annotationImage.setIgnoreFlush(true);
                annotationImage.show();
                rm = RoiManager.getRoiManager();
                //OverlayLabels overlayLabels = new OverlayLabels();
                //overlayLabels.showDialog();
                addRegionsThread = new Thread(new Runnable(){

                    public void run(){
                        int currentCount = rm.getCount();
                        System.out.println("Count before while loop: " + currentCount);
                        while(running){
                            //System.out.println("ROI manager count: " + rm.getCount());
                            //System.out.println("Current count: " + currentCount);
                            if(rm.getCount() != currentCount){
                                System.out.println("Current count start of loop: " + currentCount);
                                System.out.println("get count start of loop: " + rm.getCount());
                                currentCount = rm.getCount();
                                Roi[] roiList = rm.getRoisAsArray();
                                System.out.println("Roi list length after first assignment: " + roiList.length);
                                while(currentCount != roiList.length){
                                    roiList = rm.getRoisAsArray();
                                }
                                for( Roi roi : roiList) {
                                    System.out.println("Roi: " + roi.toString());
                                }
                                System.out.println("Roi list length " + roiList.length);
                                checkboxPanel.removeAll();
                                for(int i = 0; i < roiList.length; i++){
                                    addRegions(roiList[i], i);
                                }
                            }
                        }
                        System.out.println("Stopping regions thread");
                        addRegionsThread.interrupt();
                    }
                });
                addRegionsThread.start();

                timer = new Timer();
                checkForChanges = new TimerTask() {
                    @Override
                    public void run() {
                        //System.out.println("Executing every 5 seconds");
                        //System.out.println("Check component count before if statement");
                        //System.out.println(checkboxPanel.getComponentCount());
                        //Roi[] roiListCheck = rm.getRoisAsArray();
                        if(checkboxPanel.getComponentCount() > 0){
                            Roi[] roiList = rm.getRoisAsArray();
                            //System.out.println(roiList);
                            //System.out.println("Component count before removeAll");
                            //System.out.println(checkboxPanel.getComponentCount());
                            checkboxPanel.removeAll();
                            //System.out.println("Component count after removeAll");
                            //System.out.println(checkboxPanel.getComponentCount());
                            for(int i = 0; i < roiList.length; i++){
                                addRegions(roiList[i], i);
                            }
                        }
                        else{
                            checkboxPanel.removeAll();
                            jFrame.validate();
                            jFrame.repaint();
                        }
                    }
                };
                timer.scheduleAtFixedRate(checkForChanges, 0, 5000);
            }
        });
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedCount = 0;
                OverlayCommands oc = new OverlayCommands();
                Commands cmd = new Commands();
                //Overlay overlay = new Overlay();
                OverlayLabels overlayLabels = new OverlayLabels();
                for(int i = 0; i < checkboxPanel.getComponentCount(); i++) {
                    JCheckBox checkBox = (JCheckBox)checkboxPanel.getComponent(i);
                    if(checkBox.isSelected()) {
                        rm.select(Integer.parseInt(checkBox.getText().split(" ")[0])-1, true, false);
                        oc.run("add");
                        selectedCount++;
                    }
                }
                if (selectedCount != 0){
                    Roi[] selectedRois = rm.getSelectedRoisAsArray();
                    System.out.println("Selected Rois " + selectedRois);
                    System.out.println("Selected Roi list length: " + selectedRois.length);
                    rm.runCommand("Multi Measure");
                    // oc.run("add");
                    //overlayLabels.run("Stupid");
                    oc.run("flatten");
                    overlayLabels.run("Stupid");
                    oc.run("show");
                    cmd.run("save");
                    ImageWindow annotationImageWindow = annotationImage.getWindow();
                    WindowManager.setCurrentWindow(annotationImageWindow);
                    //cmd.run("close");
                    oc.run("remove");
                    //oc.run("remove");
                    //oc.run("hide");
                    //boolean saved = rm.runCommand("Save", "/Users/adityasuresh/flattened_roi.tif");
                }
            }
        });

        // // CHOICES SECTION - DENY BUTTON
        // denyButton = new JButton("<html><center>Deny and<br>Edit Results</center><html>");
        // denyButton.setPreferredSize(new DimensionUIResource(167, 50));
        // c = new GridBagConstraints();
        // c.insets = new InsetsUIResource(0, 0, 0, 16);
        // c.gridx = 0;
        // c.gridy = 0;
        // outChoicesPanel.add(denyButton, c);

        // // CHOICES SECTION - ACCEPT BUTTON
        // acceptButton = new JButton("Accept Results");
        // acceptButton.setPreferredSize(new DimensionUIResource(167, 50));
        // c = new GridBagConstraints();
        // c.gridx = 1;
        // c.gridy = 0;
        // outChoicesPanel.add(acceptButton, c);

        // // ACTION LISTENERS
        // denyButton.addActionListener(new ActionListener() {
        //     @Override
        //     public void actionPerformed(ActionEvent e) {
        //         denyOptions();
        //     }
        // });
        // acceptButton.addActionListener(new ActionListener() {
        //     @Override
        //     public void actionPerformed(ActionEvent e) {
        //         acceptOptions();
        //     }
        // });

        // FRAME FINALIZATION
        jFrame.pack();
        jFrame.setLocation(jFrame.getLocation());
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

    // UPDATE CHOICES SECTION TO INITIAL LAYOUT
    public void backOptions() {

        // EMPTY CHOICES SECTION
        outChoicesPanel.removeAll();

        // CHOICES SECTION - DENY BUTTON
        denyButton = new JButton("<html><center>Deny and<br>Edit Results</center><html>");
        denyButton.setPreferredSize(new DimensionUIResource(167, 50));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new InsetsUIResource(0, 0, 0, 16);
        c.gridx = 0;
        c.gridy = 0;
        outChoicesPanel.add(denyButton, c);

        // CHOICES SECTION - ACCEPT BUTTON
        acceptButton = new JButton("Accept Results");
        acceptButton.setPreferredSize(new DimensionUIResource(167, 50));
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        outChoicesPanel.add(acceptButton, c);

        // ACTION LISTENERS
        denyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                denyOptions();
            }
        });
        acceptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acceptOptions();
            }
        });

        // UPDATE FRAME
        jFrame.validate();
        jFrame.repaint();

    }

    // UPDATE CHOICES SECTION TO DENY LAYOUT
    public void denyOptions() {

        // EMPTY CHOICES SECTION
        outChoicesPanel.removeAll();

        // CHOICES SECTION - BACK BUTTON
        backButton = new JButton("Back");
        backButton.setPreferredSize(new DimensionUIResource(167, 45));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new InsetsUIResource(0, 0, 5, 16);
        c.gridx = 0;
        c.gridy = 0;
        outChoicesPanel.add(backButton, c);

        // CHOICES SECTION - MANUAL BUTTON
        manualButton = new JButton("Manual Annotation");
        manualButton.setPreferredSize(new DimensionUIResource(167, 45));
        c = new GridBagConstraints();
        c.insets = new InsetsUIResource(0, 0, 5, 16);
        c.gridx = 0;
        c.gridy = 1;
        outChoicesPanel.add(manualButton, c);

        // CHOICES SECTION - EDIT BUTTON
        editButton = new JButton("Edit CNN Results");
        editButton.setPreferredSize(new DimensionUIResource(167, 45));
        c = new GridBagConstraints();
        c.insets = new InsetsUIResource(0, 0, 0, 16);
        c.gridx = 0;
        c.gridy = 2;
        outChoicesPanel.add(editButton, c);

        // CHOICES SECTION - REGION TITLE
        regionsPanel = new JPanel();
        regionsPanel.setLayout(new GridBagLayout());
        regionsPanel.setPreferredSize(new DimensionUIResource(167, 45));
        regionsLabel = new JLabel("<html><u>Regions</u></html>");
        regionsPanel.add(regionsLabel);
        c = new GridBagConstraints();
        c.insets = new InsetsUIResource(0, 0, 5, 0);
        c.gridx = 1;
        c.gridy = 0;
        outChoicesPanel.add(regionsPanel, c);

        // CHOICES SECTION - REGIONS
        checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new GridBagLayout());
        scrollPane = new JScrollPane(checkboxPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new DimensionUIResource(167, 95));
        scrollPane.setEnabled(true);
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 1;
        c.gridheight = 2;
        outChoicesPanel.add(scrollPane, c);

        // ADD REGIONS
        addTempRegions();

        // CHOICES SECTION - EXPORT BUTTON
        exportButton = new JButton("Export");
        exportButton.setPreferredSize(new DimensionUIResource(75, 25));
        c = new GridBagConstraints();
        c.insets = new InsetsUIResource(5, 0, 0, 0);
        c.gridx = 1;
        c.gridy = 3;
        outChoicesPanel.add(exportButton, c);

        // ACTION LISTENERS
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        manualButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backOptions();
            }
        });
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        // UPDATE FRAME
        jFrame.validate();
        jFrame.repaint();

    }

    // UPDATE CHOICES SECTION WITH ACCEPT LAYOUT
    public void acceptOptions() {

        // EMPTY CHOICES SECTION
        outChoicesPanel.removeAll();

        // CHOICES SECTION - BACK BUTTON
        backButton = new JButton("Back");
        backButton.setPreferredSize(new DimensionUIResource(167, 45));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new InsetsUIResource(0, 0, 0, 16);
        c.gridx = 0;
        c.gridy = 0;
        outChoicesPanel.add(backButton, c);

        // CHOICES SECTION - REGION TITLE
        regionsPanel = new JPanel();
        regionsPanel.setLayout(new GridBagLayout());
        regionsPanel.setPreferredSize(new DimensionUIResource(167, 45));
        regionsLabel = new JLabel("<html><u>Regions</u></html>");
        regionsPanel.add(regionsLabel);
        c = new GridBagConstraints();
        c.insets = new InsetsUIResource(0, 0, 5, 0);
        c.gridx = 1;
        c.gridy = 0;
        outChoicesPanel.add(regionsPanel, c);

        // CHOICES SECTION - REGIONS
        checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new GridBagLayout());
        scrollPane = new JScrollPane(checkboxPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new DimensionUIResource(167, 95));
        scrollPane.setEnabled(true);
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 1;
        c.gridheight = 2;
        outChoicesPanel.add(scrollPane, c);

        // ADD REGIONS
        addTempRegions();

        // CHOICES SECTION - EXPORT BUTTON
        exportButton = new JButton("Export");
        exportButton.setPreferredSize(new DimensionUIResource(75, 25));
        c = new GridBagConstraints();
        c.insets = new InsetsUIResource(5, 0, 0, 0);
        c.gridx = 1;
        c.gridy = 3;
        outChoicesPanel.add(exportButton, c);

        // ACTION LISTENERS
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                backOptions();
            }
        });
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        // UPDATE FRAME
        jFrame.validate();
        jFrame.repaint();

    }

    // CREATE TEMPORARY REGIONS
    public void addTempRegions() {

        JCheckBox region_1, region_2, region_3, region_4, region_5;

        region_1 = new JCheckBox("1");
        region_1.setMnemonic(KeyEvent.VK_C);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        checkboxPanel.add(region_1, c);

        region_2 = new JCheckBox("2");
        region_2.setMnemonic(KeyEvent.VK_C);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        checkboxPanel.add(region_2, c);

        region_3 = new JCheckBox("3");
        region_3.setMnemonic(KeyEvent.VK_C);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        checkboxPanel.add(region_3, c);

        region_4 = new JCheckBox("4");
        region_4.setMnemonic(KeyEvent.VK_C);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        checkboxPanel.add(region_4, c);

        region_5 = new JCheckBox("5");
        region_5.setMnemonic(KeyEvent.VK_C);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        checkboxPanel.add(region_5, c);

    }

    public void addRegions(Roi roi, int roiIndex) {

        JCheckBox region;
        //region = new JCheckBox(String.valueOf(roiIndex + 1));
        region = new JCheckBox(String.valueOf(roiIndex + 1) + " (" + roi.getName() + ")");
        region.setMnemonic(KeyEvent.VK_C);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = roiIndex;
        checkboxPanel.add(region, c);
        //System.out.println("Added region!");
        jFrame.validate();
        jFrame.repaint();
        //outChoicesPanel.add(regionsPanel);
        //regionsPanel.repaint();
    }

    // CREATE SLIDERS
    public JSlider makeSlider(int initial, int max) {

        JSlider tempSlider = new JSlider(JSlider.HORIZONTAL,1, max, initial);
        tempSlider.setMajorTickSpacing(max-1);
        tempSlider.setMinorTickSpacing(1);
        tempSlider.setPaintTicks(true);
        tempSlider.setPaintLabels(true);
        tempSlider.setSnapToTicks(true);

        return tempSlider;

    }

    // UPDATE CURRENT INPUT IMAGE
    public void updateImage(ImagePlus imp, JSlider channel, JSlider z, JSlider time, JLabel jLabel) {

        imp.setPosition(channel.getValue(), z.getValue(), time.getValue());
        imp.updateImage();
        BufferedImage image = resize(imp.getBufferedImage(), 350, 350);
        jLabel.setIcon(new ImageIcon(image));
        jLabel.repaint();

    }

    // RESIZE IMAGES
    public BufferedImage resize(BufferedImage img, int width, int height) {

        Image temp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = output.createGraphics();
        g2d.drawImage(temp, 0, 0, null);
        g2d.dispose();

        return output;

    }

    // OPEN FILES
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

    // RUN CNN
    private ImagePlus runCNN(ImagePlus imp) {

        String venvName = "env";
        String venvPath, venvCommand;
        String[] executionCommand;

        File dir = new File(model[0][0] + model[0][1]);
        String dirPath = model[0][0] + model[0][1];
        Path path = Paths.get(dirPath);

        String cnnFile = "";
        String saveDir = "";
        String cnnDir = "";

        try {
            List<Path> files = Files.walk(path).filter(Files::isRegularFile).collect(Collectors.toList());

            for(int i = 0; i < files.size(); i++) {
                if(files.get(i).toString().endsWith(".pb")) {
                    // This is supposed to get the directory that the *.pb saved model file is in.
                    // This stuff works in Eclipse...
                    cnnDir = files.get(i).getParent().toString();
                    //System.out.println("Directory for saved model: " + cnnDir);
                }
            }
        } catch (IOException fe) {
            fe.printStackTrace();
        }

        for (File file : dir.listFiles()) {
            if(file.getName().toLowerCase().endsWith((".py"))) {
                // Python filepath (*.py file from the jupyter notebook)
                cnnFile = file.getAbsolutePath();
                saveDir = file.getAbsoluteFile().getParent();
            }
        }

        String imageFilePath = saveDir + "/" + "file_for_cnn.png";
        File imageFile = new File(saveDir + "/" + "file_for_cnn.png");

        try {
            BufferedImage imageForCnn = resize(imp.getBufferedImage(), 350, 350);
            ImageIO.write(imageForCnn, "png", imageFile);
        } catch(IOException ie) {
            ie.printStackTrace();
        }

        String pythonCommand = cnnFile + " " + imageFile + " " + cnnDir;

        if (IJ.isWindows()){
            venvPath = venvName + "\\" + "Scripts\\activate.bat";
            venvCommand = model[0][0] + model[0][1] + "\\" + venvPath;
            String[] windowsCommand = {"cmd.exe", "/c", venvCommand + " & python " + pythonCommand};
            executionCommand = windowsCommand;
        }
        else {
            venvPath = venvName + "/" + "bin/activate";
            venvCommand = model[0][0] + model[0][1] + "/" + venvPath;
            String[] macCommand = {"/bin/bash", "-c", "source " + venvCommand + " && python " + pythonCommand};
            executionCommand = macCommand;
        }

        try {
            Process p = Runtime.getRuntime().exec(executionCommand, null, null);
            p.waitFor();
        }
        catch(Exception ioe) {
            ioe.printStackTrace();
        }

        try {
            Files.deleteIfExists(Paths.get(imageFilePath));
        } catch(Exception fe) {
            fe.printStackTrace();
        }

        ImagePlus imp_out = opener.openImage(saveDir, "result_output_test.tif");

        return(imp_out);

    }

}
