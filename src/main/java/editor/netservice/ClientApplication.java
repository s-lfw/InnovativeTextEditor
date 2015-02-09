package editor.netservice;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author Vsevolod Kosulnikov
 */
public class ClientApplication {
    private final String host;
    private final int port;
    public ClientApplication(String host, int port) {
        this.host = host;
        this.port = port;
    }
    public void run() throws IOException {
        try (Socket socket = new Socket(host, port)) {
            Scanner scanner = new Scanner(System.in);
            PromptProtocol protocol = new PromptProtocol(socket.getInputStream(),
                    socket.getOutputStream());
            String inLine;
            while ((inLine = scanner.nextLine())!=null) {
                if (inLine.equalsIgnoreCase("exit")) {
                    socket.close();
                    return;
                } else {
                    try {
                        String response = protocol.request(inLine);
                        System.out.println(response);
                    } catch (BadRequestException e) {
                        PromptProtocol.printUsage(System.out);
                    } catch (IOException e) {
                        System.out.println("IOException occurred during request");
                    }
                }
            }
        }
    }
}
