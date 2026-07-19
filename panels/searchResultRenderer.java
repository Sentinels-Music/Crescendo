package panels;

import controllers.SearchResult;
import java.awt.*;
import javax.swing.*;

public class searchResultRenderer extends JPanel implements ListCellRenderer<SearchResult> {
    private JLabel titleLabel = new JLabel();
    private JLabel infoLabel = new JLabel();
    private JLabel typeLabel = new JLabel();
    private JLabel ratingLabel = new JLabel();

    public searchResultRenderer() {
        Font titleLabelFont = new Font("SansSerif", Font.BOLD, 16);
        Font typeLabelFont = new Font("SansSerif", Font.ITALIC, 12);
        Font infoLabelFont = new Font("SansSerif", Font.PLAIN, 14);
        Font ratingLabelFont = new Font("SansSerif", Font.PLAIN, 14);

        Color ratingLabelColor = new Color(212, 175, 55);
        Color typeLabelColor = Color.GRAY;

        setLayout(new BorderLayout(10, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);

        titleLabel.setFont(titleLabelFont);
        typeLabel.setFont(typeLabelFont);
        typeLabel.setForeground(typeLabelColor);
        infoLabel.setFont(infoLabelFont);
        
        ratingLabel.setFont(ratingLabelFont);
        ratingLabel.setForeground(ratingLabelColor); 

        // left side of panel
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(infoLabel);

        // right side of panel
        JPanel rightPanel = new JPanel(new GridLayout(2, 1));
        rightPanel.setOpaque(false);
        typeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        ratingLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        rightPanel.add(typeLabel);
        rightPanel.add(ratingLabel);

        add(textPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends SearchResult> list, SearchResult value, int index, boolean isSelected, boolean cellHasFocus) {
        titleLabel.setText(value.getTitle());
        typeLabel.setText(value.getType().toUpperCase());
        infoLabel.setText(value.getAdditionalInfo());

        if (value.getType().equalsIgnoreCase("User") || value.getAverageRating() == 0.0) {
            ratingLabel.setText("");
        } else {

            ratingLabel.setText(getStarString(value.getAverageRating()) + "  " + String.format("%.1f", value.getAverageRating()));
        }

        if (isSelected) {
            setBackground(new Color(240, 240, 240));
        } else {
            setBackground(Color.WHITE);
        }
        return this;
    }

    private String getStarString(double rating) {
        int fullStars = (int) Math.round(rating);
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < fullStars) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }
}