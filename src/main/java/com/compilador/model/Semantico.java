package com.compilador.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Semantico implements Constants {
    private String codigoObjeto = "";
    private String operadorRelacional = "";
    private String tipo = "";
    private ArrayList<String> listaIdentificadores = new ArrayList<>();
    private HashMap<String, String> tabelaSimbolos = new HashMap<>();
    private Stack<String> pilhaTipos = new Stack<>();
    private Stack<String> pilhaRotulos = new Stack<>();
    private int contadorRotulos = 0;
    private boolean erroSemantico = false;

    public void executeAction(int action, Token token) throws SemanticError {
        if (erroSemantico)
            return;

        try {
            switch (action) {
                // Ação 101 - Início do programa
                case 101:
                    codigoObjeto = ".assembly extern mscorlib {}\n" +
                            ".assembly _programa{}\n" +
                            ".module _programa.exe\n" +
                            ".class public _programa{\n" +
                            " .method static public void _principal(){\n" +
                            " .entrypoint\n";
                    break;

                // Ação 102 - Fim do programa
                case 102:
                    codigoObjeto += "\n        ret\n    }\n}";
                    salvarCodigoObjeto();
                    break;

                // Ação 103 - Guardar tipo
                case 103:
                    tipo = token.getLexeme();
                    // Converter tipos para IL
                    if (tipo.equals("int"))
                        tipo = "int64";
                    else if (tipo.equals("float"))
                        tipo = "float64";
                    else if (tipo.equals("char"))
                        tipo = "string";
                    break;

                // Ação 104 - Processar lista de identificadores
                case 104:
                    for (String id : listaIdentificadores) {
                        if (tabelaSimbolos.containsKey(id)) {
                            throw new SemanticError(id + " já declarado", token.getPosition());
                        } else {
                            tabelaSimbolos.put(id, tipo);
                            codigoObjeto += "        .locals (" + tipo + " " + id + ")\n";
                        }
                    }
                    listaIdentificadores.clear();
                    break;

                // Ação 105 - Adicionar identificador à lista
                case 105:
                    listaIdentificadores.add(token.getLexeme());
                    break;

                // Ação 106 - Atribuição
                case 106:
                    String tipoExpr = pilhaTipos.pop();
                    String id = listaIdentificadores.get(0);

                    if (!tabelaSimbolos.containsKey(id)) {
                        throw new SemanticError(id + " não declarado", token.getPosition());
                    }

                    String tipoVar = tabelaSimbolos.get(id);

                    // Conversão de tipos se necessário
                    if (tipoExpr.equals("int64") && tipoVar.equals("float64")) {
                        codigoObjeto += "        conv.r8\n";
                    } else if (tipoExpr.equals("float64") && tipoVar.equals("int64")) {
                        codigoObjeto += "        conv.i8\n";
                    }

                    codigoObjeto += "        stloc " + id + "\n";
                    listaIdentificadores.clear();
                    break;

                // Ação 107 - Entrada (request)
                case 107:
                    for (String idReq : listaIdentificadores) {
                        if (!tabelaSimbolos.containsKey(idReq)) {
                            throw new SemanticError(idReq + " não declarado", token.getPosition());
                        }

                        String tipoReq = tabelaSimbolos.get(idReq);
                        if (tipoReq.equals("char") || tipoReq.equals("bool")) {
                            throw new SemanticError(idReq + " inválido para comando de entrada", token.getPosition());
                        }

                        codigoObjeto += "        call string [mscorlib]System.Console::ReadLine()\n";

                        if (tipoReq.equals("int64")) {
                            codigoObjeto += "        call int64 [mscorlib]System.Int64::Parse(string)\n";
                        } else if (tipoReq.equals("float64")) {
                            codigoObjeto += "        call float64 [mscorlib]System.Double::Parse(string)\n";
                        }

                        codigoObjeto += "        stloc " + idReq + "\n";
                    }
                    listaIdentificadores.clear();
                    break;

                // Ação 108 - Saída (echo)
                case 108:
                    String tipoSaida = pilhaTipos.pop();

                    if (tipoSaida.equals("int64")) {
                        codigoObjeto += "        conv.r8\n";
                        codigoObjeto += "        conv.i8\n";
                        codigoObjeto += "        call void [mscorlib]System.Console::Write(int64)\n";
                    } else if (tipoSaida.equals("float64")) {
                        codigoObjeto += "        call void [mscorlib]System.Console::Write(float64)\n";
                    } else if (tipoSaida.equals("string") || tipoSaida.equals("char")) {
                        codigoObjeto += "        call void [mscorlib]System.Console::Write(string)\n";
                    } else if (tipoSaida.equals("bool")) {
                        codigoObjeto += "        call void [mscorlib]System.Console::Write(bool)\n";
                    }
                    break;

                // Ação 109 - Início do switch
                case 109:
                    String rotuloFimSwitch = "ROTULO_" + (contadorRotulos++);
                    pilhaRotulos.push(rotuloFimSwitch);
                    break;

                // Ação 110 - Fim do switch
                case 110:
                    codigoObjeto += "        pop\n";
                    String rotuloFim = pilhaRotulos.pop();
                    codigoObjeto += "        " + rotuloFim + ":\n";
                    break;

                // Ação 111 - Case
                case 111:
                    codigoObjeto += "        dup\n";
                    break;

                // Ação 112 - Expressão do case
                case 112:
                    codigoObjeto += "        ceq\n";
                    String rotuloProximoCase = "ROTULO_" + (contadorRotulos++);
                    pilhaRotulos.push(rotuloProximoCase);
                    codigoObjeto += "        brfalse " + rotuloProximoCase + "\n";
                    break;

                // Ação 113 - Comandos do case
                case 113:
                    codigoObjeto += "        pop\n";
                    String rotuloProximo = pilhaRotulos.pop();
                    String rotuloFimSwitchAtual = pilhaRotulos.pop();
                    codigoObjeto += "        br " + rotuloFimSwitchAtual + "\n";
                    codigoObjeto += "        " + rotuloProximo + ":\n";
                    pilhaRotulos.push(rotuloFimSwitchAtual);
                    break;

                // Ação 114 - Início do do-while/do-until
                case 114:
                    String rotuloInicio = "ROTULO_" + (contadorRotulos++);
                    codigoObjeto += "        " + rotuloInicio + ":\n";
                    pilhaRotulos.push(rotuloInicio);
                    break;

                // Ação 115 - While
                case 115:
                    String rotuloInicioWhile = pilhaRotulos.pop();
                    codigoObjeto += "        brtrue " + rotuloInicioWhile + "\n";
                    break;

                // Ação 116 - Until
                case 116:
                    String rotuloInicioUntil = pilhaRotulos.pop();
                    codigoObjeto += "        brfalse " + rotuloInicioUntil + "\n";
                    break;

                // Ação 117 - Operador &
                case 117:
                    codigoObjeto += "        and\n";
                    pilhaTipos.pop();
                    pilhaTipos.pop();
                    pilhaTipos.push("bool");
                    break;

                // Ação 118 - Operador |
                case 118:
                    codigoObjeto += "        or\n";
                    pilhaTipos.pop();
                    pilhaTipos.pop();
                    pilhaTipos.push("bool");
                    break;

                // Ação 119 - True
                case 119:
                    codigoObjeto += "        ldc.i4.1\n";
                    pilhaTipos.push("bool");
                    break;

                // Ação 120 - False
                case 120:
                    codigoObjeto += "        ldc.i4.0\n";
                    pilhaTipos.push("bool");
                    break;

                // Ação 121 - Operador !
                case 121:
                    codigoObjeto += "        not\n";
                    pilhaTipos.pop();
                    pilhaTipos.push("bool");
                    break;

                // Ação 122 - Operador relacional
                case 122:
                    operadorRelacional = token.getLexeme();
                    break;

                // Ação 123 - Comparação relacional
                case 123:

                    if (operadorRelacional.equals("==")) {
                        codigoObjeto += "        ceq\n";
                    } else if (operadorRelacional.equals("!=")) {
                        codigoObjeto += "        ceq\n";
                        codigoObjeto += "        ldc.i4.0\n";
                        codigoObjeto += "        ceq\n";
                    } else if (operadorRelacional.equals("<")) {
                        codigoObjeto += "        clt\n";
                    } else if (operadorRelacional.equals(">")) {
                        codigoObjeto += "        cgt\n";
                    }

                    pilhaTipos.push("bool");
                    operadorRelacional = "";
                    break;

                // Ação 124 - Operador +
                case 124:
                    codigoObjeto += "        add\n";
                    resolverTipoOperacaoAritmetica();
                    break;

                // Ação 125 - Operador -
                case 125:
                    codigoObjeto += "        sub\n";
                    resolverTipoOperacaoAritmetica();
                    break;

                // Ação 126 - Operador *
                case 126:
                    codigoObjeto += "        mul\n";
                    resolverTipoOperacaoAritmetica();
                    break;

                // Ação 127 - Operador /
                case 127:
                    codigoObjeto += "        div\n";
                    pilhaTipos.pop();
                    pilhaTipos.pop();
                    pilhaTipos.push("float64");
                    break;

                // Ação 128 - Identificador em expressão
                case 128:
                    String idExpr = token.getLexeme();
                    if (!tabelaSimbolos.containsKey(idExpr)) {
                        throw new SemanticError(idExpr + " não declarado", token.getPosition());
                    }

                    String tipoId = tabelaSimbolos.get(idExpr);
                    codigoObjeto += "        ldloc " + idExpr + "\n";

                    if (tipoId.equals("int64")) {
                        codigoObjeto += "        conv.r8\n";
                    }

                    pilhaTipos.push(tipoId);
                    break;

                // Ação 129 - Constante int
                case 129:
                    codigoObjeto += "        ldc.i8 " + token.getLexeme() + "\n";
                    codigoObjeto += "        conv.r8\n";
                    pilhaTipos.push("int64");
                    break;

                // Ação 130 - Constante float
                case 130:
                    codigoObjeto += "        ldc.r8 " + token.getLexeme() + "\n";
                    pilhaTipos.push("float64");
                    break;

                // Ação 131 - Constante char
                case 131:
                    String charValue = token.getLexeme();
                    // Tratar caracteres especiais
                    if (charValue.equals("\\n"))
                        charValue = "\"\\n\"";
                    else if (charValue.equals("\\t"))
                        charValue = "\"\\t\"";
                    else if (charValue.equals("\\s"))
                        charValue = "\" \"";
                    else
                        charValue = "\"" + charValue + "\"";

                    codigoObjeto += "        ldstr " + charValue + "\n";
                    pilhaTipos.push("string");
                    break;

                // Ação 132 - Constante string
                case 132:
                    codigoObjeto += "        ldstr " + token.getLexeme() + "\n";
                    pilhaTipos.push("string");
                    break;

                // Ação 133 - Operador unário 
                case 133:
                    codigoObjeto += "        neg\n";
                    break;
            }
        } catch (SemanticError e) {
            erroSemantico = true;
            throw e;
        }
    }

    private void resolverTipoOperacaoAritmetica() {
        String tipo2 = pilhaTipos.pop();
        String tipo1 = pilhaTipos.pop();

        if (tipo1.equals("float64") || tipo2.equals("float64")) {
            pilhaTipos.push("float64");
        } else {
            pilhaTipos.push("int64");
        }
    }

    private void salvarCodigoObjeto() throws SemanticError {
        if (arquivoAtual == null) {
            throw new SemanticError("Nenhum arquivo aberto para salvar o código objeto", -1);
        }

        String nomeArquivo = arquivoAtual.getAbsolutePath();
        if (nomeArquivo.endsWith(".txt")) {
            nomeArquivo = nomeArquivo.substring(0, nomeArquivo.length() - 4) + ".il";
        } else {
            nomeArquivo += ".il";
        }

        try (FileWriter writer = new FileWriter(nomeArquivo)) {
            writer.write(codigoObjeto);
        } catch (IOException e) {
            throw new SemanticError("Erro ao salvar o código objeto", -1);
        }
    }

    // Método auxiliar para acessar o arquivo atual da UI
    private File arquivoAtual;

    public void setArquivoAtual(File arquivo) {
        this.arquivoAtual = arquivo;
    }
}