package ru.ideaplatform.tickets.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {
    private String origin;

    @JsonSetter("origin_name")
    private String originName;
    private String destination;

    @JsonSetter("destination_name")
    private String destinationName;

    @JsonSetter("departure_date")
    @JsonFormat(pattern = "dd.MM.yy")
    private LocalDate departureDate;

    @JsonSetter("departure_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "H:mm")
    private LocalTime departureTime;

    @JsonSetter("arrival_date")
    @JsonFormat(pattern = "dd.MM.yy")
    private LocalDate arrivalDate;

    @JsonSetter("arrival_time")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "H:mm")
    private LocalTime arrivalTime;
    private String carrier;
    private Integer stops;

    @Setter
    @JsonIgnore
    private Long flightTime;

    /**
     * В ТЗ ничего не сказано про тип
     * и так как в json только целые числа выбрал Integer.
     */
    private Integer price;
}
