package com.fenago.kafka.producer;

import com.fenago.kafka.StockAppConstants;

import static com.fenago.kafka.producer.support.StockPriceSerializer.StockPriceProducerUtils.*;

public class StockPriceProducer {

    public static void main(final String... args) {
        startProducer(StockAppConstants.TOPIC);
    }
}
