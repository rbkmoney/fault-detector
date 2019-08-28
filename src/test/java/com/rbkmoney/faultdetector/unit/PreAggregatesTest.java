package com.rbkmoney.faultdetector.unit;

import com.rbkmoney.faultdetector.data.PreAggregates;
import org.junit.Test;

import java.util.Deque;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class PreAggregatesTest {

    @Test
    public void compareTest() {
        Set<PreAggregates> preAggregatesSet = new ConcurrentSkipListSet();
        preAggregatesSet.add(getPreAggregates(1L));
        preAggregatesSet.add(getPreAggregates(3L));
        preAggregatesSet.add(getPreAggregates(2L));
        preAggregatesSet.add(getPreAggregates(5L));
        preAggregatesSet.add(getPreAggregates(4L));
        String aggTimesString = preAggregatesSet.stream()
                .map(agg -> agg.getAggregationTime().toString())
                .collect(Collectors.joining());
        assertEquals("The order of the preaggregates is not equal to the target",
                "12345", aggTimesString);
    }

    private PreAggregates getPreAggregates(Long aggregationTime) {
        PreAggregates preAggregates = new PreAggregates();
        preAggregates.setAggregationTime(aggregationTime);
        return preAggregates;
    }

    @Test
    public void stackTest() {
        Deque<String> people = new ConcurrentLinkedDeque<>();
        people.addFirst("one");
        people.addFirst("two");
        people.addFirst("three");
        people.addFirst("four");

        for (String person : people) {
            System.out.println("1: " + person);
        }

        Iterator<String> descIterator = people.descendingIterator();
        while (descIterator.hasNext()) {
            System.out.println("2: " + descIterator.next());
        }


    }

}
