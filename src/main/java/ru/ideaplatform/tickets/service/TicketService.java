package ru.ideaplatform.tickets.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.java.Log;
import ru.ideaplatform.tickets.model.Flight;
import ru.ideaplatform.tickets.model.Ticket;

import static java.util.stream.Collectors.*;

import java.io.File;
import java.io.IOException;
import java.time.*;
import java.util.*;

@Log
public class TicketService {

    /**
     * Инициализация маппера для json.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .registerModule(new JavaTimeModule());

    private static final String ORIGIN_NAME = "Владивосток";
    private static final String DESTINATION_NAME = "Тель-Авив";


    /**
     * Метод для получения минимального времени полета
     * между городами Владивосток и Тель-Авив для каждого авиаперевозчика.
     */
    public static void minimumFlightTime() {
        List<Ticket> tickets = ticketsToPOJO().getTickets();
        Map<String, Optional<Ticket>> carriers = tickets
                .stream() // создаем стрим
                .filter(it -> it.getOriginName().equals(ORIGIN_NAME)
                        && it.getDestinationName().equals(DESTINATION_NAME)) // фильтруем по направлению
                .peek(it -> {
                    Long minutes = mapperLocalDateTimeToMinutes(it.getDepartureDate(), it.getDepartureTime(),
                            it.getArrivalDate(), it.getArrivalTime());
                    log.info(String.valueOf(minutes));
                    it.setFlightTime(minutes);
                    log.info(it.getFlightTime().toString());
                }) // заполняем время перелета
                // группируем в map, где ключ - название перевозчика,
                // значение минимальное время перелета.
                .collect(groupingBy(Ticket::getCarrier,
                        minBy(Comparator.comparing(Ticket::getFlightTime))));
        // выводим результат в log
        carriers.forEach((k, v) -> log
                .info(minutesToString(v.orElseThrow().getFlightTime())));
    }

    /**
     * Метод получения разницы между средней ценой
     * и медианой для полета между городами Владивосток и Тель-Авив.
     */
    public static void differenceBetweenAveragePriceAndMedianValue() {
        // создаем сортированный список цен на перелет по заданному направлению.
        int[] array = ticketsToPOJO().getTickets()
                .stream()
                .filter(it -> it.getOriginName().equals(ORIGIN_NAME)
                        && it.getDestinationName().equals(DESTINATION_NAME))
                .mapToInt(Ticket::getPrice)
                .sorted()
                .toArray();

        // получаем среднюю стоимость перелета
        double averagePrice = Arrays.stream(array).average().orElseThrow();

        // получаем медиану стоимости перелета
        double medianPrice;
        int length = array.length;
        if (length % 2 == 0) {
            medianPrice = (array[length / 2 - 1] + array[length / 2]) / 2.0;
        } else {
            medianPrice = array[(length + 1) / 2 - 1];
        }

        // выводим результат в лог
        log.info(String.format("Median price: %.2f, Average price: %.2f, Difference: %.2f", medianPrice, averagePrice,
                averagePrice - medianPrice));

    }

    /**
     * Метод для десирилезации json в список POJO
     * @return Перелет со списком билетов.
     */
    private static Flight ticketsToPOJO() {
        Flight flight;
        try {
            File file = new File("src/main/resources/tickets_new.json");
            flight = objectMapper.readValue(file, Flight.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return flight;
    }

    private static Long mapperLocalDateTimeToMinutes(LocalDate departureDate, LocalTime departureTime,
                                                     LocalDate arrivalDate, LocalTime arrivalTime) {

        LocalDateTime departure = LocalDateTime.of(departureDate, departureTime);
        LocalDateTime arrival = LocalDateTime.of(arrivalDate, arrivalTime);

        return Duration.between(departure, arrival).toMinutes();
    }

    public static String minutesToString(long minutes) {
        long hours = minutes / 60;
        long days = hours / 24;
        long hoursResult = hours - (days * 24);
        long minutesResult = minutes - (hours * 60);
        if (hours > 24) {
            return days + "/days " + hoursResult + "/hours " + minutesResult + "/minutes";
        } else if (minutes <= 60) {
            return minutes + "/minutes";
        } else {
            return hours + "/hours " + minutesResult + "/minutes";
        }
    }
}
