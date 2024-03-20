import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/*
 * ThriftStoreGUI makes and manages the user interface for an app that simulates a thrift shop.
 * It shows details about assistants, customers, deliveries, and research reports.
 */

public class ThriftStoreGUI {
    private JFrame frame;
    private JLabel tickLabel;
    private JTextArea assistantInfoArea;
    private JTextArea customerInfoArea;
    private JTextArea deliveryInfoArea;
    private JTextArea analysisReportArea;
    private JButton terminateButton;

    // GUI constructor initializes the main window of the application
    public ThriftStoreGUI() {
        initializeGUI();
    }

    // Initialises the GUI components and sets up the layout
    private void initializeGUI() {
        frame = new JFrame("ThriftStore Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout(5, 5));
        frame.getContentPane().setBackground(new Color(173, 216, 230)); // Sets a light blue background for the GUI.

        initialiseTopPanel();
        initializeCenterPanel();

        frame.setVisible(true);
    }

    // Initialises and attaches the top panel to the frame. The tick label and terminate button are located on the top panel.
    private void initialiseTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        topPanel.setBackground(new Color(173, 216, 230)); // Ensures consistency in background color.

        tickLabel = new JLabel("Tick: 0");
        tickLabel.setForeground(Color.BLACK);
        tickLabel.setFont(new Font("Serif", Font.BOLD, 18));
        topPanel.add(tickLabel);

        terminateButton = new JButton("Terminate");
        terminateButton.setBackground(new Color(30, 144, 255)); // Uses a distinct blue for visibility.
        terminateButton.setForeground(Color.BLACK);
        terminateButton.setFocusPainted(false);
        terminateButton.setFont(new Font("Arial", Font.BOLD, 14));
        terminateButton.addActionListener(this::terminateApplication);
        topPanel.add(terminateButton);

        frame.add(topPanel, BorderLayout.NORTH);
    }

    // Initialises and attaches the centre panel to the frame. The centre panel has text fields for helpers, clients, delivery, and analytical reports.
    private void initializeCenterPanel() {
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(173, 216, 230));

        // Adds initialised text areas to the centre panel
        assistantInfoArea = createTextArea("Assistants");
        customerInfoArea = createTextArea("Customers");
        deliveryInfoArea = createTextArea("Deliveries");
        analysisReportArea = createTextArea("Analysis Report");

        JTextArea[] areas = {assistantInfoArea, customerInfoArea, deliveryInfoArea, analysisReportArea};
        for (JTextArea area : areas) {
            JScrollPane scrollPane = new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            centerPanel.add(scrollPane);
        }

        frame.add(centerPanel, BorderLayout.CENTER);
    }

    // Produces a text area with predefined design and a specified title
    private JTextArea createTextArea(String title) {
        JTextArea textArea = new JTextArea(10, 30);
        textArea.setBackground(new Color(240, 248, 255));
        textArea.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 2), title));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        return textArea;
    }

    // Method of action responder for the terminate button
    private void terminateApplication(ActionEvent e) {
        frame.dispose();
        System.exit(0);
    }

    public void updateTick(int tick) {
        SwingUtilities.invokeLater(() -> tickLabel.setText("Tick: " + tick));
    }
    
    private void updateTextArea(JTextArea textArea, String info) {
        SwingUtilities.invokeLater(() -> textArea.append(info + "\n"));
    }
    
    public void updateAssistantInformation(String info) {
        updateTextArea(assistantInfoArea, info);
    }
    
    public void updateCustomerInformation(String info) {
        updateTextArea(customerInfoArea, info);
    }
    
    public void updateDeliveryInformation(String info) {
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
