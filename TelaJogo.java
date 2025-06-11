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
 * Representa a interface gráfica (GUI) para o Jogo da Velha.
 * Esta classe é a "cara" da nossa aplicação. Ela constrói a janela, o tabuleiro e os controles
 * que o usuário vê e com os quais interage. Sua principal função é capturar as ações do usuário
 * (como cliques) e se comunicar com a classe de lógica (JogoDaVelha) para executar o jogo.
 */
public class TelaJogo {

    // --- ATRIBUTOS DA CLASSE (Componentes Visuais e de Controle) --- //

    // Componentes visuais (Swing) que formam a nossa tela.
    private JFrame frmJogoDaVelha; // A janela principal da aplicação.
    private JogoDaVelha jogo; // A referência para o "cérebro" do jogo. A tela depende dele para funcionar.
    
    // Usamos um array de JLabels para o tabuleiro, conforme o requisito do projeto,
    // em vez de botões. Cada JLabel representa uma célula do tabuleiro.
    private JLabel[] labelsTabuleiro = new JLabel[9];
    
    // Menus dropdown para o usuário configurar a partida.
    private JComboBox<String> comboBoxSimboloP1;
    private JComboBox<String> comboBoxSimboloP2;
    private JComboBox<String> comboBoxModoJogo;
    private JComboBox<String> comboBoxNivelMaquina;
    
    // Botões para as ações principais do usuário.
    private JButton btnIniciarReiniciar;
    private JButton btnHistoricoPartidas;
    
    // Labels para mostrar informações importantes para o usuário.
    private JLabel lblStatus; // Informa de quem é a vez ou o resultado do jogo.
    private JLabel lblJogadas; // Mostra a contagem de jogadas.

    // Variáveis de controle do estado da interface.
    private List<String> historicoResultadosPartidas = new ArrayList<>(); // Guarda o resultado de cada partida da sessão.
    private boolean isMaquinaJogando = false; // Uma "flag" para impedir que o jogador clique enquanto a máquina "pensa".

    /**
     * Ponto de entrada principal da aplicação.
     * O método 'main' usa EventQueue.invokeLater para garantir que a interface gráfica
     * seja criada e atualizada na thread correta do Swing, evitando problemas de concorrência.
     * @param args Argumentos da linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    // Cria uma nova instância da nossa tela e a torna visível.
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
     * É aqui que a "mágica" começa. Ele chama os métodos responsáveis por
     * construir a interface e prepará-la para o usuário.
     */
    public TelaJogo() {
        initialize(); // Método que cria e posiciona todos os componentes na tela.
        configurarEstadoInicialControles(); // Método que ajusta a tela para o estado "pronto para começar".
    }

    /**
     * Inicializa e organiza todos os componentes visuais dentro da janela principal.
     * Podemos pensar neste método como o "arquiteto" da nossa tela.
     */
    private void initialize() {
        // 1. Configuração da janela principal (JFrame)
        frmJogoDaVelha = new JFrame();
        frmJogoDaVelha.setTitle("Jogo da Velha");
        frmJogoDaVelha.setBounds(100, 100, 628, 600); // Posição e tamanho da janela.
        frmJogoDaVelha.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Garante que o programa fecha ao clicar no "X".
        frmJogoDaVelha.getContentPane().setLayout(new BorderLayout(10, 10)); // Define o layout principal.

        // 2. Criação do painel superior com os controles de configuração
        JPanel painelControles = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Layout que alinha os componentes um ao lado do outro.
        // Adiciona os menus dropdown (JComboBox) para o usuário escolher as opções do jogo.
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
        // Adiciona uma "ação" que será disparada toda vez que o modo de jogo for alterado.
        comboBoxModoJogo.addActionListener(e -> atualizarVisibilidadeControlesModoJogo());
        painelControles.add(new JLabel("Modo:"));
        painelControles.add(comboBoxModoJogo);

        comboBoxNivelMaquina = new JComboBox<>();
        comboBoxNivelMaquina.setModel(new DefaultComboBoxModel<>(new String[]{"Fácil (1)", "Difícil (2)"}));
        painelControles.add(new JLabel("Nível:"));
        painelControles.add(comboBoxNivelMaquina);
        
        frmJogoDaVelha.getContentPane().add(painelControles, BorderLayout.NORTH); // Adiciona o painel de controles na parte de cima da janela.

        // 3. Criação do painel central com o tabuleiro do jogo
        JPanel painelTabuleiro = new JPanel(new GridLayout(3, 3, 5, 5)); // GridLayout é perfeito para um tabuleiro 3x3.
        // Este laço cria as 9 células do tabuleiro.
        for (int i = 0; i < 9; i++) {
            labelsTabuleiro[i] = new JLabel("", SwingConstants.CENTER); // Cria uma label vazia e centralizada.
            labelsTabuleiro[i].setFont(new Font("Arial", Font.BOLD, 40)); // Define a fonte para "X" e "O".
            labelsTabuleiro[i].setOpaque(true); // Permite que a cor de fundo seja visível.
            labelsTabuleiro[i].setBackground(Color.WHITE);
            labelsTabuleiro[i].setBorder(BorderFactory.createLineBorder(Color.GRAY)); // Desenha uma borda para delimitar a célula.
            
            final int posicao = i; // Variável 'final' para que possa ser usada dentro do listener abaixo.
            // Aqui, adicionamos um "ouvinte" de clique para cada célula do tabuleiro.
            // Quando o usuário clicar em uma label, o código dentro de 'mouseClicked' será executado.
            labelsTabuleiro[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    labelTabuleiroClicado(posicao); // Chama o método que trata a jogada do usuário.
                }
            });
            painelTabuleiro.add(labelsTabuleiro[i]); // Adiciona a célula criada ao painel do tabuleiro.
        }
        frmJogoDaVelha.getContentPane().add(painelTabuleiro, BorderLayout.CENTER); // Adiciona o tabuleiro no centro da janela.

        // 4. Criação do painel inferior para exibir status e botões de ação.
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
        // Adiciona a ação que será executada ao clicar no botão "Iniciar".
        btnIniciarReiniciar.addActionListener(e -> acaoIniciarReiniciarJogo());
        painelBotoesAcao.add(btnIniciarReiniciar);

        btnHistoricoPartidas = new JButton("Histórico de Partidas");
        btnHistoricoPartidas.setFont(new Font("Tahoma", Font.PLAIN, 12));
        btnHistoricoPartidas.addActionListener(e -> mostrarHistoricoPartidas());
        painelBotoesAcao.add(btnHistoricoPartidas);

        painelStatusAcoes.add(painelBotoesAcao, BorderLayout.SOUTH);

        frmJogoDaVelha.getContentPane().add(painelStatusAcoes, BorderLayout.SOUTH); // Adiciona o painel na parte de baixo da janela.
    }

    /**
     * Reseta a interface para o estado inicial, como se o programa tivesse acabado de abrir.
     * Isso limpa o tabuleiro e habilita os controles de configuração para uma nova partida.
     */
    private void configurarEstadoInicialControles() {
        // Limpa o texto e a cor de fundo de todas as células do tabuleiro.
        for (JLabel lbl : labelsTabuleiro) {
            lbl.setText("");
            lbl.setBackground(Color.WHITE);
        }
        // Habilita os controles para que o usuário possa configurar um novo jogo.
        comboBoxSimboloP1.setEnabled(true);
        comboBoxModoJogo.setEnabled(true);
        atualizarVisibilidadeControlesModoJogo(); // Ajusta quais controles devem estar visíveis.
        btnIniciarReiniciar.setText("Iniciar Jogo");
        lblStatus.setText("Configure o jogo e clique em Iniciar.");
        lblJogadas.setText("Jogadas: 0");
    }

    /**
     * Atualiza a interface de forma dinâmica com base na escolha do modo de jogo.
     * Se o modo for "Jogador vs Jogador", habilita a escolha de símbolo para o P2.
     * Se for "Jogador vs Máquina", habilita a escolha do nível da máquina.
     */
    private void atualizarVisibilidadeControlesModoJogo() {
        String modoSelecionado = (String) comboBoxModoJogo.getSelectedItem();
        boolean modoVsJogador = "Jogador vs Jogador".equals(modoSelecionado);
        
        comboBoxSimboloP2.setEnabled(modoVsJogador);
        comboBoxNivelMaquina.setEnabled(!modoVsJogador);
    }
    
    /**
     * Ação executada ao clicar no botão "Iniciar Jogo" ou "Reiniciar Jogo".
     * Este é o ponto que conecta a configuração da tela com a lógica do jogo.
     */
    private void acaoIniciarReiniciarJogo() {
        try {
            // 1. Lê as configurações escolhidas pelo usuário na tela.
            String simboloP1 = (String) comboBoxSimboloP1.getSelectedItem();
            String modoSelecionado = (String) comboBoxModoJogo.getSelectedItem();
            
            // 2. Decide qual tipo de jogo criar com base no modo selecionado.
            if ("Jogador vs Máquina".equals(modoSelecionado)) {
                // Se for contra a máquina, verifica o nível de dificuldade.
                String nivelSelecionado = (String) comboBoxNivelMaquina.getSelectedItem();
                int nivelMaquina = "Difícil (2)".equals(nivelSelecionado) ? 2 : 1;
                // Cria um novo objeto JogoDaVelha para uma partida contra a máquina.
                jogo = new JogoDaVelha(simboloP1, nivelMaquina);
            } else { // Se for Jogador vs Jogador...
                String simboloP2 = (String) comboBoxSimboloP2.getSelectedItem();
                // Valida se os jogadores não escolheram o mesmo símbolo.
                if (simboloP1.equals(simboloP2)) {
                    JOptionPane.showMessageDialog(frmJogoDaVelha, "Jogador 1 e Jogador 2 não podem ter o mesmo símbolo!", "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
                    return; // Interrompe a ação se a configuração for inválida.
                }
                // Cria um novo objeto JogoDaVelha para uma partida entre dois humanos.
                jogo = new JogoDaVelha(simboloP1, simboloP2);
            }

            // 3. Prepara a interface para o jogo que vai começar.
            configurarParaJogoEmAndamento();
            atualizarInterface();

        } catch (IllegalArgumentException ex) {
            // Caso a classe JogoDaVelha encontre um erro de lógica na criação, exibe a mensagem.
            JOptionPane.showMessageDialog(frmJogoDaVelha, ex.getMessage(), "Erro de Configuração", JOptionPane.ERROR_MESSAGE);
            jogo = null;
            configurarEstadoInicialControles();
        }
    }

    /**
     * Configura a interface para o estado de "jogo em andamento".
     * Basicamente, "trava" os controles de configuração para que não possam ser alterados durante a partida.
     */
    private void configurarParaJogoEmAndamento() {
        for (JLabel lbl : labelsTabuleiro) {
            lbl.setText(""); // Garante que o tabuleiro está visualmente limpo.
        }
        // Desabilita os menus de configuração.
        comboBoxSimboloP1.setEnabled(false);
        comboBoxSimboloP2.setEnabled(false);
        comboBoxModoJogo.setEnabled(false);
        comboBoxNivelMaquina.setEnabled(false);
        btnIniciarReiniciar.setText("Reiniciar Jogo"); // Muda o texto do botão.
    }

    /**
     * Este método é o coração da interatividade. Ele é chamado sempre que o usuário clica em uma célula do tabuleiro.
     * @param posicao A posição (0 a 8) da célula que foi clicada.
     */
    private void labelTabuleiroClicado(int posicao) {
        // Esta verificação (cláusula de guarda) impede qualquer ação se o jogo não estiver em um estado jogável.
        if (jogo == null || jogo.terminou() || isMaquinaJogando || !labelsTabuleiro[posicao].getText().isEmpty()) {
            return;
        }

        try {
            // Pega o jogador da vez da classe de lógica.
            int jogadorQueVaiJogar = jogo.getJogadorAtual();
            // Envia a jogada para a classe de lógica processar. A tela não sabe as regras, ela só informa o que aconteceu.
            jogo.jogaJogador(jogadorQueVaiJogar, posicao);
            // Pede para a interface se redesenhar para mostrar o resultado da jogada.
            atualizarInterface();

            // Após a jogada do humano, verifica se o jogo continua e se agora é a vez da máquina.
            if (!jogo.terminou() && jogo.isModoVsMaquina() && jogo.getJogadorAtual() == 2) {
                fazerJogadaMaquina(); // Se sim, chama o método para a jogada da máquina.
            }

        } catch (IllegalStateException | IllegalArgumentException ex) {
            // Se a classe de lógica retornar um erro (ex: posição já ocupada), a tela exibe um aviso.
            JOptionPane.showMessageDialog(frmJogoDaVelha, ex.getMessage(), "Jogada Inválida", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Controla a jogada da máquina, adicionando um pequeno atraso para simular que ela está "pensando".
     * Isso melhora a experiência do usuário.
     */
    private void fazerJogadaMaquina() {
        // Se o jogo não estiver rodando ou não for a vez da máquina, não faz nada.
        if (jogo == null || jogo.terminou() || !jogo.isModoVsMaquina() || jogo.getJogadorAtual() != 2) {
            return;
        }

        isMaquinaJogando = true; // Bloqueia cliques do usuário para evitar jogadas fora de hora.
        lblStatus.setText("Máquina (" + jogo.getSimbolo(2) + ") está pensando...");
        
        int delay = 1000; // Atraso de 1000 milissegundos (1 segundo).
        // O Timer do Swing executa uma ação após um certo tempo, sem travar a interface.
        Timer timer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    // Após o atraso, pede para a classe de lógica executar a jogada da máquina.
                    jogo.jogaMaquina();
                } catch (IllegalStateException | IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(frmJogoDaVelha, "Erro na jogada da máquina: " + ex.getMessage(), "Erro Máquina", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // Independentemente de ter dado erro ou não, desbloqueia os cliques do usuário.
                    isMaquinaJogando = false; 
                    // E atualiza a tela para mostrar a jogada da máquina.
                    atualizarInterface();
                }
            }
        });
        timer.setRepeats(false); // Garante que o timer execute apenas uma vez.
        timer.start();
    }

    /**
     * Sincroniza a interface gráfica com o estado atual do objeto 'jogo'.
     * Este método é o responsável por "desenhar" o que a classe de lógica diz que está acontecendo.
     */
    private void atualizarInterface() {
        if (jogo == null) {
            configurarEstadoInicialControles();
            return;
        }

        // 1. Atualiza o tabuleiro: percorre as células da lógica e atualiza o texto de cada JLabel na tela.
        String[] celulas = jogo.getCelulas();
        for (int i = 0; i < 9; i++) {
            labelsTabuleiro[i].setText(celulas[i] == null ? "" : celulas[i]);
        }

        // 2. Atualiza o contador de jogadas.
        lblJogadas.setText("Jogadas: " + jogo.getQuantidadeJogadas());

        // 3. Verifica o status do jogo: Terminou ou continua?
        if (jogo.terminou()) {
            // Se terminou, descobre o resultado e monta a mensagem final.
            String statusFinal = "";
            int resultado = jogo.getResultado();
            if (resultado == 0) {
                statusFinal = "Empate!";
            } else if (resultado == 1) {
                statusFinal = "Jogador " + jogo.getSimbolo(1) + " venceu!";
            } else if (resultado == 2) {
                // Mensagem personalizada se o vencedor for a máquina.
                statusFinal = ((jogo.isModoVsMaquina()) ? "Máquina " : "Jogador ") + jogo.getSimbolo(2) + " venceu!";
            }
            lblStatus.setText(statusFinal);
            
            // Adiciona o resultado ao histórico de partidas da sessão.
            if (!statusFinal.isEmpty()) {
                historicoResultadosPartidas.add(statusFinal + " (Jogadas: " + jogo.getQuantidadeJogadas() + ")");
            }
            
            // Reconfigura os controles para permitir que um novo jogo comece.
            configurarControlesParaFimDeJogo();
        } else {
            // Se o jogo continua, atualiza a label de status para indicar de quem é a vez.
            if(!isMaquinaJogando) { // Só atualiza o status se não for a vez da máquina "pensar".
                lblStatus.setText("Vez do " + (jogo.isModoVsMaquina() && jogo.getJogadorAtual() == 2 ? "Máquina" : "Jogador " + jogo.getJogadorAtual()) + " (" + jogo.getSimboloJogadorAtual() + ")");
            }
        }
    }
    
    /**
     * Libera os controles de configuração ao final de uma partida, 
     * permitindo que o usuário inicie um novo jogo.
     */
    private void configurarControlesParaFimDeJogo() {
        comboBoxSimboloP1.setEnabled(true);
        comboBoxModoJogo.setEnabled(true);
        atualizarVisibilidadeControlesModoJogo();
        btnIniciarReiniciar.setText("Iniciar Novo Jogo");
    }

    /**
     * Exibe uma janela de diálogo (JOptionPane) mostrando o histórico de resultados
     * das partidas que foram jogadas nesta sessão.
     */
    private void mostrarHistoricoPartidas() {
        if (historicoResultadosPartidas.isEmpty()) {
            JOptionPane.showMessageDialog(frmJogoDaVelha, "Nenhuma partida foi jogada ainda nesta sessão.", "Histórico de Partidas", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Usa um StringBuilder para construir a lista de resultados de forma eficiente.
        StringBuilder sb = new StringBuilder("Resultados das Partidas:\n\n");
        for (int i = 0; i < historicoResultadosPartidas.size(); i++) {
            sb.append((i + 1)).append(". ").append(historicoResultadosPartidas.get(i)).append("\n");
        }

        // Coloca o texto dentro de uma área de texto com barra de rolagem para o caso de o histórico ser grande.
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(350, 200));

        // Exibe a janela com o histórico.
        JOptionPane.showMessageDialog(frmJogoDaVelha, scrollPane, "Histórico de Partidas", JOptionPane.PLAIN_MESSAGE);
    }
}