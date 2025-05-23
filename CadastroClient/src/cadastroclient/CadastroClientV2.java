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

    // Codigos ANSI para cores
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

            // Esperar a resposta de autenticacao do servidor
            String authResponse = (String) inputStream.readObject();
            if (authResponse.startsWith("ERRO:")) {
                System.out.println(ANSI_YELLOW + authResponse + ANSI_RESET);
                System.exit(0); // Sai se o login for invalido
            } else {
                System.out.println(ANSI_GREEN + authResponse + ANSI_RESET);
            }

            // Instancia a janela de mensagem
            SaidaFrame saidaFrame = new SaidaFrame(login);
            SwingUtilities.invokeLater(() -> saidaFrame.setVisible(true));

            // Instanciar a Thread para comunicacao com o servidor.
            ThreadClient threadClient = new ThreadClient(inputStream, saidaFrame, login);
            threadClient.start();
            System.out.println("CLIENTE: ThreadClient iniciada.");

            while (isRunning) {
                // Menu estilizado
                System.out.println("\n=== MENU DE OPCOES DA LOJA DE FRANCINALDO ===");
                System.out.println("[L] Listar Produtos");
                System.out.println("[E] Entrada de Produtos");
                System.out.println("[S] Saida de Produtos");
                System.out.println("[X] Finalizar");
                System.out.print("\nEscolha uma opcao: ");
                
                String comando = reader.readLine();
                System.out.println("CLIENTE: Comando digitado pelo usuario: '" + comando + "'");

                // Processamento do comando
                switch (comando.toUpperCase()) {
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
                        
                        System.out.print("Digite o valor unitario: ");
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
                        
                        System.out.print("Digite o valor unitario: ");
                        float valorUnitario = Float.parseFloat(reader.readLine());
                        outputStream.writeObject(valorUnitario);
                        break;
                    }
                    case "X": {
                        outputStream.writeObject("X");
                        System.out.println("CLIENTE: Enviado comando 'X' para o servidor. Finalizando...");
                        isRunning = false;
                        break;
                    }
                    default:
                        System.out.println("Comando invalido. Por favor, tente novamente.");
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("CLIENTE: Erro de I/O ou conexao: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("CLIENTE: Classe nao encontrada ao deserializar objeto: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("CLIENTE: Aplicacao cliente finalizada.");
            System.exit(0);
        }
    }
}