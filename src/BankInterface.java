import java.rmi.Remote;
import java.rmi.RemoteException;


public interface BankInterface extends Remote {
		public double getBalance(int accountNumber) throws RemoteException;
		public String getName(int accountNumber) throws RemoteException;
		public int getNumber(int accountNumber) throws RemoteException;
		public void deposit(int accountNumber, double amount) throws RemoteException;
		public void withdraw(int accountNumber, double amount) throws RemoteException;
		public void transfer(int accountNumber_from, int accountNumber_to, double amount)  throws RemoteException;
		public boolean login(int accountNumber, String password) throws RemoteException;
		public void logout(int accountNumber) throws RemoteException;
		public boolean signup(String name, int accountNumber,int balance, boolean logged,  String password) throws RemoteException;
		public boolean checkIfNumberExists(int number) throws RemoteException;
}
