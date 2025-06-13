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
 * Gerencia toda a interação com o usuário e a exibição do estado do jogo.
 * @author [Felipe Antonio Ramalho Macedo - 20232370036]
 * @author [Francisco Viana Maia Neto - 20232370011]
 */
public class TelaJogo {

    // --- ATRIBUTOS (COMPONENTES SWING E CONTROLE DE UI) --- //
    private JFrame frmJogoDaVelha;
    private JogoDaVelha jogo; // A instância da nossa classe de lógica.
    // Array de labels que funcionam como as 9 células do tabuleiro na tela.
    private JLabel[] labelsTabuleiro = new JLabel[9];
    // ComboBoxes para o usuário escolher os símbolos e o modo de jogo.
    private JComboBox<String> comboBoxSimboloP1, comboBoxSimboloP2, comboBoxModoJogo, comboBoxNivelMaquina;
    // Botões para iniciar/reiniciar o jogo e ver o histórico.
    private JButton btnIniciarReiniciar, btnHistoricoPartidas;
    // Labels para mostrar informações como o status atual e o total de jogadas.
    private JLabel lblStatus, lblJogadas;

    // Lista para guardar os dados de todas as partidas jogadas nesta sessão.
    private List<PartidaCompleta> historicoDePartidas = new ArrayList<>();
    // Flags para controlar o estado da interface.
    private boolean isMaquinaJogando = false; // Evita que o jogador clique enquanto a máquina "pensa".
    private boolean modoVsMaquina = false;
    private int jogadorAtual = 1;
    private int totalJogadas = 0;

    /**
     * Classe interna para encapsular os dados de uma partida finalizada.
     * Facilita a exibição no histórico.
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

        public LinkedHashMap<Integer, String> getJogadas() { return jogadas; }

        // O método toString é usado para exibir a partida de forma amigável na lista do histórico.
        @Override
        public String toString() {
            return "Partida " + id + ": " + resultado;
        }
    }

    /**
     * Ponto de entrada da aplicação. Cria e exibe a janela do jogo.
     */
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

    /**
     * Construtor da tela. Chama a inicialização dos componentes.
     */
    public TelaJogo() {
        initialize();
        configurarEstadoInicialControles();
    }

    /**
     * Monta todos os componentes visuais da janela (painéis, botões, labels).
     */
    private void initialize() {
        // Configuração geral da janela principal.
        frmJogoDaVelha = new JFrame("Jogo da Velha");
        frmJogoDaVelha.setBounds(100, 100, 628, 600);
        frmJogoDaVelha.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmJogoDaVelha.getContentPane().setLayout(new BorderLayout(10, 10));

        // Painel superior com as opções de configuração do jogo.
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

        // Painel central com o tabuleiro 3x3.
        JPanel painelTabuleiro = new JPanel(new GridLayout(3, 3, 5, 5));
        for (int i = 0; i < 9; i++) {
            labelsTabuleiro[i] = new JLabel("", SwingConstants.CENTER);
            labelsTabuleiro[i].setFont(new Font("Arial", Font.BOLD, 40));
            labelsTabuleiro[i].setOpaque(true);
            labelsTabuleiro[i].setBackground(Color.WHITE);
            labelsTabuleiro[i].setBorder(BorderFactory.createLineBorder(Color.GRAY));
            final int posicao = i; // Variável final para ser usada dentro do listener.
            // Adiciona um "ouvinte" de clique a cada célula do tabuleiro.
            labelsTabuleiro[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    labelTabuleiroClicado(posicao);
                }
            });
            painelTabuleiro.add(labelsTabuleiro[i]);
        }
        frmJogoDaVelha.getContentPane().add(painelTabuleiro, BorderLayout.CENTER);

        // Painel inferior para exibir status e botões de ação.
        JPanel painelStatusAcoes = new JPanel(new BorderLayout(10, 10));
        JPanel painelInfo = new JPanel(new FlowLayout(FlowLayout.CENTER));
        lblStatus = new JLabel("Configure o jogo e clique em Iniciar.");
        lblJogadas = new JLabel("Jogadas: 0");
        painelInfo.add(lblStatus);
        painelInfo.add(lblJogadas);
        painelStatusAcoes.add(painelInfo, BorderLayout.NORTH);
        
        JPanel painelBotoesAcao = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnIniciarReiniciar = new JButton("Iniciar Jogo");
        btnIniciarReiniciar.addActionListener(e -> acaoIniciarReiniciarJogo());
        painelBotoesAcao.add(btnIniciarReiniciar);
        
        btnHistoricoPartidas = new JButton("Histórico de Partidas");
        btnHistoricoPartidas.addActionListener(e -> mostrarHistoricoPartidas());
        btnHistoricoPartidas.setEnabled(false); // O botão só é habilitado após a primeira partida.
        painelBotoesAcao.add(btnHistoricoPartidas);
        
        painelStatusAcoes.add(painelBotoesAcao, BorderLayout.SOUTH);
        frmJogoDaVelha.getContentPane().add(painelStatusAcoes, BorderLayout.SOUTH);
    }

    /**
     * Reseta a interface para o estado inicial, antes de um jogo começar.
     */
    private void configurarEstadoInicialControles() {
        limparTabuleiroVisual();
        comboBoxSimboloP1.setEnabled(true);
        comboBoxModoJogo.setEnabled(true);
        atualizarVisibilidadeControlesModoJogo();
        btnIniciarReiniciar.setText("Iniciar Jogo");
        lblStatus.setText("Configure o jogo e clique em Iniciar.");
        lblJogadas.setText("Jogadas: 0");
        jogo = null; // A instância do jogo é descartada.
    }
    
    /**
     * Habilita/desabilita os controles de P2 e Nível da Máquina dependendo do modo de jogo.
     */
    private void atualizarVisibilidadeControlesModoJogo() {
        boolean vsJogador = "Jogador vs Jogador".equals(comboBoxModoJogo.getSelectedItem());
        comboBoxSimboloP2.setEnabled(vsJogador);
        comboBoxNivelMaquina.setEnabled(!vsJogador);
    }

    /**
     * Ação do botão "Iniciar/Reiniciar". Cria uma nova instância de JogoDaVelha.
     */
    private void acaoIniciarReiniciarJogo() {
        try {
            String simboloP1 = (String) comboBoxSimboloP1.getSelectedItem();
            this.modoVsMaquina = "Jogador vs Máquina".equals(comboBoxModoJogo.getSelectedItem());

            // Cria o objeto 'jogo' com o construtor apropriado, conforme o modo.
            if (modoVsMaquina) {
                int nivel = "Difícil (2)".equals(comboBoxNivelMaquina.getSelectedItem()) ? 2 : 1;
                jogo = new JogoDaVelha(simboloP1, nivel);
            } else {
                String simboloP2 = (String) comboBoxSimboloP2.getSelectedItem();
                jogo = new JogoDaVelha(simboloP1, simboloP2);
            }
            configurarParaJogoEmAndamento();
        } catch (IllegalArgumentException ex) {
            // Captura erros de configuração (ex: símbolos iguais) e exibe uma mensagem.
            JOptionPane.showMessageDialog(frmJogoDaVelha, ex.getMessage(), "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
            configurarEstadoInicialControles();
        }
    }

    /**
     * Configura a interface para um jogo que está acontecendo (trava configurações).
     */
    private void configurarParaJogoEmAndamento() {
        limparTabuleiroVisual();
        this.jogadorAtual = 1;
        this.totalJogadas = 0;
        // Desabilita as opções de configuração durante a partida.
        comboBoxSimboloP1.setEnabled(false);
        comboBoxSimboloP2.setEnabled(false);
        comboBoxModoJogo.setEnabled(false);
        comboBoxNivelMaquina.setEnabled(false);
        btnIniciarReiniciar.setText("Reiniciar Jogo");
        atualizarInterface();
    }

    /**
     * Limpa os símbolos do tabuleiro visualmente.
     */
    private void limparTabuleiroVisual() {
        for (JLabel lbl : labelsTabuleiro) {
            lbl.setText("");
            lbl.setBackground(Color.WHITE);
        }
    }

    /**
     * Chamado quando uma célula do tabuleiro é clicada.
     * @param posicao A posição (0-8) que foi clicada.
     */
    private void labelTabuleiroClicado(int posicao) {
        // Ignora o clique se o jogo não começou, já terminou, ou se a máquina está jogando.
        if (jogo == null || jogo.terminou() || isMaquinaJogando || !labelsTabuleiro[posicao].getText().isEmpty()) {
            return;
        }

        try {
            // Manda a jogada para a classe de lógica.
            jogo.jogaJogador(this.jogadorAtual, posicao);
            this.totalJogadas++;
            this.jogadorAtual = (this.jogadorAtual == 1) ? 2 : 1; // Troca o turno.
            atualizarInterface();

            // Se for a vez da máquina, chama a jogada dela.
            if (!jogo.terminou() && modoVsMaquina && this.jogadorAtual == 2) {
                fazerJogadaMaquina();
            }
        } catch (Exception ex) {
            // Normalmente não acontece, pois a UI já previne cliques inválidos.
        }
    }

    /**
     * Orquestra a jogada da máquina com um pequeno delay para simular que ela está "pensando".
     */
    private void fazerJogadaMaquina() {
        isMaquinaJogando = true;
        lblStatus.setText("Máquina (" + jogo.getSimbolo(2) + ") está pensando...");

        // Usamos um Timer para dar um efeito visual e não travar a interface.
        Timer timer = new Timer(1000, e -> { // Delay de 1 segundo (1000 ms).
            try {
                jogo.jogaMaquina(); // Chama a lógica da máquina.
                this.totalJogadas++;
                this.jogadorAtual = 1; // Volta o turno para o jogador humano.
            } finally {
                isMaquinaJogando = false;
                atualizarInterface(); // Atualiza a tela após a jogada da máquina.
            }
        });
        timer.setRepeats(false); // O timer executa apenas uma vez.
        timer.start();
    }

    /**
     * Sincroniza a interface gráfica com o estado atual do objeto 'jogo'.
     */
    private void atualizarInterface() {
        if (jogo == null) return;
        
        // Pega a "foto" do tabuleiro da classe de lógica e atualiza os labels.
        String foto = jogo.getFoto();
        String[] celulasFlat = foto.replace("\n-----\n", " | ").split(" \\| ");
        for (int i = 0; i < 9; i++) {
            labelsTabuleiro[i].setText(celulasFlat[i].trim());
        }

        lblJogadas.setText("Jogadas: " + this.totalJogadas);

        // Verifica se o jogo terminou para exibir o resultado.
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
            
            // Salva a partida concluída no nosso histórico.
            historicoDePartidas.add(new PartidaCompleta(statusFinal + " em " + this.totalJogadas + " jogadas", jogo.getHistorico()));
            
            // Habilita o botão de histórico se ele ainda não estiver.
            if (!btnHistoricoPartidas.isEnabled()) {
                btnHistoricoPartidas.setEnabled(true);
            }
            
            configurarControlesParaFimDeJogo();
        } else if (!isMaquinaJogando) {
            // Se o jogo continua, atualiza o status para mostrar de quem é a vez.
            String nomeJogador = (modoVsMaquina && jogadorAtual == 2) ? "Máquina" : "Jogador " + jogadorAtual;
            lblStatus.setText("Vez do " + nomeJogador + " (" + jogo.getSimbolo(jogadorAtual) + ")");
        }
    }

    /**
     * Libera os controles de configuração para que um novo jogo possa ser iniciado.
     */
    private void configurarControlesParaFimDeJogo() {
        comboBoxSimboloP1.setEnabled(true);
        comboBoxModoJogo.setEnabled(true);
        atualizarVisibilidadeControlesModoJogo();
        btnIniciarReiniciar.setText("Iniciar Novo Jogo");
    }

    /**
     * Abre uma nova janela (JDialog) para mostrar a lista de partidas jogadas.
     */
    private void mostrarHistoricoPartidas() {
        JDialog dialogoHistorico = new JDialog(frmJogoDaVelha, "Histórico de Partidas", true);
        dialogoHistorico.setSize(400, 300);
        dialogoHistorico.setLocationRelativeTo(frmJogoDaVelha); // Centraliza na janela principal.
        dialogoHistorico.setLayout(new BorderLayout(10, 10));

        // Usa um JList para exibir a lista de partidas.
        DefaultListModel<PartidaCompleta> listModel = new DefaultListModel<>();
        historicoDePartidas.forEach(listModel::addElement);

        JList<PartidaCompleta> listaPartidas = new JList<>(listModel);
        listaPartidas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(listaPartidas);
        
        JButton btnVerJogadas = new JButton("Ver Jogadas");
        btnVerJogadas.setEnabled(false); // Só habilita quando um item é selecionado.

        listaPartidas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnVerJogadas.setEnabled(!listaPartidas.isSelectionEmpty());
            }
        });

        btnVerJogadas.addActionListener(e -> {
            PartidaCompleta partidaSelecionada = listaPartidas.getSelectedValue();
            dialogoHistorico.dispose(); // Fecha a lista para mostrar os detalhes.
            exibirDetalhesDaPartida(partidaSelecionada);
        });

        JButton btnFechar = new JButton("Fechar");
        btnFechar.addActionListener(e -> dialogoHistorico.dispose());

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelBotoes.add(btnVerJogadas);
        painelBotoes.add(btnFechar);

        dialogoHistorico.add(scrollPane, BorderLayout.CENTER);
        dialogoHistorico.add(painelBotoes, BorderLayout.SOUTH);
        
        dialogoHistorico.setVisible(true);
    }

    /**
     * Mostra os detalhes de uma partida específica (jogada a jogada) em um JOptionPane.
     * @param partida A partida selecionada na tela de histórico.
     */
    private void exibirDetalhesDaPartida(PartidaCompleta partida) {
        StringBuilder detalhes = new StringBuilder();
        detalhes.append("Detalhes da ").append(partida.toString()).append("\n\n");
        
        int nJogada = 1;
        // Percorre o mapa de histórico da partida para formatar a exibição.
        for (Map.Entry<Integer, String> jogada : partida.getJogadas().entrySet()) {
            detalhes.append("Jogada ").append(nJogada++).append(": ");
            detalhes.append("Símbolo '").append(jogada.getValue()).append("' ");
            detalhes.append("na Posição ").append(jogada.getKey()).append("\n");
        }

        // Usa um JTextArea dentro de um JScrollPane para o caso de o histórico ser longo.
        JTextArea textArea = new JTextArea(detalhes.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        
        // Oferece a opção de voltar para a lista ou fechar tudo.
        Object[] options = {"Voltar ao Histórico", "Fechar"};
        int result = JOptionPane.showOptionDialog(frmJogoDaVelha, scrollPane, "Detalhes da Partida",
                                                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                                                null, options, options[1]); 

        if (result == 0) {
            mostrarHistoricoPartidas(); // Reabre a tela de histórico.
        }
    }
}