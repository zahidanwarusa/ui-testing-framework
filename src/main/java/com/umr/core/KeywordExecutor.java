package com.umr.core;

import com.umr.core.annotation.Keyword;
import com.umr.utils.LogUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Executes keywords by mapping keyword names to method implementations.
 * Uses reflection to find and invoke methods with the @Keyword annotation.
 */
public class KeywordExecutor {
    private final Map<String, Method> keywordMap = new HashMap<>();
    private final Object keywordInstance;

    /**
     * Creates a new KeywordExecutor for the specified keyword class instance.
     *
     * @param keywordInstance The instance of the class containing keyword methods
     */
    public KeywordExecutor(Object keywordInstance) {
        this.keywordInstance = keywordInstance;
        loadKeywords();
    }

    /**
     * Loads all keyword methods from the keyword instance using reflection.
     * Methods must be annotated with @Keyword to be recognized.
     */
    private void loadKeywords() {
        Method[] methods = keywordInstance.getClass().getMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(Keyword.class)) {
                Keyword annotation = method.getAnnotation(Keyword.class);
                String keywordName = annotation.value().toUpperCase();

                keywordMap.put(keywordName, method);
                LogUtil.debug("Loaded keyword: " + keywordName);
            }
        }

        LogUtil.info("Loaded " + keywordMap.size() + " keywords from " +
                keywordInstance.getClass().getSimpleName());
    }

    /**
     * Executes a keyword with the given context.
     *
     * @param keyword The keyword to execute
     * @param context The test context
     * @return True if the keyword execution was successful, false otherwise
     */
    public boolean executeKeyword(String keyword, TestContext context) {
        String keywordUpper = keyword.toUpperCase();
        LogUtil.info("Executing keyword: " + keywordUpper);

        if (!keywordMap.containsKey(keywordUpper)) {
            LogUtil.error("Unknown keyword: " + keywordUpper);
            context.setTestFailed("Unknown keyword: " + keywordUpper);
            return false;
        }

        try {
            Method method = keywordMap.get(keywordUpper);
            boolean isMandatory = method.getAnnotation(Keyword.class).mandatory();

            Object result = method.invoke(keywordInstance, context);

            if (result instanceof Boolean) {
                boolean success = (Boolean) result;

                if (!success && isMandatory) {
                    LogUtil.error("Mandatory keyword failed: " + keywordUpper);
                    context.setTestFailed("Mandatory keyword failed: " + keywordUpper);
                } else if (!success) {
                    LogUtil.warn("Non-mandatory keyword failed: " + keywordUpper);
                }

                return success;
            } else {
                LogUtil.warn("Keyword method did not return a boolean: " + keywordUpper);
                return true; // Assume success if the method doesn't return a boolean
            }
        } catch (Exception e) {
            LogUtil.error("Error executing keyword: " + keywordUpper, e);
            context.setTestFailed("Error executing keyword: " + keywordUpper + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if the executor has a keyword with the given name.
     *
     * @param keyword The keyword name to check
     * @return True if the keyword exists, false otherwise
     */
    public boolean hasKeyword(String keyword) {
        return keywordMap.containsKey(keyword.toUpperCase());
    }

    /**
     * Gets the number of loaded keywords.
     *
     * @return The number of loaded keywords
     */
    public int getKeywordCount() {
        return keywordMap.size();
    }
}