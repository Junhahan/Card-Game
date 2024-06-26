import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class warGameClient{
    private String host;
    private int port;
    private String playerName;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Thread listenThread;
    private boolean running = false;
    private warGameGUI gui;
    private Deck deck;
    private int turn;
    private boolean hands;
    private ArrayList<card> p1Hand;
    private ArrayList<card> p2Hand;

    public warGameClient(String host, int port, String playerName, warGameGUI gui){
        this.host = host;
        this.port = port;
        this.playerName = playerName;
        this.gui = gui;
    }

    public void setBattleButtonEnabled(boolean enabled){
        SwingUtilities.invokeLater(() -> {
            gui.getBattleButton().setEnabled(enabled);
        });
    }
    public int sendReady(int ready){
        try{
            out.writeObject(ready);
            out.flush();

            /***ended up not being able to use the pictures
            String p1CardPath = p1Hand.get(turn).getFileName();
            String p2CardPath = p2Hand.get(turn).getFileName();
            gui.switchButtonActivityReady(p1CardPath, p2CardPath);
            */
        }catch(IOException e){
            e.printStackTrace();
        }
        return ready--;
    }

    public boolean connect(){
        try{
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(playerName);
            out.flush();
            startListening();
            prepareDeck();
            return true;
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }

    private void startListening(){
        running = true;
        listenThread = new Thread(() -> {
            try{
                while(running){
                    Object message = in.readObject();
                    handleMessage(message);
                }
            }catch(IOException | ClassNotFoundException e){
                running = false;
                e.printStackTrace();
            }
        });
        listenThread.start();
    }

    private void handleMessage(Object message){
        if(message instanceof String){
            String msg = (String)message;
            if(msg.startsWith("ScoreUpdate:")){
            processScoreUpdate(msg.substring("ScoreUpdate:".length()));
            }
            else if(msg.startsWith("PlayerNames:")){
                String[] names = msg.substring("PlayerNames:".length()).split(",");
                if(names.length >= 2){
                    gui.updatePlayerNames(names[0].trim(), names[1].trim());
                }
            }
           
            else if(msg.startsWith("ReadyStatus:")){
                String readinessMessage = msg.split(":")[1].trim();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(gui, readinessMessage);
                });
            }
            else if(msg.equals("AllPlayersReady")){
                SwingUtilities.invokeLater(() -> {
                    gui.switchButtonActivityReady(msg, msg);
                });
            }
            else if(msg.startsWith("PlayerDisconnected:")){
                String disconnectedPlayer = msg.split(":")[1].trim();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(gui, disconnectedPlayer + " has disconnected.", "Player Disconnected", JOptionPane.INFORMATION_MESSAGE);
                });
            }
            else if(msg.startsWith("Player1:")){
                String p1win = msg.split(":")[1].trim();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(gui, p1win + " Scored!", "Player1", JOptionPane.INFORMATION_MESSAGE);
                });
            }
            else if(msg.startsWith("Player2:")){
                String p2win = msg.split(":")[1].trim();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(gui, p2win + " Scored!", "Player2", JOptionPane.INFORMATION_MESSAGE);
                });
            }
            else if(msg.startsWith("Result:")){
                String resultpg = msg;
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(gui, resultpg,"Round : " + turn, JOptionPane.INFORMATION_MESSAGE);
                });
            }
            else if(msg.startsWith("Tied:")){
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(gui, "Tied!","Tie", JOptionPane.INFORMATION_MESSAGE);
                });
            }
            else if(msg.startsWith("TurnCount:")){
                String turnCount = msg.split(":")[1].trim();
                turn = Integer.parseInt(turnCount);
            }
        }
    }

    private void processScoreUpdate(String scoreData){
        String[] playerScores = scoreData.split(",");
        for(String playerScore : playerScores){
            String[] parts = playerScore.split(":");
            if(parts.length == 2){
                String playerName = parts[0];
                try{
                    int score = Integer.parseInt(parts[1].trim());
                    SwingUtilities.invokeLater(() -> {
                        gui.updateScore(playerName, score);
                    });
                }catch(NumberFormatException e){
                    System.err.println("Invalid score");
                }
            }
        }
    }

    public void disconnect(){
        running = false;
        try{
            if(listenThread != null && listenThread.isAlive()) listenThread.interrupt();
            
            if(socket != null) socket.close();

            if(in != null) in.close();

            if(out != null) out.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    //// maybe calling inthe server would be better, I think that if we do it this
    /// way we may have to pass the hands bool to the other clients
    /// create the deck and the hands that the players need to play with
    private synchronized void prepareDeck(){
        // Base case: if the hands exist than dont make a new deck with new hands
        if(hands) return;

        deck = new Deck();
        deck.shuffle();
        deck.shuffle();
        p1Hand = deck.dealHand();
        p2Hand = deck.dealHand();
        hands = true;
    }

    /// Once battle is activated the two cards in the active stage will have their values
    /// comapred and then the score of the winning player will increment accordingly
    public void battle(){
        if(turn == 0 && !hands) prepareDeck();

        int p1CardValue = p1Hand.get(turn).getValue();
        int p2CardValue = p2Hand.get(turn).getValue();
        // Send battle result to the server instead of directly updating the GUI
        try{
            out.writeObject(new BattleResult(p1CardValue, p2CardValue));
            out.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
        // Check for end of the game
        if(turn == 3){
            gui.showWinner();
            hands = false;
        }
    }
}