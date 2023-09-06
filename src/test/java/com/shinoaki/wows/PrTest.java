package com.shinoaki.wows;

import com.shinoaki.wows.api.data.ShipInfo;
import com.shinoaki.wows.api.pr.PrCalculation;
import com.shinoaki.wows.api.pr.PrCalculationDetails;
import org.junit.Test;

import java.util.List;

/**
 * @author Xun
 * @date 2023/5/10 23:54 星期三
 */
public class PrTest {


    @Test
    public void prTestV2() {
        PrCalculation a1 = new PrCalculation(0, 378514, 6, 4);
        PrCalculation a2 = new PrCalculation(0, 257377.05176087, 3.569681586001, 2.528365056522);
        PrCalculationDetails pr = PrCalculation.pr(a2, a1, a2);
        //v2
        System.out.println(pr);
        var l = List.of(ShipInfo.prInfo(1, 2, 54468, 1, 1),
                ShipInfo.prInfo(2, 1, 155185, 1, 1),
                ShipInfo.prInfo(3, 1, 51576, 1, 2),
                ShipInfo.prInfo(4, 1, 117285, 1, 2));
        var m = List.of(new PrCalculation(1, 53714.65373459, 0.69215373459027, 50.076602610586),
                new PrCalculation(2, 46214.891317926, 0.80169477409952, 51.192287112219),
                new PrCalculation(3, 25865.747542449, 0.69754915102768, 51.114238159069),
                new PrCalculation(4, 77867.10543131, 0.68613019169328, 50.376775159745));
        PrCalculationDetails prCalculationDetails = PrCalculation.shipList(l, m);
        System.out.println(prCalculationDetails);
    }
}
