# ATM Transaction Simulation System

**Java Programming - Case Study No. 113**
Ashutosh Mishra | B.Tech CSE (AI) 2025-29 | Semester III | ITM Skills University

A Java Swing simulation of an ATM. A customer can log in with an account number and
PIN, check the balance, withdraw, deposit, transfer money and view recent transactions.
The ATM also keeps track of the cash notes left inside the machine.

---

## Folder Structure

```
ATM_Simulation_Project/
├── src/
│   └── ATMLite.java              <- main project (single file, Swing GUI)
├── full_version/
│   └── ATMSimulation.java        <- longer version with more classes and exceptions
├── web/
│   └── index.html                <- browser (HTML/JS) version of the same ATM
└── README.md
```

---

## How to Run

### Java version (main project)
Requires JDK 8 or higher.

```
cd src
javac ATMLite.java
java ATMLite
```

### Full version
```
cd full_version
javac ATMSimulation.java
java ATMSimulation
```

### Web version
Double-click `web/index.html` to open it in any browser. No installation needed.

---

## Demo Accounts

| Account No | PIN  | Name            |
|------------|------|-----------------|
| 1001       | 1234 | Ashutosh Mishra |
| 1002       | 4321 | Rahul Sharma    |
| 1003       | 1111 | Priya Verma     |

The full version also has account 1004 (PIN 2222), which starts locked for demo.

---

## Features

- **Login** with account number and 4-digit PIN. The account locks after 3 wrong PINs.
- **Balance inquiry**, always visible in the header after login.
- **Withdraw**: amount must be a multiple of 100. The ATM gives notes of Rs. 2000, 500,
  200 and 100 from its stock. If the exact amount cannot be paid, the balance is unchanged.
- **Deposit**
- **Transfer** to another account (receiver must exist and cannot be your own account).
- **Mini statement**: last 10 transactions, newest first.
- **ATM cash status**: notes left, total cash and total transactions.
- **Dark-themed GUI** with a login screen and a menu screen.

---

## Java Concepts Used

| Concept | Where it is used |
|---|---|
| OOP | `Account`, `ATM`, `ATMException` and the GUI class are separate, with private-style data handling and logic kept apart from the GUI |
| ArrayList | `log` stores every transaction done on the ATM |
| LinkedList | Each account's history works like a queue: newest added at the front, oldest removed after 10 entries |
| HashMap | `accounts` (account number -> Account) and `cash` (note value -> number of notes) |
| Exception handling | `ATMException` is thrown for wrong PIN, locked account, low balance, low ATM cash and invalid amounts, and shown as an error message |
| Swing GUI | `JFrame`, `CardLayout`, `JOptionPane`, and a custom rounded `FancyButton` |

---

## Quick Test Guide

1. Login with `1001 / 1234` -> menu opens.
2. Click **Balance** -> Rs. 25000.00.
3. **Withdraw** `1000` -> notes shown, balance becomes 24000.
4. **Withdraw** `150` -> error (must be a multiple of 100).
5. **Withdraw** `99900` -> insufficient balance error.
6. **Transfer** to `1002`, amount `1000` -> balance reduces.
7. **Mini Statement** -> shows the transactions above.
8. **ATM Cash Status** -> shows remaining notes.
9. Logout, then try 3 wrong PINs on one account -> account locks.

---

## Limitations

- Data is stored in memory only and resets when the program is closed.
- Accounts are pre-loaded (no create/unlock option in the compact version).
- PINs are not encrypted, since this is a simulation.
