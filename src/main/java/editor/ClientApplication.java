package editor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * @author Vsevolod Kosulnikov
 */
public class ClientApplication {
    public ClientApplication(String ipOrHost, int port) {
        try (Socket socket = new Socket(ipOrHost, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()))) {
            Scanner scanner = new Scanner(System.in);
            String inLine;
            while ((inLine = scanner.nextLine())!=null) {
                if (inLine.startsWith("get ")) {
                    out.println(inLine);
                    String response;
                    while ((response = in.readLine())!=null) {
                        System.out.println(response);
                    }
                } else {
                    System.out.println("Usage: \"get <prefix>\"");
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ClientApplication("localhost", 7321);
    }
}
