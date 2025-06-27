package com.compilador.model;

public class Token
{
    private int id;
    private String lexeme;
    private int position;

    public Token(int id, String lexeme, int position)
    {
        this.id = id;
        this.lexeme = lexeme;
        this.position = position;
    }

    public final int getId()
    {
        return id;
    }

    public final String getLexeme()
    {
        return lexeme;
    }

    public final int getPosition()
    {
        return position;
    }

    public String toString()
    {
        return id+" ( "+lexeme+" ) @ "+position;
    };

        public String getDescricaoToken(Token token) {
        int id = token.getId();
        if (id == 2)
            return "identificador";
        if (id >= 3 && id <= 6) {
            switch (id) {
                case 3:
                    return "constante_int";
                case 4:
                    return "constante_float";
                case 5:
                    return "constante_char";
                case 6:
                    return "constante_string";
            }
        }
        if (id >= 7 && id <= 22)
            return "símbolo especial";
        if (id >= 23 && id <= 39)
            return "palavra reservada";

        return "desconhecido";
    }
}
