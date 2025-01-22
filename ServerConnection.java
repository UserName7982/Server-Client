import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import javax.crypto.SecretKey;

public class ServerConnection {
    private Socket socket;

    private ObjectOutputStream pr;
    private ObjectInputStream br;
    private Message messages;
    private EncryptionAndDecryption encryptionAndDecryption;
    private SecretKey secretKey;
    private volatile boolean isRunning = true;
    private boolean isInitialized = false;

    public ServerConnection(Socket socket, SecretKey secretKey) {
        this.socket = socket;
        this.secretKey = secretKey;
        try {
            System.out.println("Initializing ServerConnection...");
            if (socket == null || socket.isClosed()) {
                throw new IllegalStateException("Socket is null or closed.");
            }
            pr = new ObjectOutputStream(socket.getOutputStream());
            br = new ObjectInputStream(socket.getInputStream());
            encryptionAndDecryption = new EncryptionAndDecryption();
            System.out.println("SecretKey generated: " + secretKey);
            isInitialized = true;
        } catch (IOException e) {
            System.err.println("Error initializing ServerConnection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void start() {
        if (!isInitialized) {
            System.err.println("ServerConnection is not properly initialized. Cannot start.");
            return;
        }

        // Reader Thread
        new Thread(() -> {
            try {
                // br=new ObjectInputStream(socket.getInputStream());
                System.out.println(br);
                while (isRunning) {
                    try {
                            String encryptedMessage = (String) br.readObject();
                            System.out.println(encryptedMessage);
                            try {
                                messages = (Message) encryptionAndDecryption.Decrypt(encryptedMessage, secretKey);
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            String input = messages.getMessage();

                            if (input != null) {
                                System.out.println("Client: " + input);
                                if (input.equalsIgnoreCase("exit")) {
                                    System.out.println("Client closed the connection.");
                                    br.close();
                                    stopConnection();
                                    break;
                                }
                            }
                        

                    } catch (ClassNotFoundException e) {
                        System.err.println("Error deserializing message: " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error reading from client: " + e.getMessage());
                }
            }finally{
                stopConnection();
            }
        }).start();

        // Writer Thread
        new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (isRunning) {
                    String message = scanner.nextLine();
                    messages = new Message(message, socket.getLocalAddress().toString(),
                            socket.getInetAddress().toString());

                    try {
                        String encryptedMessage = encryptionAndDecryption.encrypt(messages, secretKey);
                        pr.writeObject(encryptedMessage);
                        pr.flush();
                    } catch (Exception e) {
                        System.err.println("Error encrypting or sending message: " + e.getMessage());
                    }

                    if (message.equalsIgnoreCase("exit")) {
                        System.out.println("Closing connection...");
                        stopConnection();
                        break;
                    }
                }
            }
        }).start();
    }

    private void stopConnection() {
        isRunning = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }
}
