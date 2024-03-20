import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

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

        // Enhancing Top Panel
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.setBackground(new Color(64, 64, 64)); // A darker shade for contrast

        // Enhancing Tick Label
        tickLabel = new JLabel("Tick: 0");
        tickLabel.setForeground(new Color(255, 215, 0)); // Gold color for prominence
        tickLabel.setFont(new Font("Serif", Font.BOLD, 18));
        topPanel.add(tickLabel);

        // Enhancing Terminate Button
        terminateButton = new JButton("Terminate");
        terminateButton.setBackground(new Color(220, 20, 60)); // Crisp red background for urgency
        terminateButton.setForeground(Color.WHITE); // Ensuring text is visible with white color
        terminateButton.setFocusPainted(false);
        terminateButton.setFont(new Font("Arial", Font.BOLD, 14));
        terminateButton.setBorder(BorderFactory.createRaisedBevelBorder()); // Adding a beveled border for a 3D effect
        terminateButton.addActionListener(e -> {
            frame.dispose();
            System.exit(0);
        });
        topPanel.add(terminateButton);

        // Setting up the Center Panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        // Text Areas Setup
        assistantInfoArea = createTextArea("Assistants");
        customerInfoArea = createTextArea("Customers");
        deliveryInfoArea = createTextArea("Deliveries");
        analysisReportArea = createTextArea("Analysis Report");

        // Adding Text Areas to the Center Panel
        JTextArea[] areas = {assistantInfoArea, customerInfoArea, deliveryInfoArea, analysisReportArea};
        Arrays.stream(areas).forEach(area -> {
            JScrollPane scrollPane = new JScrollPane(area);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            centerPanel.add(scrollPane);
        });

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private JTextArea createTextArea(String title) {
        JTextArea textArea = new JTextArea(10, 30);
        textArea.setBackground(new Color(230, 230, 250)); // Lavender background for a gentle feel
        textArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(105, 105, 105), 2), title));
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
