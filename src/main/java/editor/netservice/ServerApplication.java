package editor.netservice;

import editor.Dictionary;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Vsevolod Kosulnikov
 */
public class ServerApplication {
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private Dictionary dictionary;
    private final int port;

    public ServerApplication(File dictionaryFile, int port) throws IOException {
        System.out.println("Reading dictionary from \'"+dictionaryFile+"\'...");
        dictionary = Dictionary.initDictionary(dictionaryFile);
        this.port = port;
    }

    public void run() {
        System.out.println("Initializing socket listener...");
        startListenerThread(port);
        System.out.println("Server started. For stop server and close all connections type " +
                "\'exit\'");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String request = scanner.nextLine();
            if (request.equalsIgnoreCase("exit")) {
                closeAllConnections();
                System.exit(0);
            }
        }
    }

    private void startListenerThread(final int port) {
        Runnable portListener = new Thread() {
            @Override
            public void run() {
                try (ServerSocket serverSocket = new ServerSocket(port)) {
                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        processConnection(clientSocket);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        executorService.submit(portListener);
    }

    private void processConnection(final Socket clientSocket) {
        Runnable connectionListener = new Thread() {
            private PromptProtocol protocol;
            @Override
            public void run() {
                try {
                    protocol = new PromptProtocol(clientSocket.getInputStream(),
                            clientSocket.getOutputStream(), dictionary);
                    protocol.listen();
                } catch (IOException e) {
                    e.printStackTrace();
                    protocol.closeConnection();
                }
            }

            @Override
            public void interrupt() {
                super.interrupt();
                protocol.closeConnection();
            }
        };
        executorService.submit(connectionListener);
    }

    private void closeAllConnections() {
        executorService.shutdownNow();
    }
}
