import java.awt.*;
import java.awt.event.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import javax.swing.*;

public class BankClient extends JFrame implements ActionListener {
	public static JButton btnSignUp = new JButton("Đăng ký");
    public static JButton btnLogin = new JButton("Đăng nhập");
    private JButton btnGetBalance = new JButton("Số dư hiện tại");
    private JButton btnDeposit = new JButton("Nạp tiền");
    private JButton btnWithdraw = new JButton("Rút tiền");
    private JButton btnAccountDetails = new JButton("Thông tin tài khoản");
    private JButton btnTransfer = new JButton("Chuyển khoản");
    private JButton btnLogout = new JButton("Đăng xuất");
    private JPanel accountPanel = new JPanel();
    private JPanel loginPanel = new JPanel();
    public static int NUMBER = 0;
    public static BankInterface ACCOUNT;
    

    public static void main(String[] args) {
        new BankClient();
        try {
            ACCOUNT = (BankInterface) Naming.lookup("rmi://localhost:2008/BankService");
            System.out.println("Client sẵn sàng");
        } catch (Exception e) {
            System.out.println("Lỗi kết nối: " + e);
        }

        // Xử lý đăng xuất khi đóng ứng dụng
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (NUMBER != 0) {
                try {
                    ACCOUNT.logout(NUMBER);
                    System.out.println("Tài khoản " + NUMBER + " đã đăng xuất do đóng ứng dụng.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    public BankClient() {
        accountPanel.add(btnGetBalance);
        accountPanel.add(btnDeposit);
        accountPanel.add(btnWithdraw);
        accountPanel.add(btnAccountDetails);
        accountPanel.add(btnTransfer);
        accountPanel.add(btnLogout);
        accountPanel.setVisible(false);

        loginPanel.add(btnLogin);
        loginPanel.add(btnSignUp);
        btnLogin.addActionListener(this);
        btnSignUp.addActionListener(this);
        btnGetBalance.addActionListener(this);
        btnDeposit.addActionListener(this);
        btnWithdraw.addActionListener(this);
        btnAccountDetails.addActionListener(this);
        btnTransfer.addActionListener(this);
        btnLogout.addActionListener(this);

        setLayout(new BorderLayout());
        add(loginPanel, BorderLayout.NORTH);
        add(accountPanel, BorderLayout.SOUTH);
        setTitle("Ngân hàng của bạn");
        setSize(700, 300);
        setVisible(true);

        // Xử lý sự kiện đóng cửa sổ
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (NUMBER != 0) {
                    try {
                        ACCOUNT.logout(NUMBER);
                        System.out.println("Tài khoản " + NUMBER + " đã đăng xuất khi đóng cửa sổ.");
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
                System.exit(0);
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        try {
        	if (e.getSource() == btnSignUp) {
                try {
                	String accountName = JOptionPane.showInputDialog("Nhập tên của bạn: ");
                    String password = JOptionPane.showInputDialog("Nhập mật khẩu của bạn: ");
                    int randomNumber;
                    do {
                        randomNumber = (int)(Math.random() * (99999 - 10000 + 1)) + 10000;
                    } while (ACCOUNT.checkIfNumberExists(randomNumber));
                    // Kiểm tra đăng nhập
                    if (ACCOUNT.signup(accountName, randomNumber, password)) {
                        // Nếu đăng nhập thành công
                        loginPanel.setVisible(true);
                        JOptionPane.showMessageDialog(null, "Đăng ký thành công!");
                    } else {
                        // Nếu tài khoản đã được đăng nhập hoặc mật khẩu sai
                        JOptionPane.showMessageDialog(null, "Tài khoản đã tồn tại.");
                    }
                } catch (RemoteException e1) {
                    e1.printStackTrace();
                }
            }
            if (e.getSource() == btnLogin) {
                int accountNumber = Integer.parseInt(JOptionPane.showInputDialog("Nhập số tài khoản:"));
                String password = JOptionPane.showInputDialog("Nhập mật khẩu:");

                if (ACCOUNT.login(accountNumber, password)) {
                    NUMBER = accountNumber;
                    loginPanel.setVisible(false);
                    accountPanel.setVisible(true);
                    JOptionPane.showMessageDialog(null, "Đăng nhập thành công!");
                } else {
                    JOptionPane.showMessageDialog(null, "Sai tài khoản/mật khẩu hoặc tài khoản đang đăng nhập ở nơi khác.");
                }
                
            } else if (e.getSource() == btnLogout) {
                ACCOUNT.logout(NUMBER);
                loginPanel.setVisible(true);
                accountPanel.setVisible(false);
                JOptionPane.showMessageDialog(null, "Đăng xuất thành công!");
                NUMBER = 0;
            } else if (e.getSource() == btnGetBalance) {
                double balance = ACCOUNT.getBalance(NUMBER);
                JOptionPane.showMessageDialog(null, "Số dư hiện tại của bạn là: " + balance);
            } else if (e.getSource() == btnDeposit) {
                double amount = Double.parseDouble(JOptionPane.showInputDialog("Nhập số tiền muốn nạp:"));
                ACCOUNT.deposit(NUMBER, amount);
                JOptionPane.showMessageDialog(null, "Nạp tiền thành công!");
            } else if (e.getSource() == btnWithdraw) {
                double amount = Double.parseDouble(JOptionPane.showInputDialog("Nhập số tiền muốn rút:"));
                ACCOUNT.withdraw(NUMBER, amount);
                JOptionPane.showMessageDialog(null, "Rút tiền thành công!");
            } else if (e.getSource() == btnAccountDetails) {
                String name = ACCOUNT.getName(NUMBER);
                int number = ACCOUNT.getNumber(NUMBER);
                double balance = ACCOUNT.getBalance(NUMBER);
                JOptionPane.showMessageDialog(null, "Tên: " + name + "\nSố tài khoản: " + number + "\nSố dư: " + balance);
            } else if (e.getSource() == btnTransfer) {
                int toAccount = Integer.parseInt(JOptionPane.showInputDialog("Nhập số tài khoản nhận:"));
                double amount = Double.parseDouble(JOptionPane.showInputDialog("Nhập số tiền muốn chuyển:"));
                ACCOUNT.transfer(NUMBER, toAccount, amount);
                JOptionPane.showMessageDialog(null, "Chuyển tiền thành công!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
