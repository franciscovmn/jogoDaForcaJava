package projeto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Random;

/**
 * Representa a lógica central e as regras do Jogo da Velha.
 * Esta classe é o "cérebro" do jogo, responsável por gerenciar o tabuleiro,
 * validar jogadas, verificar o vencedor e controlar a inteligência da máquina,
 * sem se preocupar com a interface gráfica.
 */
public class JogoDaVelha {

    // --- ATRIBUTOS PRINCIPAIS --- //

    /**
     * O tabuleiro do jogo. É um array de 9 posições que representa cada célula.
     * Armazena o símbolo do jogador que ocupou a célula ("X", "O", "m") ou uma string vazia se estiver livre.
     */
    private String[] celulas;

    /**
     * Armazena os símbolos dos dois jogadores. A posição 0 é do Jogador 1 e a posição 1 é do Jogador 2 (ou da máquina).
     */
    private String[] simbolos;

    /**
     * Um mapa que guarda o histórico de cada movimento na ordem em que aconteceu.
     * A chave (Integer) é a posição da jogada (0-8) e o valor (String) é o símbolo jogado.
     * Usamos LinkedHashMap para manter a ordem de inserção.
     */
    private LinkedHashMap<Integer, String> historico;

    /**
     * Conta quantas jogadas já foram feitas na partida. Vai de 0 a 9.
     */
    private int quantidadeJogadas;

    /**
     * Define o nível de dificuldade da máquina. 0 se não houver máquina, 1 para fácil (aleatório) e 2 para difícil (estratégico).
     */
    private int nivelEspertezaMaquina;

    /**
     * Indica de quem é a vez de jogar. Armazena 1 para o Jogador 1 e 2 para o Jogador 2/Máquina.
     */
    private int jogadorAtual;
    
    /**
     * Constante para o símbolo da máquina, conforme exigido no projeto.
     */
    private static final String SIMBOLO_MAQUINA = "m";

    // --- CONSTRUTORES --- //

    /**
     * Construtor para uma partida "Jogador vs. Jogador".
     * Recebe os símbolos de cada um e inicia o jogo.
     *
     * @param simbolo1 Símbolo escolhido para o Jogador 1.
     * @param simbolo2 Símbolo escolhido para o Jogador 2.
     * @throws IllegalArgumentException se os símbolos forem iguais, vazios ou o símbolo reservado para a máquina.
     */
    public JogoDaVelha(String simbolo1, String simbolo2) {
        // Validação inicial para garantir que os símbolos são válidos e diferentes.
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
        this.nivelEspertezaMaquina = 0; // Nível 0 indica que não há máquina no jogo.
        inicializarJogo();
    }

    /**
     * Construtor para uma partida "Jogador vs. Máquina".
     *
     * @param simboloJogador1 Símbolo escolhido pelo jogador humano.
     * @param nivel Nível de dificuldade da máquina (1 para fácil, 2 para difícil).
     * @throws IllegalArgumentException se o símbolo do jogador ou o nível da máquina forem inválidos.
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
    
    // --- MÉTODOS DE CONTROLE DO JOGO --- //

    /**
     * Prepara o jogo para um novo começo.
     * Limpa o tabuleiro, zera o histórico e contadores, e define o Jogador 1 como o primeiro a jogar.
     */
    private void inicializarJogo() {
        this.celulas = new String[9];
        Arrays.fill(this.celulas, ""); // Preenche todas as células com "" para indicar que estão vazias.
        this.historico = new LinkedHashMap<>();
        this.quantidadeJogadas = 0;
        this.jogadorAtual = 1; // O Jogador 1 sempre começa.
    }
    
    /**
     * Efetiva a jogada de um jogador humano no tabuleiro.
     *
     * @param numeroJogador O jogador que está fazendo o movimento (1 ou 2).
     * @param posicao A célula escolhida para a jogada (0 a 8).
     * @throws IllegalStateException se o jogo já acabou.
     * @throws IllegalArgumentException se a jogada for inválida (posição ocupada, fora do tabuleiro ou não for a vez do jogador).
     */
    public void jogaJogador(int numeroJogador, int posicao) {
        // 1. Validação completa da jogada para garantir a integridade do jogo.
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

        // 2. Se a jogada é válida, ela é registrada.
        celulas[posicao] = getSimbolo(numeroJogador);
        historico.put(posicao, celulas[posicao]);
        quantidadeJogadas++;
        
        // 3. Passa a vez para o próximo jogador.
        jogadorAtual = (jogadorAtual == 1) ? 2 : 1;
    }

    /**
     * Executa a jogada da máquina, com base no seu nível de inteligência.
     */
    public void jogaMaquina() {
        // Validações para garantir que a máquina só jogue quando for a vez dela.
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
        // Se o nível for 1 (fácil), a máquina escolhe uma posição livre de forma aleatória.
        if (nivelEspertezaMaquina == 1) {
            Random random = new Random();
            ArrayList<Integer> posicoesDisponiveis = getPosicoesDisponiveis();
            posicaoEscolhida = posicoesDisponiveis.get(random.nextInt(posicoesDisponiveis.size()));
        } else { // Se o nível for 2 (difícil), a máquina usa uma estratégia.
            posicaoEscolhida = encontrarMelhorJogada();
        }
        
        // Efetiva a jogada decidida pela máquina.
        celulas[posicaoEscolhida] = getSimbolo(2);
        historico.put(posicaoEscolhida, celulas[posicaoEscolhida]);
        quantidadeJogadas++;
        jogadorAtual = 1; // Passa a vez de volta para o jogador humano.
    }

    /**
     * Lógica da IA "Difícil". Pensa de forma estratégica para decidir a melhor jogada.
     * @return A posição (0-8) da melhor jogada encontrada.
     */
    private int encontrarMelhorJogada() {
        ArrayList<Integer> posicoesDisponiveis = getPosicoesDisponiveis();

        // Estratégia 1: Vencer o jogo.
        // A máquina simula colocar sua peça em cada posição livre. Se uma dessas jogadas resulta em vitória, ela a escolhe.
        for (int pos : posicoesDisponiveis) {
            celulas[pos] = getSimbolo(2); // Simula a jogada.
            if (verificaVencedor(getSimbolo(2))) {
                celulas[pos] = ""; // Desfaz a simulação.
                return pos; // Retorna a posição vencedora.
            }
            celulas[pos] = ""; // Desfaz a simulação.
        }

        // Estratégia 2: Bloquear o oponente.
        // Se não pode vencer, a máquina verifica se o jogador humano pode vencer na próxima rodada e o bloqueia.
        for (int pos : posicoesDisponiveis) {
            celulas[pos] = getSimbolo(1); // Simula a jogada do oponente.
            if (verificaVencedor(getSimbolo(1))) {
                celulas[pos] = ""; // Desfaz a simulação.
                return pos; // Retorna a posição para bloquear.
            }
            celulas[pos] = ""; // Desfaz a simulação.
        }
        
        // Estratégia 3: Ocupar o centro.
        // A posição central (4) é a mais estratégica. Se estiver livre, é uma boa escolha.
        if (celulas[4].isEmpty()) {
            return 4;
        }

        // Estratégia 4: Ocupar um canto.
        // Os cantos (0, 2, 6, 8) são as próximas melhores posições. Escolhe um canto livre aleatoriamente.
        ArrayList<Integer> cantos = new ArrayList<>(Arrays.asList(0, 2, 6, 8));
        cantos.retainAll(posicoesDisponiveis); // Filtra para manter apenas os cantos disponíveis.
        if (!cantos.isEmpty()) {
            Random random = new Random();
            return cantos.get(random.nextInt(cantos.size()));
        }
        
        // Estratégia 5: Jogada aleatória (último recurso).
        // Se nenhuma das estratégias acima se aplicar, joga em qualquer lugar livre.
        Random random = new Random();
        return posicoesDisponiveis.get(random.nextInt(posicoesDisponiveis.size()));
    }


    /**
     * Verifica se a partida terminou, seja por vitória de um jogador ou por empate.
     * @return true se o jogo acabou, false caso contrário.
     */
    public boolean terminou() {
        // O jogo termina se o Jogador 1 venceu, OU se o Jogador 2 venceu, OU se não há mais espaços livres (empate).
        return verificaVencedor(simbolos[0]) || verificaVencedor(simbolos[1]) || quantidadeJogadas == 9;
    }

    /**
     * Algoritmo que confere todas as 8 combinações de vitória possíveis para um dado símbolo.
     * @param simbolo O símbolo a ser verificado ("X", "O" ou "m").
     * @return true se o símbolo formou uma linha vencedora, false caso contrário.
     */
    private boolean verificaVencedor(String simbolo) {
        // Checa as 3 linhas horizontais
        if (celulas[0].equals(simbolo) && celulas[1].equals(simbolo) && celulas[2].equals(simbolo)) return true;
        if (celulas[3].equals(simbolo) && celulas[4].equals(simbolo) && celulas[5].equals(simbolo)) return true;
        if (celulas[6].equals(simbolo) && celulas[7].equals(simbolo) && celulas[8].equals(simbolo)) return true;
        // Checa as 3 colunas verticais
        if (celulas[0].equals(simbolo) && celulas[3].equals(simbolo) && celulas[6].equals(simbolo)) return true;
        if (celulas[1].equals(simbolo) && celulas[4].equals(simbolo) && celulas[7].equals(simbolo)) return true;
        if (celulas[2].equals(simbolo) && celulas[5].equals(simbolo) && celulas[8].equals(simbolo)) return true;
        // Checa as 2 diagonais
        if (celulas[0].equals(simbolo) && celulas[4].equals(simbolo) && celulas[8].equals(simbolo)) return true;
        if (celulas[2].equals(simbolo) && celulas[4].equals(simbolo) && celulas[6].equals(simbolo)) return true;
        
        return false; // Nenhuma condição de vitória foi atendida.
    }

    // --- MÉTODOS GETTERS (para a interface gráfica usar) --- //

    /**
     * Informa o status final da partida.
     * @return 1 se o Jogador 1 venceu, 2 se o Jogador 2/Máquina venceu, 0 se foi empate, ou -1 se o jogo ainda está em andamento.
     */
    public int getResultado() {
        if (verificaVencedor(simbolos[0])) return 1;
        if (verificaVencedor(simbolos[1])) return 2;
        if (quantidadeJogadas == 9) return 0;
        return -1; // Jogo não terminou.
    }

    /**
     * Retorna o símbolo de um jogador específico.
     * @param numeroJogador 1 para o Jogador 1, 2 para o Jogador 2/Máquina.
     * @return O símbolo (String) do jogador.
     */
    public String getSimbolo(int numeroJogador) {
        if (numeroJogador != 1 && numeroJogador != 2) {
            throw new IllegalArgumentException("Número do jogador deve ser 1 ou 2.");
        }
        return simbolos[numeroJogador - 1];
    }
    
    /**
     * Gera uma representação visual do tabuleiro em formato de texto.
     * Útil para debug ou para uma interface de console.
     * @return Uma String formatada em 3x3 que mostra o estado atual do tabuleiro.
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
     * Retorna uma lista com todas as posições do tabuleiro que ainda estão livres.
     * @return um ArrayList de Integers, onde cada inteiro é o índice de uma célula vazia.
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
     * Retorna uma cópia do histórico de jogadas.
     * @return Um LinkedHashMap contendo os pares <posição, símbolo> de todas as jogadas.
     */
    public LinkedHashMap<Integer, String> getHistorico() {
        return new LinkedHashMap<>(historico); // Retorna uma cópia para evitar modificação externa.
    }

    /**
     * Retorna a quantidade de jogadas realizadas.
     */
    public int getQuantidadeJogadas() {
        return quantidadeJogadas;
    }

    /**
     * Retorna o número do jogador da vez (1 ou 2).
     */
    public int getJogadorAtual() {
        return jogadorAtual;
    }
    
    /**
     * Retorna o símbolo do jogador da vez.
     */
    public String getSimboloJogadorAtual() {
        return getSimbolo(this.jogadorAtual);
    }

    /**
     * Retorna uma cópia segura do array de células do tabuleiro.
     */
    public String[] getCelulas() {
        return Arrays.copyOf(celulas, celulas.length);
    }
    
    /**
     * Informa se o modo de jogo atual é contra a máquina.
     */
    public boolean isModoVsMaquina() {
        return nivelEspertezaMaquina > 0;
    }
}