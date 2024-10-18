import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;

public class BankAccount extends UnicastRemoteObject implements BankInterface {
    private static final long serialVersionUID = 1L;
    public static Connection con = DbConnection.getConnection();  // Kết nối CSDL

    // Constructor
    public BankAccount() throws RemoteException {
        super();
    }

    // Login
    public synchronized boolean login(int accountNumber, String password) throws RemoteException {
        try {
            // Bắt đầu transaction
            con.setAutoCommit(false);

            // Khóa bản ghi bằng SELECT FOR UPDATE
            String query = "SELECT is_logged_in, password FROM account WHERE number = ? FOR UPDATE";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                boolean isLoggedIn = rs.getBoolean("is_logged_in");
                String correctPassword = rs.getString("password");

                if (isLoggedIn) {
                    System.out.println("Tài khoản đang được đăng nhập ở nơi khác.");
                    con.rollback(); // Hoàn tác nếu không thể đăng nhập
                    return false;
                } else if (!password.equals(correctPassword)) {
                    System.out.println("Mật khẩu sai.");
                    con.rollback(); // Hoàn tác nếu sai mật khẩu
                    return false;
                } else {
                    // Cập nhật trạng thái đăng nhập
                    updateLoginStatus(accountNumber, true);
                    System.out.println("Đăng nhập thành công.");
                    con.commit();  // Hoàn tất giao dịch và mở khóa
                    return true;
                }
            } else {
                System.out.println("Tài khoản không tồn tại.");
                con.rollback(); // Hoàn tác nếu không tìm thấy tài khoản
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                con.rollback(); // Hoàn tác nếu có lỗi
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                con.setAutoCommit(true); // Bật lại auto-commit sau khi xử lý xong
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public boolean signup(String name, int accountNumber, String password) throws RemoteException {
	    try {
	        String query = "INSERT INTO account (name, number, password, is_logged_in) VALUES (?, ?, ?, ?)";
	        PreparedStatement stmt = con.prepareStatement(query);
	        stmt.setString(1, name);
	        stmt.setInt(2, accountNumber);
	        stmt.setString(3, password);
	        stmt.setInt(4, 0);
	        int rowsInserted = stmt.executeUpdate();

	        if (rowsInserted > 0) {
	            System.out.println("Tài khoản đã được thêm thành công.");
	            return true;
	        } else {
	            System.out.println("Không thể thêm tài khoản.");
	            return false;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
    public boolean checkIfNumberExists(int number) throws RemoteException {
		try {
	        // Truy vấn để lấy toàn bộ các số tài khoản từ cơ sở dữ liệu
	        String query = "SELECT number FROM account";
	        PreparedStatement stmt = con.prepareStatement(query);
	        ResultSet rs = stmt.executeQuery();

	        // Duyệt qua các kết quả để kiểm tra xem số mới có trùng hay không
	        while (rs.next()) {
                return false;
	        }
	        // Nếu không tìm thấy số trùng, trả về true
	        return true;
	        
	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}

    // Logout
    public synchronized void logout(int accountNumber) throws RemoteException {
        try {
            updateLoginStatus(accountNumber, false);
            System.out.println("Đăng xuất thành công.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateLoginStatus(int accountNumber, boolean isLoggedIn) throws SQLException {
        String updateQuery = "UPDATE account SET is_logged_in = ? WHERE number = ?";
        PreparedStatement stmt = con.prepareStatement(updateQuery);
        stmt.setBoolean(1, isLoggedIn);
        stmt.setInt(2, accountNumber);
        stmt.executeUpdate();
    }

    // Get Balance
    public synchronized double getBalance(int accountNumber) throws RemoteException {
        try {
            String query = "SELECT Balance FROM account WHERE number = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("Balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Get Name
    public synchronized String getName(int accountNumber) throws RemoteException {
        try {
            String query = "SELECT Name FROM account WHERE number = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, accountNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("Name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get Account Number
    public synchronized int getNumber(int accountNumber) throws RemoteException {
        return accountNumber;
    }

    // Deposit
    public synchronized void deposit(int accountNumber, double amount) throws RemoteException {
        if (amount < 0) {
            throw new IllegalArgumentException("Số tiền nạp không hợp lệ.");
        }

        try {
            double currentBalance = getBalance(accountNumber);
            double newBalance = currentBalance + amount;

            String updateQuery = "UPDATE account SET Balance = ? WHERE number = ?";
            PreparedStatement stmt = con.prepareStatement(updateQuery);
            stmt.setDouble(1, newBalance);
            stmt.setInt(2, accountNumber);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Withdraw
    public synchronized void withdraw(int accountNumber, double amount) throws RemoteException {
        if (amount < 0) {
            throw new IllegalArgumentException("Số tiền rút không hợp lệ.");
        }

        try {
            double currentBalance = getBalance(accountNumber);
            if (currentBalance < amount) {
                throw new IllegalArgumentException("Số dư không đủ.");
            }

            double newBalance = currentBalance - amount;

            String updateQuery = "UPDATE account SET Balance = ? WHERE number = ?";
            PreparedStatement stmt = con.prepareStatement(updateQuery);
            stmt.setDouble(1, newBalance);
            stmt.setInt(2, accountNumber);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Transfer
    public synchronized void transfer(int fromAccount, int toAccount, double amount) throws RemoteException {
        if (amount < 0) {
            throw new IllegalArgumentException("Số tiền chuyển không hợp lệ.");
        }

        try {
            con.setAutoCommit(false);  // Bắt đầu giao dịch

            double fromBalance = getBalance(fromAccount);
            if (fromBalance < amount) {
                throw new IllegalArgumentException("Số dư không đủ để chuyển.");
            }

            double toBalance = getBalance(toAccount);

            // Cập nhật số dư
            withdraw(fromAccount, amount);  // Trừ tiền từ tài khoản gốc
            deposit(toAccount, amount);     // Thêm tiền vào tài khoản nhận

            con.commit();  // Commit giao dịch
        } catch (SQLException e) {
            try {
                con.rollback();  // Hoàn tác nếu có lỗi
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                con.setAutoCommit(true);  // Bật lại auto-commit
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
