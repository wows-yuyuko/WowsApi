package com.shinoaki.wows;

import com.shinoaki.wows.api.pr.PrData;
import com.shinoaki.wows.api.pr.PrUtils;
import org.junit.Test;

/**
 * @author Xun
 * @date 2023/5/10 23:54 星期三
 */
public class PrTest {

    @Test
    public void prTest(){
        PrData user = PrData.server(378514,6,4);
        PrData server = PrData.server(258054.12643816,3.5773156633305,2.530217729783);
        int pr = PrUtils.pr(user, server);
        System.out.println(pr);
    }
}
