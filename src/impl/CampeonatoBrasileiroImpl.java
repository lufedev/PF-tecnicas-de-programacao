package impl;

import dominio.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CampeonatoBrasileiroImpl {

    private Map<Integer, List<Jogo>> brasileirao;
    private List<Jogo> jogos;
    private Predicate<Jogo> filtro;

    public CampeonatoBrasileiroImpl(Path arquivo, Predicate<Jogo> filtro) throws IOException {
        this.jogos = lerArquivo(arquivo);
        this.filtro = filtro;
        this.brasileirao = jogos.stream()
                .filter(filtro) //filtrar por ano
                .collect(Collectors.groupingBy(
                        Jogo::rodada,
                        Collectors.mapping(Function.identity(), Collectors.toList())));

    }

    public List<Jogo> lerArquivo(Path file) throws IOException {
        List<Jogo> jogos = new ArrayList<>();
        Map<String, String> diasDaSemana = new HashMap<>();
        diasDaSemana.put("sábado", "SATURDAY");
        diasDaSemana.put("domingo", "SUNDAY");
        diasDaSemana.put("segunda-feira", "MONDAY");
        diasDaSemana.put("terça-feira", "TUESDAY");
        diasDaSemana.put("quarta-feira", "WEDNESDAY");
        diasDaSemana.put("quinta-feira", "THURSDAY");
        diasDaSemana.put("sexta-feira", "FRIDAY");

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            reader.readLine(); // Ignorar a primeira linha com os nomes das colunas
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(";");
                Integer rodada = Integer.parseInt(fields[0]);
                LocalDate data = LocalDate.parse(fields[1], DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                String horarioString = fields[2];
                LocalTime horario = null;
                if (!horarioString.isEmpty()) {
                    if (horarioString.charAt(2) == 'h') {
                        horario = LocalTime.parse(fields[2], DateTimeFormatter.ofPattern("HH'h'mm"));
                    } else {
                        horario = LocalTime.parse(fields[2], DateTimeFormatter.ofPattern("HH':'mm"));
                    }
                }
                String dia = fields[3].toLowerCase();
                String diaEmIngles = diasDaSemana.get(dia);
                DayOfWeek dayOfWeek = DayOfWeek.valueOf(diaEmIngles);
                Time mandante = new Time(fields[4]);
                Time visitante = new Time(fields[5]);
                Time vencedor = fields[6].equals("-") ? null : new Time(fields[6]);
                String arena = fields[7];
                Integer mandantePlacar = Integer.parseInt(fields[8]);
                Integer visitantePlacar = Integer.parseInt(fields[9]);
                String estadoMandante = fields[10];
                String estadoVisitante = fields[11];
                String estadoVencedor = fields[12];
                DataDoJogo dataDoJogo = new DataDoJogo(data, horario, dayOfWeek);
                Jogo jogo = new Jogo(rodada, dataDoJogo, mandante, visitante, vencedor, arena, mandantePlacar, visitantePlacar, estadoMandante, estadoVisitante, estadoVencedor);
                jogos.add(jogo);
            }
        }

        return jogos;
    }

    public IntSummaryStatistics getEstatisticasPorJogo() {
        IntSummaryStatistics statistics = brasileirao.values().stream()
                .flatMap(List::stream)
                .mapToInt(jogo -> jogo.mandantePlacar() + jogo.visitantePlacar())
                .summaryStatistics();

        return statistics;
    }
    private Map<Jogo, Integer> getMediaGolsPorJogo() {
        return this.brasileirao.values().stream().flatMap(List::stream)
                .collect(Collectors.toMap(Function.identity(), jogo -> (jogo.mandantePlacar() + jogo.visitantePlacar()) / 2));
    }

    public List<Jogo> todosOsJogos() {
        return this.brasileirao.values().stream().flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public Long getTotalVitoriasEmCasa() {
        return brasileirao.values().stream()
                .flatMap(jogosRodada -> jogosRodada.stream())
                .filter(jogo -> jogo.mandantePlacar() > jogo.visitantePlacar())
                .count();
    }

    public Long getTotalVitoriasForaDeCasa() {
        return brasileirao.values().stream()
                .flatMap(jogosRodada -> jogosRodada.stream())
                .filter(jogo -> jogo.mandantePlacar() < jogo.visitantePlacar())
                .count();
    }

    public Long getTotalEmpates() {
        return brasileirao.values().stream()
                .flatMap(jogosRodada -> jogosRodada.stream())
                .filter(jogo -> jogo.mandantePlacar() == jogo.visitantePlacar())
                .count();
    }

    public Long getTotalJogosComMenosDe3Gols() {
        return brasileirao.values().stream()
                .flatMap(jogosRodada -> jogosRodada.stream())
                .filter(jogo -> (jogo.mandantePlacar() + jogo.visitantePlacar()) < 3)
                .count();
    }

    public Long getTotalJogosCom3OuMaisGols() {
        return brasileirao.values().stream()
                .flatMap(List::stream)
                .filter(jogo -> jogo.mandantePlacar() + jogo.visitantePlacar() >= 3)
                .count();
    }

    public Map<Resultado, Long> getTodosOsPlacares() {
        return brasileirao.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(jogo -> new Resultado(jogo.mandantePlacar(), jogo.visitantePlacar()), Collectors.counting()));
    }

    public Map.Entry<Resultado, Long> getPlacarMaisRepetido() {
        Map<Resultado, Long> placares = brasileirao.values().stream().flatMap(List::stream)
                .collect(Collectors.groupingBy(jogo -> new Resultado(jogo.mandantePlacar(), jogo.visitantePlacar()), Collectors.counting()));
        return Collections.max(placares.entrySet(), Map.Entry.comparingByValue());
    }
    public Map.Entry<Resultado, Long> getPlacarMenosRepetido() {
        Map<Resultado, Long> placares = brasileirao.values().stream().flatMap(List::stream)
                .collect(Collectors.groupingBy(jogo -> new Resultado(jogo.mandantePlacar(), jogo.visitantePlacar()), Collectors.counting()));
        return Collections.min(placares.entrySet(), Map.Entry.comparingByValue());
    }

    private List<Time> getTodosOsTimes() {
        List<Jogo> jogosFiltrados = this.brasileirao.values().stream().flatMap(List::stream).collect(Collectors.toList());
        return jogosFiltrados.stream()
                .map(jogo -> Arrays.asList(jogo.mandante(), jogo.visitante()))
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    private Map<Time, List<Jogo>> getTodosOsJogosPorTimeComoMandantes() {
        Map<Time, List<Jogo>> jogosPorTimeMandantes = new HashMap<>();
        for (Map.Entry<Integer, List<Jogo>> entry : brasileirao.entrySet()) {
            List<Jogo> jogosDaRodada = entry.getValue();
            for (Jogo jogo : jogosDaRodada) {
                Time timeMandante = jogo.mandante();
                List<Jogo> jogosDoTime = jogosPorTimeMandantes.computeIfAbsent(timeMandante, k -> new ArrayList<>());
                jogosDoTime.add(jogo);
            }
        }
        return jogosPorTimeMandantes;
    }

    private Map<Time, List<Jogo>> getTodosOsJogosPorTimeComoVisitante() {
        Map<Time, List<Jogo>> jogosPorTimeVisitante = new HashMap<>();
        for (Map.Entry<Integer, List<Jogo>> entry : brasileirao.entrySet()) {
            List<Jogo> jogosDaRodada = entry.getValue();
            for (Jogo jogo : jogosDaRodada) {
                Time timeVisitante = jogo.visitante();
                List<Jogo> jogosDoTime = jogosPorTimeVisitante.computeIfAbsent(timeVisitante, k -> new ArrayList<>());
                jogosDoTime.add(jogo);
            }
        }
        return jogosPorTimeVisitante;
    }

    public Map<Time, List<Jogo>> getTodosOsJogosPorTime() {
        return jogos.stream()
                .filter(filtro)
                .collect(Collectors.groupingBy(Jogo::mandante, Collectors.toList()))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .flatMap(jogo -> Stream.of(jogo.mandante(), jogo.visitante()))
                                .distinct()
                                .map(time -> jogos.stream()
                                        .filter(jogo -> jogo.mandante().equals(time) || jogo.visitante().equals(time))
                                        .collect(Collectors.toList()))
                                .flatMap(List::stream)
                                .collect(Collectors.toList())));
    }

    public Map<Time, Map<Boolean, List<Jogo>>> getJogosParticionadosPorMandanteTrueVisitanteFalse() {
        return jogos.stream()
                .filter(filtro)
                .collect(Collectors.groupingBy(Jogo::mandante, Collectors.toList()))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .collect(Collectors.partitioningBy(jogo -> jogo.mandante().equals(entry.getKey())))
                ));
    }

    public Set<PosicaoTabela> getTabela() {
        return jogos.stream()
                .filter(filtro)
                .collect(Collectors.groupingBy(Jogo::mandante, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    Time time = entry.getKey();
                    List<Jogo> jogosDoTime = entry.getValue();
                    Long vitorias = jogosDoTime.stream().filter(Jogo::vitoria).count();
                    Long derrotas = jogosDoTime.stream().filter(Jogo::perdedor).count();
                    Long empates = jogosDoTime.stream().filter(Jogo::empate).count();
                    Long golsPositivos = jogosDoTime.stream().mapToLong(jogo -> jogo.mandante().equals(time) ? jogo.mandantePlacar() : jogo.visitantePlacar()).sum();
                    Long golsSofridos = jogosDoTime.stream().mapToLong(jogo -> jogo.mandante().equals(time) ? jogo.visitantePlacar() : jogo.mandantePlacar()).sum();
                    Long saldoDeGols = golsPositivos - golsSofridos;
                    Long jogosRealizados = (long) jogosDoTime.size();
                    return new PosicaoTabela(time, vitorias, derrotas, empates, golsPositivos, golsSofridos, saldoDeGols, jogosRealizados);
                }).collect(Collectors.toSet());
    }

    private DayOfWeek getDayOfWeek(String dia) {
        return Arrays.stream(DayOfWeek.values())
                .filter(day -> day.toString().toLowerCase().startsWith(dia.toLowerCase().substring(0, 3)))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    private Map<Integer, Integer> getTotalGolsPorRodada() {
        return jogos.stream()
                .collect(Collectors.groupingBy(Jogo::rodada, Collectors.summingInt(jogo -> jogo.mandantePlacar() + jogo.visitantePlacar())));
    }

    private Map<Time, Integer> getTotalDeGolsPorTime() {
        return jogos.stream()
                .flatMap(jogo -> Stream.of(jogo.mandante(), jogo.visitante()))
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        time -> jogos.stream()
                                .filter(jogo -> jogo.mandante().equals(time) || jogo.visitante().equals(time))
                                .collect(Collectors.summingInt(jogo -> jogo.mandantePlacar() + jogo.visitantePlacar()))
                ));
    }

    private Map<Integer, Double> getMediaDeGolsPorRodada() {
        Map<Integer, Integer> totalGolsPorRodada = getTotalGolsPorRodada();
        return totalGolsPorRodada.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (double) entry.getValue() / jogos.stream().filter(jogo -> jogo.rodada() == entry.getKey()).count()
                ));
    }

}