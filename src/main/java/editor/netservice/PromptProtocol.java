package editor.netservice;

import editor.Dictionary;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Date: 09.02.15
 *
 * @author Vsevolod Kosulnikov
 */
public class PromptProtocol {
    private static final String BAD_REQUEST = "%%bad_request%%";
    private static final String CHARSET_NAME = "UTF-8";

    private final BufferedReader in;
    private final PrintWriter out;
    private final Dictionary dictionary;
    public PromptProtocol(InputStream in, OutputStream out) {
        this(in, out, null);
    }
    public PromptProtocol(InputStream in, OutputStream out, Dictionary dictionary) {
        this.in = new BufferedReader(new InputStreamReader(in, Charset.forName(CHARSET_NAME)));
        this.out = new PrintWriter(new OutputStreamWriter(out, Charset.forName(CHARSET_NAME)), true);
        this.dictionary = dictionary;
    }

    public void listen() throws IOException {
        String inLine;
        while ((inLine = in.readLine()) != null) {
            processRequest(inLine);
        }
    }

    public void processRequest(String request) {
        if (!request.startsWith("get ")) {
            out.println(BAD_REQUEST);
        }
        List<String> selection = dictionary.getSelection(request.substring(4));
        out.println(selection.size());
        for (String s : selection) {
            out.println(s);
        }
    }

    public String request(String request) throws IOException {
        out.println(request);
        String response;
        StringBuilder responseBuilder = new StringBuilder();
        int length = 0;
        int stringCount = 0;
        while (stringCount<=length && (response = in.readLine()) != null) {
            if (response.equals(BAD_REQUEST)) {
                throw new BadRequestException("Bad request: "+request);
            } else if (length==0) {
                length = Integer.parseInt(response);
            } else {
                responseBuilder.append(response).append("\r\n");
            }
            ++stringCount;
        }
        return responseBuilder.toString();
    }

    public void closeConnection() {
        try {
            if (out!=null) {
                out.close();
            }
            if (in!=null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace(); //todo
        }
    }

    public static void printUsage(PrintStream out) {
        out.println("Usage: \"get <prefix>\" to create a request");
        out.println("Usage: \"exit\" to close connection and exit");
    }
}
