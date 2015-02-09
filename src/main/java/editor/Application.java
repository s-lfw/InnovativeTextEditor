package editor;

import editor.netservice.ClientApplication;
import editor.netservice.ServerApplication;

import java.io.*;
import java.util.List;

/**
 * @author Vsevolod Kosulnikov
 */
public class Application {
    private Dictionary dictionary;

    public void run() throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            initDictionary(br);
            doWork(br);
        }
    }

    public void initDictionary(BufferedReader br) throws IOException {
        dictionary = Dictionary.initDictionary(br);
    }

    public void doWork(BufferedReader br) throws IOException {
        // doing the work!
        int queriesCount;
        String currentLine = br.readLine();
        try {
            queriesCount = Integer.parseInt(currentLine);
        } catch (NumberFormatException e) {
            throw new IOException("Cannot resolve queries count M, trying to parse \'"
                    +currentLine+"\'");
        }
        for (int query = 0; query<queriesCount; ++query) {
            List<String> selection = getSelection(br.readLine());
            for (String s : selection) {
                System.out.println(s);
            }
        }
    }

    protected List<String> getSelection(String prefix) {
        return dictionary.getSelection(prefix);
    }

    public static void main(String[] args) {
        LaunchMode launchMode = LaunchMode.DESKTOP;
        String host = null;
        String dictionaryFilePath = "";
        int port = -1;
        if (args.length>0) {
            switch (args[0]) {
                case "-server":
                    launchMode = LaunchMode.SERVER;
                    dictionaryFilePath = args[1];
                    port = Integer.parseInt(args[2]);
                    break;
                case "-client":
                    launchMode = LaunchMode.CLIENT;
                    host = args[1];
                    port = Integer.parseInt(args[2]);
                    break;
            }
        }
        try {
            switch (launchMode) {
                case DESKTOP:
                    Application app = new Application();
                    app.run();
                    break;
                case SERVER:
                    ServerApplication serverApp =
                            new ServerApplication(new File(dictionaryFilePath), port);
                    serverApp.run();
                    break;
                case CLIENT:
                    ClientApplication clientApp = new ClientApplication(host, port);
                    clientApp.run();
            }
        } catch (Throwable e) {
            System.err.println("Oops! Exception occurred, program will be terminated.");
            e.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }

    protected static enum LaunchMode {
        DESKTOP, SERVER, CLIENT
    }
}
