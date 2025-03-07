import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    // Mapa de palabras reservadas
    private static final Map<String, TipoToken> palabrasReservadas;
    static {
        palabrasReservadas = new HashMap<>();
        palabrasReservadas.put("and",    TipoToken.AND);
        palabrasReservadas.put("else",   TipoToken.ELSE);
        palabrasReservadas.put("false",  TipoToken.FALSE);
        palabrasReservadas.put("for",    TipoToken.FOR);
        palabrasReservadas.put("fun",    TipoToken.FUN);
        palabrasReservadas.put("if",     TipoToken.IF);
        palabrasReservadas.put("null",   TipoToken.NULL);
        palabrasReservadas.put("or",     TipoToken.OR);
        palabrasReservadas.put("print",  TipoToken.PRINT);
        palabrasReservadas.put("return", TipoToken.RETURN);
        palabrasReservadas.put("true",   TipoToken.TRUE);
        palabrasReservadas.put("var",    TipoToken.VAR);
        palabrasReservadas.put("while",  TipoToken.WHILE);
        // Nueva funcionalidad: palabra reservada "read" para leer entrada del teclado
        palabrasReservadas.put("read",   TipoToken.READ);
    }

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
            if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).getTipo() == TipoToken.ERROR) {
                return tokens; // Detener el análisis si hay un error
            }
        }
        // Agregar token EOF con lexema "$" (sin literal ni línea)
        tokens.add(new Token(TipoToken.EOF, "$", null, 0));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch(c) {
            // Signos de puntuación
            case '(': addToken(TipoToken.LEFT_PAREN); break;
            case ')': addToken(TipoToken.RIGHT_PAREN); break;
            case '{': addToken(TipoToken.LEFT_BRACE); break;
            case '}': addToken(TipoToken.RIGHT_BRACE); break;
            case ',': addToken(TipoToken.COMMA); break;
            case '.': addToken(TipoToken.DOT); break;
            case '-': addToken(TipoToken.MINUS); break;
            case '+': addToken(TipoToken.PLUS); break;
            case ';': addToken(TipoToken.SEMICOLON); break;
            case '*': addToken(TipoToken.STAR); break;

            // Operadores de uno o dos caracteres
            case '!': addToken(match('=') ? TipoToken.BANG_EQUAL : TipoToken.BANG); break;
            case '=': addToken(match('=') ? TipoToken.EQUAL_EQUAL : TipoToken.EQUAL); break;
            case '<': addToken(match('=') ? TipoToken.LESS_EQUAL : TipoToken.LESS); break;
            case '>': addToken(match('=') ? TipoToken.GREATER_EQUAL : TipoToken.GREATER); break;

            case '/':
                if (match('/')) {
                    // Comentario de una línea: descartar hasta el fin de línea
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    // Comentario de múltiples líneas: descartar hasta encontrar "*/"
                    while (!(peek() == '*' && peekNext() == '/') && !isAtEnd()) {
                        if (peek() == '\n') line++;
                        advance();
                    }
                    if (isAtEnd()) {
                        System.out.println("ERROR: Comentario sin terminar en línea " + line);
                        tokens.add(new Token(TipoToken.ERROR, "", null, line));
                        return;
                    }
                    // Consumir "*/"
                    advance(); // consume '*'
                    advance(); // consume '/'
                } else {
                    addToken(TipoToken.SLASH);
                }
                break;

            // Ignorar espacios en blanco
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;

            case '"': string(); break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    System.out.println("ERROR: Carácter no válido '" + c + "' en la línea " + line);
                    tokens.add(new Token(TipoToken.ERROR, "", null, line));
                }
                break;
        }
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TipoToken type) {
        addToken(type, null);
    }

    private void addToken(TipoToken type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    // Procesa cadenas encerradas en comillas
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                // Error: cadena con salto de línea antes de cerrarse
                System.out.println("ERROR: Se detectó una cadena sin cerrar en la línea " + line);
                tokens.add(new Token(TipoToken.ERROR, "", null, line));
                return;
            }
            advance();
        }
        if (isAtEnd()) {
            System.out.println("ERROR: Se detectó una cadena sin cerrar en la línea " + line);
            tokens.add(new Token(TipoToken.ERROR, "", null, line));
            return;
        }
        // Consumir la comilla de cierre
        advance();
        // El literal es el contenido sin las comillas
        String value = source.substring(start + 1, current - 1);
        addToken(TipoToken.STRING, value);
    }

    // Procesa números (enteros, flotantes, y con exponente)
    private void number() {
        while (isDigit(peek())) advance();

        // Parte decimal
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // consume '.'
            while (isDigit(peek())) advance();
        }

        // Parte exponencial (por ejemplo, E o e)
        if (peek() == 'E' || peek() == 'e') {
            advance(); // consume 'E' o 'e'
            if (peek() == '+' || peek() == '-') {
                advance();
            }
            while (isDigit(peek())) advance();
        }

        String numberText = source.substring(start, current);
        double value = Double.parseDouble(numberText);
        addToken(TipoToken.NUMBER, value);
    }

    // Procesa identificadores y palabras reservadas
    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TipoToken type = palabrasReservadas.get(text);
        if (type == null) type = TipoToken.IDENTIFIER;
        addToken(type);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
