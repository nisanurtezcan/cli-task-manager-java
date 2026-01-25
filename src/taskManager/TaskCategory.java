
package taskManager;

import java.util.*;

/**
 * Manages predefined task categories for the task management system.
 * Provides validation and category listing functionality.
 */
public class TaskCategory {
    
    // Predefined categories - can be easily modified or loaded from config
    private static final Set<String> VALID_CATEGORIES = new LinkedHashSet<>(Arrays.asList(
        "WORK", "PERSONAL", "SHOPPING", "HEALTH", "EDUCATION", "FINANCE", "TRAVEL", "HOME"
    ));
    
    /**
     * Validates if a category name is valid (case-insensitive)
     */
    public static boolean isValidCategory(String category) {
        return category != null && VALID_CATEGORIES.contains(category.toUpperCase());
    }
    
    /**
     * Returns the properly formatted category name
     */
    public static String formatCategory(String category) {
        return category != null ? category.toUpperCase() : null;
    }
    
    /**
     * Returns all available categories as a formatted string
     */
    public static String getAvailableCategories() {
        return "Available Categories: " + String.join(", ", VALID_CATEGORIES);
    }

    
    /**
     * Returns all valid categories as a list
     */
    public static List<String> getCategoryList() {
        return new ArrayList<>(VALID_CATEGORIES);
    }
}