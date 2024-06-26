import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class WarGameServer {
    private int port;
    private List<ClientHandler> clients;
    private ServerSocket serverSocket;
    private final int maxClients = 2;
    private Map<String, Integer> playerScores;
    private int turn;

    public WarGameServer(int port) {
        this.port = port;
        this.clients = new ArrayList<>();
        this.playerScores = new HashMap<>();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port : " + port);
            while (true) {
                if (clients.size() > maxClients) {
                    broadcast("Maxinum players exceeded");
                    break;
                }
                if (clients.size() <= maxClients) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected from " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());
                    ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                    clients.add(clientHandler);
                    new Thread(clientHandler).start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public synchronized void updateScores(String playerName, int newScore) {
        playerScores.put(playerName, newScore);
        broadcastScores();
    }
    public void result(String p1,String p2, int p1Value, int p2Value) {
        String resultMsg = "Result: " + p1 +" : " + p1Value + " " + p2 + " : " + p2Value;
        broadcast(resultMsg);
    }

    private void broadcastScores() {
        StringBuilder scoreMessage = new StringBuilder("ScoreUpdate:");
        for (Map.Entry<String, Integer> entry : playerScores.entrySet()) {
            scoreMessage.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
        }
        broadcast(scoreMessage.toString());
    }

    public synchronized void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcast("PlayerDisconnected:" + clientHandler.getPlayerName());
        broadcastPlayerNames();
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void broadcastPlayerNames() {
        StringBuilder names = new StringBuilder("PlayerNames:");
        for (ClientHandler client : clients) {
            if(client.getPlayerName() != null) {
                names.append(client.getPlayerName()).append(",");
            }
        }
        broadcast(names.toString());
    }
   
    public void checkAllPlayersReady() {
        for (ClientHandler client : clients) {
            String readinessMessage = client.getPlayerName() + (client.isReady() ? " is ready" : " is not ready");
            broadcast("ReadyStatus:" + readinessMessage);
        }
        boolean allReady = clients.stream().allMatch(ClientHandler::isReady);
        if (allReady) {
            broadcast("AllPlayersReady");
        }
    }
    public void broadcastTurnCount() {
        broadcast("TurnCount:" + turn);
    }    

    public static void main(String[] args){
        WarGameServer server = new WarGameServer(12345);
        server.start();
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private WarGameServer server;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private String playerName;
        private boolean isReady = false;

        public ClientHandler(Socket socket, WarGameServer server){
            this.socket = socket;
            this.server = server;
            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                Object nameObj = in.readObject();
                if (nameObj instanceof String){
                    this.playerName =(String)nameObj;
                    server.broadcastPlayerNames();
                }
                while (true) {
                    Object message = in.readObject();
                    if (message instanceof Integer){
                        isReady= ((Integer) message) > 0;
                        server.checkAllPlayersReady();
                    }
                    else if (message instanceof BattleResult) {
                        BattleResult battleResult = (BattleResult) message;
                        server.processBattleResult(this, battleResult);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                closeConnections();
            }
        }

        private void closeConnections() {
            try {
                server.removeClient(this);
                socket.close();
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String message) {
            try {
                out.writeObject(message);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean isReady() {
            return isReady;
        }

        public String getPlayerName() {
            return playerName;
        }
    }

    public synchronized void processBattleResult(ClientHandler clientHandler, BattleResult battleResult) {
        turn ++;
        String playerName = clientHandler.getPlayerName();
        int p1CardValue = battleResult.getP1CardValue();
        int p2CardValue = battleResult.getP2CardValue();
    
        // Find the other player
        String otherPlayerName = null;
        for (ClientHandler client : clients) {
            if (!client.getPlayerName().equals(playerName)) {
                otherPlayerName = client.getPlayerName();
                break;
            }
        }
        
        if (p1CardValue > p2CardValue) {
            result(playerName, otherPlayerName, p1CardValue, p2CardValue);
            updateScores(playerName, playerScores.getOrDefault(playerName, 0) + 1);
            broadcast("Player1:"+playerName);
        } else if (p2CardValue > p1CardValue) {
            result(playerName, otherPlayerName, p1CardValue, p2CardValue);
            updateScores(otherPlayerName, playerScores.getOrDefault(otherPlayerName, 0) + 1);
            broadcast("Player2:" + otherPlayerName);
        } else if (p1CardValue == p2CardValue) {
            result(playerName,otherPlayerName,p1CardValue,p2CardValue);
            broadcast("Tied:");
        }
        broadcastTurnCount();
    }   
}