package com.coveo.service.impl;

import com.coveo.constants.SearchprovidercoveosearchservicesConstants;
import com.coveo.data.CoveoSearchSnSearchProviderConfiguration;
import com.coveo.pushapiclient.StreamService;
import com.coveo.pushapiclient.UpdateStreamService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.apiregistryservices.model.ConsumedDestinationModel;
import de.hybris.platform.apiregistryservices.model.ConsumedOAuthCredentialModel;
import de.hybris.platform.apiregistryservices.services.DestinationService;
import de.hybris.platform.searchservices.admin.data.SnIndexConfiguration;
import de.hybris.platform.searchservices.indexer.service.SnIndexerRequest;
import de.hybris.platform.searchservices.indexer.service.impl.DefaultSnIndexerContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@UnitTest
public class CoveoSnIndexerContextFactoryTest {

    DefaultSnIndexerContext context;

    @InjectMocks
    CoveoSnIndexerContextFactory coveoSnIndexerContextFactory = new CoveoSnIndexerContextFactory();

    @Mock
    private DestinationService<ConsumedDestinationModel> destinationService;

    @Before
    public void setUp() {
        context = new DefaultSnIndexerContext();
        SnIndexConfiguration indexConfiguration = new SnIndexConfiguration();
        CoveoSearchSnSearchProviderConfiguration coveoSearchSnSearchProviderConfiguration = new CoveoSearchSnSearchProviderConfiguration();
        coveoSearchSnSearchProviderConfiguration.setDestinationId("destinationId");
        coveoSearchSnSearchProviderConfiguration.setDestinationTargetId("destinationTargetId");
        indexConfiguration.setSearchProviderConfiguration(coveoSearchSnSearchProviderConfiguration);
        context.setIndexConfiguration(indexConfiguration);

        ConsumedDestinationModel consumedDestinationModel = new ConsumedDestinationModel();
        consumedDestinationModel.setUrl("https://api.cloud.coveo.com/push/v1/organizations/organizationId/sources/sourceId/");
        ConsumedOAuthCredentialModel consumedOAuthCredentialModel = new ConsumedOAuthCredentialModel();
        consumedOAuthCredentialModel.setClientSecret("client");
        consumedDestinationModel.setCredential(consumedOAuthCredentialModel);
        when(destinationService.getDestinationByIdAndByDestinationTargetId("destinationId", "destinationTargetId")).thenReturn(consumedDestinationModel);

    }

    @After
    public void tearDown() {
        context = null;
    }

    @Test
    public void populateIndexerContext() {
        coveoSnIndexerContextFactory.populateIndexerContext(context, mock(SnIndexerRequest.class));
        Object updateStreamService = context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_UPDATE_STREAM_SERVICE_KEY);
        Object rebuildStreamService = context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_REBUILD_STREAM_SERVICE_KEY);
        assertNotNull(updateStreamService);
        assertTrue(updateStreamService instanceof UpdateStreamService);
        assertNotNull(rebuildStreamService);
        assertTrue(rebuildStreamService instanceof StreamService);
    }
}