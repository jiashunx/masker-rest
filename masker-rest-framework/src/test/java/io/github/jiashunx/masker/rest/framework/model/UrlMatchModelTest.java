package io.github.jiashunx.masker.rest.framework.model;

import static org.junit.Assert.*;
import org.junit.Test;

public class UrlMatchModelTest {

    @Test
    public void test_precomputedPatternModelEquivalence() {
        String url = "/servlet/m/xxx";
        String pattern = "/servlet/m/{abc}";
        UrlMatchModel modelWithComputed = new UrlMatchModel(url, pattern);
        UrlMatchModel modelWithPrecomputed = new UrlMatchModel(url, pattern, new UrlPatternModel(pattern));
        assertEquals(modelWithComputed.isMatched(), modelWithPrecomputed.isMatched());
        assertEquals(modelWithComputed.getUrlPattern(), modelWithPrecomputed.getUrlPattern());
        assertEquals(modelWithComputed.getPlaceholderMap(), modelWithPrecomputed.getPlaceholderMap());
    }

}
