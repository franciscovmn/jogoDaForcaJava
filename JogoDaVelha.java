package projeto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Random;

/**
 * Classe de lógica do Jogo da Velha.
 * Gerencia o estado do jogo, jogadas e regras.
 * @author [Felipe Antonio Ramalho Macedo - 20232370036]
 * @author [Francisco Viana Maia Neto - 20232370011]
 */
public class JogoDaVelha {

    // --- ATRIBUTOS --- //
    private String[] celulas;
    private String[] simbolos;
    private LinkedHashMap<Integer, String> historico;
    private int quantidadeJogadas;
    private int nivelEspertezaMaquina;
    private int jogadorAtual;
    private static final String SIMBOLO_MAQUINA = "m";

    // --- CONSTRUTORES --- //

    /**
     * Construtor para modo: Jogador vs. Jogador.
     * @param simbolo1 Símbolo do Jogador 1.
     * @param simbolo2 Símbolo do Jogador 2.
     */
    public JogoDaVelha(String simbolo1, String simbolo2) {
        if (simbolo1 == null || simbolo1.trim().isEmpty() || simbolo1.equalsIgnoreCase(SIMBOLO_MAQUINA)) {
            throw new IllegalArgumentException("Símbolo do jogador 1 não pode ser vazio ou '" + SIMBOLO_MAQUINA + "'.");
        }
        if (simbolo2 == null || simbolo2.trim().isEmpty() || simbolo2.equalsIgnoreCase(SIMBOLO_MAQUINA)) {
            throw new IllegalArgumentException("Símbolo do jogador 2 não pode ser vazio ou '" + SIMBOLO_MAQUINA + "'.");
        }
        if (simbolo1.equals(simbolo2)) {
            throw new IllegalArgumentException("Os símbolos dos jogadores não podem ser iguais.");
        }
        this.simbolos = new String[]{simbolo1, simbolo2};
        this.nivelEspertezaMaquina = 0; // 0 significa que não há máquina.
        inicializarJogo();
    }

    /**
     * Construtor para modo: Jogador vs. Máquina.
     * @param simboloJogador1 Símbolo do jogador humano.
     * @param nivel Nível de dificuldade da máquina (1 ou 2).
     */
    public JogoDaVelha(String simboloJogador1, int nivel) {
        if (simboloJogador1 == null || simboloJogador1.trim().isEmpty() || simboloJogador1.equalsIgnoreCase(SIMBOLO_MAQUINA)) {
            throw new IllegalArgumentException("Símbolo do jogador não pode ser vazio ou '" + SIMBOLO_MAQUINA + "'.");
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
     * Realiza a jogada de um jogador.
     * @param numeroJogador Jogador que está jogando (1 ou 2).
     * @param posicao Posição no tabuleiro (0-8).
     */
    public void jogaJogador(int numeroJogador, int posicao) {
        if (terminou()) {
            throw new IllegalStateException("O jogo já terminou. Não é possível fazer mais jogadas.");
        }
        if (numeroJogador != this.jogadorAtual) {
            throw new IllegalArgumentException("Não é a vez do jogador " + numeroJogador);
        }
        if (posicao < 0 || posicao > 8) {
            throw new IllegalArgumentException("Posição " + posicao + " é inválida. Deve ser entre 0 e 8.");
        }
        if (!celulas[posicao].isEmpty()) {
            throw new IllegalArgumentException("Posição " + posicao + " já está ocupada.");
        }
        efetivarJogada(posicao, numeroJogador);
    }

    /**
     * Realiza a jogada da máquina.
     */
    public void jogaMaquina() {
        if (nivelEspertezaMaquina == 0) {
            throw new IllegalStateException("Não há máquina neste modo de jogo.");
        }
        if (this.jogadorAtual != 2) { // Máquina é sempre o jogador 2.
            throw new IllegalStateException("Não é a vez da máquina.");
        }
        if (terminou()) {
            throw new IllegalStateException("O jogo já terminou.");
        }

        int posicaoEscolhida;
        if (nivelEspertezaMaquina == 1) { // Nível Fácil: jogada aleatória.
            Random random = new Random();
            ArrayList<Integer> posicoesDisponiveis = getPosicoesDisponiveis();
            posicaoEscolhida = posicoesDisponiveis.get(random.nextInt(posicoesDisponiveis.size()));
        } else { // Nível Difícil: usa estratégia.
            posicaoEscolhida = encontrarMelhorJogada();
        }
        efetivarJogada(posicaoEscolhida, 2);
    }

    /**
     * Verifica se o jogo terminou (vitória ou empate).
     * @return true se o jogo acabou, false caso contrário.
     */
    public boolean terminou() {
        return verificaVencedor(simbolos[0]) || verificaVencedor(simbolos[1]) || quantidadeJogadas == 9;
    }

    /**
     * Retorna o resultado da partida.
     * @return 1 (vitória P1), 2 (vitória P2/máquina), 0 (empate), -1 (em andamento).
     */
    public int getResultado() {
        if (verificaVencedor(simbolos[0])) return 1;
        if (verificaVencedor(simbolos[1])) return 2;
        if (quantidadeJogadas == 9) return 0;
        return -1;
    }

    /**
     * Retorna o símbolo de um jogador.
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
     * Retorna uma string formatada do tabuleiro.
     * @return A representação textual do tabuleiro.
     */
    public String getFoto() {
        StringBuilder foto = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            foto.append(celulas[i].isEmpty() ? " " : celulas[i]);
            if ((i + 1) % 3 == 0) {
                if (i < 8) foto.append("\n-----\n");
            } else {
                foto.append(" | ");
            }
        }
        return foto.toString();
    }
    
    /**
     * Retorna uma lista de posições ainda não ocupadas.
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
     * Retorna uma cópia do histórico de jogadas da partida atual.
     * @return Um LinkedHashMap contendo os pares <posição, símbolo>.
     */
    public LinkedHashMap<Integer, String> getHistorico() {
        return new LinkedHashMap<>(historico);
    }

    // --- MÉTODOS PRIVADOS (LÓGICA INTERNA E GETTERS NÃO PÚBLICOS) --- //

    private void inicializarJogo() {
        this.celulas = new String[9];
        Arrays.fill(this.celulas, "");
        this.historico = new LinkedHashMap<>();
        this.quantidadeJogadas = 0;
        this.jogadorAtual = 1; // Jogador 1 sempre começa.
    }

    private void efetivarJogada(int posicao, int numeroJogador) {
        celulas[posicao] = getSimbolo(numeroJogador);
        historico.put(posicao, celulas[posicao]);
        quantidadeJogadas++;
        jogadorAtual = (jogadorAtual == 1) ? 2 : 1;
    }

    private int encontrarMelhorJogada() {
        ArrayList<Integer> posicoesDisponiveis = getPosicoesDisponiveis();

        // 1. Tenta ganhar
        for (int pos : posicoesDisponiveis) {
            celulas[pos] = getSimbolo(2);
            if (verificaVencedor(getSimbolo(2))) {
                celulas[pos] = "";
                return pos;
            }
            celulas[pos] = "";
        }

        // 2. Tenta bloquear
        for (int pos : posicoesDisponiveis) {
            celulas[pos] = getSimbolo(1);
            if (verificaVencedor(getSimbolo(1))) {
                celulas[pos] = "";
                return pos;
            }
            celulas[pos] = "";
        }
        
        // 3. Ocupa o centro
        if (celulas[4].isEmpty()) return 4;

        // 4. Ocupa um dos cantos
        ArrayList<Integer> cantos = new ArrayList<>(Arrays.asList(0, 2, 6, 8));
        cantos.retainAll(posicoesDisponiveis);
        if (!cantos.isEmpty()) {
            return cantos.get(new Random().nextInt(cantos.size()));
        }
        
        // 5. Joga em qualquer posição livre
        return posicoesDisponiveis.get(new Random().nextInt(posicoesDisponiveis.size()));
    }

    private boolean verificaVencedor(String simbolo) {
        int[][] vitorias = {{0,1,2}, {3,4,5}, {6,7,8}, {0,3,6}, {1,4,7}, {2,5,8}, {0,4,8}, {2,4,6}};
        for (int[] condicao : vitorias) {
            if (celulas[condicao[0]].equals(simbolo) && celulas[condicao[1]].equals(simbolo) && celulas[condicao[2]].equals(simbolo)) {
                return true;
            }
        }
        return false;
    }
    
    // --- Getters que não fazem parte da API pública e foram privatizados ---
    private int getQuantidadeJogadas() { return quantidadeJogadas; }
    private int getJogadorAtual() { return jogadorAtual; }
    private String getSimboloJogadorAtual() { return getSimbolo(this.jogadorAtual); }
    private String[] getCelulas() { return Arrays.copyOf(celulas, celulas.length); }
    private boolean isModoVsMaquina() { return nivelEspertezaMaquina > 0; }
}