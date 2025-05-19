package cadastroserver;

import controller.MovimentoJpaController;
import controller.PessoaJpaController;
import controller.ProdutoJpaController;
import controller.UsuarioJpaController;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import model.Movimento;
import model.Pessoa;
import model.Produto;
import model.Usuario;

public class CadastroThreadV2 extends Thread {

    private ProdutoJpaController ctrlProduto;
    private UsuarioJpaController ctrlUsuario;
    private Socket s1;
    private MovimentoJpaController ctrlMov;
    private PessoaJpaController ctrlPessoa;

    public CadastroThreadV2(ProdutoJpaController ctrlProduto, UsuarioJpaController ctrlUsuario, Socket s1, MovimentoJpaController ctrlMov, PessoaJpaController ctrlPessoa) {
        this.ctrlProduto = ctrlProduto;
        this.ctrlUsuario = ctrlUsuario;
        this.s1 = s1;
        this.ctrlMov = ctrlMov;
        this.ctrlPessoa = ctrlPessoa;
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(s1.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(s1.getOutputStream())) {

            String login = (String) in.readObject();
            String senha = (String) in.readObject();

            List<Usuario> usuarios = ctrlUsuario.findUsuario(login, senha);
            if (usuarios.isEmpty()) {
                System.out.println("Usuario invalido");
                s1.close();
                return;
            }
            // Se houver múltiplos usuários com o mesmo login/senha, você precisará
            // implementar uma lógica para determinar qual usuário prosseguir.
            // Por exemplo, você pode pegar o primeiro da lista:
            Usuario usuario = usuarios.get(0);
            System.out.println("Usuario valido");

            boolean isRunning = true;
            while (isRunning) {
                try {
                    String comando = ((String) in.readObject()).toUpperCase();
                    switch (comando) {
                        case "E": {
                            int idPessoa = (int) in.readObject();
                            int idProduto = (int) in.readObject();
                            int quantidade = (int) in.readObject();
                            float valorUnitario = (float) in.readObject();

                            Pessoa pessoa = ctrlPessoa.findPessoa(idPessoa);
                            Produto produto = ctrlProduto.findProduto(idProduto);

                            if (produto == null) {
                                out.writeObject("Produto Inexistente");
                                break;
                            }

                            produto.setQuantidade(produto.getQuantidade() + quantidade);
                            ctrlProduto.edit(produto);

                            Movimento movimento = new Movimento();
                            movimento.setUsuario(usuario);
                            movimento.setTipo('E');
                            movimento.setPessoa(pessoa);
                            movimento.setProduto(produto);
                            movimento.setQuantidade(quantidade);
                            movimento.setValorUnitario(valorUnitario);
                            ctrlMov.create(movimento);
                            break;
                        }

                        case "S": {
                            int idPessoa = (int) in.readObject();
                            int idProduto = (int) in.readObject();
                            int quantidade = (int) in.readObject();
                            float valorUnitario = (float) in.readObject();

                            Pessoa pessoa = ctrlPessoa.findPessoa(idPessoa);
                            Produto produto = ctrlProduto.findProduto(idProduto);

                            if (produto == null) {
                                out.writeObject("Produto Inexistente");
                                break;
                            }

                            if (produto.getQuantidade() < quantidade) {
                                out.writeObject("Quantidade insuficiente em estoque!");
                                break;
                            }

                            produto.setQuantidade(produto.getQuantidade() - quantidade);
                            ctrlProduto.edit(produto);

                            Movimento movimento = new Movimento();
                            movimento.setUsuario(usuario);
                            movimento.setTipo('S');
                            movimento.setPessoa(pessoa);
                            movimento.setProduto(produto);
                            movimento.setQuantidade(quantidade);
                            movimento.setValorUnitario(valorUnitario);
                            ctrlMov.create(movimento);
                            break;
                        }

                        case "L": {
                            List<Produto> produtos = ctrlProduto.findProdutoEntities();
                            out.writeObject(produtos);
                            break;
                        }

                        case "X": {
                            isRunning = false;
                            break;
                        }

                        default:
                            System.out.println("Comando invalido: " + comando);
                            break;
                    }
                } catch (EOFException eof) {
                    System.out.println("Conexao encerrada pelo cliente.");
                    isRunning = false;
                } catch (IOException e) {
                    e.printStackTrace();
                    isRunning = false;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    isRunning = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    isRunning = false;
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
    }
}