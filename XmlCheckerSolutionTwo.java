import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CSC201 DSA Task 1 -- XML Checker
 * Solution Two
 *
 * Student name : Meekhum Hang Nembang
 * Student ID   : 1187755
 *
 * Data-structure combination used by this solution:
 *   Stack (ArrayDeque) for open-tag tracking, HashSet for duplicate-attribute detection.
 */
public class XmlCheckerSolutionTwo {

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

    /** An opening tag waiting for its matching closing tag. */
    static final class OpenTag {
        final String name;
        final int line;

        OpenTag(String name, int line) {
            this.name = name;
            this.line = line;
        }
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
     * TODO: implement your second checking solution here.
     *
     * @param lines the file content, one entry per line (line i of the file is
     *              lines.get(i - 1), so line numbers for error reporting are
     *              index + 1)
     * @return Result.ok() if well-formed, otherwise Result.fail(line, code)
     *         for the FIRST violation in reading order.
     */
    static Result check(List<String> lines) {
        // ---------------- YOUR CODE STARTS HERE ----------------
        Deque<OpenTag> openTags = new ArrayDeque<>();
        boolean hasRoot = false;

        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            String line = lines.get(lineIndex);
            int lineNumber = lineIndex + 1;
            int pos = 0;

            while (pos < line.length()) {
                int tagStart = line.indexOf('<', pos);
                if (tagStart < 0) {
                    break;
                }

                if (tagStart + 1 < line.length() && line.charAt(tagStart + 1) == '?') {
                    int declEnd = line.indexOf("?>", tagStart + 2);
                    if (declEnd < 0) {
                        break;
                    }
                    pos = declEnd + 2;
                    continue;
                }

                int tagEnd = findTagEnd(line, tagStart);
                if (tagEnd < 0) {
                    break;
                }

                String tagBody = line.substring(tagStart + 1, tagEnd).trim();
                if (tagBody.isEmpty()) {
                    pos = tagEnd + 1;
                    continue;
                }

                if (tagBody.charAt(0) == '/') {
                    String closingName = tagBody.substring(1).trim();
                    if (openTags.isEmpty()) {
                        return Result.fail(lineNumber, ErrorCode.MISMATCHED_TAG);
                    }
                    OpenTag opened = openTags.pop();
                    if (!opened.name.equals(closingName)) {
                        return Result.fail(lineNumber, ErrorCode.MISMATCHED_TAG);
                    }
                } else {
                    boolean selfClosing = isSelfClosing(tagBody);
                    String tagContent = selfClosing ? tagBody.substring(0, tagBody.length() - 1).trim() : tagBody;

                    int nameEnd = 0;
                    while (nameEnd < tagContent.length()
                            && !Character.isWhitespace(tagContent.charAt(nameEnd))) {
                        nameEnd++;
                    }
                    String tagName = tagContent.substring(0, nameEnd);
                    String attributeSection = tagContent.substring(nameEnd);

                    Result attributeResult = checkAttributes(attributeSection, lineNumber);
                    if (attributeResult != null) {
                        return attributeResult;
                    }

                    if (openTags.isEmpty()) {
                        if (hasRoot) {
                            return Result.fail(lineNumber, ErrorCode.MULTIPLE_ROOTS);
                        }
                        hasRoot = true;
                    }

                    if (!selfClosing) {
                        openTags.push(new OpenTag(tagName, lineNumber));
                    }
                }

                pos = tagEnd + 1;
            }
        }

        if (!openTags.isEmpty()) {
            return Result.fail(openTags.peek().line, ErrorCode.UNCLOSED_TAG);
        }

        return Result.ok();
        // ----------------- YOUR CODE ENDS HERE -----------------
    }

    /** Finds the index of the closing '>' for a tag, ignoring '>' inside quoted values. */
    private static int findTagEnd(String line, int tagStart) {
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = tagStart + 1; i < line.length(); i++) {
            char c = line.charAt(i);
            if (!inDoubleQuote && c == '\'') {
                inSingleQuote = !inSingleQuote;
            } else if (!inSingleQuote && c == '"') {
                inDoubleQuote = !inDoubleQuote;
            } else if (!inSingleQuote && !inDoubleQuote && c == '>') {
                return i;
            }
        }
        return -1;
    }

    private static boolean isSelfClosing(String tagBody) {
        int i = tagBody.length() - 1;
        while (i >= 0 && Character.isWhitespace(tagBody.charAt(i))) {
            i--;
        }
        return i >= 0 && tagBody.charAt(i) == '/';
    }

    /** Validates attributes left-to-right; returns the first violation found. */
    private static Result checkAttributes(String attributeSection, int lineNumber) {
        Set<String> seenNames = new HashSet<>();
        int i = 0;

        while (i < attributeSection.length()) {
            while (i < attributeSection.length() && Character.isWhitespace(attributeSection.charAt(i))) {
                i++;
            }
            if (i >= attributeSection.length()) {
                break;
            }

            int nameStart = i;
            while (i < attributeSection.length()
                    && attributeSection.charAt(i) != '='
                    && !Character.isWhitespace(attributeSection.charAt(i))) {
                i++;
            }
            if (i >= attributeSection.length() || attributeSection.charAt(i) != '=') {
                break;
            }
            String attributeName = attributeSection.substring(nameStart, i).trim();
            i++;

            while (i < attributeSection.length() && Character.isWhitespace(attributeSection.charAt(i))) {
                i++;
            }
            if (i >= attributeSection.length()) {
                return Result.fail(lineNumber, ErrorCode.UNQUOTED_ATTRIBUTE);
            }

            char quote = attributeSection.charAt(i);
            if (quote != '\'' && quote != '"') {
                return Result.fail(lineNumber, ErrorCode.UNQUOTED_ATTRIBUTE);
            }
            i++;

            while (i < attributeSection.length() && attributeSection.charAt(i) != quote) {
                i++;
            }
            if (i >= attributeSection.length()) {
                return Result.fail(lineNumber, ErrorCode.UNQUOTED_ATTRIBUTE);
            }
            i++;

            if (seenNames.contains(attributeName)) {
                return Result.fail(lineNumber, ErrorCode.DUPLICATE_ATTRIBUTE);
            }
            seenNames.add(attributeName);
        }

        return null;
    }

}
