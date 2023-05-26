import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try (
                Socket socket = new Socket("127.0.0.1", 8989);
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            reader.readLine();

            Scanner scanner = new Scanner(System.in);
            String searchRequest = scanner.nextLine().toLowerCase();
            writer.println(searchRequest);
            String line = "";
            StringBuilder responseStrBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                responseStrBuilder.append(line);
                responseStrBuilder.append("\n");
            }
            System.out.println(responseStrBuilder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}