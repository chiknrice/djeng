package org.chiknrice.djeng;

import java.util.Stack;

/**
 * @author <a href="mailto:chiknrice@gmail.com">Ian Bondoc</a>
 */
public class CodecContext {

    private static final ThreadLocal<Boolean> DEBUG_ENABLED = new ThreadLocal();
    private static final ThreadLocal<Stack<String>> INDEX_STACK = new ThreadLocal();

    private CodecContext() {}

    public static void clear() {
        DEBUG_ENABLED.remove();
        INDEX_STACK.remove();
    }

    public static boolean isDebugEnabled() {
        Boolean debugEnabled = DEBUG_ENABLED.get();
        if (debugEnabled == null) {
            throw new IllegalStateException(DEBUG_ENABLED + " not defined");
        }
        return debugEnabled;
    }

    public static void setDebugEnabled(boolean debugEnabled) {
        DEBUG_ENABLED.set(Boolean.valueOf(debugEnabled));
    }

    public static void dumpLogs() {
        // TODO do only log the details or save them in a map for later?
    }

    public static void pushIndex(String index) {
        Stack<String> indexStack = INDEX_STACK.get();
        if (indexStack == null) {
            indexStack = new Stack<>();
            INDEX_STACK.set(indexStack);
        }
        indexStack.push(index);
    }

    public static void popIndex() {
        Stack<String> indexStack = INDEX_STACK.get();
        if (indexStack == null) {
            indexStack = new Stack<>();
            throw new IllegalStateException("No existing " + INDEX_STACK);
        }
        indexStack.pop();
    }

    public static String getCurrentIndexPath() {
        Stack<String> indexStack = INDEX_STACK.get();
        StringBuilder indexPath = new StringBuilder();
        for (String index : indexStack) {
            if (indexPath.length() > 0) {
                indexPath.append(".");
            }
            indexPath.append(index);
        }
        return indexPath.toString();
    }

}
