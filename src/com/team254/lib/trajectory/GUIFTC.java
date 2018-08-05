package com.team254.lib.trajectory; /**
 * Created by Florent Astié on 5/31/2017.
 */
import com.team254.lib.trajectory.Path;
import com.team254.lib.trajectory.PathGenerator;
import com.team254.lib.trajectory.TrajectoryGenerator;
import com.team254.lib.trajectory.WaypointSequence;
import com.team254.lib.trajectory.io.TextFileSerializer;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.*;

import static com.sun.glass.ui.Cursor.setVisible;
import static com.team254.lib.trajectory.Main.joinPath;
import static java.awt.Color.BLACK;
import static java.awt.Color.DARK_GRAY;
import static java.awt.Color.RED;

public class GUIFTC extends JFrame implements ActionListener,MouseListener{
    double dt = 0.033333333;
    double max_acc = 40.0;
    double max_jerk = 60.0;
    double max_vel = 35;
    DecimalFormat d = new DecimalFormat("#.####");
    double basex = 0;
    double basey = 0;
    TrajectoryGenerator.Config config = new TrajectoryGenerator.Config(dt,max_acc,max_vel,max_jerk);


    public static String joinPath(String path1, String path2)
    {
        File file1 = new File(path1);
        File file2 = new File(file1, path2);
        return file2.getPath();
    }

    private static boolean writeFile(String path, String data) {
        try {
            File file = new File(path);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(data);
            bw.close();
        } catch (IOException e) {
            return false;
        }

        return true;
    }
    JLabel field;
    JTextField filein;
    JLabel width;
    JTextField widthInput;
    JTextField xField;
    JTextField yField;
    double xInput,yInput;
    JLabel filepath;
    JButton reset;
    JLabel feedback;
    JLabel angInput;
    JLabel totalWaypoints;
    JButton export;
    JButton setWidth;
    JButton addWaypoint;
    JTextField angleInput;
    double pixelsPerInchY = 600 / 12.0 / 12.0;
    double pixelsPerInchX = 600 / 12.0 / 12.0;
    WaypointSequence p = new WaypointSequence(10);

    final double kWheelbaseWidth = 15.25;
    String path_name = "test";
    Path path;
    public GUIFTC(){
        super("Trajectory Planner");
        setSize(1366,730);
        setVisible(true);
        setWidth = new JButton("Remove Last Waypoint");
        SpringLayout layout = new SpringLayout();
        setLayout(layout);
        ImageIcon icon = new ImageIcon("field.jpg");
        field = new JLabel(icon);
        widthInput = new JTextField(4);
        width = new JLabel("Wheelbase width:");
        angInput = new JLabel("Angle in degrees: ");
        feedback = new JLabel();
        xField = new JTextField(5);
        yField = new JTextField(5);
        filein = new JTextField(10);
        filepath = new JLabel("Target filename: ");
        totalWaypoints = new JLabel("Total Waypoints: 0");
        export = new JButton("To text file!");
        reset = new JButton("Reset");
        addWaypoint = new JButton("Add Waypoint");
        angleInput = new JTextField(10);
        reset.setVisible(true);

        field.setBounds(0, 0, 601, 601);

        add(filepath);
        add(field);
        add(xField);
        add(yField);
        add(angInput);
        add(reset);
        add(angleInput);
        add(totalWaypoints);
        add(feedback);
        add(addWaypoint);
        add(filein);
        add(export);
        add(setWidth);
        export.addActionListener(this);
        reset.addActionListener(this);
        addWaypoint.addActionListener(this);
        field.addMouseListener(this);
        setWidth.addActionListener(this);

        layout.putConstraint(SpringLayout.WEST, field, 0, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, field, 0, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, reset,1200 +15,SpringLayout.WEST, field);
        layout.putConstraint(SpringLayout.NORTH, reset, 120, SpringLayout.NORTH, field);
        layout.putConstraint(SpringLayout.WEST, xField,1200 +15,SpringLayout.WEST, field);
        layout.putConstraint(SpringLayout.NORTH, xField, 43, SpringLayout.NORTH, field);
        layout.putConstraint(SpringLayout.WEST, yField,1260 +15,SpringLayout.WEST, field);
        layout.putConstraint(SpringLayout.NORTH, yField, 43, SpringLayout.NORTH, field);
        layout.putConstraint(SpringLayout.WEST, angleInput,1200 +15,SpringLayout.WEST, field);
        layout.putConstraint(SpringLayout.NORTH, angleInput, -20, SpringLayout.NORTH, reset);
        layout.putConstraint(SpringLayout.WEST, addWaypoint,1200 +15,SpringLayout.WEST, field);
        layout.putConstraint(SpringLayout.NORTH, addWaypoint, 5, SpringLayout.SOUTH, reset);
        layout.putConstraint(SpringLayout.WEST, angInput,1200 +15,SpringLayout.WEST, field);
        layout.putConstraint(SpringLayout.NORTH, angInput, -25, SpringLayout.NORTH, angleInput);
        layout.putConstraint(SpringLayout.WEST, export,1200 +15,SpringLayout.WEST, field);
        layout.putConstraint(SpringLayout.NORTH, export, 35, SpringLayout.NORTH, addWaypoint);
        layout.putConstraint(SpringLayout.WEST, totalWaypoints,1200 +15,SpringLayout.WEST, field);
        layout.putConstraint(SpringLayout.NORTH, totalWaypoints, 35, SpringLayout.NORTH, export);
        layout.putConstraint(SpringLayout.WEST, filepath,1200 +15,SpringLayout.WEST, field);
        layout.putConstraint(SpringLayout.NORTH, filepath, 20, SpringLayout.NORTH, totalWaypoints);
        layout.putConstraint(SpringLayout.WEST, filein,1200 +15,SpringLayout.WEST, field);
        layout.putConstraint(SpringLayout.NORTH, filein, 15, SpringLayout.NORTH, filepath);
        layout.putConstraint(SpringLayout.WEST, setWidth,1170 +15,SpringLayout.WEST, field);
        layout.putConstraint(SpringLayout.NORTH, setWidth, 20, SpringLayout.NORTH, filein);




        // add the button to the JFrame
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );


        d.setRoundingMode(RoundingMode.CEILING);

    }
    public void paint(Graphics g){
            super.paint(g);
            Graphics2D g2 = (Graphics2D) g;
        if (p.getNumWaypoints() >= 2) {
            Trajectory left = path.getLeftWheelTrajectory();
            Trajectory right = path.getRightWheelTrajectory();

            for (int i = 0; i < (path.getLeftWheelTrajectory().getNumSegments() - 1); i ++) {
                Trajectory.Segment segmentl = left.getSegment(i);
                Trajectory.Segment segmentr = right.getSegment(i);
                Trajectory.Segment segmentl2 = left.getSegment(i + 1);
                Trajectory.Segment segmentr2 = right.getSegment(i + 1);
                Double xin = segmentl.x * pixelsPerInchX;
                Double yin = segmentl.y * pixelsPerInchY;
                Double xin2 = segmentl2.x * pixelsPerInchX;
                Double yin2 = segmentl2.y * pixelsPerInchY;
                Double xin3 = segmentr.x * pixelsPerInchX;
                Double yin3 = segmentr.y * pixelsPerInchY;
                Double xin4 = segmentr2.x * pixelsPerInchX;
                Double yin4 = segmentr2.y * pixelsPerInchY;

                /*Double t1 = test.Waypoints.get(scount).getX() * pixelsPerInchX;
                Double t2 =  test.Waypoints.get(scount).getY() * pixelsPerInchY;
                Double t3 =  test.Waypoints.get(scount).getX() * pixelsPerInchX;
                Double t4 =  test.Waypoints.get(scount).getY() * pixelsPerInchY;*/

                //g2.drawLine(t1.intValue(),t2.intValue(),t3.intValue(),t4.intValue());
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(4));
                g2.drawLine(xin.intValue() + 8, yin.intValue() + 32, xin2.intValue() + 8, yin2.intValue() + 32);
                g2.setColor(Color.BLACK);
                g2.drawLine(xin3.intValue() + 8, yin3.intValue() + 32, xin4.intValue() + 8, yin4.intValue() + 32);

            }

            for (int num = 0; num < p.getNumWaypoints(); num++) {
                g2.setColor(RED);
                g2.drawOval((int)(p.getWaypoint(num).x*pixelsPerInchX)+5,(int)(p.getWaypoint(num).y*pixelsPerInchY)+ 28,5,5);
            }


        }


    }

    public void actionPerformed( ActionEvent evt) {
        if (evt.getSource() == reset) {
            p.removeWaypoints();
            totalWaypoints.setText("Total waypoints: " + p.getNumWaypoints());
            repaint();
        }
        if (evt.getSource() == export) {
            String directory = "C:\\Users\\HTML\\Desktop\\Paths";
            final String path_name = "Test";
            /*path =  PathGenerator.makePath(p, config,
                    kWheelbaseWidth, path_name);*/
            // Outputs to the directory supplied as the first argument.
            TextFileSerializer js = new TextFileSerializer();
            String serialized = js.serialize(path);
            //System.out.print(serialized);
            String fullpath = joinPath(directory, path_name + ".txt");
            if (!writeFile(fullpath, serialized)) {
                System.err.println(fullpath + " could not be written!!!!");
                System.exit(1);
            } else {
                System.out.println("Wrote " + fullpath);
            }

            filein.setText("");
            feedback.setText("Wrote path to text file.");
        }

        if(evt.getSource() == addWaypoint){
          //  test.Waypoints.add(new WaypointSequence.Waypoint((double)(xInput),(double)(yInput),-Math.toRadians(Double.parseDouble(angleInput.getText()))));
            xInput = Double.parseDouble(xField.getText());
            yInput = Double.parseDouble(yField.getText());
            double angle = ((3.14159265/180)*Double.parseDouble(angleInput.getText()));
            System.out.println(angle);
            p.addWaypoint(new WaypointSequence.Waypoint((double)(xInput),(double)(yInput),(angle)));

            feedback.setText("<html>Added Waypoint!<br>Click next waypoint location.</html>");
            totalWaypoints.setText("Total waypoints: "+ p.getNumWaypoints());
            if(p.getNumWaypoints()> 1) {
                path = PathGenerator.makePath(p, config,
                        kWheelbaseWidth, path_name);
            }

            repaint();

                //feedback.setText(String.valueOf(test.Segments.size()));
        }
        if(evt.getSource() == setWidth){ //recycled to remove last waypoint
            p.removeWaypoint();
            if(p.getNumWaypoints()> 1) {
                path = PathGenerator.makePath(p, config,
                        kWheelbaseWidth, path_name);
            }
            repaint();

        }
    }
    public static void main(String[] args){
        GUIFTC  gui = new GUIFTC();
        gui.setVisible(true);

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        xField.setText(d.format((e.getX())/pixelsPerInchX));
        yField.setText(d.format((e.getY())/pixelsPerInchY));
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
