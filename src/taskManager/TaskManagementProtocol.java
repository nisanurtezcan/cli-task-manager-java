package taskManager;

import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaskManagementProtocol {

    private static final String DATA_DIR = "data/";

    static {
        new File(DATA_DIR).mkdirs();
    }

    static class ClientSession {
        boolean loggedIn = false;
        String username = null;
    }

    public static void handleClient(Socket clientSock, Logger logger) {
        ClientSession session = new ClientSession();

        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(clientSock.getInputStream()));
            PrintWriter writer = new PrintWriter(
                clientSock.getOutputStream(), true)
        ) {
            writer.println("HELLO! Welcome to Task Management Server.");
            writer.println("REGISTER <username> <password> OR LOGIN <username> <password>");

            String line;
            while ((line = reader.readLine()) != null) {
                String response;

                if (session.loggedIn) {
                    response = processTaskCommand(line, session);
                } else {
                    response = processCommand(line, session);
                }

                writer.println(response);
            }

        } catch (IOException e) {
            logger.log(Level.WARNING, "Client communication error", e);
        }
    }

    //Auth Commands
    private static String processCommand(String msg, ClientSession session) {
        String[] parts = msg.trim().split("\\s+");
        if (parts.length == 0) return "Invalid command";

        switch (parts[0].toUpperCase()) {
            case "REGISTER":
                if (parts.length != 3)
                    return "Usage: REGISTER <username> <password>";
                return handleRegister(parts[1], parts[2]);

            case "LOGIN":
                if (parts.length != 3)
                    return "Usage: LOGIN <username> <password>";
                if (handleLogin(parts[1], parts[2], session))
                    return "LOGIN OK";
                return "LOGIN FAILED";

            default:
                return "Unknown command. Use REGISTER or LOGIN.";
        }
    }

    private static String handleRegister(String username, String password) {
        File userFile = new File(DATA_DIR + username + ".txt");
        if (userFile.exists()) return "USER EXISTS";

        try (PrintWriter pw = new PrintWriter(new FileWriter(userFile))) {
            pw.println(password);
            pw.println("nextId:1");
            return "REGISTER OK";
        } catch (IOException e) {
            return "REGISTER FAILED";
        }
    }

    private static boolean handleLogin(String username, String password, ClientSession session) {
        File userFile = new File(DATA_DIR + username + ".txt");
        if (!userFile.exists()) return false;

        try (BufferedReader br = new BufferedReader(new FileReader(userFile))) {
            String stored = br.readLine();
            if (stored != null && stored.equals(password)) {
                session.loggedIn = true;
                session.username = username;
                return true;
            }
        } catch (IOException ignored) {}
        return false;
    }

    //All Task Commands
    private static String processTaskCommand(String msg, ClientSession session) {
        String[] parts = msg.trim().split("\\s+", 4);

        switch (parts[0].toUpperCase()) {
        case "ADD":
            if (parts.length < 4)
                return "Usage: ADD <Category> <Date> <Description>. Available Categories: WORK, PERSONAL, SHOPPING, HEALTH, EDUCATION, FINANCE, TRAVEL, HOME";

            //Category validation before adding a task
            if (!TaskCategory.isValidCategory(parts[1]))
                return "Error: Invalid category. Use CATEGORIES.";

            //Date validation before adding a task
            try {
                LocalDate.parse(parts[2]);
            } catch (Exception e) {
                return "Error: Invalid date format. Use YYYY-MM-DD. (e.g 2026-01-01)";
            }

            return TaskDataManager.addTask(
                    session.username,
                    TaskCategory.formatCategory(parts[1]),
                    parts[2],
                    parts[3]);


            case "VIEW":
                return checkUserReminders(session.username)
                        + TaskDataManager.getFormattedTasks(session.username);

            case "DELETE":
                if (parts.length < 2)
                    return "Usage: DELETE <TaskID>";
                return TaskDataManager.deleteTask(session.username, parts[1]);

            case "CATEGORIES":
                return TaskCategory.getAvailableCategories();

            case "LOGOUT":
                session.loggedIn = false;
                session.username = null;
                return "Logged out.";

            default:
                return "Unknown command.";
        }
    }

    //Reminders
    private static String checkUserReminders(String username) {
        StringBuilder sb = new StringBuilder();
        LocalDate today = LocalDate.now();

        File file = new File(DATA_DIR + username + ".txt");
        if (!file.exists()) return "";

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // password
            String line = br.readLine();
            if (line != null && line.startsWith("nextId:"))
                line = br.readLine();

            boolean hasAny = false;

            while (line != null) {
                String[] p = line.split("\\|");
                if (p.length >= 4) {
                    LocalDate date = LocalDate.parse(p[2]);
                    if (!hasAny &&
                        (date.isBefore(today) ||
                         date.isEqual(today) ||
                         date.isBefore(today.plusDays(4)))) {
                        sb.append("\nREMINDERS:\n");
                        hasAny = true;
                    }

                    if (date.isBefore(today))
                        sb.append("- OVERDUE: ").append(p[3]).append("\n");
                    else if (date.isEqual(today))
                        sb.append("- DUE TODAY: ").append(p[3]).append("\n");
                    else if (date.isBefore(today.plusDays(4)))
                        sb.append("- DUE SOON: ").append(p[3]).append("\n");
                }
                line = br.readLine();
            }
        } catch (Exception ignored) {}

        return sb.toString();
    }
}
