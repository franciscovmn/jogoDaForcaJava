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
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;

/**
 * Interface gráfica (GUI) do Jogo da Velha.
 * Gerencia a interação do usuário e a exibição do jogo.
 * @author [Felipe Antonio Ramalho Macedo - 20232370036]
 * @author [Francisco Viana Maia Neto - 20232370011]
 */
public class TelaJogo {

    // --- ATRIBUTOS (COMPONENTES SWING E CONTROLE) --- //

    private JFrame frmJogoDaVelha; // Janela principal.
    private JogoDaVelha jogo; // Objeto com a lógica do jogo.
    private JLabel[] labelsTabuleiro = new JLabel[9]; // Células do tabuleiro.
    
    // Controles de configuração da partida.
    private JComboBox<String> comboBoxSimboloP1;
    private JComboBox<String> comboBoxSimboloP2;
    private JComboBox<String> comboBoxModoJogo;
    private JComboBox<String> comboBoxNivelMaquina;
    
    // Botões de ação.
    private JButton btnIniciarReiniciar;
    private JButton btnHistoricoPartidas;
    
    // Labels de informação.
    private JLabel lblStatus; // Exibe o status atual (vez do jogador, vencedor, etc.).
    private JLabel lblJogadas; // Exibe a contagem de jogadas.

    // Controle de estado da UI.
    private List<String> historicoResultadosPartidas = new ArrayList<>(); // Armazena resultados da sessão.
    private boolean isMaquinaJogando = false; // Flag para bloquear input do usuário.

    /**
     * Ponto de entrada da aplicação.
     * @param args Argumentos da linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        // Garante que a UI seja construída na thread de eventos do Swing.
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
     * Construtor. Inicia a criação da UI.
     */
    public TelaJogo() {
        initialize(); // Monta os componentes.
        configurarEstadoInicialControles(); // Define o estado inicial da tela.
    }

    /**
     * Cria e organiza todos os componentes visuais na janela.
     */
    private void initialize() {
        // Configuração da janela principal.
        frmJogoDaVelha = new JFrame();
        frmJogoDaVelha.setTitle("Jogo da Velha");
        frmJogoDaVelha.setBounds(100, 100, 628, 600);
        frmJogoDaVelha.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmJogoDaVelha.getContentPane().setLayout(new BorderLayout(10, 10));

        // Painel superior para os controles de configuração.
        JPanel painelControles = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        comboBoxSimboloP1 = new JComboBox<>(new String[]{"X", "O"});
        painelControles.add(new JLabel("P1:"));
        painelControles.add(comboBoxSimboloP1);

        comboBoxSimboloP2 = new JComboBox<>(new String[]{"O", "X"});
        painelControles.add(new JLabel("P2:"));
        painelControles.add(comboBoxSimboloP2);
        
        comboBoxModoJogo = new JComboBox<>(new String[]{"Jogador vs Jogador", "Jogador vs Máquina"});
        comboBoxModoJogo.addActionListener(e -> atualizarVisibilidadeControlesModoJogo()); // Listener para alterar a UI.
        painelControles.add(new JLabel("Modo:"));
        painelControles.add(comboBoxModoJogo);

        comboBoxNivelMaquina = new JComboBox<>(new String[]{"Fácil (1)", "Difícil (2)"});
        painelControles.add(new JLabel("Nível:"));
        painelControles.add(comboBoxNivelMaquina);
        
        frmJogoDaVelha.getContentPane().add(painelControles, BorderLayout.NORTH);

        // Painel central para o tabuleiro 3x3.
        JPanel painelTabuleiro = new JPanel(new GridLayout(3, 3, 5, 5));
        // Laço para criar as 9 células do tabuleiro.
        for (int i = 0; i < 9; i++) {
            labelsTabuleiro[i] = new JLabel("", SwingConstants.CENTER);
            labelsTabuleiro[i].setFont(new Font("Arial", Font.BOLD, 40));
            labelsTabuleiro[i].setOpaque(true);
            labelsTabuleiro[i].setBackground(Color.WHITE);
            labelsTabuleiro[i].setBorder(BorderFactory.createLineBorder(Color.GRAY));
            
            final int posicao = i; // Variável final para uso no listener.
            // Adiciona um listener de clique a cada célula.
            labelsTabuleiro[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    labelTabuleiroClicado(posicao); // Trata a jogada.
                }
            });
            painelTabuleiro.add(labelsTabuleiro[i]);
        }
        frmJogoDaVelha.getContentPane().add(painelTabuleiro, BorderLayout.CENTER);

        // Painel inferior para status e botões de ação.
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

    // Reseta a UI para o estado pré-jogo.
    private void configurarEstadoInicialControles() {
        // Limpa o tabuleiro visualmente.
        for (JLabel lbl : labelsTabuleiro) {
            lbl.setText("");
            lbl.setBackground(Color.WHITE);
        }
        // Libera os controles de configuração.
        comboBoxSimboloP1.setEnabled(true);
        comboBoxModoJogo.setEnabled(true);
        atualizarVisibilidadeControlesModoJogo();
        btnIniciarReiniciar.setText("Iniciar Jogo");
        lblStatus.setText("Configure o jogo e clique em Iniciar.");
        lblJogadas.setText("Jogadas: 0");
    }

    // Habilita/desabilita controles com base no modo de jogo selecionado.
    private void atualizarVisibilidadeControlesModoJogo() {
        String modoSelecionado = (String) comboBoxModoJogo.getSelectedItem();
        boolean modoVsJogador = "Jogador vs Jogador".equals(modoSelecionado);
        
        comboBoxSimboloP2.setEnabled(modoVsJogador);
        comboBoxNivelMaquina.setEnabled(!modoVsJogador);
    }
    
    // Ação do botão "Iniciar Jogo" / "Reiniciar Jogo".
    private void acaoIniciarReiniciarJogo() {
        try {
            // Lê as configurações da UI.
            String simboloP1 = (String) comboBoxSimboloP1.getSelectedItem();
            String modoSelecionado = (String) comboBoxModoJogo.getSelectedItem();
            
            // Cria a instância de JogoDaVelha de acordo com o modo.
            if ("Jogador vs Máquina".equals(modoSelecionado)) {
                String nivelSelecionado = (String) comboBoxNivelMaquina.getSelectedItem();
                int nivelMaquina = "Difícil (2)".equals(nivelSelecionado) ? 2 : 1;
                jogo = new JogoDaVelha(simboloP1, nivelMaquina);
            } else { // Jogador vs Jogador
                String simboloP2 = (String) comboBoxSimboloP2.getSelectedItem();
                // Validação de símbolos.
                if (simboloP1.equals(simboloP2)) {
                    JOptionPane.showMessageDialog(frmJogoDaVelha, "Jogador 1 e Jogador 2 não podem ter o mesmo símbolo!", "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                jogo = new JogoDaVelha(simboloP1, simboloP2);
            }

            // Prepara a UI para a partida.
            configurarParaJogoEmAndamento();
            atualizarInterface();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(frmJogoDaVelha, ex.getMessage(), "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
            jogo = null;
            configurarEstadoInicialControles();
        }
    }

    // "Trava" os controles de configuração durante uma partida.
    private void configurarParaJogoEmAndamento() {
        for (JLabel lbl : labelsTabuleiro) {
            lbl.setText(""); // Limpa o tabuleiro.
        }
        // Desabilita os menus.
        comboBoxSimboloP1.setEnabled(false);
        comboBoxSimboloP2.setEnabled(false);
        comboBoxModoJogo.setEnabled(false);
        comboBoxNivelMaquina.setEnabled(false);
        btnIniciarReiniciar.setText("Reiniciar Jogo");
    }

    /**
     * Trata o clique do usuário em uma célula do tabuleiro.
     * @param posicao Posição clicada (0-8).
     */
    private void labelTabuleiroClicado(int posicao) {
        // Impede a jogada se o jogo não estiver ativo.
        if (jogo == null || jogo.terminou() || isMaquinaJogando || !labelsTabuleiro[posicao].getText().isEmpty()) {
            return;
        }

        try {
            // Envia a jogada para a classe de lógica.
            jogo.jogaJogador(jogo.getJogadorAtual(), posicao);
            // Atualiza a tela para refletir a jogada.
            atualizarInterface();

            // Se for a vez da máquina, chama sua jogada.
            if (!jogo.terminou() && jogo.isModoVsMaquina() && jogo.getJogadorAtual() == 2) {
                fazerJogadaMaquina();
            }

        } catch (IllegalStateException | IllegalArgumentException ex) {
            // Exibe aviso de jogada inválida (ex: célula ocupada).
            // JOptionPane.showMessageDialog(frmJogoDaVelha, ex.getMessage(), "Jogada Inválida", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    // Controla a jogada da máquina com um delay para simular "pensamento".
    private void fazerJogadaMaquina() {
        if (jogo == null || jogo.terminou() || !jogo.isModoVsMaquina() || jogo.getJogadorAtual() != 2) {
            return;
        }

        isMaquinaJogando = true; // Bloqueia input do usuário.
        lblStatus.setText("Máquina (" + jogo.getSimbolo(2) + ") está pensando...");
        
        // Timer para criar um atraso de 1 segundo.
        Timer timer = new Timer(1000, e -> {
            try {
                jogo.jogaMaquina(); // Lógica da jogada da máquina.
            } finally {
                isMaquinaJogando = false; // Desbloqueia input.
                atualizarInterface(); // Atualiza a tela.
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Sincroniza a UI com o estado atual do objeto 'jogo'.
     * Chamado após cada evento importante (jogada, início, fim).
     */
    private void atualizarInterface() {
        if (jogo == null) {
            return;
        }

        // Atualiza o texto de cada célula do tabuleiro.
        String[] celulas = jogo.getCelulas();
        for (int i = 0; i < 9; i++) {
            labelsTabuleiro[i].setText(celulas[i]);
        }

        // Atualiza o contador de jogadas.
        lblJogadas.setText("Jogadas: " + jogo.getQuantidadeJogadas());

        // Verifica se o jogo terminou.
        if (jogo.terminou()) {
            // Se sim, exibe o resultado final.
            String statusFinal = "";
            int resultado = jogo.getResultado();
            if (resultado == 0) {
                statusFinal = "Empate!";
            } else if (resultado == 1) {
                statusFinal = "Jogador " + jogo.getSimbolo(1) + " venceu!";
            } else if (resultado == 2) {
                statusFinal = ((jogo.isModoVsMaquina()) ? "Máquina " : "Jogador ") + jogo.getSimbolo(2) + " venceu!";
            }
            lblStatus.setText(statusFinal);
            historicoResultadosPartidas.add(statusFinal + " (Jogadas: " + jogo.getQuantidadeJogadas() + ")");
            configurarControlesParaFimDeJogo();
        } else {
            // Se não, exibe de quem é a vez.
            if(!isMaquinaJogando) {
                lblStatus.setText("Vez do " + (jogo.isModoVsMaquina() && jogo.getJogadorAtual() == 2 ? "Máquina" : "Jogador " + jogo.getJogadorAtual()) + " (" + jogo.getSimboloJogadorAtual() + ")");
            }
        }
    }
    
    // Libera os controles para que um novo jogo possa ser iniciado.
    private void configurarControlesParaFimDeJogo() {
        comboBoxSimboloP1.setEnabled(true);
        comboBoxModoJogo.setEnabled(true);
        atualizarVisibilidadeControlesModoJogo();
        btnIniciarReiniciar.setText("Iniciar Novo Jogo");
    }

    /**
     * Exibe uma janela com o histórico de resultados da sessão.
     */
    private void mostrarHistoricoPartidas() {
        if (historicoResultadosPartidas.isEmpty()) {
            JOptionPane.showMessageDialog(frmJogoDaVelha, "Nenhuma partida foi jogada ainda nesta sessão.", "Histórico de Partidas", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Constrói o texto do histórico.
        StringBuilder sb = new StringBuilder("Resultados das Partidas:\n\n");
        for (int i = 0; i < historicoResultadosPartidas.size(); i++) {
            sb.append((i + 1)).append(". ").append(historicoResultadosPartidas.get(i)).append("\n");
        }

        // Cria uma área de texto com rolagem para exibir o histórico.
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(350, 200));

        // Mostra o histórico em um JOptionPane.
        JOptionPane.showMessageDialog(frmJogoDaVelha, scrollPane, "Histórico de Partidas", JOptionPane.PLAIN_MESSAGE);
    }
}