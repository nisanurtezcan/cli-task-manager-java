package taskManager;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class TCPClient {
    public static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Parameters: <Server> <Port>");
        }
        
        String server = args[0];
        int port = Integer.parseInt(args[1]);
        
        try (Socket socket = new Socket(server, port);
             Scanner input = new Scanner(System.in);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            
            // Read and display welcome messages from server
            String serverMsg;
            while ((serverMsg = reader.readLine()) != null) {
                System.out.println(serverMsg);
                if (serverMsg.contains("REGISTER") || serverMsg.contains("LOGIN")) {
                    break;
                }
            }
            
            // Authentication loop (REGISTER/LOGIN)
            boolean authenticated = false;
            while (!authenticated) {
                System.out.print("Enter command: ");
                String command = input.nextLine();
                writer.println(command);
                
                String response = reader.readLine();
                if (response == null) break;
                System.out.println(response);
                
                if (response.equals("LOGIN OK")) {
                    System.out.println("You are now logged in.");
                    authenticated = true;
                }
            }
            
            // Task management loop (ADD/VIEW/DELETE/LOGOUT)
            if (authenticated) {
                boolean sessionActive = true;
                while (sessionActive) {
                    System.out.print("\nTask command (ADD/VIEW/DELETE/LOGOUT): ");
                    String taskCommand = input.nextLine();
                    writer.println(taskCommand);
                    
                    // Read the complete multi-line response
                    String line;
                    boolean inTaskList = false;
                    int separatorDashCount = 0; // Count only the long separator dashes
                    
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        
                        // Detect task list header
                        if (line.contains("--- YOUR TASKS ---")) {
                            inTaskList = true;
                        } 
                        // Count separator lines (long dashes, NOT the header)
                        else if (inTaskList && line.startsWith("---") && line.length() > 50) {
                            separatorDashCount++;
                            // Task list has 2 separator lines (after header, at bottom)
                            if (separatorDashCount >= 2) {
                                break; // Complete task list received
                            }
                        }
                        
                        // Single-line responses (when not in a task list)
                        if (!inTaskList && 
                            (line.equals("Logged out.") || 
                             line.startsWith("Usage:") || 
                             line.startsWith("Unknown command") ||
                             line.startsWith("Available") ||
                             line.startsWith("Error"))) {
                            break;
                        }
                    }
                    
                    // Exit task loop if user logged out
                    if (taskCommand.toUpperCase().startsWith("LOGOUT")) {
                        sessionActive = false;
                    }
                }
            }
            
        } catch (UnknownHostException e) {
            System.out.println("Server not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        }
    }
}