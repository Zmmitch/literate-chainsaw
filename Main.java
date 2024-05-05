
public class Main {
    private static final List<String> lines = new ArrayList<>();
    public static void main(String[] args) {
//        filePath = "/Users/zacharymitchell/Desktop/Spring 2024/Programming Concepts/Project1/ex.txt";
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please type your file path: ");
        String filePath = scanner.nextLine();
        System.out.println("Please enter the file input type: Python or Java");
        String option =  scanner.nextLine();
        option = option.toLowerCase(Locale.ROOT);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
               // System.out.println(line);
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return; // Terminate the program if file reading fails
        }
        if (option.equals("python")) {
            Python_Analyzer analyzer = new Python_Analyzer();  // Initialize Java_Analyzer
            for (String line : lines) {
                analyzer.tokenizeJavaLine(line);  // Tokenize each line of Java code
                // Print tokens for the current line
                for (Python_Analyzer.Token token : analyzer.getTokens()) {
                    System.out.println(token.type + "\t" + token.value);
                }
                analyzer.clearTokens();  // Clear tokens after processing each line
            }
        }
        else if (option.equals("java")) {
            Java_Analyzer analyzer = new Java_Analyzer();  // Initialize Java_Analyzer
            for (String line : lines) {
                analyzer.tokenizeJavaLine(line);  // Tokenize each line of Java code
                // Print tokens for the current line
                for (Java_Analyzer.Token token : analyzer.getTokens()) {
                    System.out.println(token.type + "\t" + token.value);
                }
                analyzer.clearTokens();  // Clear tokens after processing each line
            }
        }
        else {
            System.out.println("Error please run the program again");
        }
    }
}
 class Java_Analyzer {
    private String expression;
    private int currentPos;
    private List<Token> tokens;
    private final List<String> javaKeywords = Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
            "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
            "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "try", "void", "volatile", "while"
    );
     public Java_Analyzer() {
         this.tokens = new ArrayList<>();
     }
     // Method to process each line of Java code
     public void tokenizeJavaLine(String line) {
         this.expression = line;
         this.currentPos = 0;
         this.tokens.clear();  // Clear previous tokens
         lex();  // Start the lexical analysis process
     }
    private void lex() {
        skipWhiteSpace();  // Skip initial whitespace
        while (currentPos < expression.length()) {
            char currentChar = expression.charAt(currentPos);
            // First, check for the start of comments to handle them before other tokens
            if (currentChar == '/') {
                if (peekChar() == '/') {
                    handleSingleLineComment();  // Handle single-line comments
                    continue;  // Skip to the next iteration after handling the comment
                } else if (peekChar() == '*') {
                    handleMultiLineComment();  // Handle multi-line comments
                    continue;  // Skip to the next iteration after handling the comment
                }
            }
            // Use lookup to identify if the current character is a known operator or symbol
            String tokenType = lookup(currentChar);
            if (!tokenType.equals("UNKNOWN")) {  // Known token type
                tokens.add(new Token(tokenType, Character.toString(currentChar)));
                currentPos++;  // Move to the next character
            } else {
                // Handle more complex structures like identifiers, numbers, strings
                if (Character.isDigit(currentChar) || (currentChar == '.' && Character.isDigit(peekChar()))) {
                    handleNumericLiteral();  // Handle numeric literals including floats
                } else if (Character.isLetter(currentChar) || currentChar == '_') {
                    handleIdentifier();  // Handle identifiers and potentially keywords
                } else if (currentChar == '"') {
                    handleStringLiteral();  // Handle string literals
                } else if (currentChar == '\'') {
                    handleCharacterLiteral();  // Handle character literals
                } else {
                    System.out.println("UNKNOWN\t" + currentChar);  // Unrecognized character
                    currentPos++;
                }
            }
            skipWhiteSpace();  // Skip whitespace after processing a token
        }
    }
    private void skipWhiteSpace() {
        while (currentPos < expression.length() && Character.isWhitespace(expression.charAt(currentPos))) {
            currentPos++;
        }
    }
    private char peekChar() {
        return currentPos + 1 < expression.length() ? expression.charAt(currentPos + 1) : '\0';
    }
    private  void handleComments() {
        if (peekChar() == '/') { // This checks for single-line comments
            handleSingleLineComment();
        } else if (peekChar() == '*') { // This checks for multi-line comments
            handleMultiLineComment();
        }
    }
    private  void handleSingleLineComment() {
        // Move currentPos to the end of the line to skip the comment
        while (currentPos < expression.length() && expression.charAt(currentPos) != '\n') {
            currentPos++;
        }
        // Optionally, skip the newline character itself
        if (currentPos < expression.length() && expression.charAt(currentPos) == '\n') {
            currentPos++;
        }
    }
    private  void handleMultiLineComment() {
        // Increment currentPos to avoid rechecking the '*' at the start
        currentPos++;
        // Keep incrementing currentPos until the end of comment "*/" is found
        while (currentPos + 1 < expression.length()) {
            if (expression.charAt(currentPos) == '*' && expression.charAt(currentPos + 1) == '/') {
                currentPos += 2; // Skip past the closing "*/"
                break;
            }
            currentPos++;
        }
    }
     public void clearTokens() {
         tokens.clear();
     }
    private void handleNumericLiteral() {
        StringBuilder number = new StringBuilder();
        boolean isFloat = false;  // Flag to track if the number is a float
        while (currentPos < expression.length() && (Character.isDigit(expression.charAt(currentPos)) || expression.charAt(currentPos) == '.')) {
            if (expression.charAt(currentPos) == '.') {
                if (peekChar() != '\0' && Character.isDigit(peekChar())) {  // Ensure that the dot is followed by a digit
                    isFloat = true;
                }
            }
            number.append(expression.charAt(currentPos));
            currentPos++;
        }
        if (isFloat) {
            tokens.add(new Token("FLOAT_LITERAL", number.toString()));
        } else {
            tokens.add(new Token("INTEGER_LITERAL", number.toString()));
        }
    }
    private void handleIdentifier() {
        StringBuilder identifier = new StringBuilder();
        while (currentPos < expression.length() &&
                (Character.isLetterOrDigit(expression.charAt(currentPos)) || expression.charAt(currentPos) == '_')) {
            identifier.append(expression.charAt(currentPos));
            currentPos++;
        }
        // Check if the identifier matches any keyword
        if (javaKeywords.contains(identifier.toString())) {
            tokens.add(new Token("KEYWORD", identifier.toString()));
        } else {
            tokens.add(new Token("IDENTIFIER", identifier.toString()));
        }
    }
    private void handleStringLiteral() {
        StringBuilder stringLiteral = new StringBuilder();
        currentPos++; // Skip the initial double quote
        while (currentPos < expression.length() && expression.charAt(currentPos) != '"') {
            if (expression.charAt(currentPos) == '\\' && currentPos + 1 < expression.length()) { // Handle escape sequences
                stringLiteral.append(expression.charAt(currentPos + 1));
                currentPos += 2; // Skip the escape character and the escaped character
                continue;
            }
            stringLiteral.append(expression.charAt(currentPos));
            currentPos++;
        }
        currentPos++; // Skip the closing double quote
        tokens.add(new Token("STRING_LITERAL", stringLiteral.toString()));
    }
    private void handleCharacterLiteral() {
        StringBuilder charLiteral = new StringBuilder();
        currentPos++; // Skip the initial single quote
        if (expression.charAt(currentPos) == '\\' && currentPos + 1 < expression.length()) { // Handle escape sequences
            charLiteral.append(expression.charAt(currentPos + 1));
            currentPos += 2; // Move past the escape character and the escaped character
        } else {
            charLiteral.append(expression.charAt(currentPos));
            currentPos++;
        }
        currentPos++; // Skip the closing single quote
        tokens.add(new Token("CHAR_LITERAL", charLiteral.toString()));
    }
    private static String lookup(char ch) {
        switch (ch) {
            case '+':
                return "ADD_OPr";
            case '-':
                return "SUB_OPr";
            case '*':
                return "MULT_OPr";
            case '/':
                return "DIV_OPr";
            case ';':
                return "SEMICOLON";
            case ':':
                return "COLON";
            case '(':
                return "LEFT_PAREN";
            case ')':
                return "RIGHT_PAREN";
            case '{':
                return "LEFT_BRACE";
            case '}':
                return "RIGHT_BRACE";
            case '[':
                return "LEFT_BRACKET";
            case ']':
                return "RIGHT_BRACKET";
            case ',':
                return "Comma";
            case '=':
                return "ASS_OP";
            case '<':
                return "LessThan_OP";
            case '>':
                return "GreaterThan_OP";
            case '.':
                return "IDENTIFIER";
            default:
                return "UNKNOWN";
        }
    }
    public List<Token> getTokens() {
        return tokens;
    }
 class Token {
    String type;
    String value;
    public Token(String type, String value) {
        this.type = type;
        this.value = value;
    }
}
}
class Python_Analyzer {
    private String expression;
    private int currentPos;
    private List<Token> tokens;
    private final List<String> pythonKeywords =  Arrays.asList(
            "False", "None", "True", "and", "as", "assert", "async", "await",
            "break", "class", "continue", "def", "del", "elif", "else", "except",
            "finally", "for", "from", "global", "if", "import", "in", "is",
            "lambda", "nonlocal", "not", "or", "pass", "raise", "return", "try",
            "while", "with", "yield"
    );
    public Python_Analyzer() {
        this.tokens = new ArrayList<>();
    }
    // Method to process each line of Java code
    public void tokenizeJavaLine(String line) {
        this.expression = line;
        this.currentPos = 0;
        this.tokens.clear();  // Clear previous tokens
        lex();  // Start the lexical analysis process
    }
    private void lex() {
        skipWhiteSpace();  // Skip initial whitespace, but not newlines as they are significant in Python
        while (currentPos < expression.length()) {
            char currentChar = expression.charAt(currentPos);
            // Handling comments first to ignore them in the tokenization process
            if (currentChar == '#') {
                handleComment();
                continue;  // Move to the next iteration after handling the comment
            }
            // Handling string literals which can be single, double, or triple-quoted
            if (currentChar == '\'' || currentChar == '"') {
                handleStringLiteral();
                continue;  // Continue after handling the string to avoid processing closing quotes
            }
            // Use lookup to identify if the current character is a known operator or symbol
            String tokenType = lookup(currentChar);
            if (!tokenType.equals("UNKNOWN")) {  // Known token type such as operators or punctuations
                tokens.add(new Token(tokenType, Character.toString(currentChar)));
                currentPos++;  // Move to the next character
            } else {
                // Handle numeric literals including integers, floats, and complex numbers
                if (Character.isDigit(currentChar) || (currentChar == '.' && Character.isDigit(peekChar()))) {
                    handleNumericLiteral();
                }
                // Handle identifiers which could also be keywords
                else if (Character.isLetter(currentChar) || currentChar == '_') {
                    handleIdentifier();
                }
                else {
                    System.out.println("UNKNOWN\t" + currentChar);  // Unrecognized character
                    currentPos++;
                }
            }
            skipWhiteSpace();  // Skip whitespace after processing a token
        }
    }
    private void handleComment() {
        while (currentPos < expression.length() && expression.charAt(currentPos) != '\n') {
            currentPos++;  // Move past each character until the end of the line
        }
    }
    public void clearTokens() {
        tokens.clear();
    }
    private void handleNumericLiteral() {
        StringBuilder number = new StringBuilder();
        boolean isFloat = false, isComplex = false;
        while (currentPos < expression.length() && (Character.isDigit(expression.charAt(currentPos)) || expression.charAt(currentPos) == '.' || expression.charAt(currentPos) == 'j' || expression.charAt(currentPos) == 'J')) {
            if (expression.charAt(currentPos) == '.') {
                isFloat = true;
            }
            if (expression.charAt(currentPos) == 'j' || expression.charAt(currentPos) == 'J') {
                isComplex = true;
            }
            number.append(expression.charAt(currentPos));
            currentPos++;
        }
        if (isComplex) {
            tokens.add(new Token("COMPLEX_LITERAL", number.toString()));
        } else if (isFloat) {
            tokens.add(new Token("FLOAT_LITERAL", number.toString()));
        } else {
            tokens.add(new Token("INTEGER_LITERAL", number.toString()));
        }
    }
    private void handleIdentifier() {
        StringBuilder identifier = new StringBuilder();
        while (currentPos < expression.length() &&
                (Character.isLetterOrDigit(expression.charAt(currentPos)) || expression.charAt(currentPos) == '_')) {
            identifier.append(expression.charAt(currentPos));
            currentPos++;
        }
        // Check if the identifier matches any Python keyword
        if (pythonKeywords.contains(identifier.toString())) {
            tokens.add(new Token("KEYWORD", identifier.toString()));
        } else {
            tokens.add(new Token("IDENTIFIER", identifier.toString()));
        }
    }
    private void handleStringLiteral() {
        char startQuote = expression.charAt(currentPos);
        int quoteCount = 1;
        currentPos++;
        // Check for triple quotes
        if (currentPos + 1 < expression.length() && expression.charAt(currentPos) == startQuote && expression.charAt(currentPos + 1) == startQuote) {
            quoteCount = 3;
            currentPos += 2;  // Skip the additional two quotes for triple-quoted strings
        }
        while (currentPos < expression.length()) {
            if (expression.charAt(currentPos) == startQuote) {
                quoteCount--;
                if (quoteCount == 0) {
                    currentPos++;  // Move past the closing quote
                    break;
                }
            } else {
                quoteCount = (startQuote == expression.charAt(currentPos) && quoteCount != 3) ? 1 : 3;
            }
            if (expression.charAt(currentPos) == '\\' && currentPos + 1 < expression.length()) {  // Handle escape sequences
                currentPos++;  // Skip the escape character
            }
            currentPos++;
        }
    }
    private char peekChar() {
        return currentPos + 1 < expression.length() ? expression.charAt(currentPos + 1) : '\0';
    }
    private void skipWhiteSpace() {
        while (currentPos < expression.length() && Character.isWhitespace(expression.charAt(currentPos)) && expression.charAt(currentPos) != '\n') {
            currentPos++;
        }
    }
    private void handleCharacterLiteral() {
        StringBuilder charLiteral = new StringBuilder();
        currentPos++; // Skip the initial single quote
        if (expression.charAt(currentPos) == '\\' && currentPos + 1 < expression.length()) { // Handle escape sequences
            charLiteral.append(expression.charAt(currentPos + 1));
            currentPos += 2; // Move past the escape character and the escaped character
        } else {
            charLiteral.append(expression.charAt(currentPos));
            currentPos++;
        }
        currentPos++; // Skip the closing single quote
        tokens.add(new Token("CHAR_LITERAL", charLiteral.toString()));
    }
    private static String lookup(char ch) {
        switch (ch) {
            case '+':
                return "ADD_OPr";
            case '-':
                return "SUB_OPr";
            case '*':
                return "MULT_OPr";
            case '/':
                return "DIV_OPr";
            case ';':
                return "SEMICOLON";
            case ':':
                return "COLON";
            case '(':
                return "LEFT_PAREN";
            case ')':
                return "RIGHT_PAREN";
            case '{':
                return "LEFT_BRACE";
            case '}':
                return "RIGHT_BRACE";
            case '[':
                return "LEFT_BRACKET";
            case ']':
                return "RIGHT_BRACKET";
            case ',':
                return "Comma";
            case '=':
                return "ASS_OP";
            case '<':
                return "LessThan_OP";
            case '>':
                return "GreaterThan_OP";
            case '.':
                return "IDENTIFIER";
            default:
                return "UNKNOWN";
        }
    }
    public List<Token> getTokens() {
        return tokens;
    }
    class Token {
        String type;
        String value;
        public Token(String type, String value) {
            this.type = type;
            this.value = value;
        }
    }
}
