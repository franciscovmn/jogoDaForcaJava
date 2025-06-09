package projeto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

/**
 * Representa a lógica do jogo da velha.
 */
public class JogoDaVelha {

    private String[] celulas; // Array com 9 posições para guardar os símbolos
    private String[] simbolos; // Array com 2 símbolos usados pelos jogadores
    private LinkedHashMap<Integer, String> historico; // Pares <posição, símbolo> das jogadas
    private int quantidadeJogadas;
    private int nivelEspertezaMaquina; // 1: baixo, 2: alto
    private int jogadorAtual; // 1 para jogador 1 (ou humano), 2 para jogador 2 (ou máquina)
    private static final String SIMBOLO_MAQUINA = "M"; // Símbolo padrão da máquina

    /**
     * Construtor para um jogo entre dois jogadores humanos.
     *
     * @param simbolo1 Símbolo do jogador 1.
     * @param simbolo2 Símbolo do jogador 2.
     * @throws IllegalArgumentException Se os símbolos forem iguais ou inválidos.
     */
    public JogoDaVelha(String simbolo1, String simbolo2) {
        if (simbolo1 == null || simbolo1.trim().isEmpty() || simbolo1.equals(SIMBOLO_MAQUINA)) {
            throw new IllegalArgumentException("Símbolo do jogador 1 não pode ser vazio ou '" + SIMBOLO_MAQUINA + "'.");
        }
        if (simbolo2 == null || simbolo2.trim().isEmpty() || simbolo2.equals(SIMBOLO_MAQUINA)) {
            throw new IllegalArgumentException("Símbolo do jogador 2 não pode ser vazio ou '" + SIMBOLO_MAQUINA + "'.");
        }
        if (simbolo1.equals(simbolo2)) {
            throw new IllegalArgumentException("Os símbolos dos jogadores não podem ser iguais.");
        }

        this.simbolos = new String[]{simbolo1, simbolo2};
        inicializarJogo();
        this.nivelEspertezaMaquina = 0; // Não aplicável para dois jogadores
    }

    /**
     * Construtor para um jogo entre um jogador humano e a máquina.
     *
     * @param simboloJogador1 Símbolo do jogador humano.
     * @param nivel Nível de esperteza da máquina (1: baixo, 2: alto).
     * @throws IllegalArgumentException Se o símbolo do jogador for inválido ou o nível for inválido.
     */
    public JogoDaVelha(String simboloJogador1, int nivel) {
        if (simboloJogador1 == null || simboloJogador1.trim().isEmpty() || simboloJogador1.equals(SIMBOLO_MAQUINA)) {
            throw new IllegalArgumentException("Símbolo do jogador não pode ser vazio ou '" + SIMBOLO_MAQUINA + "'.");
        }
        if (nivel != 1 && nivel != 2) {
            throw new IllegalArgumentException("Nível da máquina deve ser 1 (baixo) ou 2 (alto).");
        }

        this.simbolos = new String[]{simboloJogador1, SIMBOLO_MAQUINA};
        this.nivelEspertezaMaquina = nivel;
        inicializarJogo();
    }

    private void inicializarJogo() {
        this.celulas = new String[9];
        Arrays.fill(this.celulas, ""); // Inicializa células como vazias
        this.historico = new LinkedHashMap<>();
        this.quantidadeJogadas = 0;
        this.jogadorAtual = 1; // Jogador 1 sempre começa
    }

    /**
     * Reinicia o jogo para o estado inicial.
     */
    public void reiniciarJogo() {
        inicializarJogo();
    }


    /**
     * Retorna o símbolo do jogador especificado.
     *
     * @param numeroJogador 1 para jogador 1, 2 para jogador 2/máquina.
     * @return O símbolo do jogador.
     * @throws IllegalArgumentException Se o número do jogador for inválido.
     */
    public String getSimbolo(int numeroJogador) {
        if (numeroJogador != 1 && numeroJogador != 2) {
            throw new IllegalArgumentException("Número do jogador deve ser 1 ou 2.");
        }
        return simbolos[numeroJogador - 1];
    }

    /**
     * Realiza a jogada para o jogador especificado na posição informada.
     *
     * @param numeroJogador O número do jogador (1 ou 2).
     * @param posicao A posição no tabuleiro (0 a 8).
     * @throws IllegalStateException Se o jogo já terminou.
     * @throws IllegalArgumentException Se a posição for inválida, ocupada, ou não for a vez do jogador.
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

        celulas[posicao] = getSimbolo(numeroJogador);
        historico.put(posicao, celulas[posicao]);
        quantidadeJogadas++;
        // Troca o jogador
        jogadorAtual = (jogadorAtual == 1) ? 2 : 1;
    }

    /**
     * Realiza a jogada da máquina.
     *
     * @throws IllegalStateException Se não for a vez da máquina ou o jogo já terminou.
     */
    public void jogaMaquina() {
        if (nivelEspertezaMaquina == 0) {
            throw new IllegalStateException("Não há máquina neste modo de jogo.");
        }
        if (this.jogadorAtual != 2) { // Máquina é sempre o jogador 2 neste contexto
            throw new IllegalStateException("Não é a vez da máquina.");
        }
        if (terminou()) {
            throw new IllegalStateException("O jogo já terminou.");
        }

        int posicaoEscolhida;
        ArrayList<Integer> posicoesDisponiveis = getPosicoesDisponiveis();

        if (posicoesDisponiveis.isEmpty()) {
            return; // Não deveria acontecer se o jogo não terminou, mas por segurança.
        }

        if (nivelEspertezaMaquina == 1) { // Nível Baixo: Aleatório
            Random random = new Random();
            posicaoEscolhida = posicoesDisponiveis.get(random.nextInt(posicoesDisponiveis.size()));
        } else { // Nível Alto
            posicaoEscolhida = -1;

            // 1. Tentar ganhar
            for (int pos : posicoesDisponiveis) {
                celulas[pos] = getSimbolo(2); // Simula jogada da máquina
                if (verificaVencedor(getSimbolo(2))) {
                    posicaoEscolhida = pos;
                    celulas[pos] = ""; // Desfaz simulação
                    break;
                }
                celulas[pos] = ""; // Desfaz simulação
            }

            // 2. Tentar bloquear o oponente
            if (posicaoEscolhida == -1) {
                for (int pos : posicoesDisponiveis) {
                    celulas[pos] = getSimbolo(1); // Simula jogada do oponente
                    if (verificaVencedor(getSimbolo(1))) {
                        posicaoEscolhida = pos;
                        celulas[pos] = ""; // Desfaz simulação
                        break;
                    }
                    celulas[pos] = ""; // Desfaz simulação
                }
            }
            
            // 3. Tentar centro
            if (posicaoEscolhida == -1 && celulas[4].isEmpty()) {
                posicaoEscolhida = 4;
            }

            // 4. Tentar um canto aleatório
            if (posicaoEscolhida == -1) {
                ArrayList<Integer> cantos = new ArrayList<>(Arrays.asList(0, 2, 6, 8));
                cantos.retainAll(posicoesDisponiveis); // Mantém apenas cantos disponíveis
                if (!cantos.isEmpty()) {
                    Random random = new Random();
                    posicaoEscolhida = cantos.get(random.nextInt(cantos.size()));
                }
            }
            
            // 5. Aleatório como último recurso
            if (posicaoEscolhida == -1) {
                Random random = new Random();
                posicaoEscolhida = posicoesDisponiveis.get(random.nextInt(posicoesDisponiveis.size()));
            }
        }
        
        // Efetiva a jogada da máquina (jogador 2)
        celulas[posicaoEscolhida] = getSimbolo(2);
        historico.put(posicaoEscolhida, celulas[posicaoEscolhida]);
        quantidadeJogadas++;
        jogadorAtual = 1; // Volta para o jogador 1
    }


    /**
     * Verifica se o jogo terminou (vitória de alguém ou empate).
     *
     * @return true se o jogo terminou, false caso contrário.
     */
    public boolean terminou() {
        return verificaVencedor(simbolos[0]) || verificaVencedor(simbolos[1]) || quantidadeJogadas == 9;
    }

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

    /**
     * Retorna o resultado do jogo.
     *
     * @return -1 (jogo em andamento), 0 (empate), 1 (vitória do jogador1), 2 (vitória do jogador2/máquina).
     */
    public int getResultado() {
        if (verificaVencedor(simbolos[0])) {
            return 1; // Jogador 1 venceu
        }
        if (verificaVencedor(simbolos[1])) {
            return 2; // Jogador 2 (ou máquina) venceu
        }
        if (quantidadeJogadas == 9) {
            return 0; // Empate
        }
        return -1; // Jogo em andamento
    }

    /**
     * Retorna uma representação textual do tabuleiro para exibição.
     * As células são dispostas em formato 3x3. Células vazias são representadas por " ".
     *
     * @return String representando o tabuleiro.
     */
    public String getFoto() {
        StringBuilder foto = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            foto.append(celulas[i].isEmpty() ? " " : celulas[i]);
            if ((i + 1) % 3 == 0) {
                if (i < 8) foto.append("\n-----\n"); // Separador de linha
            } else {
                foto.append(" | "); // Separador de coluna
            }
        }
        return foto.toString();
    }

    /**
     * Retorna uma lista com as posições (índices 0-8) ainda não utilizadas no jogo.
     *
     * @return ArrayList de Integers com as posições disponíveis.
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
     * Retorna o histórico de jogadas.
     *
     * @return LinkedHashMap com os pares <posição, símbolo> das jogadas.
     */
    public LinkedHashMap<Integer, String> getHistorico() {
        return new LinkedHashMap<>(historico); // Retorna uma cópia para proteger o original
    }

    // Getters adicionais que podem ser úteis para a TelaJogo
    public int getQuantidadeJogadas() {
        return quantidadeJogadas;
    }

    public int getJogadorAtual() {
        return jogadorAtual;
    }
    
    public String getSimboloJogadorAtual() {
        return getSimbolo(this.jogadorAtual);
    }

    public String[] getCelulas() {
        return Arrays.copyOf(celulas, celulas.length);
    }

    public boolean isModoVsMaquina() {
        return nivelEspertezaMaquina > 0;
    }
}