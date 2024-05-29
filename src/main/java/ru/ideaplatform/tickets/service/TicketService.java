package ru.ideaplatform.tickets.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.java.Log;
import ru.ideaplatform.tickets.model.Flight;
import ru.ideaplatform.tickets.model.Ticket;

import static java.util.stream.Collectors.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
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
                    Duration duration = Duration.between(it.getDepartureTime(), it.getArrivalTime());
                    it.setFlightTime(LocalTime.of(
                            (int) duration.toHours(),
                            (int) (duration.toMinutes() - (duration.toHours() * 60))));
                }) // заполняем время перелета
                // группируем в map, где ключ - название перевозчика,
                // значение минимальное время перелета.
                .collect(groupingBy(Ticket::getCarrier,
                        minBy(Comparator.comparing(Ticket::getFlightTime))));
        // выводим результат в log
        carriers.forEach((k, v) -> log
                .info(String.format("Carrier: %s, minTime: %s", k, v.orElseThrow()
                        .getFlightTime())));
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
        log.info(String.format("Average price: %.2f, Median price: %.2f, Difference: %.2f", medianPrice, averagePrice,
                averagePrice - medianPrice));

    }

    /**
     * Метод для десирилезации json в список POJO
     * @return Перелет со списком билетов.
     */
    private static Flight ticketsToPOJO() {
        Flight flight;
        try {
            File file = new File("src/main/resources/tickets.json");
            flight = objectMapper.readValue(file, Flight.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return flight;
    }
}
