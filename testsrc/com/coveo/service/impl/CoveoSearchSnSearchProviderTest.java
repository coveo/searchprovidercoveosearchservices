package com.coveo.service.impl;

import com.coveo.constants.SearchprovidercoveosearchservicesConstants;
import com.coveo.pushapiclient.DocumentBuilder;
import com.coveo.pushapiclient.StreamService;
import com.coveo.pushapiclient.UpdateStreamService;
import com.coveo.pushapiclient.exceptions.NoOpenFileContainerException;
import com.coveo.pushapiclient.exceptions.NoOpenStreamException;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.searchservices.admin.data.SnField;
import de.hybris.platform.searchservices.admin.data.SnIndexConfiguration;
import de.hybris.platform.searchservices.admin.data.SnIndexType;
import de.hybris.platform.searchservices.admin.data.SnLanguage;
import de.hybris.platform.searchservices.core.SnException;
import de.hybris.platform.searchservices.core.service.SnContext;
import de.hybris.platform.searchservices.document.data.SnDocument;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationRequest;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationResponse;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchRequest;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchResponse;
import de.hybris.platform.searchservices.enums.SnDocumentOperationStatus;
import de.hybris.platform.searchservices.enums.SnIndexerOperationStatus;
import de.hybris.platform.searchservices.enums.SnIndexerOperationType;
import de.hybris.platform.searchservices.index.data.SnIndex;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hybris.platform.searchservices.indexer.data.SnIndexerOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@UnitTest
public class CoveoSearchSnSearchProviderTest {

    private static final String INDEX_TYPE_ID = "indexTypeId";
    private static final int DOCS_TO_INDEX = 2;
    private static final String LANGUAGE = "en";
    private static final String PRODUCT_CODE_A = "codeA";
    private static final String PRODUCT_NAME_A = "nameA";
    private static final String PRODUCT_CODE_B = "codeB";
    private static final String PRODUCT_NAME_B = "nameB";

    @Mock
    private SnContext snContext;
    @Mock
    private SnIndexType snIndexType;
    @Mock
    private SnIndexConfiguration snIndexConfiguration;
    @Mock
    private SnLanguage snLanguage;
    @Mock
    private SnDocumentBatchRequest snDocumentBatchRequest;
    @Mock
    private SnDocumentBatchOperationRequest requestA;
    @Mock
    private SnDocumentBatchOperationRequest requestB;
    @Mock
    private UpdateStreamService updateStreamService;
    @Mock
    private StreamService rebuildStreamService;

    private CoveoSearchSnSearchProvider coveoSearchSnSearchProvider;
    @Before
    public void setUp() {

        when(snIndexType.getId()).thenReturn(INDEX_TYPE_ID);
        when(snContext.getIndexType()).thenReturn(snIndexType);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(SearchprovidercoveosearchservicesConstants.COVEO_REBUILD_STREAM_SERVICE_KEY, rebuildStreamService);
        attributes.put(SearchprovidercoveosearchservicesConstants.COVEO_UPDATE_STREAM_SERVICE_KEY, updateStreamService);
        when(snContext.getAttributes()).thenReturn(attributes);
        when(snLanguage.getId()).thenReturn(LANGUAGE);
        when(snIndexConfiguration.getLanguages()).thenReturn(Collections.singletonList(snLanguage));
        when(snContext.getIndexConfiguration()).thenReturn(snIndexConfiguration);
        SnDocument snDocumentA = createDocumentFields(PRODUCT_NAME_A, PRODUCT_CODE_A);
        SnDocument snDocumentB = createDocumentFields(PRODUCT_NAME_B, PRODUCT_CODE_B);

        when(requestA.getDocument()).thenReturn(snDocumentA);
        when(requestB.getDocument()).thenReturn(snDocumentB);
        List<SnDocumentBatchOperationRequest> requests = new ArrayList<>();
        requests.add(requestA);
        requests.add(requestB);
        when(snDocumentBatchRequest.getRequests()).thenReturn(requests);

        coveoSearchSnSearchProvider = new CoveoSearchSnSearchProvider();

    }

    private SnDocument createDocumentFields(String name, String code) {
        Map<Locale, Object> localizedName = new HashMap<>();
        SnDocument snDocument = new SnDocument();
        localizedName.put(new Locale(LANGUAGE), name);
        SnField nameField = new SnField();
        nameField.setId("name");
        nameField.setLocalized(true);
        snDocument.setFieldValue(nameField, localizedName);
        SnField codeField = new SnField();
        codeField.setId("code");
        codeField.setLocalized(false);
        snDocument.setFieldValue(codeField, code);
        snDocument.setId(code);
        return snDocument;
    }

    @After
    public void tearDown() throws Exception {
        coveoSearchSnSearchProvider = null;
    }

    @Test
    public void createIndex() throws SnException {
        final SnIndex index = coveoSearchSnSearchProvider.createIndex(snContext);
        assertNotNull(index);
        assertTrue(index.getActive());
        assertEquals(INDEX_TYPE_ID, index.getIndexTypeId());
        assertEquals(INDEX_TYPE_ID, index.getId());
    }

    @Test
    public void createIndexerOperation() throws SnException {
        final SnIndexerOperation operation = coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.FULL, DOCS_TO_INDEX);
        assertEquals(INDEX_TYPE_ID, operation.getIndexTypeId());
        assertEquals(INDEX_TYPE_ID, operation.getIndexId());
        assertEquals(SnIndexerOperationType.FULL, operation.getOperationType());
        assertEquals(SnIndexerOperationStatus.RUNNING, operation.getStatus());
    }

    @Test(expected = SnException.class)
    public void createIndexerOperation_ShouldThrowException_WhenNoStreamService() throws SnException {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(SearchprovidercoveosearchservicesConstants.COVEO_UPDATE_STREAM_SERVICE_KEY, updateStreamService);
        when(snContext.getAttributes()).thenReturn(attributes);
        coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.FULL, DOCS_TO_INDEX);
    }

    @Test(expected = SnException.class)
    public void createIndexerOperation_ShouldThrowException_WhenNoUpdateStreamService() throws SnException {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(SearchprovidercoveosearchservicesConstants.COVEO_REBUILD_STREAM_SERVICE_KEY, rebuildStreamService);
        when(snContext.getAttributes()).thenReturn(attributes);
        coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.FULL, DOCS_TO_INDEX);
    }

    @Test(expected = SnException.class)
    public void createIndexerOperation_ShouldThrowException_WhenNoOperationType() throws SnException {
        coveoSearchSnSearchProvider.createIndexerOperation(snContext, null, DOCS_TO_INDEX);
    }

    @Test
    public void executeDocumentBatch_ForFullOperationType() throws SnException, IOException, InterruptedException {
        coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.FULL, DOCS_TO_INDEX);
        SnDocumentBatchResponse response = coveoSearchSnSearchProvider.executeDocumentBatch(snContext, INDEX_TYPE_ID, snDocumentBatchRequest, INDEX_TYPE_ID);
        assertNotNull(response);
        List<SnDocumentBatchOperationResponse> responses = response.getResponses();
        assertEquals(DOCS_TO_INDEX, responses.size());
        assertThat(responses, hasItem(hasProperty("id", is(PRODUCT_CODE_A))));
        assertThat(responses, hasItem(hasProperty("id", is(PRODUCT_CODE_B))));
        assertTrue(responses.stream().allMatch(item -> SnDocumentOperationStatus.CREATED.equals(item.getStatus())));
        verify(rebuildStreamService, times(2)).add(any());
    }

    @Test
    public void executeDocumentBatch_ForIncrementOperationType() throws SnException, IOException, InterruptedException {
        coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.INCREMENTAL, DOCS_TO_INDEX);
        SnDocumentBatchResponse response = coveoSearchSnSearchProvider.executeDocumentBatch(snContext, INDEX_TYPE_ID, snDocumentBatchRequest, INDEX_TYPE_ID);
        assertNotNull(response);
        List<SnDocumentBatchOperationResponse> responses = response.getResponses();
        assertEquals(DOCS_TO_INDEX, responses.size());
        assertThat(responses, hasItem(hasProperty("id", is(PRODUCT_CODE_A))));
        assertThat(responses, hasItem(hasProperty("id", is(PRODUCT_CODE_B))));
        assertTrue(responses.stream().allMatch(item -> SnDocumentOperationStatus.UPDATED.equals(item.getStatus())));
        verify(updateStreamService, times(2)).addOrUpdate(any());
    }

    @Test
    public void commit_ForFullOperationType() throws SnException, IOException, InterruptedException, NoOpenStreamException {
        coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.FULL, DOCS_TO_INDEX);
        coveoSearchSnSearchProvider.commit(snContext, INDEX_TYPE_ID);
        verify(rebuildStreamService, times(1)).close();
    }

    @Test
    public void commit_ForIncrementOperationType() throws SnException, IOException, NoOpenFileContainerException, InterruptedException {
        coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.INCREMENTAL, DOCS_TO_INDEX);
        coveoSearchSnSearchProvider.commit(snContext, INDEX_TYPE_ID);
        verify(updateStreamService, times(1)).close();
    }

    @Test
    public void createCoveoDocument() {
        SnDocument snDocument = createDocumentFields(PRODUCT_NAME_A, PRODUCT_CODE_A);
        DocumentBuilder coveoDocument = coveoSearchSnSearchProvider.createCoveoDocument(snDocument, LANGUAGE);
        assertNotNull(coveoDocument);
        assertEquals(PRODUCT_NAME_A, coveoDocument.getDocument().title);
    }

    @Test
    public void createCoveoDocument_MissingName() {
        SnDocument snDocument = createDocumentFields("", PRODUCT_CODE_A);
        DocumentBuilder coveoDocument = coveoSearchSnSearchProvider.createCoveoDocument(snDocument, LANGUAGE);
        assertNull(coveoDocument);
    }
}