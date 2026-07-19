package panels;

import java.awt.*;
import javax.swing.*;


public class sidebarPanel extends JPanel {
    public sidebarPanel() {
        initializeSidebar();
    }

    private void initializeSidebar() {
        //constants
        final Color backgroundColor = new Color(20,20,20);
        final Color goldColor = new Color(212,175,55);
        final Color navButtonColor = Color.WHITE;
        final Font logoFont = new Font("SansSerif",Font.BOLD,32);
        final Font navButtonFont = new Font("SansSerif", Font.BOLD, 16);
        final Font profileLabelFont = new Font("SansSerif", Font.BOLD, 14);
        final Font verifiedLabelFont = new Font("SansSerif", Font.ITALIC, 12);
        final Dimension panelSize = new Dimension(200,700);
        

        //panel basics
        setBackground(backgroundColor);
        setPreferredSize(panelSize);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        //setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding

        //logo
        JLabel logoLabel = new JLabel("Crescendo");
        logoLabel.setForeground(goldColor); 
        logoLabel.setFont(logoFont);
        logoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        add(logoLabel);

        //buttons for navigatin between pages;
        String[] navItems = {"Home", "Discover", "Listen Later"};
        for (String item : navItems) {
            JButton navButton = new JButton(item);
            navButton.setFont(navButtonFont);
            navButton.setForeground(navButtonColor);
            // deletes default button border colors
            navButton.setBorderPainted(false); 
            navButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            navButton.setHorizontalAlignment(SwingConstants.LEFT);       
            add(navButton);

        }

        // puts profile section to bottom
        add(Box.createVerticalGlue()); 

        // User profile
        JLabel profileLabel = new JLabel("mustafa");
        profileLabel.setForeground(Color.WHITE);
        profileLabel.setFont(profileLabelFont);
        
        JLabel verifiedLabel = new JLabel("Verified");
        verifiedLabel.setForeground(goldColor);
        verifiedLabel.setFont(verifiedLabelFont);

        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setBackground(backgroundColor);
        profilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        profilePanel.add(profileLabel);
        profilePanel.add(verifiedLabel);

        add(profilePanel);
    }
}
