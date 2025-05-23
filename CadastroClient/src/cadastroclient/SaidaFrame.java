package cadastroclient;

import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class SaidaFrame extends JFrame {  // Mudanca de JDialog para JFrame

    private JTextArea texto;
    private String mensagemInicial;

    public SaidaFrame(String usuario) {
        // Configuracoes basicas da janela
        setTitle("Controle de Movimentacao da Loja de FRANCINALDO");
        setSize(700, 500);  // Define o tamanho da janela
        setLocationRelativeTo(null);  // Centraliza a janela na tela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // Fecha a janela ao clicar no "X"
        setResizable(true);  // Permite redimensionar a janela

        // Configura a area de texto
        texto = new JTextArea();
        texto.setEditable(false);  // Impede a edicao do texto
        texto.setFont(new Font("Arial", Font.PLAIN, 14));  // Define a fonte do texto
        texto.setLineWrap(true);  // Ativa a quebra de linha automatica
        texto.setWrapStyleWord(true);  // Ativa a quebra de linha por palavra
        texto.setBackground(new Color(240, 240, 240));  // Define uma cor de fundo suave
        texto.setForeground(new Color(50, 50, 50));  // Define a cor do texto
        texto.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 1));  // Adiciona uma borda ao redor da area de texto

        // Adiciona um painel de titulo
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(70, 130, 180));  // Cor de fundo do painel de titulo
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  // Margem ao redor do painel
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER));  // Layout do painel de titulo

        // Adiciona um rotulo ao painel de titulo
        JLabel titleLabel = new JLabel("Mensagens do Sistema");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));  // Fonte do rotulo
        titleLabel.setForeground(Color.WHITE);  // Cor do texto do rotulo
        titlePanel.add(titleLabel);  // Adiciona o rotulo ao painel de titulo

        // Adiciona o painel de titulo a parte superior da janela
        getContentPane().add(titlePanel, BorderLayout.NORTH);

        // Envolve a area de texto em um painel de rolagem
        JScrollPane scrollPane = new JScrollPane(texto);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  // Adiciona margem ao redor do texto

        // Adiciona o painel de rolagem a janela
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Adiciona a mensagem inicial com o usuario e a data/hora atuais
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        mensagemInicial = "Usuario: " + usuario + "\nConectado com Sucesso em: " + now.format(formatter) + "\n\n";
        texto.append(mensagemInicial);

        // Cria o painel de botoes
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));  // Margem ao redor do painel

        // Personalizacao dos botoes
        JButton clearButton = createCustomButton("Limpar", new Color(255, 99, 71));
        clearButton.addActionListener(e -> texto.setText(mensagemInicial));
        buttonPanel.add(clearButton);

        JButton printButton = createCustomButton("Imprimir", new Color(60, 179, 113));
        printButton.addActionListener(e -> {
            try {
                texto.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Erro ao imprimir: " + ex.getMessage());
            }
        });
        buttonPanel.add(printButton);

        JButton saveButton = createCustomButton("Salvar", new Color(70, 130, 180));
        saveButton.addActionListener(e -> salvarTexto());
        buttonPanel.add(saveButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // Torna a janela visivel
        setVisible(true);
    }

    /**
     * Retorna a area de texto onde as mensagens sao exibidas.
     *
     * @return JTextArea para exibicao de mensagens.
     */
    public JTextArea getTextArea() {
        return this.texto;
    }

    public void adicionarMensagem(String mensagem) {
        texto.append(mensagem + "\n");
    }

    private void salvarTexto() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                FileWriter writer = new FileWriter(fileChooser.getSelectedFile().getAbsolutePath());
                writer.write(texto.getText());
                writer.close();
                JOptionPane.showMessageDialog(this, "Arquivo salvo com sucesso.");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar arquivo: " + ex.getMessage());
        }
    }

    /**
     * Cria um botao personalizado com cores e bordas arredondadas.
     */
    private JButton createCustomButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(backgroundColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(backgroundColor.darker(), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}
