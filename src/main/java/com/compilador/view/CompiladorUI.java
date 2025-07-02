package com.compilador.view;

import javax.swing.*;
import com.compilador.model.LexicalError;
import com.compilador.model.Lexico;
import com.compilador.model.SemanticError;
import com.compilador.model.Semantico;
import com.compilador.model.Sintatico;
import com.compilador.model.SyntaticError;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class CompiladorUI extends JFrame {
    private JTextArea editorTexto;
    private JPanel areaMensagens;
    private JLabel barraStatus;
    private JTextArea linhasNumeradas;
    private File arquivoAtual;
    private JScrollPane mensagemScrollPane;

    public CompiladorUI() {
        setTitle("Compilador");
        setSize(1500, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JToolBar toolBar = new JToolBar();
        toolBar.setPreferredSize(new Dimension(1500, 70));

        adicionarBotao(toolBar, "Novo", "/icones/novo.png", "ctrl N", (e) -> novoArquivo());
        adicionarBotao(toolBar, "Abrir", "/icones/abrir.png", "ctrl O", (e) -> abrirArquivo());
        adicionarBotao(toolBar, "Salvar", "/icones/salvar.png", "ctrl S", (e) -> salvarArquivo());
        toolBar.addSeparator();
        adicionarBotao(toolBar, "Copiar", "/icones/copiar.png", "ctrl C", (e) -> copiarTexto());
        adicionarBotao(toolBar, "Colar", "/icones/colar.png", "ctrl V", (e) -> colarTexto());
        adicionarBotao(toolBar, "Recortar", "/icones/recortar.png", "ctrl X", (e) -> recortarTexto());
        toolBar.addSeparator();
        adicionarBotao(toolBar, "Compilar", "/icones/compilar.png", "F7", (e) -> compilarCodigo());
        toolBar.addSeparator();
        adicionarBotao(toolBar, "Equipe", "/icones/time.png", "F1", (e) -> exibirEquipe());

        editorTexto = new JTextArea();
        editorTexto.setBorder(new NumberedBorder());
        editorTexto.setLineWrap(false);
        editorTexto.setWrapStyleWord(false);

        JScrollPane editorScrollPane = new JScrollPane(editorTexto);
        editorScrollPane.setRowHeaderView(linhasNumeradas);
        editorScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        areaMensagens = new JPanel(new BorderLayout());
        mensagemScrollPane = new JScrollPane(areaMensagens);
        mensagemScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        mensagemScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        mensagemScrollPane.setPreferredSize(new Dimension(1500, 100));

        barraStatus = new JLabel(" Pronto");
        barraStatus.setPreferredSize(new Dimension(1500, 25));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorScrollPane, mensagemScrollPane);
        splitPane.setResizeWeight(0.8);
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.8));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolBar, BorderLayout.NORTH);
        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(barraStatus, BorderLayout.SOUTH);
    }

    private void adicionarBotao(JToolBar toolBar, String nome, String icone, String atalho, ActionListener acao) {
        JButton botao = new JButton(nome);
        botao.setIcon(new ImageIcon(getClass().getResource(icone)));
        botao.setVerticalTextPosition(SwingConstants.BOTTOM);
        botao.setHorizontalTextPosition(SwingConstants.CENTER);
        botao.setPreferredSize(new Dimension(100, 60));
        botao.addActionListener(acao);
        toolBar.add(botao);
    }

    private void novoArquivo() {
        editorTexto.setText("");
        areaMensagens.removeAll();
        areaMensagens.revalidate();
        areaMensagens.repaint();
        barraStatus.setText(" Pronto");
        arquivoAtual = null;
    }

    private void abrirArquivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos de Texto (*.txt)", "txt"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
                StringBuilder conteudo = new StringBuilder();
                String linha;
                while ((linha = reader.readLine()) != null) {
                    conteudo.append(linha).append("\n");
                }
                editorTexto.setText(conteudo.toString());
                barraStatus.setText(arquivo.getAbsolutePath());
                arquivoAtual = arquivo;
                areaMensagens.removeAll();
                areaMensagens.revalidate();
                areaMensagens.repaint();
            } catch (IOException e) {
                barraStatus.setText(" Erro ao abrir o arquivo.");
            }
        }
    }

    private void salvarArquivo() {
        if (arquivoAtual == null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos de Texto (*.txt)", "txt"));
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                arquivoAtual = fileChooser.getSelectedFile();
                if (!arquivoAtual.getName().toLowerCase().endsWith(".txt")) {
                    arquivoAtual = new File(arquivoAtual.getAbsolutePath() + ".txt");
                }
            } else {
                return;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoAtual))) {
            writer.write(editorTexto.getText());
            barraStatus.setText(" Arquivo salvo: " + arquivoAtual.getName());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar o arquivo.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void copiarTexto() {
        editorTexto.copy();
    }

    private void colarTexto() {
        editorTexto.paste();
    }

    private void recortarTexto() {
        editorTexto.cut();
    }


    private void compilarCodigo() {
        salvarArquivo();
        
        areaMensagens.removeAll();
        areaMensagens.revalidate();
        areaMensagens.repaint();
        
        String codigo = editorTexto.getText();

        if (codigo.trim().isEmpty()) {
            JLabel lblErro = new JLabel("Erro: Nenhum código inserido!");
            areaMensagens.add(lblErro, BorderLayout.CENTER);
            barraStatus.setText(" Erro: código vazio");
            areaMensagens.revalidate();
            return;
        }

        Lexico lexico = new Lexico(codigo);
        Sintatico sintatico = new Sintatico();
		Semantico semantico = new Semantico();


        try {
            semantico.setArquivoAtual(arquivoAtual);
            sintatico.parse(lexico, semantico);
            JLabel lblEquipe = new JLabel("Programa compilado com sucesso!");
            areaMensagens.add(lblEquipe, BorderLayout.NORTH);
            areaMensagens.revalidate();
            areaMensagens.repaint();
        } catch (LexicalError e) {
            tratarErroLexico(e, codigo);
        } catch (SyntaticError e) {
            tratarErroSintatico(e, codigo);
        } catch (SemanticError e) {
            tratarErroSemantico(e, codigo);
        }
    }

    private void tratarErroLexico(LexicalError e, String codigo) {
        areaMensagens.removeAll();
        
        int errorPos = e.getPosition();
        char errorChar = errorPos < codigo.length() ? codigo.charAt(errorPos) : '\0';

        String[] linhas = codigo.split("\n");
        int linhaErro = 1;
        int posAcumulada = 0;

        for (String l : linhas) {
            posAcumulada += l.length() + 1;
            if (errorPos < posAcumulada)
                break;
            linhaErro++;
        }

        String mensagemErro;
        if (errorChar != '\0') {
            mensagemErro = String.format("ERRO na linha %d: %c - %s",
                    linhaErro, errorChar, e.getMessage());
        } else {
            mensagemErro = String.format("ERRO na linha %d: %s",
                    linhaErro, e.getMessage());
        }

        JTextArea lblErro = new JTextArea(mensagemErro);
        lblErro.setEditable(false);
        areaMensagens.add(lblErro);

        barraStatus.setText(" Erro durante a compilação");

        areaMensagens.revalidate();
        areaMensagens.repaint();
        
        SwingUtilities.invokeLater(() -> {
            mensagemScrollPane.getVerticalScrollBar().setValue(0);
            mensagemScrollPane.getHorizontalScrollBar().setValue(0);
        });
    }

private void tratarErroSintatico(SyntaticError e, String codigo) {
    areaMensagens.removeAll();
    
    int errorPos = e.getPosition();
    
    // Encontrar a linha e a coluna do erro
    String[] linhas = codigo.split("\n");
    int linhaErro = 1;
    int posAcumulada = 0;
    int inicioLinha = 0;
    
    for (String l : linhas) {
        if (errorPos < posAcumulada + l.length() + 1) {
            inicioLinha = posAcumulada;
            break;
        }
        posAcumulada += l.length() + 1;
        linhaErro++;
    }
    
    // Extrair o token encontrado
    String tokenEncontrado = "";
    if (errorPos < codigo.length()) {
        // Encontrar o token atual na linha
        String linhaAtual = linhas.length > linhaErro - 1 ? linhas[linhaErro - 1] : "";
        int colunaErro = errorPos - inicioLinha;
        
        // Encontrar o início e fim do token
        int inicioToken = colunaErro;
        while (inicioToken > 0 && !Character.isWhitespace(linhaAtual.charAt(inicioToken - 1))) {
            inicioToken--;
        }
        
        int fimToken = colunaErro;
        while (fimToken < linhaAtual.length() && !Character.isWhitespace(linhaAtual.charAt(fimToken))) {
            fimToken++;
        }
        
        tokenEncontrado = linhaAtual.substring(
            Math.min(inicioToken, linhaAtual.length()),
            Math.min(fimToken, linhaAtual.length())
        ).trim();
    } else {
        tokenEncontrado = "$";
    }
    
    // Obter a mensagem de erro original
    String mensagemOriginal = e.getMessage();
    
    // Formatar a mensagem de acordo com as especificações
    String mensagemErro;
    
    // Caso 1: Quando for encontrado $
    if (tokenEncontrado.equals("$")) {
        mensagemErro = String.format("ERRO na linha %d - encontrado EOF esperado %s",
            linhaErro, mensagemOriginal.replace("esperado ", ""));
    }
    // Caso 2: Quando for esperado "fim de programa"
    else if (mensagemOriginal.contains("EOF")) {
        mensagemErro = String.format("ERRO na linha %d - encontrado %s esperado EOF",
            linhaErro, tokenEncontrado.isEmpty() ? "EOF" : tokenEncontrado);
    }
    // Caso 3: Para strings constantes encontradas
    else if (tokenEncontrado.startsWith("\"") && tokenEncontrado.endsWith("\"")) {
        mensagemErro = String.format("ERRO na linha %d - encontrado constante_string esperado %s",
            linhaErro, mensagemOriginal.replace("esperado ", ""));
    }
    // Caso padrão
    else {
        mensagemErro = String.format("ERRO na linha %d - encontrado %s %s",
            linhaErro, tokenEncontrado, mensagemOriginal);
    }
    
    JTextArea lblErro = new JTextArea(mensagemErro);
    lblErro.setEditable(false);
    areaMensagens.add(lblErro);
    
    barraStatus.setText(" Erro durante a compilação");
    
    areaMensagens.revalidate();
    areaMensagens.repaint();
    
    SwingUtilities.invokeLater(() -> {
        mensagemScrollPane.getVerticalScrollBar().setValue(0);
        mensagemScrollPane.getHorizontalScrollBar().setValue(0);
    });
}

private void tratarErroSemantico(SemanticError e, String codigo) {
    areaMensagens.removeAll();
    
    int errorPos = e.getPosition();
    
    // Encontrar a linha do erro
    String[] linhas = codigo.split("\n");
    int linhaErro = 1;
    int posAcumulada = 0;
    
    for (String l : linhas) {
        posAcumulada += l.length() + 1;
        if (errorPos < posAcumulada)
            break;
        linhaErro++;
    }
    
    // Formatar a mensagem de erro
    String mensagemErro;
    if (errorPos >= 0) {
        mensagemErro = String.format("ERRO na linha %d: %s", linhaErro, e.getMessage());
    } else {
        // Para erros sem posição específica (como -1)
        mensagemErro = String.format("ERRO: %s", e.getMessage());
    }
    
    JTextArea lblErro = new JTextArea(mensagemErro);
    lblErro.setEditable(false);
    areaMensagens.add(lblErro);
    
    barraStatus.setText(" Erro semântico durante a compilação");
    
    areaMensagens.revalidate();
    areaMensagens.repaint();
    
    SwingUtilities.invokeLater(() -> {
        mensagemScrollPane.getVerticalScrollBar().setValue(0);
        mensagemScrollPane.getHorizontalScrollBar().setValue(0);
    });
}

    private void exibirEquipe() {
        areaMensagens.removeAll();
        JLabel lblEquipe = new JLabel("Equipe: João Victor Rodrigues e Lucas Samuel Gustzaki");
        areaMensagens.add(lblEquipe, BorderLayout.CENTER);
        areaMensagens.revalidate();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CompiladorUI compilador = new CompiladorUI();
            compilador.setVisible(true);
        });
    }
}