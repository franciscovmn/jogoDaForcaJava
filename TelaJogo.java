package projeto;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * Interface gráfica (GUI) do Jogo da Velha.
 * Gerencia a interação do usuário e a exibição do jogo.
 * @author [Felipe Antonio Ramalho Macedo - 20232370036]
 * @author [Francisco Viana Maia Neto - 20232370011]
 */
public class TelaJogo {

    // --- ATRIBUTOS (COMPONENTES SWING) --- //
    private JFrame frmJogoDaVelha;
    private JogoDaVelha jogo;
    private JLabel[] labelsTabuleiro = new JLabel[9];
    private JComboBox<String> comboBoxSimboloP1, comboBoxSimboloP2, comboBoxModoJogo, comboBoxNivelMaquina;
    private JButton btnIniciarReiniciar, btnHistoricoPartidas;
    private JLabel lblStatus, lblJogadas;

    // --- CONTROLE DE ESTADO DA UI --- //
    private List<PartidaCompleta> historicoDePartidas = new ArrayList<>();
    private boolean isMaquinaJogando = false;
    private boolean modoVsMaquina = false;
    private int jogadorAtual = 1;
    private int totalJogadas = 0;

    /**
     * Classe interna para armazenar os detalhes de uma partida finalizada.
     */
    private static class PartidaCompleta {
        private final String resultado;
        private final LinkedHashMap<Integer, String> jogadas;
        private static int proximoId = 1;
        private final int id;

        PartidaCompleta(String resultado, LinkedHashMap<Integer, String> jogadas) {
            this.id = proximoId++;
            this.resultado = resultado;
            this.jogadas = jogadas;
        }

        public LinkedHashMap<Integer, String> getJogadas() {
            return jogadas;
        }

        @Override
        public String toString() {
            return "Partida " + id + ": " + resultado;
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                TelaJogo window = new TelaJogo();
                window.frmJogoDaVelha.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public TelaJogo() {
        initialize();
        configurarEstadoInicialControles();
    }

    private void initialize() {
        frmJogoDaVelha = new JFrame("Jogo da Velha");
        frmJogoDaVelha.setBounds(100, 100, 628, 600);
        frmJogoDaVelha.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmJogoDaVelha.getContentPane().setLayout(new BorderLayout(10, 10));

        JPanel painelControles = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        painelControles.add(new JLabel("P1:"));
        comboBoxSimboloP1 = new JComboBox<>(new String[]{"X", "O"});
        painelControles.add(comboBoxSimboloP1);
        painelControles.add(new JLabel("P2:"));
        comboBoxSimboloP2 = new JComboBox<>(new String[]{"O", "X"});
        painelControles.add(comboBoxSimboloP2);
        painelControles.add(new JLabel("Modo:"));
        comboBoxModoJogo = new JComboBox<>(new String[]{"Jogador vs Jogador", "Jogador vs Máquina"});
        comboBoxModoJogo.addActionListener(e -> atualizarVisibilidadeControlesModoJogo());
        painelControles.add(comboBoxModoJogo);
        painelControles.add(new JLabel("Nível:"));
        comboBoxNivelMaquina = new JComboBox<>(new String[]{"Fácil (1)", "Difícil (2)"});
        painelControles.add(comboBoxNivelMaquina);
        frmJogoDaVelha.getContentPane().add(painelControles, BorderLayout.NORTH);

        JPanel painelTabuleiro = new JPanel(new GridLayout(3, 3, 5, 5));
        for (int i = 0; i < 9; i++) {
            labelsTabuleiro[i] = new JLabel("", SwingConstants.CENTER);
            labelsTabuleiro[i].setFont(new Font("Arial", Font.BOLD, 40));
            labelsTabuleiro[i].setOpaque(true);
            labelsTabuleiro[i].setBackground(Color.WHITE);
            labelsTabuleiro[i].setBorder(BorderFactory.createLineBorder(Color.GRAY));
            final int posicao = i;
            labelsTabuleiro[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    labelTabuleiroClicado(posicao);
                }
            });
            painelTabuleiro.add(labelsTabuleiro[i]);
        }
        frmJogoDaVelha.getContentPane().add(painelTabuleiro, BorderLayout.CENTER);

        JPanel painelStatusAcoes = new JPanel(new BorderLayout(10, 10));
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
        
        // --- ALTERAÇÃO AQUI: Botão de histórico começa desabilitado ---
        btnHistoricoPartidas.setEnabled(false);
        
        painelBotoesAcao.add(btnHistoricoPartidas);
        painelStatusAcoes.add(painelBotoesAcao, BorderLayout.SOUTH);
        frmJogoDaVelha.getContentPane().add(painelStatusAcoes, BorderLayout.SOUTH);
    }

    private void configurarEstadoInicialControles() {
        limparTabuleiroVisual();
        comboBoxSimboloP1.setEnabled(true);
        comboBoxModoJogo.setEnabled(true);
        atualizarVisibilidadeControlesModoJogo();
        btnIniciarReiniciar.setText("Iniciar Jogo");
        lblStatus.setText("Configure o jogo e clique em Iniciar.");
        lblJogadas.setText("Jogadas: 0");
        jogo = null;
    }

    private void atualizarVisibilidadeControlesModoJogo() {
        boolean vsJogador = "Jogador vs Jogador".equals(comboBoxModoJogo.getSelectedItem());
        comboBoxSimboloP2.setEnabled(vsJogador);
        comboBoxNivelMaquina.setEnabled(!vsJogador);
    }

    private void acaoIniciarReiniciarJogo() {
        try {
            String simboloP1 = (String) comboBoxSimboloP1.getSelectedItem();
            String modoSelecionado = (String) comboBoxModoJogo.getSelectedItem();
            this.modoVsMaquina = "Jogador vs Máquina".equals(modoSelecionado);

            if (modoVsMaquina) {
                int nivel = "Difícil (2)".equals(comboBoxNivelMaquina.getSelectedItem()) ? 2 : 1;
                jogo = new JogoDaVelha(simboloP1, nivel);
            } else {
                String simboloP2 = (String) comboBoxSimboloP2.getSelectedItem();
                jogo = new JogoDaVelha(simboloP1, simboloP2);
            }
            configurarParaJogoEmAndamento();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(frmJogoDaVelha, ex.getMessage(), "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
            configurarEstadoInicialControles();
        }
    }

    private void configurarParaJogoEmAndamento() {
        limparTabuleiroVisual();
        this.jogadorAtual = 1;
        this.totalJogadas = 0;
        comboBoxSimboloP1.setEnabled(false);
        comboBoxSimboloP2.setEnabled(false);
        comboBoxModoJogo.setEnabled(false);
        comboBoxNivelMaquina.setEnabled(false);
        btnIniciarReiniciar.setText("Reiniciar Jogo");
        atualizarInterface();
    }

    private void limparTabuleiroVisual() {
        for (JLabel lbl : labelsTabuleiro) {
            lbl.setText("");
            lbl.setBackground(Color.WHITE);
        }
    }

    private void labelTabuleiroClicado(int posicao) {
        if (jogo == null || jogo.terminou() || isMaquinaJogando || !labelsTabuleiro[posicao].getText().isEmpty()) {
            return;
        }

        try {
            jogo.jogaJogador(this.jogadorAtual, posicao);
            this.totalJogadas++;
            this.jogadorAtual = (this.jogadorAtual == 1) ? 2 : 1;
            atualizarInterface();

            if (!jogo.terminou() && modoVsMaquina && this.jogadorAtual == 2) {
                fazerJogadaMaquina();
            }
        } catch (Exception ex) {
            // A lógica de validação na classe JogoDaVelha já impede a maioria dos erros.
        }
    }

    private void fazerJogadaMaquina() {
        isMaquinaJogando = true;
        lblStatus.setText("Máquina (" + jogo.getSimbolo(2) + ") está pensando...");

        Timer timer = new Timer(1000, e -> {
            try {
                jogo.jogaMaquina();
                this.totalJogadas++;
                this.jogadorAtual = 1;
            } finally {
                isMaquinaJogando = false;
                atualizarInterface();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void atualizarInterface() {
        if (jogo == null) return;
        
        String foto = jogo.getFoto();
        String[] linhas = foto.split("\n-----\n");
        String[] celulas = new String[9];
        int k = 0;
        for (String linha : linhas) {
            String[] partes = linha.split(" \\| ");
            for (String parte : partes) {
                if (k < celulas.length) {
                    celulas[k++] = parte.trim();
                }
            }
        }
        for (int i = 0; i < 9; i++) {
            labelsTabuleiro[i].setText(celulas[i]);
        }

        lblJogadas.setText("Jogadas: " + this.totalJogadas);

        if (jogo.terminou()) {
            String statusFinal;
            int resultado = jogo.getResultado();
            if (resultado == 0) {
                statusFinal = "Empate!";
            } else {
                String simboloVencedor = jogo.getSimbolo(resultado);
                String nomeVencedor = (modoVsMaquina && resultado == 2) ? "Máquina" : "Jogador " + resultado;
                statusFinal = nomeVencedor + " (" + simboloVencedor + ") venceu!";
            }
            lblStatus.setText(statusFinal);
            
            historicoDePartidas.add(new PartidaCompleta(statusFinal + " em " + this.totalJogadas + " jogadas", jogo.getHistorico()));
            
            // --- ALTERAÇÃO AQUI: Habilita o botão de histórico após a primeira partida ---
            if (!btnHistoricoPartidas.isEnabled()) {
                btnHistoricoPartidas.setEnabled(true);
            }
            
            configurarControlesParaFimDeJogo();
        } else if (!isMaquinaJogando) {
            String nomeJogador = (modoVsMaquina && jogadorAtual == 2) ? "Máquina" : "Jogador " + jogadorAtual;
            lblStatus.setText("Vez do " + nomeJogador + " (" + jogo.getSimbolo(jogadorAtual) + ")");
        }
    }

    private void configurarControlesParaFimDeJogo() {
        comboBoxSimboloP1.setEnabled(true);
        comboBoxModoJogo.setEnabled(true);
        atualizarVisibilidadeControlesModoJogo();
        btnIniciarReiniciar.setText("Iniciar Novo Jogo");
    }

    // --- ALTERAÇÃO AQUI: Lógica do histórico refeita com JDialog ---
    private void mostrarHistoricoPartidas() {
        JDialog dialogoHistorico = new JDialog(frmJogoDaVelha, "Histórico de Partidas", true);
        dialogoHistorico.setSize(400, 300);
        dialogoHistorico.setLocationRelativeTo(frmJogoDaVelha);
        dialogoHistorico.setLayout(new BorderLayout(10, 10));

        DefaultListModel<PartidaCompleta> listModel = new DefaultListModel<>();
        historicoDePartidas.forEach(listModel::addElement);

        JList<PartidaCompleta> listaPartidas = new JList<>(listModel);
        listaPartidas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(listaPartidas);
        
        JButton btnVerJogadas = new JButton("Ver Jogadas");
        JButton btnFechar = new JButton("Fechar");
        
        btnVerJogadas.setEnabled(false); // Começa desabilitado

        listaPartidas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnVerJogadas.setEnabled(!listaPartidas.isSelectionEmpty());
            }
        });

        btnVerJogadas.addActionListener(e -> {
            PartidaCompleta partidaSelecionada = listaPartidas.getSelectedValue();
            dialogoHistorico.dispose(); // Fecha o diálogo atual antes de abrir o próximo
            exibirDetalhesDaPartida(partidaSelecionada);
        });

        btnFechar.addActionListener(e -> dialogoHistorico.dispose());

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelBotoes.add(btnVerJogadas);
        painelBotoes.add(btnFechar);

        dialogoHistorico.add(scrollPane, BorderLayout.CENTER);
        dialogoHistorico.add(painelBotoes, BorderLayout.SOUTH);
        
        dialogoHistorico.setVisible(true);
    }

    private void exibirDetalhesDaPartida(PartidaCompleta partida) {
        StringBuilder detalhes = new StringBuilder();
        detalhes.append("Detalhes da ").append(partida.toString()).append("\n\n");
        
        int nJogada = 1;
        for (Map.Entry<Integer, String> jogada : partida.getJogadas().entrySet()) {
            detalhes.append("Jogada ").append(nJogada++).append(": ");
            detalhes.append("Símbolo '").append(jogada.getValue()).append("' ");
            detalhes.append("na Posição ").append(jogada.getKey()).append("\n");
        }

        JTextArea textArea = new JTextArea(detalhes.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(350, 200));
        
        Object[] options = {"Voltar ao Histórico", "Fechar"};
        int result = JOptionPane.showOptionDialog(frmJogoDaVelha, 
                                                scrollPane, 
                                                "Detalhes da Partida",
                                                JOptionPane.DEFAULT_OPTION, 
                                                JOptionPane.INFORMATION_MESSAGE,
                                                null, 
                                                options, 
                                                options[1]); 

        if (result == 0) {
            mostrarHistoricoPartidas(); 
        }
    }
}