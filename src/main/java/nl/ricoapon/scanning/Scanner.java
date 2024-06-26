package nl.ricoapon.scanning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.ricoapon.Lox;
import static nl.ricoapon.scanning.TokenType.AND;
import static nl.ricoapon.scanning.TokenType.BANG;
import static nl.ricoapon.scanning.TokenType.BANG_EQUAL;
import static nl.ricoapon.scanning.TokenType.CLASS;
import static nl.ricoapon.scanning.TokenType.COMMA;
import static nl.ricoapon.scanning.TokenType.DOT;
import static nl.ricoapon.scanning.TokenType.ELSE;
import static nl.ricoapon.scanning.TokenType.EOF;
import static nl.ricoapon.scanning.TokenType.EQUAL;
import static nl.ricoapon.scanning.TokenType.EQUAL_EQUAL;
import static nl.ricoapon.scanning.TokenType.FALSE;
import static nl.ricoapon.scanning.TokenType.FOR;
import static nl.ricoapon.scanning.TokenType.FUN;
import static nl.ricoapon.scanning.TokenType.GREATER;
import static nl.ricoapon.scanning.TokenType.GREATER_EQUAL;
import static nl.ricoapon.scanning.TokenType.IDENTIFIER;
import static nl.ricoapon.scanning.TokenType.IF;
import static nl.ricoapon.scanning.TokenType.LEFT_BRACE;
import static nl.ricoapon.scanning.TokenType.LEFT_PAREN;
import static nl.ricoapon.scanning.TokenType.LESS;
import static nl.ricoapon.scanning.TokenType.LESS_EQUAL;
import static nl.ricoapon.scanning.TokenType.MINUS;
import static nl.ricoapon.scanning.TokenType.NIL;
import static nl.ricoapon.scanning.TokenType.NUMBER;
import static nl.ricoapon.scanning.TokenType.OR;
import static nl.ricoapon.scanning.TokenType.PLUS;
import static nl.ricoapon.scanning.TokenType.PRINT;
import static nl.ricoapon.scanning.TokenType.RETURN;
import static nl.ricoapon.scanning.TokenType.RIGHT_BRACE;
import static nl.ricoapon.scanning.TokenType.RIGHT_PAREN;
import static nl.ricoapon.scanning.TokenType.SEMICOLON;
import static nl.ricoapon.scanning.TokenType.SLASH;
import static nl.ricoapon.scanning.TokenType.STAR;
import static nl.ricoapon.scanning.TokenType.STRING;
import static nl.ricoapon.scanning.TokenType.SUPER;
import static nl.ricoapon.scanning.TokenType.THIS;
import static nl.ricoapon.scanning.TokenType.TRUE;
import static nl.ricoapon.scanning.TokenType.VAR;
import static nl.ricoapon.scanning.TokenType.WHILE;

public class Scanner {
    private static final Map<String, TokenType> keywords = Map.ofEntries(
            Map.entry("and", AND),
            Map.entry("class", CLASS),
            Map.entry("else", ELSE),
            Map.entry("false", FALSE),
            Map.entry("for", FOR),
            Map.entry("fun", FUN),
            Map.entry("if", IF),
            Map.entry("nil", NIL),
            Map.entry("or", OR),
            Map.entry("print", PRINT),
            Map.entry("return", RETURN),
            Map.entry("super", SUPER),
            Map.entry("this", THIS),
            Map.entry("true", TRUE),
            Map.entry("var", VAR),
            Map.entry("while", WHILE));

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        Character c = advance();
        switch (c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case ';' -> addToken(SEMICOLON);
            case '*' -> addToken(STAR);
            case '!' -> addToken(match('=') ? BANG_EQUAL : BANG);
            case '=' -> addToken(match('=') ? EQUAL_EQUAL : EQUAL);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);
            case '/' -> {
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd())
                        advance();
                } else {
                    addToken(SLASH);
                }
            }
            case ' ', '\r', '\t' -> {
            }
            case '\n' -> line++;
            case '"' -> string();
            case Character x when isDigit(x) -> number();
            case Character x when isAlpha(x) -> identifier();
            default -> Lox.error(line, "Unexpected character: " + c);
        }
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++;
            }
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void number() {
        while (isDigit(peek())) {
            advance();
        }

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek())) {
                advance();
            }
        }

        addToken(NUMBER, Double.valueOf(source.substring(start, current)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }
        String text = source.substring(start, current);
        TokenType type = keywords.getOrDefault(text, IDENTIFIER);
        addToken(type);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean match(char expected) {
        if (isAtEnd()) {
            return false;
        }
        if (source.charAt(current) != expected) {
            return false;
        }

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}