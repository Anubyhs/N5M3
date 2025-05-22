package cadastroclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.SwingUtilities;

public class CadastroClientV2 {

    private static volatile boolean isRunning = true;

    // Códigos ANSI para cores
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BOLD = "\u001B[1m";

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 4321);
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
             BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("CLIENTE: Conectado ao servidor.");

            // Leitura do login e senha
            System.out.print("Digite o login: ");
            String login = reader.readLine();
            System.out.print("Digite a senha: ");
            String senha = reader.readLine();

            // Enviar o login e a senha para o servidor.
            outputStream.writeObject(login);
            outputStream.writeObject(senha);
            System.out.println("CLIENTE: Login e senha enviados.");

            // Esperar a resposta de autenticação do servidor
            String authResponse = (String) inputStream.readObject();
            if (authResponse.startsWith("ERRO:")) {
                System.out.println(ANSI_YELLOW + authResponse + ANSI_RESET);
                System.exit(0); // Sai se o login for inválido
            } else {
                System.out.println(ANSI_GREEN + authResponse + ANSI_RESET);
            }

            // Instancia a janela de mensagem
            SaidaFrame saidaFrame = new SaidaFrame(login);
            SwingUtilities.invokeLater(() -> saidaFrame.setVisible(true));

            // Instanciar a Thread para comunicação com o servidor.
            ThreadClient threadClient = new ThreadClient(inputStream, saidaFrame, login);
            threadClient.start();
            System.out.println("CLIENTE: ThreadClient iniciada.");

            while (isRunning) {
                // Menu estilizado
                System.out.println("\n=== MENU DE OPÇÕES ===");
                System.out.println("[C] Cadastrar/Atualizar Produto");
                System.out.println("[L] Listar Produtos");
                System.out.println("[E] Entrada de Produtos");
                System.out.println("[S] Saída de Produtos");
                System.out.println("[ME] Movimentações de Entrada");
                System.out.println("[MS] Movimentações de Saída");
                System.out.println("[X] Finalizar");
                System.out.print("\nEscolha uma opção: ");
                
                String comando = reader.readLine();
                System.out.println("CLIENTE: Comando digitado pelo usuário: '" + comando + "'");

                // Processamento do comando
                switch (comando.toUpperCase()) {
                    case "C": {
                        outputStream.writeObject("C");
                        System.out.println("CLIENTE: Enviado comando 'C' para o servidor.");
                        
                        outputStream.writeObject(0);
                        
                        System.out.print("Digite o nome do produto: ");
                        String nome = reader.readLine();
                        outputStream.writeObject(nome);
                        
                        System.out.print("Digite a quantidade: ");
                        int quantidade = Integer.parseInt(reader.readLine());
                        outputStream.writeObject(quantidade);
                        
                        System.out.print("Digite o preço unitário: ");
                        float precoUnitario = Float.parseFloat(reader.readLine());
                        outputStream.writeObject(precoUnitario);
                        break;
                    }
                    case "L": {
                        outputStream.writeObject("L");
                        System.out.println("CLIENTE: Enviado comando 'L' para o servidor.");
                        break;
                    }
                    case "E": {
                        outputStream.writeObject("E");
                        System.out.println("CLIENTE: Enviado comando 'E' para o servidor.");
                        
                        System.out.print("Digite o ID da pessoa: ");
                        int pessoaId = Integer.parseInt(reader.readLine());
                        outputStream.writeObject(pessoaId);
                        
                        System.out.print("Digite o ID do produto: ");
                        int produtoId = Integer.parseInt(reader.readLine());
                        outputStream.writeObject(produtoId);
                        
                        System.out.print("Digite a quantidade: ");
                        int quantidade = Integer.parseInt(reader.readLine());
                        outputStream.writeObject(quantidade);
                        
                        System.out.print("Digite o valor unitário: ");
                        float valorUnitario = Float.parseFloat(reader.readLine());
                        outputStream.writeObject(valorUnitario);
                        break;
                    }
                    case "S": {
                        outputStream.writeObject("S");
                        System.out.println("CLIENTE: Enviado comando 'S' para o servidor.");
                        
                        System.out.print("Digite o ID da pessoa: ");
                        int pessoaId = Integer.parseInt(reader.readLine());
                        outputStream.writeObject(pessoaId);
                        
                        System.out.print("Digite o ID do produto: ");
                        int produtoId = Integer.parseInt(reader.readLine());
                        outputStream.writeObject(produtoId);
                        
                        System.out.print("Digite a quantidade: ");
                        int quantidade = Integer.parseInt(reader.readLine());
                        outputStream.writeObject(quantidade);
                        
                        System.out.print("Digite o valor unitário: ");
                        float valorUnitario = Float.parseFloat(reader.readLine());
                        outputStream.writeObject(valorUnitario);
                        break;
                    }
                    case "ME": {
                        outputStream.writeObject("ME");
                        System.out.println("CLIENTE: Enviado comando 'ME' para o servidor.");
                        break;
                    }
                    case "MS": {
                        outputStream.writeObject("MS");
                        System.out.println("CLIENTE: Enviado comando 'MS' para o servidor.");
                        break;
                    }
                    case "X": {
                        outputStream.writeObject("X");
                        System.out.println("CLIENTE: Enviado comando 'X' para o servidor. Finalizando...");
                        isRunning = false;
                        break;
                    }
                    default:
                        System.out.println("Comando inválido. Por favor, tente novamente.");
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("CLIENTE: Erro de I/O ou conexão: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("CLIENTE: Classe não encontrada ao deserializar objeto: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("CLIENTE: Aplicação cliente finalizada.");
            System.exit(0);
        }
    }
}