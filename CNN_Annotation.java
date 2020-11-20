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
import java.util.stream.Collectors;
import java.nio.file.*;
import java.util.List;
import javax.imageio.*;
import java.lang.ProcessBuilder;

public class CNN_Annotation implements PlugIn {

    final String[][] image = new String[1][1];
    final String[][] model = new String[1][1];

    JFrame jFrame;
    JPanel leftPanel, rightPanel;
    static File dir;

    private ImagePlus cnnOut;

    public void run(String arg) {
        startPane();
        IJ.register(CNN_Annotation.class);
    }

    public void startPane() {

        // VARIABLES
        final JFrame startFrame;
        final JPanel jPanel, subPanel;
        final JButton singleImage, cnnModel, reset, start;
        final JLabel selectImage, selectCNN;

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

    public void mainPane() {

        // VARIABLES
        final JPanel sliders, inputButtons;
        final JButton reset, runCNNOnOneImage, runCNNOnAllImages;
        final JLabel inputLabel, picLabel, zLabel, timeLabel, channelLabel;
        final JSlider zSlider, timeSlider, channelSlider;
        final ImagePlus imp;
        final Opener opener = new Opener();

        // MAIN FRAME
        jFrame = new JFrame();
        jFrame.setTitle("Cell Annotation with CNN");
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
        inputLabel = new JLabel();
        inputLabel.setText("Input Image");
        inputLabel.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 20, 0);
        leftPanel.add(inputLabel, c);

        // INPUT PANEL - IMAGE
        imp = opener.openImage(image[0][0], image[0][1]);
        BufferedImage myPicture = resize(imp.getBufferedImage(), 350, 350);
        picLabel = new JLabel(new ImageIcon(myPicture));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 20, 0);
        leftPanel.add(picLabel, c);

        // INPUT PANEL - SLIDER PANEL
        sliders = new JPanel();
        sliders.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        sliders.setPreferredSize(new Dimension(350, 150));
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(0, 0, 20, 0);
        leftPanel.add(sliders, c);

        // INPUT PANEL - SLIDER PANEL - Z SLIDER
        zLabel = new JLabel("<html><center>z-stack<br>" + imp.getSlice() + "</center></html>");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 10, 20);
        sliders.add(zLabel, c);
        zSlider = makeSlider(imp.getSlice(), imp.getNSlices());
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.ipadx = 225;
        c.insets = new Insets(0, 0, 10, 0);
        sliders.add(zSlider, c);

        // INPUT PANEL - SLIDER PANEL - TIME SLIDER
        timeLabel = new JLabel("<html><center>time-course<br>" + imp.getFrame() + "</center></html>");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 10, 20);
        sliders.add(timeLabel, c);
        timeSlider = makeSlider(imp.getFrame(), imp.getNFrames());
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.ipadx = 225;
        c.insets = new Insets(0, 0, 10, 0);
        sliders.add(timeSlider, c);

        // INPUT PANEL - SLIDER PANEL - CHANNEL SLIDER
        channelLabel = new JLabel("<html><center>channel<br>" + imp.getChannel() + "</center></html>");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 20);
        sliders.add(channelLabel, c);
        channelSlider = makeSlider(imp.getChannel(), imp.getNChannels());
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.ipadx = 225;
        sliders.add(channelSlider, c);

        // INPUT PANEL - CHOICES PANEL
        inputButtons = new JPanel();
        inputButtons.setLayout(new GridBagLayout());
        inputButtons.setPreferredSize(new Dimension(350, 100));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.insets = new Insets(0, 0, 0, 0);
        leftPanel.add(inputButtons, c);

        // INPUT PANEL - CHOICES PANEL - RESET BUTTON
        reset = new JButton();
        reset.setText("Reset");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 0;
        c.ipady = 25;
        c.insets = new Insets(0, 0, 0, 25);
        inputButtons.add(reset, c);

        // INPUT PANEL - CHOICES PANEL - RUN CNN ON ONE IMAGE BUTTON
        runCNNOnOneImage = new JButton();
        runCNNOnOneImage.setText("Run");
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.ipadx = 50;
        c.ipady = 25;
        c.insets = new Insets(0, 0, 0, 25);
        inputButtons.add(runCNNOnOneImage, c);

        // INPUT PANEL - CHOICES PANEL - RUN CNN ON ALL IMAGES BUTTON

        runCNNOnAllImages = new JButton();
        runCNNOnAllImages.setText("Run All");
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.ipadx = 50;
        c.ipady = 25;
        //c.insets = new Insets(0, 0, 0, )
        //inputButtons.add(runCNNOnAllImages, c);

        // ACTION LISTENERS
        zSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = zSlider.getValue();
                zLabel.setText("<html><center>z-stack<br>" + value + "</center></html>");
                updateImage(imp, channelSlider, zSlider, timeSlider, picLabel);
            }
        });
        timeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = timeSlider.getValue();
                timeLabel.setText("<html><center>time-course<br>" + value + "</center></html>");
                updateImage(imp, channelSlider, zSlider, timeSlider, picLabel);
            }
        });
        channelSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = channelSlider.getValue();
                channelLabel.setText("<html><center>channel<br>" + value + "</center></html>");
                updateImage(imp, channelSlider, zSlider, timeSlider, picLabel);
            }
        });
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jFrame.dispose();
                startPane();
            }
        });
        runCNNOnOneImage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (jFrame.getWidth() == 900) {
                    jFrame.remove(rightPanel);
                }
                cnnOut = runCNN(imp);
//                cnnOut = opener.openImage("D:\\Downloads", "result_output_test.tif");
                addOutputPane();
            }
        });
        /*
        runCNNOnAllImages.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jFrame.getWidth() == 900) {
                    jFrame.remove(rightPanel);
                }
                addOutputPane(imp, true, true);
            }
        });
        */
        // UPDATING MAIN FRAME
        jFrame.pack();
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

    public void addOutputPane() {

        // VARIABLES
        final JPanel sliderPanel_out, choicePanel_out;
        final JButton denyResults, acceptResults;
        final JLabel outputLabel, imLabel_out, zLabel_out, timeLabel_out, channelLabel_out;
        final JSlider zSlider_out, timeSlider_out, channelSlider_out;
        final ImagePlus imp_out;
        final Opener opener = new Opener();

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
        outputLabel = new JLabel();
        outputLabel.setText("CNN Output");
        outputLabel.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 20, 0);
        rightPanel.add(outputLabel, c);

        // RIGHT PANEL - OUTPUT IMAGE
        BufferedImage bufferedImage_out = resize(cnnOut.getBufferedImage(), 350, 350);
        imLabel_out = new JLabel(new ImageIcon(bufferedImage_out));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 20, 0);
        rightPanel.add(imLabel_out, c);
        /*
        if (update_image == true){
            if (run_all == false){
                imp_out = runCNN();
                BufferedImage bufferedImage_out = resize(imp_out.getBufferedImage(), 350, 350);
                imLabel_out = new JLabel(new ImageIcon(bufferedImage_out));
                c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = 1;
                c.insets = new Insets(0, 0, 20, 0);
                rightPanel.add(imLabel_out, c);
            }
            else {
                //File dir = new File("/Users/adityasuresh/comp523/image_analysis-master");
                // The code below until line 445 gets the saved model (*.pb file), the Python CNN prediction
                // file (*.py) and the location of the output file (saveDir).
                //System.setProperty("scijava.log.level", "trace");
                File dir = new File(model[0][0] + model[0][1]);

                String dirPath = model[0][0] + model[0][1];
                //String dirPath = "/Users/adityasuresh/comp523/image_analysis-master/";
                Path path = Paths.get(dirPath);

                String cnnDir = "";

                try {
                    List<Path> files = Files.walk(path).filter(Files::isRegularFile).collect(Collectors.toList());

                    for(int i = 0; i < files.size(); i++) {
                        if(files.get(i).toString().endsWith(".pb")) {
                            // This is supposed to get the direcory that the *.pb saved model file is in.
                            // This stuff works in Eclipse...
                            cnnDir = files.get(i).getParent().toString();
                            //System.out.println("Directory for saved model: " + cnnDir);
                        }
                    }

                } catch (IOException fe) {
                      fe.printStackTrace();
                }

                String cnnFile = "";
                saveDir = "";
                for (File file : dir.listFiles()) {
                      if(file.getName().toLowerCase().endsWith((".py"))) {
                          // Python filepath (*.py file from the jupyter notebook)
                          cnnFile = file.getAbsolutePath();
                          saveDir = file.getAbsoluteFile().getParent();
                      }

                }

                  //System.out.println("Run this .py file: " + cnnFile);

                ///Users/adityasuresh/comp523/image_analysis-master/image_analysis-Copy1.py"
                ///Library/Frameworks/Python.framework/Versions/3.6/bin/


                //String commandToRun = "";

                try {
                    //commandToRun = "/Library/Frameworks/Python.framework/Versions/3.6/bin/python3 " + cnnFile + " " + cnnImage[0][0] + cnnImage[0][1] + " " + cnnDir;
                    // Currently path to python3 is hardcoded, I tried to get it out of running "which python3" as a command but that didn't work... We'll need to
                    // figure that out. Running with just "python3 " also doesn't work, the full path is necessary.
                    Process p = Runtime.getRuntime().exec("/Library/Frameworks/Python.framework/Versions/3.6/bin/python3 " + cnnFile + " " + image[0][0] + image[0][1] + " " + cnnDir);
                    //Process p = Runtime.getRuntime().exec("/Library/Frameworks/Python.framework/Versions/3.6/bin/python3 " + cnnFile + " " + imageFile + " " + cnnDir);
                    //Process p = Runtime.getRuntime().exec("python3 " + cnnFile + " " + cnnImage[0][0] + cnnImage[0][1] + " " + cnnDir);
                    //Process p = Runtime.getRuntime().exec("/Library/Frameworks/Python.framework/Versions/3.6/bin/python3 /Users/adityasuresh/comp523/image_analysis-master/image_analysis-Copy1.py " + "/Users/z_stack_timecourse_example.tif " + "/Users/adityasuresh/comp523/image_analysis-master/content/");
                    p.waitFor();
                }
                catch(Exception ioe) {
                    ioe.printStackTrace();
                }


                imp_out = opener.openImage(saveDir, "result_output_test.tif");
                BufferedImage bufferedImage_out = resize(imp_out.getBufferedImage(), 350, 350);
                imLabel_out = new JLabel(new ImageIcon(bufferedImage_out));
                c = new GridBagConstraints();
                c.gridx = 0;
                c.gridy = 1;
                c.insets = new Insets(0, 0, 20, 0);
                rightPanel.add(imLabel_out, c);
            }
        }
        else {


            if(saveDir.length() ==  0 || saveDir == null){
                saveDir = "this is junk and needs to change";
            }

            imp_out = imp;
            //imp_out = opener.openImage(saveDir, "result_output_test.tif");
            //BufferedImage bufferedImage_out = resize(imp.getBufferedImage(), 350, 350);
            //imLabel_out = new JLabel(new ImageIcon(bufferedImage_out));
            imLabel_out = new JLabel(new ImageIcon(imp.getBufferedImage()));
            c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 1;
            c.insets = new Insets(0, 0, 20, 0);
            rightPanel.add(imLabel_out, c);
        }

        // RIGHT PANEL - SLIDER PANEL
        sliderPanel_out = new JPanel();
        sliderPanel_out.setLayout(new GridBagLayout());
        sliderPanel_out.setPreferredSize(new Dimension(350, 150));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(0, 0, 20, 0);
        rightPanel.add(sliderPanel_out, c);

        // RIGHT PANEL - SLIDER PANEL - Z SLIDER
        zLabel_out = new JLabel("<html><center>z-stack<br>" + imp_out.getSlice() + "</center></html>");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 10, 20);
        sliderPanel_out.add(zLabel_out, c);
        zSlider_out = makeSlider(imp_out.getSlice(), imp_out.getNSlices());
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.ipadx = 225;
        c.insets = new Insets(0, 0, 10, 0);
        sliderPanel_out.add(zSlider_out, c);

        // RIGHT PANEL - SLIDER PANEL - TIME SLIDER
        timeLabel_out = new JLabel("<html><center>time-course<br>" + imp_out.getFrame() + "</center></html>");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 10, 20);
        sliderPanel_out.add(timeLabel_out, c);
        timeSlider_out = makeSlider(imp_out.getFrame(), imp_out.getNFrames());
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.ipadx = 225;
        c.insets = new Insets(0, 0, 10, 0);
        sliderPanel_out.add(timeSlider_out, c);

        // RIGHT PANEL - SLIDER PANEL - CHANNEL SLIDER
        channelLabel_out = new JLabel("<html><center>channel<br>" + imp_out.getChannel() + "</center></html>");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 20);
        sliderPanel_out.add(channelLabel_out, c);
        channelSlider_out = makeSlider(imp_out.getChannel(), imp_out.getNChannels());
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.ipadx = 225;
        sliderPanel_out.add(channelSlider_out, c);
        */
        // RIGHT PANEL - CHOICES PANEL
        choicePanel_out = new JPanel();
        choicePanel_out.setLayout(new GridBagLayout());
        choicePanel_out.setPreferredSize(new Dimension(350, 100));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.insets = new Insets(0, 0, 120, 0);
        rightPanel.add(choicePanel_out, c);

        // RIGHT PANEL - CHOICES PANEL - DENY BUTTON
        denyResults = new JButton();
        denyResults.setText("<html><center>Deny and<br>Edit Results</center><html>");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 64;
        //c.ipady = 9;
        c.fill = GridBagConstraints.VERTICAL;
        c.insets = new Insets(0, 0, 0, 40);
        choicePanel_out.add(denyResults, c);

        // RIGHT PANEL - CHOICES PANEL - ACCEPT BUTTON
        acceptResults = new JButton();
        acceptResults.setText("Accept Results");
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.ipadx = 25;
        c.ipady = 25;
        c.insets = new Insets(0, 0, 0, 0);
        choicePanel_out.add(acceptResults, c);

        // ACTION LISTENERS
        /*
        zSlider_out.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = zSlider_out.getValue();
                zLabel_out.setText("<html><center>z-stack<br>" + value + "</center></html>");
                updateImage(imp_out, channelSlider_out, zSlider_out, timeSlider_out, imLabel_out);
            }
        });
        timeSlider_out.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = timeSlider_out.getValue();
                timeLabel_out.setText("<html><center>time-course<br>" + value + "</center></html>");
                updateImage(imp_out, channelSlider_out, zSlider_out, timeSlider_out, imLabel_out);
            }
        });
        channelSlider_out.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = channelSlider_out.getValue();
                channelLabel_out.setText("<html><center>channel<br>" + value + "</center></html>");
                updateImage(imp_out, channelSlider_out, zSlider_out, timeSlider_out, imLabel_out);
            }
        });
        */
        denyResults.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jFrame.remove(rightPanel);
                denyResults(cnnOut);
            }
        });
        acceptResults.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jFrame.remove(rightPanel);
                acceptResults(cnnOut);
            }
        });

        // UPDATE MAIN FRAME
        jFrame.pack();
        jFrame.setLocation(jFrame.getLocation());
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

    public void denyResults(ImagePlus imp_out) {

        // VARIABLES
        final JPanel choicePanel_deny, checkboxPanel;
        final JButton editCNNRegions, freehand, segmented, back, export;
        final JLabel outputLabel, imLabel_out, regionTitle;
        final JCheckBox region_1, region_2, region_3, region_4, region_5, region_6;

        // RIGHT PANEL
        rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        jFrame.add(rightPanel);

        // RIGHT PANEL - TITLE
        outputLabel = new JLabel();
        outputLabel.setText("CNN Output");
        outputLabel.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 20, 0);
        rightPanel.add(outputLabel, c);

        // RIGHT PANEL - OUTPUT IMAGE
        BufferedImage bufferedImage_out = resize(imp_out.getBufferedImage(), 350, 350);
        imLabel_out = new JLabel(new ImageIcon(bufferedImage_out));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 20, 0);
        rightPanel.add(imLabel_out, c);

        // RIGHT PANEL - CHOICES PANEL
        choicePanel_deny = new JPanel();
        choicePanel_deny.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(0, 0, 22, 0);
        rightPanel.add(choicePanel_deny, c);

        // RIGHT PANEL - CHOICES PANEL - EDIT CNN BUTTON
        editCNNRegions = new JButton();
        editCNNRegions.setText("Edit CNN Results");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 25;
        c.ipady = 25;
        c.insets = new Insets(0, 0, 5, 50);
        choicePanel_deny.add(editCNNRegions, c);

        // RIGHT PANEL - CHOICES PANEL - FREEHAND BUTTON
        freehand = new JButton();
        freehand.setText("Manual Annotation");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.ipadx = 0;
        c.ipady = 25;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 5, 50);
        choicePanel_deny.add(freehand, c);

        // RIGHT PANEL - CHOICES PANEL - SEGMENTED BUTTON
        segmented = new JButton();
        segmented.setText("Segmented Lines");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.ipadx = 0;
        c.ipady = 25;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 5, 50);
        //choicePanel_deny.add(segmented, c);

        // RIGHT PANEL - CHOICES PANEL - BACK BUTTON
        back = new JButton();
        back.setText("Return");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.ipady = 25;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 50);
        choicePanel_deny.add(back, c);

        // RIGHT PANEL - CHOICES PANEL - REGION TITLE
        regionTitle = new JLabel();
        regionTitle.setText("<html><u>Regions</u></html>");
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.ipady = 25;
        c.fill = GridBagConstraints.HORIZONTAL;
        choicePanel_deny.add(regionTitle, c);

        // RIGHT PANEL - CHOICES PANEL - CHECKBOX PANE
        checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new GridBagLayout());
        JScrollPane scrollpane = new JScrollPane();
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.gridheight = 2;
        c.ipadx = 125;
        c.fill = GridBagConstraints.VERTICAL;
        c.insets = new Insets(0, 0, 0, 0);
        choicePanel_deny.add(scrollpane, c);
        scrollpane.setViewportView(checkboxPanel);

        // RIGHT PANEL - CHOICES PANEL - CHECKBOX PANEL - REGION 1
        region_1 = new JCheckBox("1");
        region_1.setMnemonic(KeyEvent.VK_C);
        region_1.setSelected(true);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 50);
        checkboxPanel.add(region_1, c);

        // RIGHT PANEL - CHOICES PANEL - CHECKBOX PANEL - REGION 2
        region_2 = new JCheckBox("2");
        region_2.setMnemonic(KeyEvent.VK_C);
        region_2.setSelected(true);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 50);
        checkboxPanel.add(region_2, c);

        // RIGHT PANEL - CHOICES PANEL - CHECKBOX PANEL - REGION 3
        region_3 = new JCheckBox("3");
        region_3.setMnemonic(KeyEvent.VK_C);
        region_3.setSelected(true);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 50);
        checkboxPanel.add(region_3, c);

        // RIGHT PANEL - CHOICES PANEL - CHECKBOX PANEL - REGION 4
        region_4 = new JCheckBox("4");
        region_4.setMnemonic(KeyEvent.VK_C);
        region_4.setSelected(true);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.insets = new Insets(0, 0, 0, 50);
        checkboxPanel.add(region_4, c);

        // RIGHT PANEL - CHOICES PANEL - CHECKBOX PANEL - REGION 5
        region_5 = new JCheckBox("5");
        region_5.setMnemonic(KeyEvent.VK_C);
        region_5.setSelected(true);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.insets = new Insets(0, 0, 0, 50);
        checkboxPanel.add(region_5, c);

        // RIGHT PANEL - CHOICES PANEL - CHECKBOX PANEL - REGION 6
        region_6 = new JCheckBox("6");
        region_6.setMnemonic(KeyEvent.VK_C);
        region_6.setSelected(true);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 5;
        c.insets = new Insets(0, 0, 0, 50);
        checkboxPanel.add(region_6, c);

        // RIGHT PANEL - CHOICES PANEL - EXPORT
        export = new JButton();
        export.setText("Export");
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 3;
        c.insets = new Insets(10, 0, 0, 0);
        choicePanel_deny.add(export, c);

        // ACTION LISTENERS
        editCNNRegions.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        freehand.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //imp_out.setIJMenuBar(true);
              //  Roi roi = imp_out.getRoi();
                imp_out.setIgnoreFlush(true);
                imp_out.show();

            }
        });
        segmented.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jFrame.remove(rightPanel);
                addOutputPane();
            }
        });
        export.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        // UPDATE MAIN FRAME
        jFrame.pack();
        jFrame.setLocation(jFrame.getLocation());
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

    public void acceptResults(ImagePlus imp_out) {

        // VARIABLES
        final JPanel choicePanel_accept, checkboxPanel;
        final JButton advanced, back, export;
        final JLabel outputLabel, imLabel_out, regionTitle;
        final JCheckBox region_1, region_2, region_3, region_4, region_5, region_6;

        // RIGHT PANEL
        rightPanel = new JPanel();
        rightPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        jFrame.add(rightPanel);

        // RIGHT PANEL - TITLE
        outputLabel = new JLabel();
        outputLabel.setText("CNN Output");
        outputLabel.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 20, 0);
        rightPanel.add(outputLabel, c);

        // RIGHT PANEL - OUTPUT IMAGE
        BufferedImage bufferedImage_out = resize(imp_out.getBufferedImage(), 350, 350);
        imLabel_out = new JLabel(new ImageIcon(bufferedImage_out));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 20, 0);
        rightPanel.add(imLabel_out, c);

        // RIGHT PANEL - CHOICES PANEL
        choicePanel_accept = new JPanel();
        choicePanel_accept.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(0, 0, 6, 0);
        rightPanel.add(choicePanel_accept, c);

        // RIGHT PANEL - CHOICES PANEL - ADVANCED
        advanced = new JButton();
        advanced.setText("Advanced");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 62;
        c.ipady = 25;
        c.insets = new Insets(0, 0, 5, 50);
        //choicePanel_accept.add(advanced, c);

        // RIGHT PANEL - CHOICES PANEL - BACK BUTTON
        back = new JButton();
        back.setText("Return");
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.ipadx = 80;
        c.ipady = 25;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 50);
        choicePanel_accept.add(back, c);

        // RIGHT PANEL - CHOICES PANEL - REGION TITLE
        regionTitle = new JLabel();
        regionTitle.setText("<html><u>Regions</u></html>");
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.ipady = 25;
        c.fill = GridBagConstraints.HORIZONTAL;
        choicePanel_accept.add(regionTitle, c);

        // RIGHT PANEL - CHOICES PANEL - CHECKBOX PANE
        checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new GridBagLayout());
        JScrollPane scrollpane = new JScrollPane();
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.gridheight = 2;
        c.ipadx = 125;
        c.ipady = 110;
        c.fill = GridBagConstraints.VERTICAL;
        c.insets = new Insets(0, 0, 5, 0);
        choicePanel_accept.add(scrollpane, c);
        scrollpane.setViewportView(checkboxPanel);

        // RIGHT PANEL - CHOICES PANEL - CHECKBOX PANEL - REGION 1
        region_1 = new JCheckBox("1");
        region_1.setMnemonic(KeyEvent.VK_C);
        region_1.setSelected(true);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 65);
        checkboxPanel.add(region_1, c);

        // RIGHT PANEL - CHOICES PANEL - CHECKBOX PANEL - REGION 2
        region_2 = new JCheckBox("2");
        region_2.setMnemonic(KeyEvent.VK_C);
        region_2.setSelected(true);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.insets = new Insets(0, 0, 0, 65);
        checkboxPanel.add(region_2, c);

        // RIGHT PANEL - CHOICES PANEL - CHECKBOX PANEL - REGION 3
        region_3 = new JCheckBox("3");
        region_3.setMnemonic(KeyEvent.VK_C);
        region_3.setSelected(true);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.insets = new Insets(0, 0, 0, 65);
        checkboxPanel.add(region_3, c);

        // RIGHT PANEL - CHOICES PANEL - CHECKBOX PANEL - REGION 4
        region_4 = new JCheckBox("4");
        region_4.setMnemonic(KeyEvent.VK_C);
        region_4.setSelected(true);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.insets = new Insets(0, 0, 0, 65);
        checkboxPanel.add(region_4, c);

        // RIGHT PANEL - CHOICES PANEL - CHECKBOX PANEL - REGION 5
        region_5 = new JCheckBox("5");
        region_5.setMnemonic(KeyEvent.VK_C);
        region_5.setSelected(true);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 4;
        c.insets = new Insets(0, 0, 0, 65);
        checkboxPanel.add(region_5, c);

        // RIGHT PANEL - CHOICES PANEL - CHECKBOX PANEL - REGION 6
        region_6 = new JCheckBox("6");
        region_6.setMnemonic(KeyEvent.VK_C);
        region_6.setSelected(true);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 5;
        c.insets = new Insets(0, 0, 0, 65);
        //checkboxPanel.add(region_6, c);

        // RIGHT PANEL - CHOICES PANEL - EXPORT
        export = new JButton();
        export.setText("Export");
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 3;
        choicePanel_accept.add(export, c);

        // ACTION LISTENERS
        advanced.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jFrame.remove(rightPanel);
                addOutputPane();
            }
        });
        export.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        // UPDATE MAIN FRAME
        jFrame.pack();
        jFrame.setLocation(jFrame.getLocation());
        jFrame.setVisible(true);
        jFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

    public JSlider makeSlider(int initial, int max) {

        JSlider tempSlider = new JSlider(JSlider.HORIZONTAL,1, max, initial);
        tempSlider.setMajorTickSpacing(max-1);
        tempSlider.setMinorTickSpacing(1);
        tempSlider.setPaintTicks(true);
        tempSlider.setPaintLabels(true);
        tempSlider.setSnapToTicks(true);

        return tempSlider;

    }

    public void updateImage(ImagePlus imp, JSlider channel, JSlider z, JSlider time, JLabel jLabel) {

        imp.setPosition(channel.getValue(), z.getValue(), time.getValue());
        imp.updateImage();
        BufferedImage image = resize(imp.getBufferedImage(), 350, 350);
        jLabel.setIcon(new ImageIcon(image));
        jLabel.repaint();

    }

    public BufferedImage resize(BufferedImage img, int width, int height) {

        Image temp = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = output.createGraphics();
        g2d.drawImage(temp, 0, 0, null);
        g2d.dispose();

        return output;

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

    private ImagePlus runCNN(ImagePlus imp) {
        final Opener opener = new Opener();
        final ImagePlus imp_out;

        File dir = new File(model[0][0] + model[0][1]);
        String dirPath = model[0][0] + model[0][1];
        Path path = Paths.get(dirPath);
        String cnnDir = "";

        try {
            List<Path> files = Files.walk(path).filter(Files::isRegularFile).collect(Collectors.toList());

            for(int i = 0; i < files.size(); i++) {
                if(files.get(i).toString().endsWith(".pb")) {
                // This is supposed to get the direcory that the *.pb saved model file is in.
                // This stuff works in Eclipse...
                cnnDir = files.get(i).getParent().toString();
                //System.out.println("Directory for saved model: " + cnnDir);
                }
            }

        } catch (IOException fe) {
            fe.printStackTrace();
        }

        String cnnFile = "";
        String saveDir = "";
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

        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        StringBuilder output = new StringBuilder();

        String[] commands_mac = {"/bin/bash", "-c", "source ~/.bash_profile && which python3"};
        String[] commands_win = {"cmd.exe", "/c", ""};

        String[] commands = {};

        if(isWindows){
            commands = commands_win;
        }
        else{
            commands = commands_mac;
        }
        try {


            Process p = Runtime.getRuntime().exec(commands, null, new File(System.getProperty("user.home")));

            p.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;

            while((line = reader.readLine()) != null){
                output.append(line);
            }

            //p.waitFor();

        }
        catch(IOException bfe){
            bfe.printStackTrace();
        }


        catch(InterruptedException iee){
            iee.printStackTrace();
        }

        try {
            //commandToRun = "/Library/Frameworks/Python.framework/Versions/3.6/bin/python3 " + cnnFile + " " + cnnImage[0][0] + cnnImage[0][1] + " " + cnnDir;
            // Currently path to python3 is hardcoded, I tried to get it out of running "which python3" as a command but that didn't work... We'll need to
            // figure that out. Running with just "python3 " also doesn't work, the full path is necessary.
            //Process p = Runtime.getRuntime().exec("/Library/Frameworks/Python.framework/Versions/3.6/bin/python3 " + cnnFile + " " + cnnImage[0][0] + cnnImage[0][1] + " " + cnnDir);

            // Changes for Leo and windows, uncomment line below to run
            //Process p = Runtime.getRuntime().exec("C:\\Users\\leowe\\AppData\\Local\\Programs\\Python\\Python37\\python.exe " + cnnFile + " " + imageFile + " " + cnnDir);
            //Process p = Runtime.getRuntime().exec("/Library/Frameworks/Python.framework/Versions/3.6/bin/python3 " + cnnFile + " " + imageFile + " " + cnnDir);
            Process p = Runtime.getRuntime().exec(output.toString() + " " + cnnFile + " " + imageFile + " " + cnnDir);
            //Process p = Runtime.getRuntime().exec("python3 " + cnnFile + " " + cnnImage[0][0] + cnnImage[0][1] + " " + cnnDir);
            //Process p = Runtime.getRuntime().exec("/Library/Frameworks/Python.framework/Versions/3.6/bin/python3 /Users/adityasuresh/comp523/image_analysis-master/image_analysis-Copy1.py " + "/Users/z_stack_timecourse_example.tif " + "/Users/adityasuresh/comp523/image_analysis-master/content/");
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

        imp_out = opener.openImage(saveDir, "result_output_test.tif");

        return(imp_out);
    }

}
