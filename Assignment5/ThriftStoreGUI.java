import javax.swing.*;
import java.awt.*;

public class ThriftStoreGUI {
    private JFrame frame;
    private JLabel tickLabel;
    // private JTextArea sectionInfoArea;
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
        // sectionInfoArea = new JTextArea(10, 20);
        assistantInfoArea = new JTextArea(10, 30);
        customerInfoArea = new JTextArea(10, 30);
        deliveryInfoArea = new JTextArea(5, 60);

        // sectionInfoArea.setBorder(BorderFactory.createTitledBorder("Sections"));
        assistantInfoArea.setBorder(BorderFactory.createTitledBorder("Assistants"));
        customerInfoArea.setBorder(BorderFactory.createTitledBorder("Customers"));
        deliveryInfoArea.setBorder(BorderFactory.createTitledBorder("Deliveries"));

        // Set text areas to wrap lines and be non-editable
        JTextArea[] areas = {assistantInfoArea, customerInfoArea, deliveryInfoArea};
        for (JTextArea area : areas) {
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setEditable(false);
        }

        // centerPanel.add(new JScrollPane(sectionInfoArea));
        centerPanel.add(new JScrollPane(assistantInfoArea));
        centerPanel.add(new JScrollPane(customerInfoArea));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(new JScrollPane(deliveryInfoArea), BorderLayout.CENTER);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    public void updateTick(int tick) {
        SwingUtilities.invokeLater(() -> tickLabel.setText("Tick: " + tick));
    }

    private void updateTextArea(JTextArea textArea, String info) {
        SwingUtilities.invokeLater(() -> textArea.append(info + "\n"));
    }

    // public void updateSectionInfo(String info) {
    //     updateTextArea(sectionInfoArea, info);
    // }

    public void updateAssistantInfo(String info) {
        updateTextArea(assistantInfoArea, info);
    }

    public void updateCustomerInfo(String info) {
        updateTextArea(customerInfoArea, info);
    }

    public void updateDeliveryInfo(String info) {
        updateTextArea(deliveryInfoArea, info);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ThriftStoreGUI::new);
    }
}
