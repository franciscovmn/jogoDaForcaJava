package projeto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Random;

/**
 * Classe de lógica do Jogo da Velha.
 * Gerencia o estado do jogo, as jogadas e as regras, separando a lógica da interface gráfica.
 * @author [Felipe Antonio Ramalho Macedo - 20232370036]
 * @author [Francisco Viana Maia Neto - 20232370011]
 */
public class JogoDaVelha {

    // --- ATRIBUTOS --- //
    // Array para representar nosso tabuleiro de 9 posições.
    private String[] celulas;
    // Guarda os símbolos dos jogadores. Ex: ["X", "O"].
    private String[] simbolos;
    // Map para guardar o histórico de cada jogada (posição -> símbolo), mantendo a ordem de inserção.
    private LinkedHashMap<Integer, String> historico;
    // Contador para sabermos quantas jogadas já foram feitas.
    private int quantidadeJogadas;
    // Nível de "esperteza" da máquina: 1 para fácil (aleatório) ou 2 para difícil (com estratégia).
    private int nivelEspertezaMaquina;
    // Controla de quem é a vez (1 ou 2).
    private int jogadorAtual;
    // Símbolo reservado para a máquina, para garantir que não seja escolhido pelo jogador.
    private static final String SIMBOLO_MAQUINA = "m";

    // --- CONSTRUTORES --- //

    /**
     * Construtor para o modo Jogador vs. Jogador.
     * Recebe os símbolos de cada um e inicia o jogo.
     * @param simbolo1 Símbolo do Jogador 1.
     * @param simbolo2 Símbolo do Jogador 2.
     */
    public JogoDaVelha(String simbolo1, String simbolo2) {
        // Validações para garantir que os símbolos são válidos e diferentes.
        if (simbolo1 == null || simbolo1.trim().isEmpty() || simbolo1.equalsIgnoreCase(SIMBOLO_MAQUINA)) {
            throw new IllegalArgumentException("Símbolo do jogador 1 não pode ser vazio ou 'm'.");
        }
        if (simbolo2 == null || simbolo2.trim().isEmpty() || simbolo2.equalsIgnoreCase(SIMBOLO_MAQUINA)) {
            throw new IllegalArgumentException("Símbolo do jogador 2 não pode ser vazio ou 'm'.");
        }
        if (simbolo1.equals(simbolo2)) {
            throw new IllegalArgumentException("Os símbolos dos jogadores não podem ser iguais.");
        }
        this.simbolos = new String[]{simbolo1, simbolo2};
        this.nivelEspertezaMaquina = 0; // Usamos 0 para indicar que não há máquina.
        inicializarJogo();
    }

    /**
     * Construtor para o modo Jogador vs. Máquina.
     * Recebe o símbolo do jogador e o nível de dificuldade da máquina.
     * @param simboloJogador1 Símbolo do jogador humano.
     * @param nivel Nível de dificuldade da máquina (1 ou 2).
     */
    public JogoDaVelha(String simboloJogador1, int nivel) {
        if (simboloJogador1 == null || simboloJogador1.trim().isEmpty() || simboloJogador1.equalsIgnoreCase(SIMBOLO_MAQUINA)) {
            throw new IllegalArgumentException("Símbolo do jogador não pode ser vazio ou 'm'.");
        }
        if (nivel != 1 && nivel != 2) {
            throw new IllegalArgumentException("Nível da máquina deve ser 1 (baixo) ou 2 (alto).");
        }
        this.simbolos = new String[]{simboloJogador1, SIMBOLO_MAQUINA};
        this.nivelEspertezaMaquina = nivel;
        inicializarJogo();
    }
    
    // --- MÉTODOS PÚBLICOS (API DO JOGO CONFORME ESPECIFICAÇÃO) --- //

    /**
     * Valida e efetiva a jogada de um jogador no tabuleiro.
     * @param numeroJogador Jogador que está jogando (1 ou 2).
     * @param posicao Posição no tabuleiro (0-8).
     */
    public void jogaJogador(int numeroJogador, int posicao) {
        // Verifica se o jogo já acabou ou se não é a vez do jogador.
        if (terminou()) {
            throw new IllegalStateException("O jogo já terminou. Não é possível fazer mais jogadas.");
        }
        if (numeroJogador != this.jogadorAtual) {
            throw new IllegalArgumentException("Não é a vez do jogador " + numeroJogador);
        }
        // Valida a posição, conforme solicitado no PDF.
        if (posicao < 0 || posicao > 8) {
            throw new IllegalArgumentException("Posição " + posicao + " é inválida. Deve ser entre 0 e 8.");
        }
        if (!celulas[posicao].isEmpty()) {
            throw new IllegalArgumentException("Posição " + posicao + " já está ocupada.");
        }
        efetivarJogada(posicao, numeroJogador);
    }

    /**
     * Escolhe e realiza a jogada da máquina com base no nível de dificuldade.
     */
    public void jogaMaquina() {
        if (nivelEspertezaMaquina == 0) {
            throw new IllegalStateException("Não há máquina neste modo de jogo.");
        }
        if (this.jogadorAtual != 2) { // A máquina é sempre o jogador 2.
            throw new IllegalStateException("Não é a vez da máquina.");
        }
        if (terminou()) {
            throw new IllegalStateException("O jogo já terminou.");
        }

        int posicaoEscolhida;
        if (nivelEspertezaMaquina == 1) { // Nível Baixo: jogada aleatória.
            Random random = new Random();
            ArrayList<Integer> posicoesDisponiveis = getPosicoesDisponiveis();
            posicaoEscolhida = posicoesDisponiveis.get(random.nextInt(posicoesDisponiveis.size()));
        } else { // Nível Alto: usa uma estratégia para vencer ou bloquear.
            posicaoEscolhida = encontrarMelhorJogada();
        }
        efetivarJogada(posicaoEscolhida, 2);
    }

    /**
     * Verifica se o jogo terminou (por vitória de alguém ou por empate).
     * @return true se o jogo acabou, false caso contrário.
     */
    public boolean terminou() {
        // O jogo termina se um dos jogadores venceu, ou se todas as 9 células foram preenchidas.
        return verificaVencedor(simbolos[0]) || verificaVencedor(simbolos[1]) || quantidadeJogadas == 9;
    }

    /**
     * Retorna o resultado final da partida.
     * @return 1 (vitória P1), 2 (vitória P2/máquina), 0 (empate), -1 (em andamento).
     */
    public int getResultado() {
        if (verificaVencedor(simbolos[0])) return 1;
        if (verificaVencedor(simbolos[1])) return 2;
        if (quantidadeJogadas == 9) return 0;
        return -1; // -1 significa que o jogo ainda não acabou.
    }

    /**
     * Retorna o símbolo de um jogador específico.
     * @param numeroJogador O número do jogador (1 ou 2).
     * @return O símbolo correspondente.
     */
    public String getSimbolo(int numeroJogador) {
        if (numeroJogador != 1 && numeroJogador != 2) {
            throw new IllegalArgumentException("Número do jogador deve ser 1 ou 2.");
        }
        return simbolos[numeroJogador - 1];
    }
    
    /**
     * Retorna uma "foto" do tabuleiro, uma representação textual bidimensional.
     * @return A string formatada do tabuleiro, pronta para ser exibida.
     */
    public String getFoto() {
        StringBuilder foto = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            foto.append(celulas[i].isEmpty() ? " " : celulas[i]); // Usa espaço para célula vazia.
            // Adiciona a formatação de quebra de linha e divisórias.
            if ((i + 1) % 3 == 0) {
                if (i < 8) foto.append("\n-----\n");
            } else {
                foto.append(" | ");
            }
        }
        return foto.toString();
    }
    
    /**
     * Retorna uma lista com as posições que ainda estão livres no tabuleiro.
     * @return ArrayList com os índices das posições disponíveis.
     */
    public ArrayList<Integer> getPosicoesDisponiveis() {
        ArrayList<Integer> disponiveis = new ArrayList<>();
        for (int i = 0; i < celulas.length; i++) {
            if (celulas[i].isEmpty()) {
                disponiveis.add(i);
            }
        }
        return disponiveis;
    }

    /**
     * Retorna uma cópia do histórico de jogadas da partida.
     * @return Um LinkedHashMap contendo os pares <posição, símbolo>.
     */
    public LinkedHashMap<Integer, String> getHistorico() {
        // Retornamos uma cópia para proteger o histórico original de modificações externas.
        return new LinkedHashMap<>(historico);
    }

    // --- MÉTODOS PRIVADOS (LÓGICA INTERNA) --- //

    /**
     * Prepara o jogo, limpando o tabuleiro e resetando os contadores.
     * Chamado pelos construtores.
     */
    private void inicializarJogo() {
        this.celulas = new String[9];
        Arrays.fill(this.celulas, ""); // Preenche o tabuleiro com strings vazias.
        this.historico = new LinkedHashMap<>();
        this.quantidadeJogadas = 0;
        this.jogadorAtual = 1; // O jogador 1 sempre começa.
    }

    /**
     * Realiza a jogada de fato: marca a célula, atualiza o histórico e troca o turno.
     */
    private void efetivarJogada(int posicao, int numeroJogador) {
        celulas[posicao] = getSimbolo(numeroJogador);
        historico.put(posicao, celulas[posicao]);
        quantidadeJogadas++;
        // Troca o jogador: se era 1 vira 2, se era 2 vira 1.
        jogadorAtual = (jogadorAtual == 1) ? 2 : 1;
    }

    /**
     * Lógica da "esperteza alta" da máquina. Segue uma hierarquia de decisões.
     * @return A melhor posição para a máquina jogar.
     */
    private int encontrarMelhorJogada() {
        ArrayList<Integer> posicoesDisponiveis = getPosicoesDisponiveis();

        // 1. Prioridade máxima: Se a máquina pode ganhar, ela joga para ganhar.
        for (int pos : posicoesDisponiveis) {
            celulas[pos] = getSimbolo(2); // Simula a jogada.
            if (verificaVencedor(getSimbolo(2))) {
                celulas[pos] = ""; // Desfaz a simulação.
                return pos;
            }
            celulas[pos] = ""; // Desfaz a simulação.
        }

        // 2. Segunda prioridade: Se o jogador está prestes a ganhar, bloqueia.
        for (int pos : posicoesDisponiveis) {
            celulas[pos] = getSimbolo(1); // Simula a jogada do oponente.
            if (verificaVencedor(getSimbolo(1))) {
                celulas[pos] = ""; // Desfaz a simulação.
                return pos; // Retorna a posição para bloquear.
            }
            celulas[pos] = ""; // Desfaz a simulação.
        }
        
        // 3. Estratégia: Ocupar a posição central (4) é quase sempre uma boa jogada.
        if (celulas[4].isEmpty()) return 4;

        // 4. Estratégia: Ocupar um dos cantos (0, 2, 6, 8).
        ArrayList<Integer> cantos = new ArrayList<>(Arrays.asList(0, 2, 6, 8));
        cantos.retainAll(posicoesDisponiveis); // Pega só os cantos que estão livres.
        if (!cantos.isEmpty()) {
            return cantos.get(new Random().nextInt(cantos.size()));
        }
        
        // 5. Se nada acima for possível, joga em qualquer lugar livre (similar ao nível fácil).
        return posicoesDisponiveis.get(new Random().nextInt(posicoesDisponiveis.size()));
    }

    /**
     * Verifica todas as combinações de vitória para um determinado símbolo.
     * @param simbolo O símbolo a ser verificado ("X" ou "O").
     * @return true se o símbolo formou uma linha vencedora.
     */
    private boolean verificaVencedor(String simbolo) {
        // Todas as 8 combinações possíveis de vitória.
        int[][] vitorias = {{0,1,2}, {3,4,5}, {6,7,8}, {0,3,6}, {1,4,7}, {2,5,8}, {0,4,8}, {2,4,6}};
        for (int[] condicao : vitorias) {
            if (celulas[condicao[0]].equals(simbolo) && celulas[condicao[1]].equals(simbolo) && celulas[condicao[2]].equals(simbolo)) {
                return true;
            }
        }
        return false;
    }
}