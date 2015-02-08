package editor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Vsevolod Kosulnikov
 */
public class ServerApplication {
    public static final String END_OF_RESPONSE = "%end_of_response%";

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private Dictionary dictionary;
    public ServerApplication(File dictionaryFile, int port) throws IOException {
        System.out.println("Reading dictionary from \'"+dictionaryFile+"\'...");
        dictionary = Dictionary.initDictionary(dictionaryFile);
        System.out.println("Initializing socket listener...");
        startListenerThread(port);
        System.out.println("Server started. For stop server and close all connections type " +
                "\'exit\'");
        Scanner scanner = new Scanner(System.in);
        while (true) {
//            String request = System.console().readLine();
            String request = scanner.nextLine();
            if (request.equalsIgnoreCase("exit")) {
                closeAllConnections();
                System.exit(0);
            } else if (request.startsWith("get ")) {
                List<String> selection = dictionary.getSelection(request.substring(4));
                for (String s : selection) {
                    System.out.println(s);
                }
            }
        }
    }

    private void startListenerThread(int port) {
        executorService.submit(new PortListener(port));
    }

    private void processConnection(Socket clientSocket) {
        executorService.submit(new ConnectionListener(clientSocket));
    }

    private void closeAllConnections() {
        executorService.shutdownNow();
    }

    private class PortListener implements Runnable {
        private final int port;
        public PortListener(int port) {
            this.port = port;
        }
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
    }

    private class ConnectionListener implements Runnable {
        private final Socket clientSocket;
        private ConnectionListener(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(
                         new InputStreamReader(clientSocket.getInputStream()))
            ) {
                String inLine;
                while ((inLine = in.readLine()) != null) {
                    if (!inLine.startsWith("get ")) {
                        continue;
                    }
                    List<String> selection = dictionary.getSelection(inLine.substring(4));
                    for (String s : selection) {
                        out.println(s);
                    }
                    out.println(END_OF_RESPONSE);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
//        try (
//                ServerSocket serverSocket = new ServerSocket(7321);
//                Socket clientSocket = serverSocket.accept();
//                PrintWriter out =
//                        new PrintWriter(clientSocket.getOutputStream(), true);
//                BufferedReader in = new BufferedReader(
//                        new InputStreamReader(clientSocket.getInputStream()));
//        ) {
//            startSession(clientSocket);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
            new ServerApplication(new File("test.in"), 7321);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
