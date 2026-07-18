package panels;

import java.awt.*;
import javax.swing.*;


public class searchPanel {
    private JFrame frame;
    private JPanel mainPanel;
    private JTextField searchField;
    private ButtonGroup filterGroup;

    public searchPanel() {
        initializeUI();
    }

    private void initializeUI() {
        //constants
        final Dimension panelSize = new Dimension(1000,700);
        final Color whiteColor = Color.WHITE;
        final Font searchTitleFont = new Font("SansSerif", Font.BOLD, 32);
        final Font searchFieldFont = new Font("SansSerif", Font.PLAIN, 18);
        final Font filterButtonFont = new Font("SansSerif", Font.PLAIN, 14);
        // search frame
        frame = new JFrame("Crescendo - Search");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(panelSize);
        frame.setLayout(new BorderLayout());

        //calls sidebar
        sidebarPanel sidebar = new sidebarPanel();

        //mainPanel
        mainPanel = new JPanel();
        mainPanel.setBackground(whiteColor);
        mainPanel.setLayout(new BorderLayout()); 

        //header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 40)); 

        //search label
        JLabel searchTitle = new JLabel("Find anything");
        searchTitle.setFont(searchTitleFont);
        
        
        //search Field
        searchField = new JTextField();
        searchField.setFont(searchFieldFont);

        //filters
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0)); 
        filterPanel.setBackground(whiteColor);
        filterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        //filter types
        String[] filters = {"All", "Artists", "Albums", "Songs", "People"};
        filterGroup = new ButtonGroup(); 

        for (String filter : filters) {
            JToggleButton filterButton = new JToggleButton(filter);
            filterButton.setFont(filterButtonFont);

            filterGroup.add(filterButton); 
            filterPanel.add(filterButton); 
        }

        headerPanel.add(searchTitle);
        headerPanel.add(searchField);
        headerPanel.add(filterPanel);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // adding sidebar left and mainpanel center
        frame.add(sidebar, BorderLayout.WEST);
        frame.add(mainPanel, BorderLayout.CENTER);
    }

    public void show() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        searchPanel searchPanel = new searchPanel();
        searchPanel.show();
    }
    
}