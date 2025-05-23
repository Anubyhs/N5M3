/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package cadastroserver;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import model.Produto;
import model.Usuario;
import controller.ProdutoJpaController;
import controller.UsuarioJpaController;

public class CadastroThread extends Thread {

    private ProdutoJpaController ctrlProduto;
    private UsuarioJpaController ctrlUsuario;
    private Socket s1;

    public CadastroThread(ProdutoJpaController ctrlProduto, UsuarioJpaController ctrlUsuario, Socket s1) {
        this.ctrlProduto = ctrlProduto;
        this.ctrlUsuario = ctrlUsuario;
        this.s1 = s1;
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(s1.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(s1.getOutputStream())) {

            String login = (String) in.readObject();
            String senha = (String) in.readObject();

            List<Usuario> usuarios = ctrlUsuario.findUsuario(login, senha);
            Usuario usuario = null; // Inicializa usuario

            if (usuarios.isEmpty()) {
                System.out.println("Usuario invalido");
                out.writeObject("ERRO: Usuario invalido"); // Envia erro ao cliente
                s1.close();
                return;
            } else {
                // Se houver múltiplos usuários, você pode implementar uma lógica para escolher um.
                // Por enquanto, pegamos o primeiro da lista.
                usuario = usuarios.get(0);
                System.out.println("Usuario conectado com sucesso para: " + usuario.getLogin());
                out.writeObject("OK: Usuario conectado"); // Envia confirmação ao cliente
            }

            // Obter a data e hora atuais
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            System.out.println("Conexao estabelecida em: " + now.format(formatter));

            boolean conectado = true;
            while (conectado) {
                try {
                    String comando = (String) in.readObject();

                    if (comando.equalsIgnoreCase("L")) {
                        List<Produto> produtos = ctrlProduto.findProdutoEntities();
                        out.writeObject(produtos);
                    } else if (comando.equalsIgnoreCase("SAIR")) {
                        conectado = false;
                        out.writeObject("OK: Desconectado");
                    } else {
                        System.out.println("Comando invalido recebido: " + comando);
                        out.writeObject("ERRO: Comando invalido");
                    }
                } catch (EOFException e) {
                    System.out.println("Conexao encerrada pelo cliente.");
                    conectado = false;
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    conectado = false;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (!s1.isClosed()) {
                    s1.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Thread do cliente finalizada.");
    }
}