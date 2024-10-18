import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.rmi.Naming;

public class BankServer extends JFrame {

    private JButton startButton;
    private JLabel statusLabel;

    public BankServer() {
        // Thiết lập giao diện
        setTitle("Bank Server");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Tạo nút Start và nhãn trạng thái
        startButton = new JButton("Start Server");
        statusLabel = new JLabel("Server is not running", SwingConstants.CENTER);

        // Thiết lập bố cục
        setLayout(new BorderLayout());
        add(startButton, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.CENTER);

        // Sự kiện nhấn nút Start
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });
    }

    private void startServer() {
        try {
            BankInterface h = new BankAccount();
            Naming.rebind("rmi://localhost:2008/BankService", h);
            statusLabel.setText("Server is connected and ready for operation");
            startButton.setEnabled(false); // Vô hiệu hóa nút Start sau khi server khởi chạy
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
    	
    	
        // Hiển thị giao diện
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new BankServer().setVisible(true);
            }
        });
    }
}