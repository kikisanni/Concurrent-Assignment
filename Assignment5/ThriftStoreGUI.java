import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ThriftStoreGUI {
    private JFrame frame;
    private JLabel tickLabel;
    private JTextArea assistantInfoArea;
    private JTextArea customerInfoArea;
    private JTextArea deliveryInfoArea;
    private JTextArea analysisReportArea;
    private JButton terminateButton;

    public ThriftStoreGUI() {
        initializeGUI();
    }

    private void initializeGUI() {
        frame = new JFrame("ThriftStore Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout(5, 5)); // Add some spacing

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.setBackground(Color.DARK_GRAY); // Setting the background color of the top panel

        // Tick Label
        tickLabel = new JLabel("Tick: 0");
        tickLabel.setForeground(new Color(255, 255, 0)); // Bright yellow for emphasis
        tickLabel.setFont(new Font("Serif", Font.BOLD, 16)); // Making the font larger and bold
        topPanel.add(tickLabel);

        // Terminate Button
        terminateButton = new JButton("Terminate");
        terminateButton.setBackground(new Color(255, 69, 0)); // Reddish background
        terminateButton.setForeground(Color.WHITE); // White text
        terminateButton.setFocusPainted(false); // No focus ring around the text
        terminateButton.setFont(new Font("Arial", Font.BOLD, 12)); // Stylish font
        terminateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // Close the GUI
                System.exit(0); // Terminate the application
            }
        });
        topPanel.add(terminateButton);

        // Center Panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Text Areas
        assistantInfoArea = new JTextArea(10, 30);
        customerInfoArea = new JTextArea(10, 30);
        deliveryInfoArea = new JTextArea(10, 30);
        analysisReportArea = new JTextArea(10, 30);

        // Background Colors
        Color lightBlue = new Color(204, 229, 255);
        assistantInfoArea.setBackground(lightBlue);
        customerInfoArea.setBackground(lightBlue);
        deliveryInfoArea.setBackground(lightBlue);
        analysisReportArea.setBackground(lightBlue);

        // Borders
        assistantInfoArea.setBorder(BorderFactory.createTitledBorder("Assistants"));
        customerInfoArea.setBorder(BorderFactory.createTitledBorder("Customers"));
        deliveryInfoArea.setBorder(BorderFactory.createTitledBorder("Deliveries"));
        analysisReportArea.setBorder(BorderFactory.createTitledBorder("Analysis Report"));

        // Text Areas Array
        JTextArea[] areas = {assistantInfoArea, customerInfoArea, deliveryInfoArea, analysisReportArea};
        for (JTextArea area : areas) {
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(area);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            centerPanel.add(scrollPane);
        }

        // Add Panels to Frame
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);

        // Show GUI
        frame.setVisible(true);
    }

    public void updateTick(int tick) {
        SwingUtilities.invokeLater(() -> tickLabel.setText("Tick: " + tick));
    }
    
    private void updateTextArea(JTextArea textArea, String info) {
        SwingUtilities.invokeLater(() -> textArea.append(info + "\n"));
    }
    
    public void updateAssistantInfo(String info) {
        updateTextArea(assistantInfoArea, info);
    }
    
    public void updateCustomerInfo(String info) {
        updateTextArea(customerInfoArea, info);
    }
    
    public void updateDeliveryInfo(String info) {
        updateTextArea(deliveryInfoArea, info);
    }
    
    public void updateAnalysisReport(String info) {
        SwingUtilities.invokeLater(() -> {
            analysisReportArea.setText(""); // Optionally clear previous content
            analysisReportArea.append(info);
        });
    }
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ThriftStoreGUI::new);
    }
}
