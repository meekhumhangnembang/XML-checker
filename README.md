# CSC201 DSA Task 1 — XML Checker

Read the full task specification on the course page before starting.

## What is provided

| File | Purpose |
|---|---|
| `XmlCheckerSolutionOne.java` | Skeleton for your first solution. Harness code (argument handling, file reading, output format, error codes) is complete; implement `check()`. |
| `XmlCheckerSolutionTwo.java` | Skeleton for your second solution, using a **different** combination of data structures. |
| `testFiles/testFile1.xml` | Well-formed: attributes, both quote styles, quotes nested in values, self-closing tag. Expected: `TRUE` |
| `testFiles/testFile2.xml` | Case-sensitive tag mismatch. Expected: `FALSE`, `Line 4: MISMATCHED_TAG` |
| `testFiles/testFile3.xml` | Unquoted attribute value. Expected: `FALSE`, `Line 4: UNQUOTED_ATTRIBUTE` |
| `testFiles/testFile4.xml` | Second root element. Expected: `FALSE`, `Line 5: MULTIPLE_ROOTS` |
| `testFiles/testFile5.xml` | `>` inside a quoted value (legal), but `<rule>` is never closed. Expected: `FALSE`, `Line 3: UNCLOSED_TAG` |

**These files do NOT cover every rule**. During grading, the contents of `testFiles` are replaced with a separate staff test suite. You are encouraged to add test files of your own to `testFiles`; they are not graded.

## Build and run

```bash
javac XmlCheckerSolutionOne.java XmlCheckerSolutionTwo.java
java XmlCheckerSolutionOne
java XmlCheckerSolutionTwo
```

Alternatively, you can use "Run" in the IDE.

Each solution takes **no arguments**: it reads all `.xml` files in the `testFiles` folder and prints the result for each file, in alphabetical order of file name. Add your own test files to `testFiles` while developing; during grading the folder's contents are replaced with the staff test suite.

## Rules of the harness

- Do **not** change the output format produced by `printResult`, the `ErrorCode` names, the `testFiles` folder scanning, or the no-argument command-line interface. Automated grading depends on them.
- You **may** add fields, methods, helper classes, and change how the file content is passed around internally (e.g. switch from `readAllLines` to a streaming reader if your design needs it), as long as the class name, entry point, and output format are unchanged.
- Each solution file must remain independently compilable and runnable (no shared classes between the two solutions).
- State your data-structure combination in the header comment of each file. This is assessed.

