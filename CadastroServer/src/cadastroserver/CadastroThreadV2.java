package cadastroserver;

import controller.MovimentoJpaController;
import controller.PessoaJpaController;
import controller.ProdutoJpaController;
import controller.UsuarioJpaController;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
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

            System.out.println("SERVIDOR: Conexão estabelecida para nova thread.");

            String login = (String) in.readObject();
            String senha = (String) in.readObject();
            System.out.println("SERVIDOR: Recebido login: " + login + " e senha: " + senha);


            List<Usuario> usuarios = ctrlUsuario.findUsuario(login, senha);
            if (usuarios.isEmpty()) {
                System.out.println("SERVIDOR: Login inválido para " + login);
                out.writeObject("ERRO: Usuário ou senha inválidos."); // *** CORREÇÃO: Envia erro para o cliente ***
                s1.close(); // Fecha o socket após enviar o erro
                return;
            }
            Usuario usuario = usuarios.get(0);
            System.out.println("SERVIDOR: Usuário " + usuario.getLogin() + " autenticado com sucesso.");
            out.writeObject("OK: Usuário " + usuario.getLogin() + " conectado."); // Envia confirmação de login

            boolean isRunning = true;
            while (isRunning) {
                try {
                    System.out.println("SERVIDOR: Aguardando próximo comando...");
                    String comando = ((String) in.readObject()).toUpperCase(); // Esta é a LINHA 57 (ou próxima) do seu stack trace original
                    System.out.println("SERVIDOR: Recebeu comando: '" + comando + "'");

                    switch (comando) {
                        case "C": {
                            int idProduto = (int) in.readObject();
                            String nome = (String) in.readObject();
                            int quantidade = (int) in.readObject();
                            float precoUnitario = (float) in.readObject();
                            
                            // Busca todos os produtos para verificar se já existe um com o mesmo nome
                            List<Produto> produtos = ctrlProduto.findProdutoEntities();
                            Produto produtoExistente = produtos.stream()
                                .filter(p -> p.getNome().equalsIgnoreCase(nome))
                                .findFirst()
                                .orElse(null);
                            
                            if (produtoExistente != null) {
                                // Se encontrou um produto com o mesmo nome, atualiza ele
                                produtoExistente.setQuantidade(quantidade);
                                produtoExistente.setPrecoVenda(new BigDecimal(precoUnitario));
                                ctrlProduto.edit(produtoExistente);
                                out.writeObject("Produto atualizado com sucesso! (ID: " + produtoExistente.getIdProduto() + ")");
                            } else {
                                // Se não encontrou, cria um novo produto
                                Produto novoProduto = new Produto();
                                novoProduto.setNome(nome);
                                novoProduto.setQuantidade(quantidade);
                                novoProduto.setPrecoVenda(new BigDecimal(precoUnitario));
                                ctrlProduto.create(novoProduto);
                                out.writeObject("Novo produto cadastrado com sucesso! (ID: " + novoProduto.getIdProduto() + ")");
                            }
                            break;
                        }
                        case "L": {
                            List<Produto> produtos = ctrlProduto.findProdutoEntities();
                            out.writeObject(produtos);
                            System.out.println("SERVIDOR: Enviou lista de produtos para o cliente.");
                            break;
                        }
                        case "ME": {
                            List<Movimento> movimentos = ctrlMov.findMovimentoEntities();
                            List<Movimento> entradas = movimentos.stream()
                                .filter(m -> m.getTipo() == 'E')
                                .collect(Collectors.toList());
                            out.writeObject(entradas);
                            System.out.println("SERVIDOR: Enviou lista de movimentações de entrada.");
                            break;
                        }
                        case "MS": {
                            List<Movimento> movimentos = ctrlMov.findMovimentoEntities();
                            List<Movimento> saidas = movimentos.stream()
                                .filter(m -> m.getTipo() == 'S')
                                .collect(Collectors.toList());
                            out.writeObject(saidas);
                            System.out.println("SERVIDOR: Enviou lista de movimentações de saída.");
                            break;
                        }
                        case "E": {
                            int idPessoa = (int) in.readObject();
                            int idProduto = (int) in.readObject();
                            int quantidade = (int) in.readObject();
                            float valorUnitario = (float) in.readObject();
                            System.out.println("SERVIDOR: Recebido dados para Entrada: Pessoa ID=" + idPessoa + ", Produto ID=" + idProduto + ", Qtd=" + quantidade + ", Valor Uni=" + valorUnitario);

                            Pessoa pessoa = ctrlPessoa.findPessoa(idPessoa);
                            Produto produto = ctrlProduto.findProduto(idProduto);

                            if (produto == null) {
                                out.writeObject("Produto Inexistente");
                                System.out.println("SERVIDOR: Erro - Produto Inexistente para Entrada.");
                                break;
                            }
                            if (pessoa == null) {
                                out.writeObject("Pessoa Inexistente");
                                System.out.println("SERVIDOR: Erro - Pessoa Inexistente para Entrada.");
                                break;
                            }

                            if (quantidade <= 0) {
                                out.writeObject("Quantidade para entrada deve ser positiva.");
                                System.out.println("SERVIDOR: Erro - Quantidade para entrada não positiva.");
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
                            movimento.setDataMovimento(LocalDateTime.now());
                            ctrlMov.create(movimento);
                            
                            List<Produto> produtosAtualizados = ctrlProduto.findProdutoEntities();
                            out.writeObject(produtosAtualizados); // Envia a lista atualizada
                            System.out.println("SERVIDOR: Movimento de Entrada registrado e lista de produtos atualizada enviada.");
                            break;
                        }

                        case "S": {
                            int idPessoa = (int) in.readObject();
                            int idProduto = (int) in.readObject();
                            int quantidade = (int) in.readObject();
                            float valorUnitario = (float) in.readObject();
                            System.out.println("SERVIDOR: Recebido dados para Saída: Pessoa ID=" + idPessoa + ", Produto ID=" + idProduto + ", Qtd=" + quantidade + ", Valor Uni=" + valorUnitario);


                            Pessoa pessoa = ctrlPessoa.findPessoa(idPessoa);
                            Produto produto = ctrlProduto.findProduto(idProduto);

                            if (produto == null) {
                                out.writeObject("Produto Inexistente");
                                System.out.println("SERVIDOR: Erro - Produto Inexistente para Saída.");
                                break;
                            }
                            if (pessoa == null) {
                                out.writeObject("Pessoa Inexistente");
                                System.out.println("SERVIDOR: Erro - Pessoa Inexistente para Saída.");
                                break;
                            }

                            if (quantidade <= 0) {
                                out.writeObject("Quantidade para saída deve ser positiva.");
                                System.out.println("SERVIDOR: Erro - Quantidade para saída não positiva.");
                                break;
                            }

                            if (produto.getQuantidade() < quantidade) {
                                out.writeObject("Quantidade insuficiente em estoque!");
                                System.out.println("SERVIDOR: Erro - Quantidade insuficiente em estoque para " + produto.getNome());
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
                            movimento.setDataMovimento(LocalDateTime.now());
                            ctrlMov.create(movimento);

                            List<Produto> produtosAtualizados = ctrlProduto.findProdutoEntities();
                            out.writeObject(produtosAtualizados); // Envia a lista atualizada
                            System.out.println("SERVIDOR: Movimento de Saída registrado e lista de produtos atualizada enviada.");
                            break;
                        }

                        case "X": {
                            System.out.println("SERVIDOR: Recebeu comando 'X'. Encerrando conexão para este cliente.");
                            isRunning = false; // Define para sair do loop
                            break;
                        }

                        default:
                            System.out.println("SERVIDOR: Comando inválido: " + comando);
                            out.writeObject("ERRO: Comando inválido '" + comando + "'.");
                            break;
                    }
                } catch (EOFException eof) {
                    System.out.println("SERVIDOR: Conexão encerrada pelo cliente (EOF).");
                    isRunning = false;
                } catch (IOException e) {
                    System.err.println("SERVIDOR: Erro de I/O na thread: " + e.getMessage());
                    // e.printStackTrace(); // Descomente para ver o stack trace completo completo
                    isRunning = false;
                } catch (ClassNotFoundException e) {
                    System.err.println("SERVIDOR: Erro de ClassNotFound na thread: " + e.getMessage());
                    e.printStackTrace();
                    isRunning = false;
                } catch (Exception e) { // Captura qualquer outra exceção inesperada
                    System.err.println("SERVIDOR: Erro inesperado na thread: " + e.getMessage());
                    e.printStackTrace();
                    isRunning = false;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("SERVIDOR: Erro inicial de I/O ou ClassNotFound (login/senha): " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (s1 != null && !s1.isClosed()) {
                    s1.close();
                    System.out.println("SERVIDOR: Socket fechado para este cliente.");
                }
            } catch (IOException e) {
                System.err.println("SERVIDOR: Erro ao fechar o socket: " + e.getMessage());
                e.printStackTrace();
            }
            System.out.println("SERVIDOR: Thread de conexão do cliente finalizada.");
        }
    }
}