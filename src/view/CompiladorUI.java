import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class CompiladorUI extends JFrame {
    private JTextArea editorTexto;
    private JTextArea areaMensagens;
    private JLabel barraStatus;
    private JTextArea linhasNumeradas;
    private File arquivoAtual;

    @SuppressWarnings("unused")
    public CompiladorUI() {
        setTitle("Compilador");
        setSize(1500, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JToolBar toolBar = new JToolBar();
        toolBar.setPreferredSize(new Dimension(1500, 70));

        adicionarBotao(toolBar, "Novo", "public/icones/novo.png", "ctrl N", (e) -> novoArquivo());
        adicionarBotao(toolBar, "Abrir", "public/icones/abrir.png", "ctrl O", (e) -> abrirArquivo());
        adicionarBotao(toolBar, "Salvar", "public/icones/salvar.png", "ctrl S", (e) -> salvarArquivo());
        toolBar.addSeparator();
        adicionarBotao(toolBar, "Copiar", "public/icones/copiar.png", "ctrl C", (e) -> copiarTexto());
        adicionarBotao(toolBar, "Colar", "public/icones/colar.png", "ctrl V", (e) -> colarTexto());
        adicionarBotao(toolBar, "Recortar", "public/icones/recortar.png", "ctrl X", (e) -> recortarTexto());
        toolBar.addSeparator();
        adicionarBotao(toolBar, "Compilar", "public/icones/compliar.png", "F7", (e) -> compilarCodigo());
        toolBar.addSeparator();
        adicionarBotao(toolBar, "Equipe", "public/icones/time.png", "F1", (e) -> exibirEquipe());

        editorTexto = new JTextArea();
        editorTexto.setBorder(new NumberedBorder());
        editorTexto.setLineWrap(false);
        editorTexto.setWrapStyleWord(false); 

        JScrollPane editorScrollPane = new JScrollPane(editorTexto);
        editorScrollPane.setRowHeaderView(linhasNumeradas);
        editorScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS); 
        editorScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        areaMensagens = new JTextArea();
        areaMensagens.setDisabledTextColor(Color.BLACK);
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
        editorTexto.setText("");
        areaMensagens.setText("");
        barraStatus.setText("");
        arquivoAtual = null;
    }

    private void abrirArquivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos de Texto (*.txt)", "txt"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
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
                arquivoAtual = arquivo;
            } catch (IOException e) {
                barraStatus.setText(" Erro ao abrir o arquivo.");
            }
        }
    }

    private void salvarArquivo() {
        if (arquivoAtual != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoAtual))) {
                writer.write(editorTexto.getText());
                areaMensagens.setText(" Arquivo salvo: " + arquivoAtual.getName());
            } catch (IOException e) {
                areaMensagens.setText(" Erro ao salvar o arquivo.");
            }
        } else {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Arquivos de Texto (*.txt)", "txt"));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File arquivo = fileChooser.getSelectedFile();
                if (!arquivo.getName().toLowerCase().endsWith(".txt")) {
                    arquivo = new File(arquivo.getAbsolutePath() + ".txt");
                }
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo))) {
                    writer.write(editorTexto.getText());
                    areaMensagens.setText(" Arquivo salvo: " + arquivo.getName());
                    arquivoAtual = arquivo;
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
            areaMensagens.setText("Compilando código...\nCompilação bem-sucedida!");
        }
    }

    private void exibirEquipe() {
        areaMensagens.setText("Equipe: João Victor Rodrigues e Lucas Samuel Gustzaki");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CompiladorUI compilador = new CompiladorUI();
            compilador.setVisible(true);
        });
    }
}