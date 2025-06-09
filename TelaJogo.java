package projeto;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;

/**
 * Representa a interface gráfica (GUI) para o Jogo da Velha.
 * Esta classe gerencia a interação do usuário, a exibição do tabuleiro,
 * as configurações do jogo e o histórico de partidas.
 */
public class TelaJogo {

    private JFrame frmJogoDaVelha;
    private JogoDaVelha jogo;
    private JButton[] botoesTabuleiro = new JButton[9];
    private JComboBox<String> comboBoxSimboloP1;
    private JComboBox<String> comboBoxSimboloP2;
    private JComboBox<String> comboBoxModoJogo;
    private JComboBox<String> comboBoxNivelMaquina;
    private JButton btnIniciarReiniciar;
    private JButton btnHistoricoPartidas;
    private JLabel lblStatus;
    private JLabel lblJogadas;

    private List<String> historicoResultadosPartidas = new ArrayList<>();

    /**
     * Ponto de entrada principal para a aplicação do Jogo da Velha.
     * Cria e exibe a janela do jogo.
     * @param args Argumentos da linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    TelaJogo window = new TelaJogo();
                    window.frmJogoDaVelha.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Construtor da classe TelaJogo.
     * Inicializa a interface gráfica e configura o estado inicial dos controles.
     */
    public TelaJogo() {
        initialize();
        configurarEstadoInicialControles();
    }

    /**
     * Inicializa o conteúdo do frame principal e seus componentes.
     */
    private void initialize() {
        frmJogoDaVelha = new JFrame();
        frmJogoDaVelha.setTitle("Jogo da Velha");
        frmJogoDaVelha.setBounds(100, 100, 628, 600);
        frmJogoDaVelha.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmJogoDaVelha.getContentPane().setLayout(new BorderLayout(10, 10));

        JPanel painelControles = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        comboBoxSimboloP1 = new JComboBox<>();
        comboBoxSimboloP1.setModel(new DefaultComboBoxModel<>(new String[]{"X", "O"}));
        painelControles.add(new JLabel("P1:"));
        painelControles.add(comboBoxSimboloP1);

        comboBoxSimboloP2 = new JComboBox<>();
        comboBoxSimboloP2.setModel(new DefaultComboBoxModel<>(new String[]{"O", "X"}));
        painelControles.add(new JLabel("P2:"));
        painelControles.add(comboBoxSimboloP2);
        
        comboBoxModoJogo = new JComboBox<>();
        comboBoxModoJogo.setModel(new DefaultComboBoxModel<>(new String[]{"Jogador vs Jogador", "Jogador vs Máquina"}));
        comboBoxModoJogo.addActionListener(e -> atualizarVisibilidadeControlesModoJogo());
        painelControles.add(new JLabel("Modo:"));
        painelControles.add(comboBoxModoJogo);

        comboBoxNivelMaquina = new JComboBox<>();
        comboBoxNivelMaquina.setModel(new DefaultComboBoxModel<>(new String[]{"Fácil (1)", "Difícil (2)"}));
        painelControles.add(new JLabel("Nível:"));
        painelControles.add(comboBoxNivelMaquina);
        
        frmJogoDaVelha.getContentPane().add(painelControles, BorderLayout.NORTH);

        JPanel painelTabuleiro = new JPanel(new GridLayout(3, 3, 5, 5));
        for (int i = 0; i < 9; i++) {
            botoesTabuleiro[i] = new JButton("");
            botoesTabuleiro[i].setFont(new Font("Arial", Font.BOLD, 40));
            final int posicao = i;
            botoesTabuleiro[i].addActionListener(e -> botaoTabuleiroClicado(posicao));
            painelTabuleiro.add(botoesTabuleiro[i]);
        }
        frmJogoDaVelha.getContentPane().add(painelTabuleiro, BorderLayout.CENTER);

        JPanel painelStatusAcoes = new JPanel(new BorderLayout(10,10));
        JPanel painelInfo = new JPanel(new FlowLayout(FlowLayout.CENTER));
        lblStatus = new JLabel("Configure o jogo e clique em Iniciar.");
        lblStatus.setFont(new Font("Tahoma", Font.PLAIN, 14));
        painelInfo.add(lblStatus);

        lblJogadas = new JLabel("Jogadas: 0");
        lblJogadas.setFont(new Font("Tahoma", Font.PLAIN, 12));
        painelInfo.add(lblJogadas);

        painelStatusAcoes.add(painelInfo, BorderLayout.NORTH);
        
        JPanel painelBotoesAcao = new JPanel(new FlowLayout(FlowLayout.CENTER));

        btnIniciarReiniciar = new JButton("Iniciar Jogo");
        btnIniciarReiniciar.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnIniciarReiniciar.addActionListener(e -> acaoIniciarReiniciarJogo());
        painelBotoesAcao.add(btnIniciarReiniciar);

        btnHistoricoPartidas = new JButton("Histórico de Partidas");
        btnHistoricoPartidas.setFont(new Font("Tahoma", Font.PLAIN, 12));
        btnHistoricoPartidas.addActionListener(e -> mostrarHistoricoPartidas());
        painelBotoesAcao.add(btnHistoricoPartidas);

        painelStatusAcoes.add(painelBotoesAcao, BorderLayout.SOUTH);

        frmJogoDaVelha.getContentPane().add(painelStatusAcoes, BorderLayout.SOUTH);
    }

    /**
     * Configura os controles da interface para o estado inicial, antes de um jogo começar
     * ou após o término de uma partida.
     */
    private void configurarEstadoInicialControles() {
        for (JButton btn : botoesTabuleiro) {
            btn.setEnabled(false);
            btn.setText("");
        }
        comboBoxSimboloP1.setEnabled(true);
        comboBoxModoJogo.setEnabled(true);
        atualizarVisibilidadeControlesModoJogo();
        btnIniciarReiniciar.setText("Iniciar Jogo");
        lblStatus.setText("Configure o jogo e clique em Iniciar.");
        lblJogadas.setText("Jogadas: 0");
    }

    /**
     * Atualiza a visibilidade e o estado dos controles (como ComboBox do símbolo P2 e nível da máquina)
     * com base no modo de jogo selecionado (Jogador vs Jogador ou Jogador vs Máquina).
     */
    private void atualizarVisibilidadeControlesModoJogo() {
        String modoSelecionado = (String) comboBoxModoJogo.getSelectedItem();
        boolean modoVsJogador = "Jogador vs Jogador".equals(modoSelecionado);
        
        comboBoxSimboloP2.setEnabled(modoVsJogador);
        
        if ("Jogador vs Máquina".equals(modoSelecionado)) {
            comboBoxNivelMaquina.setEnabled(true);
            if (comboBoxNivelMaquina.getItemCount() > 0) {
                comboBoxNivelMaquina.setSelectedItem("Fácil (1)");
            }
        } else {
            comboBoxNivelMaquina.setEnabled(false);
        }
    }
    
    /**
     * Define a ação a ser executada ao clicar no botão "Iniciar Jogo" ou "Reiniciar Jogo".
     * Configura uma nova instância do jogo com base nas seleções do usuário
     * (símbolos, modo de jogo, nível da máquina) e prepara a interface para o início da partida.
     * Trata exceções de configuração, como símbolos iguais para P1 e P2.
     */
    private void acaoIniciarReiniciarJogo() {
        try {
            String simboloP1 = (String) comboBoxSimboloP1.getSelectedItem();
            String modoSelecionado = (String) comboBoxModoJogo.getSelectedItem();
            int nivelMaquina = 1; // Padrão Fácil

            if ("Jogador vs Máquina".equals(modoSelecionado)) {
                String nivelSelecionado = (String) comboBoxNivelMaquina.getSelectedItem();
                if ("Difícil (2)".equals(nivelSelecionado)) {
                    nivelMaquina = 2;
                }
                jogo = new JogoDaVelha(simboloP1, nivelMaquina);
            } else { // Jogador vs Jogador
                String simboloP2 = (String) comboBoxSimboloP2.getSelectedItem();
                if (simboloP1.equals(simboloP2)) {
                    JOptionPane.showMessageDialog(frmJogoDaVelha, "Jogador 1 e Jogador 2 não podem ter o mesmo símbolo!", "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                jogo = new JogoDaVelha(simboloP1, simboloP2);
            }

            configurarParaJogoEmAndamento();
            atualizarInterface();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(frmJogoDaVelha, ex.getMessage(), "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
            jogo = null;
            configurarEstadoInicialControles();
        }
    }

    /**
     * Configura a interface para o estado de "jogo em andamento".
     * Habilita os botões do tabuleiro e desabilita os controles de configuração do jogo.
     * Altera o texto do botão principal para "Reiniciar Jogo".
     */
    private void configurarParaJogoEmAndamento() {
        for (JButton btn : botoesTabuleiro) {
            btn.setEnabled(true);
            btn.setText("");
        }
        comboBoxSimboloP1.setEnabled(false);
        comboBoxSimboloP2.setEnabled(false);
        comboBoxModoJogo.setEnabled(false);
        comboBoxNivelMaquina.setEnabled(false);
        btnIniciarReiniciar.setText("Reiniciar Jogo");
    }

    /**
     * Trata o evento de clique em um dos botões do tabuleiro.
     * Se a jogada for válida, registra-a no objeto {@link JogoDaVelha} e atualiza a interface.
     * Se for a vez da máquina jogar em um modo "Jogador vs Máquina", invoca a jogada da máquina.
     * @param posicao A posição (0-8) do botão clicado no tabuleiro.
     */
    private void botaoTabuleiroClicado(int posicao) {
        if (jogo == null || jogo.terminou() || (botoesTabuleiro[posicao].getText() != null && !botoesTabuleiro[posicao].getText().isEmpty()) ) {
            return;
        }

        try {
            int jogadorQueVaiJogar = jogo.getJogadorAtual();
            jogo.jogaJogador(jogadorQueVaiJogar, posicao);
            atualizarInterface();

            if (!jogo.terminou() && jogo.isModoVsMaquina() && jogo.getJogadorAtual() == 2) {
                fazerJogadaMaquina();
            }

        } catch (IllegalStateException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(frmJogoDaVelha, ex.getMessage(), "Jogada Inválida", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Controla a jogada da máquina no modo "Jogador vs Máquina".
     * Introduz um pequeno atraso (delay) para simular o "pensamento" da máquina
     * antes de efetuar a jogada. Atualiza a interface após a jogada.
     * Desabilita o tabuleiro durante o "pensamento" da máquina.
     */
    private void fazerJogadaMaquina() {
        if (jogo == null || jogo.terminou() || !jogo.isModoVsMaquina() || jogo.getJogadorAtual() != 2) {
            return;
        }

        lblStatus.setText("Máquina (" + jogo.getSimbolo(2) + ") está pensando...");
        for (JButton btn : botoesTabuleiro) {
            btn.setEnabled(false);
        }

        int delay = 1000; // 1 segundo
        Timer timer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    jogo.jogaMaquina();
                } catch (IllegalStateException | IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(frmJogoDaVelha, "Erro na jogada da máquina: " + ex.getMessage(), "Erro Máquina", JOptionPane.ERROR_MESSAGE);
                } finally {
                    atualizarInterface();
                }
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Atualiza todos os componentes da interface gráfica para refletir o estado atual do jogo.
     * Isso inclui os textos nos botões do tabuleiro, o status do jogo (vez do jogador, vencedor, empate),
     * a contagem de jogadas e a habilitação/desabilitação de controles.
     * Se o jogo terminar, adiciona o resultado ao histórico de partidas.
     */
    private void atualizarInterface() {
        if (jogo == null) {
            configurarEstadoInicialControles();
            return;
        }

        String[] celulas = jogo.getCelulas();
        for (int i = 0; i < 9; i++) {
            botoesTabuleiro[i].setText(celulas[i] == null ? "" : celulas[i]);
            botoesTabuleiro[i].setEnabled((celulas[i] == null || celulas[i].isEmpty()) && !jogo.terminou());
        }

        lblJogadas.setText("Jogadas: " + jogo.getQuantidadeJogadas());

        if (jogo.terminou()) {
            String statusFinal = "";
            int resultado = jogo.getResultado();
            if (resultado == 0) {
                statusFinal = "Empate!";
            } else if (resultado == 1) {
                statusFinal = "Jogador " + jogo.getSimbolo(1) + " venceu!";
            } else if (resultado == 2) {
                statusFinal = ((jogo.isModoVsMaquina()) ? "Máquina" : "Jogador ") + jogo.getSimbolo(2) + " venceu!";
            }
            lblStatus.setText(statusFinal);
            
            if (!statusFinal.isEmpty()) {
                historicoResultadosPartidas.add(statusFinal + " (Jogadas: " + jogo.getQuantidadeJogadas() + ")");
            }
            
            comboBoxSimboloP1.setEnabled(true);
            comboBoxModoJogo.setEnabled(true);
            atualizarVisibilidadeControlesModoJogo();
            btnIniciarReiniciar.setText("Iniciar Novo Jogo");
        } else {
            lblStatus.setText("Vez do " + (jogo.isModoVsMaquina() && jogo.getJogadorAtual() == 2 ? "Máquina" : "Jogador " + jogo.getJogadorAtual()) + " (" + jogo.getSimboloJogadorAtual() + ")");
        }
    }

    /**
     * Exibe uma caixa de diálogo mostrando o histórico de resultados das partidas da sessão atual.
     * Se não houver partidas jogadas, informa o usuário.
     * O histórico é apresentado em uma área de texto com rolagem.
     */
    private void mostrarHistoricoPartidas() {
        if (historicoResultadosPartidas.isEmpty()) {
            JOptionPane.showMessageDialog(frmJogoDaVelha, "Nenhuma partida foi jogada ainda nesta sessão.", "Histórico de Partidas", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder("Resultados das Partidas:\n\n");
        for (int i = 0; i < historicoResultadosPartidas.size(); i++) {
            sb.append((i + 1)).append(". ").append(historicoResultadosPartidas.get(i)).append("\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(350, 200));

        JOptionPane.showMessageDialog(frmJogoDaVelha, scrollPane, "Histórico de Partidas", JOptionPane.PLAIN_MESSAGE);
    }
}