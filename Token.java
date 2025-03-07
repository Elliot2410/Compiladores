public class Token {

    final TipoToken tipo;
    final String lexema;
    final Object literal;
    final int line;
    public Object type;

    public Token(TipoToken tipo, String lexema, Object literal, int line) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.literal = literal;
        this.line = line;
    }

    public Token(TipoToken tipo, String lexema, int line) {
        this(tipo, lexema, null, line);
    }

    public TipoToken getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }

    public Object getLiteral() {
        return literal;
    }

    public int getLine() {
        return line;
    }

    public String toString() {
        if (literal != null) {
            return "<" + tipo + ", lexema: " + lexema + ", literal: " + literal + ", línea: " + line + ">";
        } else {
            return "<" + tipo + ", línea: " + line + ">";
        }
    }
}
