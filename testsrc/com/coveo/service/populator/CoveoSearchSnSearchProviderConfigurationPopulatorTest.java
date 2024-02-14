package com.coveo.service.populator;

import com.coveo.data.CoveoSearchSnSearchProviderConfiguration;
import com.coveo.model.CoveoSearchSnSearchProviderConfigurationModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.apiregistryservices.jalo.ConsumedDestination;
import de.hybris.platform.apiregistryservices.model.ConsumedDestinationModel;
import de.hybris.platform.apiregistryservices.model.DestinationTargetModel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

@UnitTest
public class CoveoSearchSnSearchProviderConfigurationPopulatorTest {

    private static final String DESTINATION_ID = "destinationId";
    private static final String DESTINATION_TARGET_ID = "destinationTargetId";

    private CoveoSearchSnSearchProviderConfigurationModel source;
    private CoveoSearchSnSearchProviderConfiguration target;

    CoveoSearchSnSearchProviderConfigurationPopulator populator = new CoveoSearchSnSearchProviderConfigurationPopulator();

    @Before
    public void setUp() {
        source = new CoveoSearchSnSearchProviderConfigurationModel();
        target = new CoveoSearchSnSearchProviderConfiguration();

        ConsumedDestinationModel consumedDestinationModel = new ConsumedDestinationModel();
        consumedDestinationModel.setId(DESTINATION_ID);
        DestinationTargetModel destinationTargetModel = new DestinationTargetModel();
        destinationTargetModel.setId(DESTINATION_TARGET_ID);
        consumedDestinationModel.setDestinationTarget(destinationTargetModel);
        source.setConsumedDestination(consumedDestinationModel);
        target = new CoveoSearchSnSearchProviderConfiguration();
    }

    @After
    public void tearDown() {
        source = null;
        target = null;
    }

    @Test
    public void populate() {
        populator.populate(source, target);
        Assert.assertEquals(DESTINATION_ID, target.getDestinationId());
        Assert.assertEquals(DESTINATION_TARGET_ID, target.getDestinationTargetId());
    }
}