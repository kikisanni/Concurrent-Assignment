import javax.swing.*;
import java.awt.*;

public class ThriftStoreGUI {
    private JFrame frame;
    private JLabel tickLabel;
    private JTextArea sectionInfoArea;
    private JTextArea assistantInfoArea;
    private JTextArea customerInfoArea;
    private JTextArea deliveryInfoArea;

    public ThriftStoreGUI() {
        initializeGUI();
    }

    private void initializeGUI() {
        frame = new JFrame("ThriftStore Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        tickLabel = new JLabel("Tick: 0");
        topPanel.add(tickLabel);

        JPanel centerPanel = new JPanel(new GridLayout(1, 3));
        sectionInfoArea = new JTextArea(10, 20);
        assistantInfoArea = new JTextArea(10, 20);
        customerInfoArea = new JTextArea(10, 20);
        sectionInfoArea.setBorder(BorderFactory.createTitledBorder("Sections"));
        assistantInfoArea.setBorder(BorderFactory.createTitledBorder("Assistants"));
        customerInfoArea.setBorder(BorderFactory.createTitledBorder("Customers"));

        centerPanel.add(new JScrollPane(sectionInfoArea));
        centerPanel.add(new JScrollPane(assistantInfoArea));
        centerPanel.add(new JScrollPane(customerInfoArea));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        deliveryInfoArea = new JTextArea(5, 60);
        deliveryInfoArea.setBorder(BorderFactory.createTitledBorder("Deliveries"));
        bottomPanel.add(new JScrollPane(deliveryInfoArea), BorderLayout.CENTER);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    public void updateTick(int tick) {
        SwingUtilities.invokeLater(() -> tickLabel.setText("Tick: " + tick));
    }

    public void updateSectionInfo(String info) {
        SwingUtilities.invokeLater(() -> sectionInfoArea.setText(info));
    }

    public void updateAssistantInfo(String info) {
        SwingUtilities.invokeLater(() -> assistantInfoArea.setText(info));
    }

    public void updateCustomerInfo(String info) {
        SwingUtilities.invokeLater(() -> customerInfoArea.setText(info));
    }

    public void updateDeliveryInfo(String info) {
        SwingUtilities.invokeLater(() -> deliveryInfoArea.setText(info));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ThriftStoreGUI::new);
    }
}

