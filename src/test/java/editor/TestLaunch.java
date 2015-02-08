package editor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vsevolod Kosulnikov
 */
public class TestLaunch {
    private static String dictionaryFileName = "test.in";
    private static String resultFileName = "test.out";
    private static String jarName = "InnovativeTextEditor-1.0-SNAPSHOT.jar";
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.dir"), dictionaryFileName)));
            List<String> testInput = new ArrayList<>();
            String currentLine;
            while ((currentLine = br.readLine())!=null) {
                testInput.add(currentLine);
            }
            br.close();

            List<String> cmd = new ArrayList<>();
            cmd.add("java");
            cmd.add("-jar");
            cmd.add(new File(System.getProperty("user.dir"), jarName).getAbsolutePath());
            ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            final Process process = processBuilder.start();

            final Thread errorStreamReading = new Thread(new Runnable(){
                public void run(){
                    try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
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
//            errorStreamReading.setDaemon(true);
            errorStreamReading.start();

            final BufferedWriter bw = new BufferedWriter(new FileWriter(new File(System.getProperty("user.dir"), resultFileName)));
            final Thread inputStreamThread = new Thread(new Runnable(){
                public void run(){
                    try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))){
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
//            inputStreamThread.setDaemon(true);
            inputStreamThread.start();

            try (BufferedWriter inputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
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
