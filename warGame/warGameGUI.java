import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/// This class is the GUI for the war game where there is 2 screens that will show(exluding error messages) incuding: a title/lobby
/// screen used to prompt users into joining the server, and then the war screen, used to play the game where there is a war, ready,
/// and display features implimented.
public class warGameGUI extends JFrame{
    // card layout fields
    private JPanel cards;
    private CardLayout cardLay;
    private JPanel lobbyPage;
    private JPanel warPage;
    private JPanel winPage;

    // connection page fields
    private JLabel titleL;
    private JLabel cDescriptionL1;
    private JLabel cDescriptionL2;
    private JButton connectB;
    private JTextArea nameA;
    private JTextArea hostA;
    private JTextArea portA;
    private JPanel cTopP;
    private JPanel cBottomP;
 
    // game page fields
    private JPanel topP;
    private JLabel playersL;
    private JLabel scoreL;
 
    private JPanel mainPanel;
    private GridBagConstraints constraints;
    private JLabel p1Pile;
    private JLabel p2Pile;
    private JLabel p1Active;
    private JLabel p2Active;
    private JLabel p1Dead;
    private JLabel p2Dead;

    private JPanel bottomP;
    private JButton readyB;
    private JButton battleB;
    private JButton disconnectB;

    // winner page Fields
    private JPanel wPanel;
    private JLabel winnerL;
    private JLabel wDescriptionL;
    private JButton wDisconnectB;

    // data
    private int ready;
    private String score;
    private String p1Name;
    private String p2Name;
    private int p1Score;
    private int p2Score;
    public String winner;
    private int turn;
 
    private warGameClient clientNetworking;


    /// constuctor for setting up the cards in a proper layout
    public warGameGUI(){
        clientNetworking = new warGameClient("localhost", 12345, "playerName", this);

        // connection page
        cTopP = new JPanel();       
        cTopP.setLayout(new FlowLayout());
        titleL = new JLabel("WAR!");
        titleL.setFont(new Font("Serif", Font.BOLD, 40));
        nameA = new JTextArea(1, 16);
        hostA = new JTextArea(1, 16);
        portA = new JTextArea(1, 16);
        cTopP.add(titleL);
        cTopP.add(new JLabel("Name"));
        cTopP.add(nameA);
        cTopP.add(new JLabel("IP Address"));
        cTopP.add(hostA);
        cTopP.add(new JLabel("Port"));
        cTopP.add(portA);

        cBottomP = new JPanel();       
        cBottomP.setLayout(new BorderLayout());
        connectB = new JButton("Connect");
        cDescriptionL1 = new JLabel("Once connected, You will enter a lobby with the other players name presented on the top left. You will see the score on the top right.");
        cDescriptionL2 = new JLabel("And finally your cards will be presented on the bottom with your opponants on the top. There will be a ready button and a battle button which you must press in sucsession");
        
        cBottomP.add(connectB, BorderLayout.NORTH);
        cBottomP.add(cDescriptionL1, BorderLayout.CENTER);
        cBottomP.add(cDescriptionL2, BorderLayout.SOUTH);
 
        lobbyPage = new JPanel();
        lobbyPage.setLayout(new BorderLayout());
        lobbyPage.add(cTopP, BorderLayout.CENTER);
        lobbyPage.add(cBottomP, BorderLayout.SOUTH);
 
        // game page
        topP = new JPanel();
        topP.setLayout(new BorderLayout());
     
        playersL = new JLabel("Wating for another player to join...");
        scoreL = new JLabel(score);
        topP.add(playersL, BorderLayout.WEST);
        topP.add(scoreL, BorderLayout.EAST);
 
        mainPanel = new JPanel(new GridBagLayout());
        constraints = new GridBagConstraints();
        p1Pile = new JLabel();
        p2Pile = new JLabel();
        p1Active = new JLabel();
        p2Active = new JLabel();
        p1Dead = new JLabel();
        p2Dead = new JLabel();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0.5;
        constraints.weighty = 0.5;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(5, 5, 5, 5);

        mainPanel.add(p2Pile, constraints);
        constraints.gridx = 1;
        mainPanel.add(p2Active, constraints);
        constraints.gridx = 2;
        mainPanel.add(p2Dead, constraints);
        constraints.gridy = 1;
        mainPanel.add(p1Dead, constraints);
        constraints.gridx = 1;
        mainPanel.add(p1Active, constraints);
        constraints.gridx = 0;
        mainPanel.add(p1Pile, constraints);

        imageAss("Documents/cardBack.png", p1Pile);
        imageAss("Documents/cardBack.png", p2Pile);
        imageAss("Documents/white.jpg", p1Active);
        imageAss("Documents/white.jpg", p2Active);
        imageAss("Documents/white.jpg", p1Dead);
        imageAss("Documents/white.jpg", p2Dead);

        bottomP = new JPanel();
        bottomP.setLayout(new FlowLayout());
        readyB = new JButton("Ready");
        battleB = new JButton("Battle!");
        disconnectB = new JButton("Disconnect");
        bottomP.add(readyB);
        bottomP.add(battleB);
        bottomP.add(disconnectB);

        battleB.setEnabled(false);

        warPage = new JPanel();
        warPage.setLayout(new BorderLayout());
        warPage.add(topP, BorderLayout.NORTH);
        warPage.add(mainPanel, BorderLayout.CENTER);
        warPage.add(bottomP, BorderLayout.SOUTH);
 
        // winner page
        wPanel = new JPanel(){
            @Override
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                try{
                    BufferedImage wBackground = ImageIO.read(new File("/Documents/winBackground.JPEG"));
                    g.drawImage(wBackground, 0, 0, getWidth(), getHeight(), this);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        };
        wPanel.setLayout(new BorderLayout());
        winnerL = new JLabel("Winner: " + winner);
        wDescriptionL = new JLabel(winner + " has won!!!\n Press the disconnect button to return to the lobby.");
        wDisconnectB = new JButton("Disconnect");

        wPanel.setLayout(new BorderLayout());
        wPanel.add(winnerL, BorderLayout.NORTH);
        wPanel.add(wDescriptionL, BorderLayout.CENTER);
        wPanel.add(wDisconnectB, BorderLayout.SOUTH);

        winPage = new JPanel();
        winPage.setLayout((new BorderLayout()));
        winPage.add(wPanel, BorderLayout.CENTER);

        // card layout to set up pages
        cardLay = new CardLayout();
        cards = new JPanel(cardLay);
        cards.add(lobbyPage, "PAGE_1");
        cards.add(warPage, "PAGE_2");
        cards.add(winPage, "PAGE_3");

        // action listeners
        connectB.addActionListener(new connectBActionListener());
        readyB.addActionListener(new readyActionListener());
        battleB.addActionListener(new battleActionListener());
        disconnectB.addActionListener(new disconnectActionListener());
        wDisconnectB.addActionListener(new disconnectActionListener());

        add(cards);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        }
 
    /// connects to the server and switches screens to the warPage
    private class connectBActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            String host = hostA.getText();
            String portStr = portA.getText();
            String userName = nameA.getText();
            p1Name = userName;
            // error handling ( maybe change later to utilise the accessabiltiy of the buttons instead of error msgs)
            if(host.isEmpty() || portStr.isEmpty() || userName.isEmpty()){
                JOptionPane.showMessageDialog(warGameGUI.this, "Field missing, please input your name, host, and port into the proper areas.");
                return;
            }
            int port;
            try{
                port = Integer.parseInt(portStr);
            }catch(NumberFormatException ex){
                JOptionPane.showMessageDialog(warGameGUI.this, "Invalid Port");
                return;
            }
            initializeClientNetworking(host, port, userName);
            if(clientNetworking.connect()) cardLay.show(cards, "PAGE_2");

            else JOptionPane.showMessageDialog(warGameGUI.this, "Failed to connect to the server. Please check the host and port.");
        }
    }

    /// Send information to the client that this user is ready
    /// update the text field to let the other player know is ready
    private class readyActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            ready = 1;
            ready = clientNetworking.sendReady(ready);
        }
    }
   
    /// Impliments "BATTLE" mechanic
    private class battleActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            clientNetworking.battle();
            ready = 0;
        }
    }

    /// disconnects from the server and switches screens to the lobbyPage
    private class disconnectActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            clientNetworking.disconnect();
            hostA.setText("");
            portA.setText("");
            nameA.setText("");
            cardLay.show(cards, "PAGE_1");
        }
    }

    /// set an image onto a specific JLabel
    private void imageAss(String path, JLabel label){
        try{
            BufferedImage img = ImageIO.read(new File(path));
            ImageIcon icon = new ImageIcon(img);
            label.setIcon(icon);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /// Help initialize the client
    public void initializeClientNetworking(String host, int port, String userName){
        clientNetworking = new warGameClient(host, port, userName, this);
    }

    /// "BATTLE" mechanic effects helper
    public void shiftIntensity(){
        Color currCol = warPage.getBackground();
        if(currCol.equals(Color.WHITE)) warPage.setBackground(Color.RED);
      
        else warPage.setBackground(Color.WHITE);
    }
    public JButton getBattleButton(){
        return battleB;
    }
    /// Game page GUI updates
    /// update whether you or the other player is ready
    public void updateReadyStatus(int status){
        readyB.setText("Ready" + ready + "/2");
    }
    /// update the label of the current players
    public void updatePlayerNames(String p1Name, String p2Name){
        SwingUtilities.invokeLater(() -> {
            playersL.setText(p1Name + " vs " + p2Name);
        });
    }
    /// Update scores and then update the screen
    public void p1Win(){
        switchButtonActivityBattle();
    }
    public void p2Win(){
        switchButtonActivityBattle();
    }

    /// The method will move the active cards to the dead piles
    public void switchButtonActivityReady(String p1Card, String p2Card){
        battleB.setEnabled(true);
        readyB.setEnabled(false);
        imageAss(p1Card, p1Active);
        imageAss(p2Card, p2Active);
        // edge case where there are no longer cards in the piles
        if(turn == 24){
            imageAss("Documents/white.jpg", p1Pile);
            imageAss("Documents/white.jpg", p2Pile);
        }
    }
    /// The method will move the active cards to the dead piles
    public void switchButtonActivityBattle(){
        battleB.setEnabled(false);
        readyB.setEnabled(true);
        imageAss("Documents/white.jpg", p1Active);
        imageAss("Documents/white.jpg", p2Active);
        imageAss("Documents/cardBack.png", p1Dead);
        imageAss("Documents/cardBack.png", p2Dead);
        turn++;
    }

    /// update the scores of the players
    public void updateScore(String playerName, int score) {
        if(playerName.equals(this.p1Name)) this.p1Score = score;

        else if (playerName.equals(this.p2Name)) this.p2Score = score;

        scoreL.setText("Your Score : " + p1Score);
    }

    /// Show winner page after 25 rounds
    public void showWinner(){
        if(p1Score > p2Score) winner = p1Name;

        else if(p2Score > p1Score) winner = p2Name;

        else winner = p1Name + p2Name;

        wDescriptionL.setText(winner + " has won!!!\n Press the disconnect button to return to the lobby.");
        cardLay.show(cards, "PAGE_3");
    }

    /// Main to create the GUI
    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> {
            warGameGUI clientGUI = new warGameGUI();
            clientGUI.setVisible(true);
        });
    }
}