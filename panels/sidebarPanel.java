package panels;

import java.awt.*;
import javax.swing.*;


public class sidebarPanel extends JPanel {

    public sidebarPanel(){
        initializeSideBar();
    }

    private void initializeSideBar(){
        // constants
        Color logoColor = new Color(211, 175, 55);
        Font logoFont = new Font("SansSerif", Font.BOLD, 32);
        Font navButtonFont = new Font("SansSerif", Font.BOLD, 16);
        
        //Panel basics
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(200,700));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        //App logo
        JLabel logoLabel = new JLabel("CRESCENDO");
        logoLabel.setForeground(logoColor);
        logoLabel.setFont(logoFont);
        add(logoLabel);

        // Buttons for Navigating through app
        String[] navigatingButtons = {"Home", "Discover", "Listen Later"};
        for (String item : navigatingButtons) {
            JButton navButton = new JButton(item);
            navButton.setFont(navButtonFont);
            

            
        }
        

    }
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        
    }
}
