import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CSC201 DSA Task 1 -- XML Checker
 * Solution One
 *
 * Student name : Meekhum Hang Nembang
 * Student ID   : 1187755
 *
 * Data-structure combination used by this solution:
 * Uses a HashMap<Integer, List<Integer>> to group lines by indentation level
 */
public class XmlCheckerSolutionOne {

    /** Error codes required by the task specification. Do not rename. */
    enum ErrorCode {
        MISMATCHED_TAG,
        UNCLOSED_TAG,
        MULTIPLE_ROOTS,
        UNQUOTED_ATTRIBUTE,
        DUPLICATE_ATTRIBUTE
    }

    /** Result of a check: either well-formed, or the FIRST violation found. */
    static final class Result {
        final boolean wellFormed;
        final int line;           // 1-based line number of the violation
        final ErrorCode code;     // null when wellFormed

        private Result(boolean wellFormed, int line, ErrorCode code) {
            this.wellFormed = wellFormed;
            this.line = line;
            this.code = code;
        }
        static Result ok()                          { return new Result(true, -1, null); }
        static Result fail(int line, ErrorCode c)   { return new Result(false, line, c); }
    }

    public static void main(String[] args) throws IOException {
        Path dir = Path.of("testFiles");
        if (!Files.isDirectory(dir)) {
            System.err.println("Folder not found: testFiles");
            System.exit(2);
        }

        // Collect all .xml files in testFiles, in alphabetical order of file name.
        List<Path> xmlFiles;
        try (Stream<Path> s = Files.list(dir)) {
            xmlFiles = s.filter(f -> f.getFileName().toString().toLowerCase().endsWith(".xml"))
                        .sorted(Comparator.comparing(f -> f.getFileName().toString()))
                        .collect(Collectors.toList());
        }

        for (Path file : xmlFiles) {
            List<String> lines = Files.readAllLines(file);
            Result result = check(lines);
            printResult(file.getFileName().toString(), result);
        }
    }

    /** Prints the verdict in the exact format required by the task. Do not change. */
    static void printResult(String fileName, Result r) {
        if (r.wellFormed) {
            System.out.println("File " + fileName + " is TRUE");
        } else {
            System.out.println("File " + fileName + " is FALSE");
            System.out.println("Line " + r.line + ": " + r.code);
        }
    }

    /**
     * TODO: implement your first checking solution here.
     *
     * @param lines the file content, one entry per line (line i of the file is
     *              lines.get(i - 1), so line numbers for error reporting are
     *              index + 1)
     * @return Result.ok() if well-formed, otherwise Result.fail(line, code)
     *         for the FIRST violation in reading order.
     */
    private static boolean syntexCheckOpening (String item) {                                 //Checks if the line is an opening tag or not
            return (item.contains("<") && !item.contains("/")  && item.contains(">") );
        }

    private static  boolean syntexCheckClosing (String item) {                                 //Checks if the line is a closing tag or not
            return (item.contains("<") && item.contains("/") && item.contains(">"));
        }
    
    private static int spaceCount(String item) {
        int count = 0;
        for (int i = 0; i < item.length(); i++) {
            if (item.charAt(i) == ' ') {
                count++;
            }
            else {
                break;
            }
        }
        return count;
    }

    private static int equalsToCount(String item) {
        int count = 0;
        for (int i = 0; i < item.length(); i++) {
            if (item.charAt(i) == '=') {
                count++;
            }
        }
        return count;
    }

    private static int singleQuoteToCount(String item) {
        int count = 0;
        
        for (int i = 0; i < item.length(); i++) {
            if (item.charAt(i) == '\'') {
                count++;
                int sub = 0;
                for (int j = i + 1; j < item.length(); j++) {
                    if (item.charAt(j) == '"') {
                        sub++;
                    }
                    else if (item.charAt(j) == '\'') {
                        count = count + 1 - sub;
                        i = j;
                        break;
                    }


                }
            }
        }
        return count;
    }

    private static int doubleQuoteToCount(String item) {
        int count = 0;
        
        for (int i = 0; i < item.length(); i++) {
            if (item.charAt(i) == '"') {
                count++;
                int sub = 0;
                for (int j = i + 1; j < item.length(); j++) {
                    if (item.charAt(j) == '\'') {
                        sub++;
                    }
                    else if (item.charAt(j) == '"') {
                        count = count + 1 - sub;
                        i = j;
                        break;
                    }


                }
            }
        }
        return count;
    }

    private static String syntexTrim(String item){                                      //Trims the line to get the tag name
        return item.replace("<", "")
        .replace(">", "")
        .replace("/", "")
        .trim();        
    }



    static Result check(List<String> lines) {
        // ---------------- YOUR CODE STARTS HERE ----------------
        int count = lines.size();
        int lastLine = count - 1;
        String lastLineTag = syntexTrim(lines.get(lastLine));

        // Create a map to store the line numbers for each level of indentation
        Map<Integer, List<Integer>> data = new HashMap<>();
        for (int i = 0; i < count; i++) {
            int spaceInfront = spaceCount(lines.get(i));

            if (!data.containsKey(spaceInfront)) {
                data.put(spaceInfront, new ArrayList<>());
            }
            data.get(spaceInfront).add(i);
        }

        // Check for multiple roots and error for the ROOT tag
        if (data.get(0).size() > 3) {
            return Result.fail(data.get(0).get(3) + 1, ErrorCode.MULTIPLE_ROOTS);
        }
        if (data.get(0).size() < 3 && !syntexCheckClosing(lines.get(1))) {
            return Result.fail(data.get(0).get(0) + 1, ErrorCode.UNCLOSED_TAG);
        }
        if (syntexCheckOpening(lines.get(1)) && !lines.get(1).contains(lastLineTag)) {
            return Result.fail(data.get(0).get(0) + 1, ErrorCode.MISMATCHED_TAG);
        }

        // Check for unclosed tags
        int nextSet = 2;
        for (int i = 1; i < data.size(); i++) {
            
            for (int j = 0; j < data.get(nextSet).size(); j++) {
                
                if (syntexCheckOpening(lines.get(data.get(nextSet).get(j)))) {
                    if (j +1 >= data.get(nextSet).size()) {
                        return Result.fail(data.get(nextSet).get(j) + 1, ErrorCode.UNCLOSED_TAG);
                    }
                    if (syntexCheckOpening(lines.get(data.get(nextSet).get(j+1)))) {
                        return Result.fail(data.get(nextSet).get(j+1) + 1, ErrorCode.UNCLOSED_TAG);
                    }
                    
                }
            }
            nextSet += 2;
        }

        //Check for mismatched tags
        int nextSetMt = 2;
        for (int i = 1; i < data.size(); i++) {
            for (int j = 0; j < data.get(nextSetMt).size(); j++) {
                
                if (syntexCheckOpening(lines.get(data.get(nextSetMt).get(j)))) {
                    String nextLineTag = syntexTrim(lines.get(data.get(nextSetMt).get(j+1)));
                    if (!lines.get(data.get(nextSetMt).get(j)).contains(nextLineTag)){
                        return Result.fail(data.get(nextSetMt).get(j+1) + 1, ErrorCode.MISMATCHED_TAG);
                    }
                    
                }
            }
        }

        //Check for unquoted attributes
        for (int i = 0; i < count; i++) {
            // System.out.println("first line " + equalsToCount(lines.get(i)));
            if (lines.get(i).contains("=")){
                int currentNumOfEquals = equalsToCount(lines.get(i));
                int singleQuoteCount = singleQuoteToCount(lines.get(i));
                int doubleQuoteCount = doubleQuoteToCount(lines.get(i));
                int totalQuoteCount = singleQuoteCount + doubleQuoteCount;
                if (2*currentNumOfEquals != totalQuoteCount) {
                    System.out.println(""+singleQuoteCount+doubleQuoteCount+totalQuoteCount);
                    return Result.fail(i + 1, ErrorCode.UNQUOTED_ATTRIBUTE);
                }
            }
            

        }

        //Check for duplicate attributes
        for (int i = 0; i < count; i++) {
            if (lines.get(i).contains("=")){
                String[] attributes = lines.get(i).split(" ");
                List<String> attributeNames = new ArrayList<>();
                for (String attribute : attributes) {
                    if (attribute.contains("=")) {
                        String attributeName = attribute.split("=")[0];
                        if (attributeNames.contains(attributeName)) {
                            return Result.fail(i + 1, ErrorCode.DUPLICATE_ATTRIBUTE);
                        }
                        attributeNames.add(attributeName);
                    }
                }
            }
        }

        return Result.ok();
        // ----------------- YOUR CODE ENDS HERE -----------------
    }

}

