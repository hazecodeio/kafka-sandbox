package com.fenago.kafka.producer.support;

import com.fenago.kafka.model.StockPrice;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class StockSender implements Runnable{

    private final StockPrice stockPriceHigh;
    private final StockPrice stockPriceLow;
    private final Producer<String, StockPrice> producer;
    private final Logger logger = LoggerFactory.getLogger(StockSender.class);
    private final String topic;

    public StockSender(final String topic, final StockPrice stockPriceHigh,
                       final StockPrice stockPriceLow,
                       final Producer<String, StockPrice> producer) {
        this.stockPriceHigh = stockPriceHigh;
        this.stockPriceLow = stockPriceLow;
        this.producer = producer;
        this.topic = topic;
    }


    public void run() {
        final Random random = new Random(System.currentTimeMillis());
        int sentCount = 0;

        while (true) {
            sentCount++;
            final ProducerRecord <String, StockPrice> record =
                                        createRandomRecord(random);

            try {
                final Future<RecordMetadata> future = producer.send(record);
                if (sentCount % 5 == 0) {displayRecordMetaData(record, future);}
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                if (Thread.interrupted()) {
                    break;
                }
            } catch (Exception e) {
                logger.error("problem sending record to producer", e);
            }
        }
    }

    private void displayRecordMetaData(final ProducerRecord<String, StockPrice> record,
                                       final Future<RecordMetadata> future)
                                throws InterruptedException, ExecutionException {
        final RecordMetadata recordMetadata = future.get();
        logger.info(String.format("\n\t\t\tkey=%s, value=%s " +
                        "\n\t\t\tsent to topic=%s part=%d off=%d at time=%s",
                record.key(),
                record.value().toJson(),
                recordMetadata.topic(),
                recordMetadata.partition(),
                recordMetadata.offset(),
                new Date(recordMetadata.timestamp())
                ));
    }

    private int randomIntBetween(final Random random,
                                       final int max,
                                       final int min) {
        return random.nextInt(max - min + 1) + min;
    }

    private ProducerRecord<String, StockPrice> createRandomRecord(
                final Random random) {

        final int dollarAmount = randomIntBetween(random,
                stockPriceHigh.getDollars(), stockPriceLow.getDollars());

        final int centAmount = randomIntBetween(random,
                stockPriceHigh.getCents(), stockPriceLow.getCents());

        final StockPrice stockPrice = new StockPrice(
                stockPriceHigh.getName(), dollarAmount, centAmount);

        return new ProducerRecord<>(topic, stockPrice.getName(),
                stockPrice);
    }
}
