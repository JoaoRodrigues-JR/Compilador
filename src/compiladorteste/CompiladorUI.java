import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class CompiladorUI extends JFrame {
    private JTextArea editorTexto;
    private JTextArea areaMensagens;
    private JLabel barraStatus;
    private JTextArea linhasNumeradas;
    private File arquivoAtual; // Variável de instância para armazenar o arquivo atual
    private boolean textoAlterado = false; // Variável para rastrear se houve alterações no texto

    @SuppressWarnings("unused")
    public CompiladorUI() {
        setTitle("Compilador");
        setSize(1500, 800);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (confirmarSaida()) {
                    dispose();
                }
            }
        });

        JToolBar toolBar = new JToolBar();
        toolBar.setPreferredSize(new Dimension(1500, 70));

        adicionarBotao(toolBar, "Novo", "icons/new.png", "ctrl N", (e) -> novoArquivo());
        adicionarBotao(toolBar, "Abrir", "icons/open.png", "ctrl O", (e) -> abrirArquivo());
        adicionarBotao(toolBar, "Salvar", "icons/save.png", "ctrl S", (e) -> salvarArquivo());
        toolBar.addSeparator();
        adicionarBotao(toolBar, "Copiar", "icons/copy.png", "ctrl C", (e) -> copiarTexto());
        adicionarBotao(toolBar, "Colar", "icons/paste.png", "ctrl V", (e) -> colarTexto());
        adicionarBotao(toolBar, "Recortar", "icons/cut.png", "ctrl X", (e) -> recortarTexto());
        toolBar.addSeparator();
        adicionarBotao(toolBar, "Compilar", "icons/compile.png", "F7", (e) -> compilarCodigo());
        toolBar.addSeparator();
        adicionarBotao(toolBar, "Equipe", "icons/team.png", "F1", (e) -> exibirEquipe());

        linhasNumeradas = new JTextArea("1\n");
        linhasNumeradas.setEditable(false);
        linhasNumeradas.setEnabled(false);
        linhasNumeradas.setBackground(Color.LIGHT_GRAY);
        linhasNumeradas.setPreferredSize(new Dimension(40, Integer.MAX_VALUE));

        editorTexto = new JTextArea();
        editorTexto.setLineWrap(false);
        editorTexto.setWrapStyleWord(false); 
        editorTexto.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { atualizarNumeracaoLinhas(); textoAlterado = true; }
            public void removeUpdate(DocumentEvent e) { atualizarNumeracaoLinhas(); textoAlterado = true; }
            public void changedUpdate(DocumentEvent e) { atualizarNumeracaoLinhas(); textoAlterado = true; }
        });

        JScrollPane editorScrollPane = new JScrollPane(editorTexto);
        editorScrollPane.setRowHeaderView(linhasNumeradas);
        editorScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS); 
        editorScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        areaMensagens = new JTextArea();
        areaMensagens.setEditable(false);
        areaMensagens.setEnabled(false);
        areaMensagens.setLineWrap(false);
        areaMensagens.setWrapStyleWord(false); 

        JScrollPane mensagemScrollPane = new JScrollPane(areaMensagens);
        mensagemScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS); 
        mensagemScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

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
        String textoBotao = nome.toLowerCase() + " [" + atalho.replace(" ", "-").toLowerCase() + "]";
        JButton botao = new JButton(textoBotao);
        botao.setIcon(new ImageIcon(icone));
        botao.setVerticalTextPosition(SwingConstants.BOTTOM);
        botao.setHorizontalTextPosition(SwingConstants.CENTER); 
        botao.setPreferredSize(new Dimension(100, 60));
        botao.addActionListener(acao);
    
        botao.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(atalho), nome);
        botao.getActionMap().put(nome, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acao.actionPerformed(e);
            }
        });
    
        toolBar.add(botao);
    }

    private void novoArquivo() {
        // Verifica se há alterações não salvas
        if (textoAlterado) {
            int resposta = JOptionPane.showConfirmDialog(this, "Deseja salvar as alterações no arquivo atual?", "Salvar Arquivo", JOptionPane.YES_NO_CANCEL_OPTION);
            if (resposta == JOptionPane.YES_OPTION) {
                salvarArquivo();
            } else if (resposta == JOptionPane.CANCEL_OPTION) {
                return; // Cancela a operação de novo arquivo
            }
        }
    
        // Limpa o editor, a área de mensagens e a barra de status
        editorTexto.setText("");
        areaMensagens.setText("");
        atualizarNumeracaoLinhas();
        barraStatus.setText("");
        arquivoAtual = null; // Reseta o arquivo atual
        textoAlterado = false; // Reseta o estado de alteração do texto
    }

    private void abrirArquivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos de Texto (*.txt)", "txt"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            // Verifica se o arquivo tem a extensão .txt
            if (!arquivo.getName().toLowerCase().endsWith(".txt")) {
                JOptionPane.showMessageDialog(this, "Por favor, selecione um arquivo de texto (*.txt).", "Arquivo Inválido", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
                StringBuilder conteudo = new StringBuilder();
                String linha;
                while ((linha = reader.readLine()) != null) {
                    conteudo.append(linha).append("\n");
                }
                editorTexto.setText(conteudo.toString());
                barraStatus.setText(arquivo.getAbsolutePath());
                arquivoAtual = arquivo; // Atualiza o arquivo atual
                textoAlterado = false; // Reseta o estado de alteração do texto
            } catch (IOException e) {
                barraStatus.setText(" Erro ao abrir o arquivo.");
            }
        }
    }

    private void salvarArquivo() {
        if (arquivoAtual != null) {
            // Salva no arquivo atual
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoAtual))) {
                writer.write(editorTexto.getText());
                areaMensagens.setText(" Arquivo salvo: " + arquivoAtual.getName());
                textoAlterado = false; // Reseta o estado de alteração do texto
            } catch (IOException e) {
                areaMensagens.setText(" Erro ao salvar o arquivo.");
            }
        } else {
            // Abre o JFileChooser para salvar como um novo arquivo
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos de Texto (*.txt)", "txt"));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File arquivo = fileChooser.getSelectedFile();
                // Adiciona a extensão .txt se não estiver presente
                if (!arquivo.getName().toLowerCase().endsWith(".txt")) {
                    arquivo = new File(arquivo.getAbsolutePath() + ".txt");
                }
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo))) {
                    writer.write(editorTexto.getText());
                    areaMensagens.setText(" Arquivo salvo: " + arquivo.getName());
                    arquivoAtual = arquivo; // Atualiza o arquivo atual
                    textoAlterado = false; // Reseta o estado de alteração do texto
                } catch (IOException e) {
                    areaMensagens.setText(" Erro ao salvar o arquivo.");
                }
            }
        }
    }

    private void copiarTexto() { editorTexto.copy(); }
    private void colarTexto() { editorTexto.paste(); }
    private void recortarTexto() { editorTexto.cut(); }

    private void compilarCodigo() {
        salvarArquivo();

        String codigo = editorTexto.getText();
        if (codigo.trim().isEmpty()) {
            areaMensagens.setText("Erro: Nenhum código inserido!");
        } else {
            // Simulação de compilação
            areaMensagens.setText("Compilando código...\nCompilação bem-sucedida!");
        }
    }

    private void exibirEquipe() {
        areaMensagens.setText("Equipe: João Victor Rodrigues e Lucas Samuel Gustzaki");
    }

    private void atualizarNumeracaoLinhas() {
        String texto = editorTexto.getText();
        int totalLinhas = texto.split("\n", -1).length; 
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= totalLinhas; i++) {
            sb.append(i).append("\n");
        }
        linhasNumeradas.setText(sb.toString());
    }

    private boolean confirmarSaida() {
        if (textoAlterado) {
            int resposta = JOptionPane.showConfirmDialog(this, "Deseja salvar as alterações no arquivo atual?", "Salvar Arquivo", JOptionPane.YES_NO_CANCEL_OPTION);
            if (resposta == JOptionPane.YES_OPTION) {
                salvarArquivo();
                return true;
            } else if (resposta == JOptionPane.NO_OPTION) {
                return true;
            } else {
                return false; // Cancela a operação de saída
            }
        }
        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CompiladorUI compilador = new CompiladorUI();
            compilador.setVisible(true);
        });
    }
}