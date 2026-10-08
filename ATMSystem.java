import javax.swing.*;
import java.awt.*;
import java.util.*;

// Custom exception for business-rule violations
class ATMException extends Exception {
    ATMException(String msg) { super(msg); }
}

class Account {
    private final String name;
    private final int pin;
    private double bal;
    private int tries = 0;
    private boolean blocked = false;
    private final LinkedList<String> history = new LinkedList<>();   // ordered history

    Account(String name, int pin, double bal) {
        this.name = name; this.pin = pin; this.bal = bal;
        history.add("Account created | Balance: ₹" + bal);
    }

    String getName() { return name; }
    double getBalance() { return bal; }
    boolean isBlocked() { return blocked; }
    LinkedList<String> getHistory() { return history; }

    // 3 wrong PINs -> account blocked
    boolean checkPin(int p) {
        if (pin == p) { tries = 0; return true; }
        if (++tries >= 3) blocked = true;
        return false;
    }

    void deposit(double a, String note) throws ATMException {
        if (a <= 0) throw new ATMException("Invalid amount!");
        bal += a;
        history.add(note);
    }

    void withdraw(double a, String note) throws ATMException {
        if (a <= 0) throw new ATMException("Invalid amount!");
        if (a > bal) throw new ATMException("Insufficient balance!");
        bal -= a;
        history.add(note);
    }
}

public class ATMSystem {

    static HashMap<Integer, Account> accounts = new HashMap<>();                 // key-based lookup
    static ArrayList<Integer> notes = new ArrayList<>(Arrays.asList(2000, 500, 200, 100));
    static HashMap<Integer, Integer> stock = new HashMap<>();                    // ATM cash: note -> count
    static Account cur;
    static int curNo;

    // Theme colors
    static final Color BG = new Color(15, 23, 42), CARD = new Color(30, 41, 59),
            ACCENT = new Color(20, 184, 166), TEXT = new Color(241, 245, 249);

    static JFrame f = new JFrame("ATM Transaction System");
    static CardLayout cl = new CardLayout();
    static JPanel root = new JPanel(cl);
    static JTextArea out = new JTextArea();
    static JLabel welcome = lbl("", 28);

    interface Task { void go() throws Exception; }

    // Runs an action and handles every invalid operation safely
    static void run(Task t) {
        try { t.go(); }
        catch (ATMException ex) { out.setText("❌ " + ex.getMessage()); }
        catch (Exception ex) { out.setText("⚠️ Please enter valid input!"); }
    }

    static JLabel lbl(String t, int size) {
        JLabel l = new JLabel(t, SwingConstants.CENTER);
        l.setFont(new Font("Arial", Font.BOLD, size));
        l.setForeground(TEXT);
        return l;
    }

    static JButton btn(String t, java.awt.event.ActionListener a) {
        JButton b = new JButton(t);
        b.setFont(new Font("Arial", Font.BOLD, 20));
        b.setBackground(ACCENT);
        b.setForeground(BG);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.addActionListener(a);
        return b;
    }

    static double ask(String m) { return Double.parseDouble(JOptionPane.showInputDialog(f, m)); }

    public static void main(String[] args) {
        accounts.put(1001, new Account("Ashutosh", 1234, 10000));
        accounts.put(1002, new Account("Rahul", 4321, 5000));
        for (int n : notes) stock.put(n, 10);   // 10 notes of each denomination

        // ---------- LOGIN PAGE ----------
        JTextField acc = new JTextField(15);
        JPasswordField pin = new JPasswordField(15);
        for (JTextField t : new JTextField[]{acc, pin}) t.setFont(new Font("Arial", Font.PLAIN, 24));

        JPanel box = new JPanel(new GridLayout(0, 1, 15, 15));
        box.setBackground(CARD);
        box.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        box.add(lbl("🏧 ATM LOGIN", 36));
        box.add(lbl("Account Number", 18));
        box.add(acc);
        box.add(lbl("4-digit PIN", 18));
        box.add(pin);
        box.add(btn("LOGIN", e -> {
            try {
                int no = Integer.parseInt(acc.getText().trim());
                Account a = accounts.get(no);
                if (a == null) throw new ATMException("Account not found!");
                if (a.isBlocked()) throw new ATMException("Account is BLOCKED. Contact bank.");
                boolean ok = a.checkPin(Integer.parseInt(new String(pin.getPassword())));
                pin.setText("");
                if (!ok) throw new ATMException(a.isBlocked() ? "3 wrong PINs - account BLOCKED!" : "Incorrect PIN!");
                cur = a; curNo = no;
                welcome.setText("Welcome, " + cur.getName() + " 👋");
                out.setText("Select an option from the left.");
                cl.show(root, "menu");
            } catch (ATMException ex) {
                JOptionPane.showMessageDialog(f, "❌ " + ex.getMessage());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(f, "⚠️ Please enter valid details!");
            }
        }));

        JPanel login = new JPanel(new GridBagLayout());
        login.setBackground(BG);
        login.add(box);

        // ---------- MENU PAGE ----------
        out.setEditable(false);
        out.setBackground(CARD);
        out.setForeground(TEXT);
        out.setFont(new Font("Monospaced", Font.PLAIN, 26));
        out.setMargin(new Insets(30, 30, 30, 30));

        JPanel side = new JPanel(new GridLayout(0, 1, 15, 15));
        side.setBackground(BG);
        side.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        side.add(btn("💰 Balance", e -> out.setText("💰 AVAILABLE BALANCE\n\n₹" + cur.getBalance())));

        side.add(btn("💵 Deposit", e -> run(() -> {
            double a = ask("Enter deposit amount:");
            cur.deposit(a, "Deposited ₹" + a);
            out.setText("✅ DEPOSIT SUCCESSFUL\n\nAmount: ₹" + a);
        })));

        side.add(btn("💸 Withdraw", e -> run(() -> {
            double a = ask("Enter amount (multiple of ₹100):");
            if (a <= 0 || a % 100 != 0) throw new ATMException("Use positive multiples of ₹100!");
            if (a > cur.getBalance()) throw new ATMException("Insufficient balance!");

            // Plan the notes using available ATM cash
            HashMap<Integer, Integer> used = new HashMap<>();
            int rem = (int) a;
            for (int d : notes) {
                int c = Math.min(rem / d, stock.get(d));
                if (c > 0) { used.put(d, c); rem -= c * d; }
            }
            if (rem > 0) throw new ATMException("ATM doesn't have enough cash/notes for this amount!");

            cur.withdraw(a, "Withdrawn ₹" + a);
            String s = "";
            for (int d : notes)
                if (used.containsKey(d)) {
                    stock.put(d, stock.get(d) - used.get(d));
                    s += "₹" + d + " × " + used.get(d) + "\n";
                }
            out.setText("✅ WITHDRAWAL SUCCESSFUL\n\nAmount: ₹" + a + "\n\nCash Dispensed:\n" + s);
        })));

        side.add(btn("🔄 Transfer", e -> run(() -> {
            int r = (int) ask("Enter receiver account number:");
            double a = ask("Enter transfer amount:");
            Account to = accounts.get(r);
            if (to == null) throw new ATMException("Receiver account not found!");
            if (r == curNo) throw new ATMException("Cannot transfer to same account!");
            cur.withdraw(a, "Transferred ₹" + a + " → Account " + r);
            to.deposit(a, "Received ₹" + a + " ← Account " + curNo);
            out.setText("✅ TRANSFER SUCCESSFUL\n\n₹" + a + " sent to Account " + r);
        })));

        side.add(btn("📋 History", e -> {
            StringBuilder sb = new StringBuilder("📋 TRANSACTION HISTORY\n\n");
            for (String h : cur.getHistory()) sb.append("• ").append(h).append("\n");
            out.setText(sb.toString());
        }));

        side.add(btn("📊 Summary", e -> {
            int total = 0;
            StringBuilder sb = new StringBuilder("📊 SUMMARY\n\nAccount : " + curNo + " (" + cur.getName() + ")"
                    + "\nStatus  : " + (cur.isBlocked() ? "Blocked" : "Active")
                    + "\nBalance : ₹" + cur.getBalance()
                    + "\nTransactions: " + cur.getHistory().size() + "\n\n🏧 ATM CASH AVAILABLE\n");
            for (int d : notes) { sb.append("₹" + d + " × " + stock.get(d) + "\n"); total += d * stock.get(d); }
            out.setText(sb.append("Total: ₹").append(total).toString());
        }));

        side.add(btn("🚪 Logout", e -> { acc.setText(""); cl.show(root, "login"); }));

        JPanel menu = new JPanel(new BorderLayout(20, 20));
        menu.setBackground(BG);
        menu.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        menu.add(welcome, BorderLayout.NORTH);
        menu.add(side, BorderLayout.WEST);
        menu.add(new JScrollPane(out), BorderLayout.CENTER);

        // ---------- FRAME ----------
        root.add(login, "login");
        root.add(menu, "menu");
        f.add(root);
        f.setExtendedState(JFrame.MAXIMIZED_BOTH);   // full screen on run
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}