package editor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vsevolod Kosulnikov
 */
public class TestLaunch {
    private static final String DICTIONARY_FILE_NAME = "test.in";
    private static final String RESULT_FILE_NAME = "test.out";
    private static final String JAR_NAME = "InnovativeTextEditor-1.1-SNAPSHOT.jar";
    public static void main(String[] args) {
        try {
            // reading input data from file
            File dictionaryFile = new File(System.getProperty("user.dir"), DICTIONARY_FILE_NAME);
            BufferedReader br = new BufferedReader(new FileReader(dictionaryFile));
            List<String> testInput = new ArrayList<>();
            String currentLine;
            while ((currentLine = br.readLine())!=null) {
                testInput.add(currentLine);
            }
            br.close();

            // preparing and launching innovative editor
            List<String> cmd = new ArrayList<>();
            cmd.add("java");
            cmd.add("-jar");
            cmd.add(new File(System.getProperty("user.dir"), JAR_NAME).getAbsolutePath());
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            final Process process = processBuilder.start();

            // initializing listener for StdErr
            final Thread errorStreamReading = new Thread(new Runnable(){
                public void run(){
                    try (BufferedReader input = new BufferedReader(
                            new InputStreamReader(process.getErrorStream()))) {
                        String line;
                        while ((line = input.readLine()) != null) {
                            System.err.println(line);
                        }
                        input.close();
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            });
            errorStreamReading.setName("Error-reading-thread");
            errorStreamReading.start();

            // initializing listener for StdOut and redirecting it to file
            File resultFile = new File(System.getProperty("user.dir"), RESULT_FILE_NAME);
            final BufferedWriter bw = new BufferedWriter(new FileWriter(resultFile));
            final Thread inputStreamThread = new Thread(new Runnable(){
                public void run(){
                    try (BufferedReader input = new BufferedReader(
                            new InputStreamReader(process.getInputStream()))){
                        String line;
                        while ((line = input.readLine())!=null) {
                            bw.write(line);
                            bw.write("\r\n");
                        }
                        bw.close();
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            });
            inputStreamThread.setName("Input-reading-thread");
            inputStreamThread.start();

            // sending input data to StdIn
            try (BufferedWriter inputWriter = new BufferedWriter(
                    new OutputStreamWriter(process.getOutputStream()))) {
                for (String input : testInput) {
                    inputWriter.write(input);
                    inputWriter.write("\r\n");
                }
                inputWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
