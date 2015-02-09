package editor.netservice;

import editor.Application;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vsevolod Kosulnikov
 */
public class ClientApplication extends Application {
    private final Socket socket;
    private final PromptProtocol protocol;
    public ClientApplication(String host, int port) throws IOException {
        socket = new Socket(host, port);
        this.protocol = new PromptProtocol(socket.getInputStream(), socket.getOutputStream());
    }

    @Override
    public void run() throws IOException {
        try {
            super.run();
        } finally {
            socket.close();
        }
    }

    @Override
    public void initDictionary(BufferedReader br) throws IOException {
        // in net implementation dictionary on client side is not required
    }

    @Override
    protected List<String> getSelection(String prefix) {
        try {
            return protocol.request(prefix);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

}
