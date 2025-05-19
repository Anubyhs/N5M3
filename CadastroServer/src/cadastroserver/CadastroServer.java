package cadastroserver;


import controller.MovimentoJpaController;
import controller.PessoaJpaController;
import controller.ProdutoJpaController;
import controller.UsuarioJpaController;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


public class CadastroServer {

    // Códigos ANSI para cores
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BOLD = "\u001B[1m";

    public static void main(String[] args) throws IOException {
        // Exibe uma mensagem de boas-vindas ao iniciar o servidor
        System.out.println(ANSI_BLUE + "╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                                  ║");
        System.out.println("║    " + ANSI_BLUE + ANSI_BOLD + "BEM-VINDO AO CADASTROSERVER FRANCINALDO!" + ANSI_RESET + ANSI_BLUE + "                      ║");
        System.out.println("║                                                                  ║");
        System.out.println("║    " + ANSI_BLUE + ANSI_BOLD + "POR FAVOR, AGUARDE A CONEXAO..." + ANSI_RESET + ANSI_BLUE + "                               ║");
        System.out.println("║                                                                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝" + ANSI_RESET);

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("CadastroServerPU");

        ProdutoJpaController ctrlProduto = new ProdutoJpaController(emf);
        UsuarioJpaController ctrlUsuario = new UsuarioJpaController(emf);
        MovimentoJpaController ctrlMov = new MovimentoJpaController(emf);
        PessoaJpaController ctrlPessoa = new PessoaJpaController(emf);

        ServerSocket s1 = new ServerSocket(4321);

        while (true) {
            Socket s2 = s1.accept();

            CadastroThreadV2 t1 = new CadastroThreadV2(ctrlProduto, ctrlUsuario, s2, ctrlMov, ctrlPessoa);
            t1.start();
        }
    }
}
