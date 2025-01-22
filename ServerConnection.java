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
    private Scanner scanner;

    public ServerConnection(Socket socket, SecretKey secretKey) {
        this.socket = socket;
        this.secretKey = secretKey;
        try {
            System.out.println("Initializing ServerConnection...");
            if (socket == null || socket.isClosed()) {
                System.out.println("First time hi fail...");
                throw new IllegalStateException("Socket is null or closed.");
            }
            this.pr = new ObjectOutputStream(socket.getOutputStream());
            this.br = new ObjectInputStream(socket.getInputStream());
            this.scanner=new Scanner(System.in);
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
        Thread readerThread = new Thread(() -> {
            while (isRunning) {
                if (!socket.isClosed()) {
                    try {
                    
                        Object obj = br.readObject();
                        if (obj != null) {
                            String encryptedMessage = (String) obj;
                            try {
                                messages = (Message) encryptionAndDecryption.Decrypt(encryptedMessage, secretKey);
                                String input = messages.getMessage();

                                if (input != null) {
                                    System.out.println("Client: " + input);
                                    if (input.equalsIgnoreCase("exit")) {
                                        System.out.println("Client closed the connection.");
                                        stopConnection(); 
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                System.err.println("Error decrypting message: " + e.getMessage());
                                stopConnection(); 
                                break;
                            }
                        }
                    } catch (IOException e) {
                        if (isRunning && socket.isClosed()) {
                            System.err.println("Error reading from client: " + e.getMessage());
                            e.printStackTrace();
                        }
                        break;
                    } catch (ClassNotFoundException e) {
                        System.err.println("Error deserializing message: " + e.getMessage());
                    }
                }else{
                    System.out.println("Socket is get closed by Clients side");
                    break;
                }

            }
        });

        // Writer Thread
        Thread writerThread = new Thread(() -> {
            
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
                        stopConnection(); // Close socket and exit
                        break;
                    }
                }
            
        });

        // Start Threads
        readerThread.start();
        writerThread.start();
        try {
            readerThread.join();
            writerThread.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void stopConnection() {
        isRunning = false;
        try {
            if (pr != null)
                pr.close();
            if (br != null)
                br.close();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }
}
