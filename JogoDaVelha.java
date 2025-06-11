// Francisco Viana Maia Neto - 20232370011
package projeto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Random;

/**
 * Classe de lógica do Jogo da Velha.
 * Gerencia o estado do jogo, jogadas e regras.
 */
public class JogoDaVelha {

    // --- ATRIBUTOS --- //

    // Tabuleiro 3x3, representado por um array de 9 posições.
    private String[] celulas;

    // Símbolos dos jogadores. [0] = P1, [1] = P2/Máquina.
    private String[] simbolos;

    // Histórico de jogadas na ordem em que ocorreram <posição, símbolo>.
    private LinkedHashMap<Integer, String> historico;

    // Contador de jogadas da partida atual (0 a 9).
    private int quantidadeJogadas;

    // Nível da IA da máquina (1: fácil, 2: difícil).
    private int nivelEspertezaMaquina;

    // Indica de quem é a vez (1 ou 2).
    private int jogadorAtual;
    
    // Símbolo fixo para a máquina, conforme requisito. 
    private static final String SIMBOLO_MAQUINA = "m";

    // --- CONSTRUTORES --- //

    /**
     * Construtor para modo: Jogador vs. Jogador. 
     * @param simbolo1 Símbolo do Jogador 1.
     * @param simbolo2 Símbolo do Jogador 2.
     */
    public JogoDaVelha(String simbolo1, String simbolo2) {
        // Validação dos símbolos de entrada.
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
        // Validação dos parâmetros de entrada.
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
    
    // --- MÉTODOS DE CONTROLE --- //

    // Prepara o jogo para uma nova partida.
    private void inicializarJogo() {
        this.celulas = new String[9];
        Arrays.fill(this.celulas, ""); // Limpa o tabuleiro.
        this.historico = new LinkedHashMap<>();
        this.quantidadeJogadas = 0;
        this.jogadorAtual = 1; // Jogador 1 sempre começa.
    }
    
    /**
     * Realiza a jogada de um jogador. 
     * @param numeroJogador Jogador que está jogando (1 ou 2).
     * @param posicao Posição no tabuleiro (0-8).
     */
    public void jogaJogador(int numeroJogador, int posicao) {
        // Validações da jogada.
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

        // Efetiva a jogada.
        celulas[posicao] = getSimbolo(numeroJogador);
        historico.put(posicao, celulas[posicao]);
        quantidadeJogadas++;
        
        // Passa a vez.
        jogadorAtual = (jogadorAtual == 1) ? 2 : 1;
    }

    /**
     * Realiza a jogada da máquina. 
     */
    public void jogaMaquina() {
        // Valida se a máquina pode jogar.
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
        // Escolhe a jogada com base no nível de dificuldade.
        if (nivelEspertezaMaquina == 1) { // Nível Fácil: jogada aleatória.
            Random random = new Random();
            ArrayList<Integer> posicoesDisponiveis = getPosicoesDisponiveis();
            posicaoEscolhida = posicoesDisponiveis.get(random.nextInt(posicoesDisponiveis.size()));
        } else { // Nível Difícil: usa estratégia.
            posicaoEscolhida = encontrarMelhorJogada();
        }
        
        // Efetiva a jogada da máquina.
        celulas[posicaoEscolhida] = getSimbolo(2);
        historico.put(posicaoEscolhida, celulas[posicaoEscolhida]);
        quantidadeJogadas++;
        jogadorAtual = 1; // Devolve a vez para o jogador humano.
    }

    // Lógica da IA "Difícil" para encontrar a melhor jogada.
    private int encontrarMelhorJogada() {
        ArrayList<Integer> posicoesDisponiveis = getPosicoesDisponiveis();

        // 1. Tenta ganhar: Verifica se alguma jogada leva à vitória.
        for (int pos : posicoesDisponiveis) {
            celulas[pos] = getSimbolo(2); // Simula jogada.
            if (verificaVencedor(getSimbolo(2))) {
                celulas[pos] = ""; // Desfaz simulação.
                return pos;
            }
            celulas[pos] = ""; // Desfaz simulação.
        }

        // 2. Tenta bloquear: Verifica se o oponente pode ganhar e bloqueia.
        for (int pos : posicoesDisponiveis) {
            celulas[pos] = getSimbolo(1); // Simula jogada do oponente.
            if (verificaVencedor(getSimbolo(1))) {
                celulas[pos] = ""; // Desfaz simulação.
                return pos;
            }
            celulas[pos] = ""; // Desfaz simulação.
        }
        
        // 3. Ocupa o centro (posição 4), se estiver livre.
        if (celulas[4].isEmpty()) {
            return 4;
        }

        // 4. Ocupa um dos cantos (0, 2, 6, 8), se estiverem livres.
        ArrayList<Integer> cantos = new ArrayList<>(Arrays.asList(0, 2, 6, 8));
        cantos.retainAll(posicoesDisponiveis);
        if (!cantos.isEmpty()) {
            Random random = new Random();
            return cantos.get(random.nextInt(cantos.size()));
        }
        
        // 5. Último recurso: joga em qualquer posição livre.
        Random random = new Random();
        return posicoesDisponiveis.get(random.nextInt(posicoesDisponiveis.size()));
    }

    /**
     * Verifica se o jogo terminou (vitória ou empate). 
     * @return true se o jogo acabou, false caso contrário.
     */
    public boolean terminou() {
        return verificaVencedor(simbolos[0]) || verificaVencedor(simbolos[1]) || quantidadeJogadas == 9;
    }

    // Confere as 8 combinações de vitória para um símbolo.
    private boolean verificaVencedor(String simbolo) {
        // Linhas
        if (celulas[0].equals(simbolo) && celulas[1].equals(simbolo) && celulas[2].equals(simbolo)) return true;
        if (celulas[3].equals(simbolo) && celulas[4].equals(simbolo) && celulas[5].equals(simbolo)) return true;
        if (celulas[6].equals(simbolo) && celulas[7].equals(simbolo) && celulas[8].equals(simbolo)) return true;
        // Colunas
        if (celulas[0].equals(simbolo) && celulas[3].equals(simbolo) && celulas[6].equals(simbolo)) return true;
        if (celulas[1].equals(simbolo) && celulas[4].equals(simbolo) && celulas[7].equals(simbolo)) return true;
        if (celulas[2].equals(simbolo) && celulas[5].equals(simbolo) && celulas[8].equals(simbolo)) return true;
        // Diagonais
        if (celulas[0].equals(simbolo) && celulas[4].equals(simbolo) && celulas[8].equals(simbolo)) return true;
        if (celulas[2].equals(simbolo) && celulas[4].equals(simbolo) && celulas[6].equals(simbolo)) return true;
        return false;
    }

    // --- GETTERS --- //

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

    // Retorna o símbolo de um jogador.
    public String getSimbolo(int numeroJogador) {
        if (numeroJogador != 1 && numeroJogador != 2) {
            throw new IllegalArgumentException("Número do jogador deve ser 1 ou 2.");
        }
        return simbolos[numeroJogador - 1];
    }
    
    // Retorna uma string formatada do tabuleiro (para debug/console)
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
    
    // Retorna uma lista de posições ainda não ocupadas.
    public ArrayList<Integer> getPosicoesDisponiveis() {
        ArrayList<Integer> disponiveis = new ArrayList<>();
        for (int i = 0; i < celulas.length; i++) {
            if (celulas[i].isEmpty()) {
                disponiveis.add(i);
            }
        }
        return disponiveis;
    }

    // Retorna uma cópia do histórico de jogadas.
    public LinkedHashMap<Integer, String> getHistorico() {
        return new LinkedHashMap<>(historico);
    }

    // Retorna o total de jogadas.
    public int getQuantidadeJogadas() {
        return quantidadeJogadas;
    }

    // Retorna o jogador da vez.
    public int getJogadorAtual() {
        return jogadorAtual;
    }
    
    // Retorna o símbolo do jogador da vez.
    public String getSimboloJogadorAtual() {
        return getSimbolo(this.jogadorAtual);
    }

    // Retorna uma cópia do array de células.
    public String[] getCelulas() {
        return Arrays.copyOf(celulas, celulas.length);
    }
    
    // Verifica se o modo de jogo é contra a máquina.
    public boolean isModoVsMaquina() {
        return nivelEspertezaMaquina > 0;
    }
}