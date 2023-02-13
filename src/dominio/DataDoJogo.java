package dominio;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public record DataDoJogo(LocalDate data,
                         LocalTime horario,
                         DayOfWeek dia){

    @Override
    public LocalDate data() {
        return data;
    }

    @Override
    public LocalTime horario() {
        return horario;
    }

    @Override
    public DayOfWeek dia() {
        return dia;
    }
}