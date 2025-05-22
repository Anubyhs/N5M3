package cadastroclient;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.net.SocketException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Produto;
import model.Movimento;


public class ThreadClient extends Thread {

    private ObjectInputStream entrada;
    private SaidaFrame saidaFrame;
    private Map<String, BigDecimal> valoresTotaisAnteriores;
    private String usuario;

    public ThreadClient(ObjectInputStream entrada, SaidaFrame saidaFrame, String usuario) {
        this.entrada = entrada;
        this.saidaFrame = saidaFrame;
        this.valoresTotaisAnteriores = new HashMap<>();
        this.usuario = usuario;
    }

    @Override
    public void run() {
        try {
            while (true) {
                try {
                    // Recebe os dados enviados pelo servidor
                    Object obj = entrada.readObject();

                    // Se o objeto for do tipo String, apenas adiciona ao JTextArea
                    if (obj instanceof String) {
                        String mensagem = (String) obj;
                        // Atualiza a interface gráfica na thread de eventos do Swing
                        EventQueue.invokeLater(() -> {
                            saidaFrame.adicionarMensagem(mensagem);
                        });
                    } // Se o objeto for do tipo List, verifica o tipo de lista
                    else if (obj instanceof List) {
                        List<?> lista = (List<?>) obj;
                        if (!lista.isEmpty() && lista.get(0) instanceof Produto) {
                            List<Produto> produtos = (List<Produto>) lista;
                            // Atualiza a interface gráfica na thread de eventos do Swing
                            EventQueue.invokeLater(() -> {
                                // Exibe o cabeçalho da lista de produtos
                                saidaFrame.adicionarMensagem("\n");
                                saidaFrame.adicionarMensagem("LISTA DE PRODUTOS");
                                saidaFrame.adicionarMensagem("==============================================================================================================");
                                saidaFrame.adicionarMensagem(String.format("%-5s | %-30s | %-15s | %-20s | %-20s",
                                        "ID", "Nome do Produto", "Quantidade", "Valor Unitário", "Valor Total"));
                                saidaFrame.adicionarMensagem("==============================================================================================================");

                                // Para cada produto na lista
                                for (Produto produto : produtos) {
                                    String valorUnitarioFormatado = formatarValor(produto.getPrecoVenda());
                                    BigDecimal valorTotal = produto.getPrecoVenda().multiply(new BigDecimal(produto.getQuantidade()));
                                    String valorTotalFormatado = formatarValor(valorTotal);

                                    // Formata a linha do produto com colunas alinhadas
                                    // Ajustando as larguras e usando alinhamento à direita para valores numéricos
                                    String linha = String.format("%-5d | %-30s | %10d | %15s | %15s",
                                            produto.getIdProduto(),
                                            produto.getNome(),
                                            produto.getQuantidade(),
                                            valorUnitarioFormatado,
                                            valorTotalFormatado);
                                    
                                    saidaFrame.adicionarMensagem(linha);
                                }
                                saidaFrame.adicionarMensagem("==============================================================================================================");
                            });
                        } else if (!lista.isEmpty() && lista.get(0) instanceof Movimento) {
                            List<Movimento> movimentos = (List<Movimento>) lista;
                            // Atualiza a interface gráfica na thread de eventos do Swing
                            EventQueue.invokeLater(() -> {
                                // Exibe o cabeçalho da lista de movimentações
                                saidaFrame.adicionarMensagem("\n");
                                saidaFrame.adicionarMensagem("MOVIMENTAÇÕES");
                                saidaFrame.adicionarMensagem("=======================================================================================================================================");
                                saidaFrame.adicionarMensagem(String.format("%-5s | %-20s | %-10s | %-12s | %-15s | %-15s | %-20s", 
                                    "ID", "Data/Hora", "Tipo", "Quantidade", "Valor Unitário", "Valor Total", "Usuário"));
                                saidaFrame.adicionarMensagem("=======================================================================================================================================");

                                // Para cada movimentação na lista
                                for (Movimento movimento : movimentos) {
                                    String tipo = movimento.getTipo() == 'E' ? "ENTRADA" : "SAÍDA";
                                    String dataHora = (movimento.getDataMovimento() != null) ? movimento.getDataMovimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
                                    String usuarioMovimento = (movimento.getUsuario() != null) ? movimento.getUsuario().getLogin() : "";

                                    BigDecimal quantidadeBD = new BigDecimal(movimento.getQuantidade());
                                    BigDecimal valorUnitarioBD = new BigDecimal(movimento.getValorUnitario());
                                    BigDecimal valorTotalBD = quantidadeBD.multiply(valorUnitarioBD);
                                    String valorTotalFormatado = formatarValor(valorTotalBD);
                                    String valorUnitarioFormatado = formatarValor(valorUnitarioBD);

                                    saidaFrame.adicionarMensagem(String.format("%-5d | %-20s | %-10s | %-12d | %-15s | %-15s | %-20s",
                                            movimento.getIdMovimento(),
                                            dataHora,
                                            tipo,
                                            movimento.getQuantidade(),
                                            valorUnitarioFormatado,
                                            valorTotalFormatado,
                                            usuarioMovimento));
                                }
                                saidaFrame.adicionarMensagem("=======================================================================================================================================");
                            });
                        }
                    }
                } catch (SocketException se) {
                    // Socket foi fechado, termina o loop
                    System.out.println("Conexão encerrada: " + se.getMessage());
                    break;
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    break;
                }
            }
        } finally {
            try {
                if (entrada != null) {
                    entrada.close(); // Fecha o ObjectInputStream
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Método auxiliar para formatar o valor como R$
    private String formatarValor(BigDecimal valor) {
        NumberFormat formatador = NumberFormat.getCurrencyInstance();
        return formatador.format(valor);
    }
}
