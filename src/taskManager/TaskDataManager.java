package taskManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages task data storage and retrieval using file-based storage.
 * Each user has their own file in the data/ directory.
 * File format:
 * Line 1: password
 * Line 2: nextId:N (tracks next available task ID)
 * Line 3+: taskId|category|date|description
 */
public class TaskDataManager {

    private static final String DATA_DIR = "data/";
    
    // Create data directory if it doesn't exist
    static {
        new File(DATA_DIR).mkdirs();
    }
    
    //	Retrieves and formats all tasks for a user
    public static synchronized String getFormattedTasks(String username) {
        File file = new File(DATA_DIR + username + ".txt");
        if (!file.exists()) return "No account found.";

        StringBuilder sb = new StringBuilder();
        sb.append("\n--- YOUR TASKS ---\n");
        sb.append(String.format("%-10s | %-12s | %-12s | %s\n", "ID", "CATEGORY", "DATE", "DESCRIPTION"));
        sb.append("----------------------------------------------------------------------------------\n");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // Skip password line
            
            String secondLine = br.readLine();
            boolean hasTasks = false;
            
            // Handle backward compatibility: check if second line is nextId or a task
            if (secondLine != null && !secondLine.startsWith("nextId:")) {
                // Old format - second line is a task
                String[] parts = secondLine.split("\\|");
                if (parts.length >= 4) {
                    sb.append(String.format("%-10s | %-12s | %-12s | %s\n", 
                        parts[0], parts[1], parts[2], parts[3]));
                    hasTasks = true;
                }
            }

            // Read remaining task lines
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    sb.append(String.format("%-10s | %-12s | %-12s | %s\n", 
                        parts[0], parts[1], parts[2], parts[3]));
                    hasTasks = true;
                }
            }
            
            if (!hasTasks) sb.append("(You have no tasks yet)\n");
            
        } catch (IOException e) {
            return "Error reading task file.";
        }
        sb.append("----------------------------------------------------------------------------------\n");
        return sb.toString();
    }
    
    // Adds a new task to the user's file
    // username - User adding the task
    // category - Task category (e.g., Work, Personal)
    // date Due - date in YYYY-MM-DD format
    // description - Task description
    public static synchronized String addTask(String username, String category, String date, String description) {
        File file = new File(DATA_DIR + username + ".txt");
        
        if (!file.exists()) {
            return "Error: User file not found.";
        }
        
        int nextId = 1;
        List<String> fileLines = new ArrayList<>();
        
        // Read existing file content
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String password = br.readLine();
            if (password == null) return "Error: Invalid user file.";
            fileLines.add(password);
            
            String secondLine = br.readLine();
            if (secondLine != null && secondLine.startsWith("nextId:")) {
                // New format with nextId tracking
                nextId = Integer.parseInt(secondLine.split(":")[1]);
            } else {
                // Old format - calculate next ID from existing tasks
                if (secondLine != null) {
                    fileLines.add(secondLine);
                    String[] parts = secondLine.split("\\|");
                    if (parts.length > 0) {
                        try {
                            int taskId = Integer.parseInt(parts[0]);
                            nextId = Math.max(nextId, taskId + 1);
                        } catch (NumberFormatException e) {
                            // Invalid ID, keep default nextId
                        }
                    }
                }
            }
            
            // Read all remaining tasks
            String line;
            while ((line = br.readLine()) != null) {
                fileLines.add(line);
                
                // If old format, continue finding max ID
                if (secondLine == null || !secondLine.startsWith("nextId:")) {
                    String[] parts = line.split("\\|");
                    if (parts.length > 0) {
                        try {
                            int taskId = Integer.parseInt(parts[0]);
                            nextId = Math.max(nextId, taskId + 1);
                        } catch (NumberFormatException e) {
                            // Invalid ID, skip
                        }
                    }
                }
            }
        } catch (IOException e) {
            return "Error reading task file.";
        }
        
        // Write everything back with new task and updated nextId
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println(fileLines.get(0)); // Write password
            pw.println("nextId:" + (nextId + 1)); // Write updated nextId
            
            // Write all existing tasks (skip old nextId line if present)
            for (int i = 1; i < fileLines.size(); i++) {
                if (!fileLines.get(i).startsWith("nextId:")) {
                    pw.println(fileLines.get(i));
                }
            }
            
            // Add new task with assigned ID
            pw.println(nextId + "|" + category + "|" + date + "|" + description);
            
            // Force flush to ensure data is written before reading
            pw.flush();
        } catch (IOException e) {
            return "Error saving task.";
        }
        
        // Now safely read the updated file
        return ">> SUCCESS: Task Added (ID: " + nextId + ")\n" + getFormattedTasks(username);
    }
    
    // Deletes a task by ID by rewriting the file without that task
    public static synchronized String deleteTask(String username, String taskId) {
        File inputFile = new File(DATA_DIR + username + ".txt");
        File tempFile = new File(DATA_DIR + username + "_temp.txt");
        boolean deleted = false;

        // Read original file and write to temp file, skipping the deleted task
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {

            // Preserve password (line 1)
            String passwordLine = reader.readLine();
            if (passwordLine != null) writer.println(passwordLine);

            // Handle line 2 (nextId or task for backward compatibility)
            String nextIdLine = reader.readLine();
            if (nextIdLine != null) {
                if (nextIdLine.startsWith("nextId:")) {
                    // New format - preserve nextId line
                    writer.println(nextIdLine);
                } else {
                    // Old format - check if this task should be deleted
                    String[] parts = nextIdLine.split("\\|");
                    if (parts.length > 0 && parts[0].equals(taskId)) {
                        deleted = true; // Skip this line
                    } else {
                        writer.println(nextIdLine); // Keep this task
                    }
                }
            }

            // Process remaining tasks
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length > 0 && parts[0].equals(taskId)) {
                    deleted = true; // Skip this task
                } else {
                    writer.println(line); // Keep this task
                }
            }
        } catch (IOException e) {
            return "Error deleting task.";
        }

        // Replace original file with temp file
        if (!inputFile.delete()) {
            return "Error: Could not delete original file.";
        }
        if (!tempFile.renameTo(inputFile)) {
            return "Error: Could not rename temp file.";
        }

        // Return appropriate message
        if (deleted) {
            return ">> SUCCESS: Task Deleted.\n" + getFormattedTasks(username);
        } else {
            return ">> ERROR: Task ID not found.\n" + getFormattedTasks(username);
        }
    }
}