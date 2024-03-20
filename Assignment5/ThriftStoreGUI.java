import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

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
        frame.setLayout(new BorderLayout(5, 5));
        frame.getContentPane().setBackground(new Color(173, 216, 230)); // Light blue background for the whole GUI

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5)); // Increased horizontal gap
        topPanel.setBackground(new Color(173, 216, 230)); // Light blue background

        // Tick Label
        tickLabel = new JLabel("Tick: 0");
        tickLabel.setForeground(Color.BLACK); // Dark gray for contrast
        tickLabel.setFont(new Font("Serif", Font.BOLD, 18));
        topPanel.add(tickLabel);

        // Terminate Button
        terminateButton = new JButton("Terminate");
        terminateButton.setBackground(new Color(30, 144, 255)); // Dodger blue for better visibility
        terminateButton.setForeground(Color.BLACK); // White text
        terminateButton.setFocusPainted(false);
        terminateButton.setFont(new Font("Arial", Font.BOLD, 14));
        terminateButton.addActionListener(e -> {
            frame.dispose();
            System.exit(0);
        });
        topPanel.add(terminateButton);

        // Center Panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(173, 216, 230)); // Light blue background

        // Text Areas Setup
        assistantInfoArea = createTextArea("Assistants");
        customerInfoArea = createTextArea("Customers");
        deliveryInfoArea = createTextArea("Deliveries");
        analysisReportArea = createTextArea("Analysis Report");

        // Add Text Areas to Center Panel
        JTextArea[] areas = {assistantInfoArea, customerInfoArea, deliveryInfoArea, analysisReportArea};
        for (JTextArea area : areas) {
            JScrollPane scrollPane = new JScrollPane(area);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            centerPanel.add(scrollPane);
        }

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private JTextArea createTextArea(String title) {
        JTextArea textArea = new JTextArea(10, 30);
        textArea.setBackground(new Color(240, 248, 255)); // Alice blue background for a soft feel
        textArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 2), title));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        return textArea;
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
