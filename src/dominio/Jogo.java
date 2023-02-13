package dominio;

import java.util.Objects;

public record Jogo(Integer rodada,
                   DataDoJogo data,
                   Time mandante,
                   Time visitante,
                   Time vencedor,
                   String arena,
                   Integer mandantePlacar,
                   Integer visitantePlacar,
                   String estadoMandante,
                   String estadoVisitante,
                   String estadoVencedor){


    @Override
    public Integer mandantePlacar() {
        return mandantePlacar;
    }

    @Override
    public Integer visitantePlacar() {
        return visitantePlacar;
    }

    @Override
    public DataDoJogo data() {
        return data;
    }

    @Override
    public Time mandante() {
        return mandante;
    }

    @Override
    public Time visitante() {
        return visitante;
    }

    public boolean vitoria() {
        return mandantePlacar() > visitantePlacar();
    }
    public boolean perdedor() {
        return mandantePlacar() < visitantePlacar();
    }

    public boolean empate() {
        return Objects.equals(mandantePlacar(), visitantePlacar());
    }

    @Override
    public Integer rodada() {
        return rodada;
    }

    @Override
    public Time vencedor() {
        return vencedor;
    }
}

