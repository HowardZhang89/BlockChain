package group7Crypto;

import java.util.LinkedList;
import java.util.Random;
import javax.swing.*;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.*;
public class GUI extends JFrame {

    private BCThread blockchain;

	private JTextField amountField;
    private JButton balanceBtn;
    private JTextField balanceField;
    private JLabel balanceLabel;
    private JButton connectBtn;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JScrollPane jScrollPane1;
    private JButton sendBtn;
    private JLabel sendLabel;
    private JTextField toField;
    private JTextArea transactionArea;
    private JButton transactionBtn;
    private JLabel transactionLabel;
    private JLabel walletLabel;
    
    private static double balance = 0.0;
    
    public GUI(BCThread blockchainthread) {

        this.blockchain = blockchainthread;
        initComponents();
    }
 
    @SuppressWarnings("unchecked")  //
   
    private void initComponents() {

        jPanel1 = new JPanel();
        walletLabel = new JLabel();
        balanceLabel = new JLabel();
        balanceField = new JTextField();
        jPanel2 = new JPanel();
        transactionLabel = new JLabel();
        jScrollPane1 = new JScrollPane();
        transactionArea = new JTextArea();
        jPanel3 = new JPanel();
        sendLabel = new JLabel();
        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        amountField = new JTextField();
        toField = new JTextField();
        sendBtn = new JButton();
        transactionBtn = new JButton();
        balanceBtn = new JButton();
        connectBtn = new JButton();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

        walletLabel.setFont(new Font("Lucida Grande", 1, 18)); // NOI18N
        walletLabel.setText("Wallet");

        balanceLabel.setText("Balance : ");

        balanceField.setEditable(false);

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(balanceLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(balanceField, GroupLayout.PREFERRED_SIZE, 107, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addComponent(walletLabel)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(walletLabel)
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(balanceLabel)
                    .addComponent(balanceField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        jPanel2.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

        transactionLabel.setFont(new Font("Lucida Grande", 1, 18)); // NOI18N
        transactionLabel.setText("Transactions");

        transactionArea.setColumns(20);
        transactionArea.setRows(5);
        jScrollPane1.setViewportView(transactionArea);

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(94, 94, 94)
                .addComponent(transactionLabel)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(transactionLabel)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        jPanel3.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));

        sendLabel.setFont(new Font("Lucida Grande", 1, 18)); // NOI18N
        sendLabel.setText("Transfer Money");

        jLabel1.setText("Amount :");

        jLabel2.setText("To :");

        sendBtn.setText("Send");
        sendBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                sendBtnActionPerformed(evt);
            }
        });

        GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(amountField))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(sendLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2)
                        .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(58, 58, 58)
                                .addComponent(sendBtn)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(52, 52, 52)
                                .addComponent(toField)))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sendLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(amountField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(toField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(sendBtn))
        );

        transactionBtn.setText("Balance");
        transactionBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                transactionBtnActionPerformed(evt);
            }
        });

        balanceBtn.setText("Transactions");
        balanceBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                balanceBtnActionPerformed(evt);
            }
        });

        connectBtn.setText("Connect");
        connectBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                connectBtnActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel1, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(transactionBtn, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE,GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(balanceBtn, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(connectBtn, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(transactionBtn)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(balanceBtn)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(connectBtn)
                        .addGap(0, 8, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }

    // Connect Button Action
    private void connectBtnActionPerformed(ActionEvent evt) {
        // handling code here:
        
        //Random rnd = new Random();
        //balance = rnd.nextInt(100)*73;
        //balanceField.setText(balance+"");
    	
    	// I want to add a messageDialog saying: Are you sure? All 3 clients must be opened. 
    	blockchain.broadcastPublicKeys(); // will block the process until all keys are received.
    	JOptionPane.showMessageDialog(rootPane, "Success! Connected to all users","Success",JOptionPane.PLAIN_MESSAGE);
            
    }
    
    private void parseTransactions() {
    	
    	LinkedList<BlockRecord> tmp = BCThread.blockchain.getLedger();
    	
    	if(tmp == null) {
    		JOptionPane.showMessageDialog(rootPane, "No Transaction History","Error",JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    	
		StringBuilder sb = new StringBuilder();
		String userID = new Integer(BCThread.PID).toString();
		for(BlockRecord br : tmp) {
			
			if (br.getSenderID().equals(userID)) {
				sb.append("You sent " + br.getAmount() + " to User" + br.getRecipientID() + "\n");
			}
			if (br.getRecipientID().equals(userID)) {
				sb.append("You received " + br.getAmount() + " from User" + br.getRecipientID() + "\n");
			}
			if(br.getAVerificationProcessID().equals(userID)) {
				sb.append("You earned 1.00 for successfully mining a block.\n");
			}
		}
		
		//transactionLabel.setText("");   // use this to change the name of Transaction pane
		
		transactionArea.setText(sb.toString());
    	
    }
    // Transaction Button Action
    private void transactionBtnActionPerformed(ActionEvent evt) {
        //  handling code here:
    	parseTransactions();
    	
    }
    
    // helper method to check for valid numeric input
    private static boolean isNumeric(String strNum) {
        return strNum.matches("-?\\d+(\\.\\d+)?");
    }
    
    // Send Button Action
    private void sendBtnActionPerformed(ActionEvent evt) {
        //  handling code here:
    	
    	String to = toField.getText();
    	String amount = amountField.getText();
    	
    	if(!isNumeric(amount) || Double.parseDouble(amount) < 0.0) {
    		JOptionPane.showMessageDialog(rootPane, "Invalid input amount.","Error",JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    	
    	// I will handle the transaction Text in the parseTranaction method(). 
        //String trans = transactionArea.getText();
        //trans+="\n"+"Sent to { User %d, Amount = %f}".format(to, amount);
        //transactionArea.setText(trans);
        
        blockchain.sendMoney(to, amount);

    }
   
    private static void updateBalance(){
    	
    	GUI.balance = 0.0;  // recalculate balance from the beginning of the ledger
		LinkedList<BlockRecord> blockLedger = BCThread.blockchain.getLedger();

		// go through blockchain ledger and calculate
		try {
			for(BlockRecord br : blockLedger) {
				String userID = new Integer(BCThread.PID).toString();
				if (br.getRecipientID().equals(userID)) {
					balance += Double.parseDouble(br.getAmount());
				}
				if (br.getSenderID().equals(userID)) {
					balance -= Double.parseDouble(br.getAmount());
				}
				// if you successfully mined the block you get 1 bitcoin
				if (br.getAVerificationProcessID().contentEquals(userID)) {
					balance += 1.00;
				}
			}
		}catch(Exception x){
			x.printStackTrace();
		}
	}

    private void balanceBtnActionPerformed(ActionEvent evt) {
        //  handling code here:
        
    	updateBalance();
        balanceField.setText(String.format("%.2f", GUI.balance));
    }

    /*
    public static void main(String args[]) {
 
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Group7_Crypto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Group7_Crypto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Group7_Crypto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Group7_Crypto.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Group7_Crypto().setVisible(true);
            }
        });
    }
    */

   
    
   
}