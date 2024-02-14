package com.coveo.service.impl;


import com.coveo.constants.SearchprovidercoveosearchservicesConstants;
import com.coveo.data.CoveoSearchSnSearchProviderConfiguration;
import com.coveo.pushapiclient.DocumentBuilder;
import com.coveo.pushapiclient.StreamService;
import com.coveo.pushapiclient.UpdateStreamService;
import com.coveo.pushapiclient.exceptions.NoOpenFileContainerException;
import com.coveo.pushapiclient.exceptions.NoOpenStreamException;
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
import de.hybris.platform.searchservices.indexer.data.SnIndexerOperation;
import de.hybris.platform.searchservices.search.data.SnSearchQuery;
import de.hybris.platform.searchservices.search.data.SnSearchResult;
import de.hybris.platform.searchservices.spi.data.SnExportConfiguration;
import de.hybris.platform.searchservices.spi.service.impl.AbstractSnSearchProvider;
import de.hybris.platform.searchservices.suggest.data.SnSuggestQuery;
import de.hybris.platform.searchservices.suggest.data.SnSuggestResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CoveoSearchSnSearchProvider extends AbstractSnSearchProvider<CoveoSearchSnSearchProviderConfiguration> implements InitializingBean {

    private static final Logger LOG = Logger.getLogger(CoveoSearchSnSearchProvider.class);

    private StreamService rebuildStreamService;

    private UpdateStreamService updateStreamService;

    private SnIndexerOperationType operationType;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public void exportConfiguration(SnExportConfiguration exportConfiguration, List<Locale> locales) throws SnException {
        //there is no need to export any configuration to Coveo at this stage
        //a placeholder to export synonyms dictionaries
    }

    @Override
    public SnIndex createIndex(SnContext context) throws SnException {
        //a placeholder to create the source on Coveo Org
        //for now we will consider that the source is already created
        SnIndex snIndex = new SnIndex();
        snIndex.setId(context.getIndexType().getId());
        snIndex.setIndexTypeId(context.getIndexType().getId());
        snIndex.setActive(true);
        return snIndex;
    }


    @Override
    public void deleteIndex(SnContext context, String indexId) throws SnException {
    }

    @Override
    public SnIndexerOperation createIndexerOperation(SnContext context, SnIndexerOperationType indexerOperationType, int totalItems) throws SnException {
        SnIndexerOperation indexerOperation = new SnIndexerOperation();
        indexerOperation.setIndexId(context.getIndexType().getId());
        indexerOperation.setIndexTypeId(context.getIndexType().getId());
        indexerOperation.setOperationType(indexerOperationType);
        indexerOperation.setStatus(SnIndexerOperationStatus.RUNNING);
        rebuildStreamService = (StreamService) context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_REBUILD_STREAM_SERVICE_KEY);
        updateStreamService = (UpdateStreamService) context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_UPDATE_STREAM_SERVICE_KEY);
        operationType = indexerOperationType;
        if (rebuildStreamService == null || updateStreamService == null || operationType == null) {
            throw new SnException("error creating client service");
        }
        LOG.info("Using index operation type of " + operationType.getCode());
        return indexerOperation;
    }

    @Override
    public SnIndexerOperation updateIndexerOperationStatus(SnContext context, String indexerOperationId, SnIndexerOperationStatus status, String errorMessage) throws SnException {
        return null;
    }


    @Override
    public void completeIndexerOperation(SnContext context, String indexerOperationId) throws SnException {
        //TODO
    }

    @Override
    public void abortIndexerOperation(SnContext context, String indexerOperationId, String message) throws SnException {
        //TODO what should happen if the client abort the indexation before it ends ?
        closeService(context);
    }

    @Override
    public void failIndexerOperation(SnContext context, String indexerOperationId, String message) throws SnException {
    }

    @Override
    public SnDocumentBatchResponse executeDocumentBatch(SnContext context, String indexId, SnDocumentBatchRequest documentBatchRequest, String indexerOperationId) throws SnException {
        List<SnDocumentBatchOperationResponse> responses = new ArrayList<>();
        List<SnDocumentBatchOperationRequest> requests = documentBatchRequest.getRequests();
        if (LOG.isDebugEnabled()) LOG.debug("Document batch with size " + requests.size());

        String language = getLanguage(context);
        if (SnIndexerOperationType.FULL.equals(operationType)) {
            if (LOG.isDebugEnabled()) LOG.debug("Streaming Documents");
            requests.forEach(request -> {
                responses.add(rebuildStreamDocument(language, request));
            });
        } else if (SnIndexerOperationType.INCREMENTAL.equals(operationType)){
            if (LOG.isDebugEnabled()) LOG.debug("Batching Documents");
            requests.forEach(request -> {
                responses.add(incrementStreamDocument(language, request));
            });
        } else {
            LOG.error("Unable to Index due to Unsupported Operation Type: " + operationType.getType());
            throw new SnException("Unsupported Operation Type: " +  operationType.getType());
        }
        SnDocumentBatchResponse documentBatchResponse = new SnDocumentBatchResponse();
        documentBatchResponse.setResponses(responses);
        return documentBatchResponse;
    }

    private SnDocumentBatchOperationResponse rebuildStreamDocument(String language, SnDocumentBatchOperationRequest request) {
        SnDocumentBatchOperationResponse documentBatchOperationResponse = new SnDocumentBatchOperationResponse();
        documentBatchOperationResponse.setId(request.getDocument().getId());
        if (LOG.isDebugEnabled()) LOG.debug("Adding Document " + request.getDocument());
        synchronized (rebuildStreamService) {
            try {
                DocumentBuilder coveoDocument = createCoveoDocument(request.getDocument(), language);
                if (coveoDocument != null) {
                    rebuildStreamService.add(coveoDocument);
                    documentBatchOperationResponse.setStatus(SnDocumentOperationStatus.CREATED);
                }
            } catch (IOException | InterruptedException exception) {
                documentBatchOperationResponse.setStatus(SnDocumentOperationStatus.FAILED);
                LOG.error("failed to index " + request.getDocument().getId(), exception);
            }
        }
        return documentBatchOperationResponse;
    }

    private SnDocumentBatchOperationResponse incrementStreamDocument(String language, SnDocumentBatchOperationRequest request) {
        SnDocumentBatchOperationResponse documentBatchOperationResponse = new SnDocumentBatchOperationResponse();
        documentBatchOperationResponse.setId(request.getDocument().getId());
        if (LOG.isDebugEnabled()) LOG.debug("Adding Document " + request.getDocument());
        synchronized (updateStreamService) {
            try {
                DocumentBuilder coveoDocument = createCoveoDocument(request.getDocument(), language);
                if (coveoDocument != null) {
                    updateStreamService.addOrUpdate(coveoDocument);
                    documentBatchOperationResponse.setStatus(SnDocumentOperationStatus.UPDATED);
                }
            } catch (IOException | InterruptedException exception) {
                documentBatchOperationResponse.setStatus(SnDocumentOperationStatus.FAILED);
                LOG.error("failed to index " + request.getDocument().getId(), exception);
            }
        }
        return documentBatchOperationResponse;
    }

    private String getLanguage(SnContext context) throws SnException {
        List<SnLanguage> languages = context.getIndexConfiguration().getLanguages();
        if (CollectionUtils.isEmpty(languages)) {
            throw new SnException("No Language is specified in index configuration");
        }
        return languages.get(0).getId();
    }

    @Override
    public void commit(SnContext context, String indexId) throws SnException {
        closeService(context);
    }

    @Override
    public SnSearchResult search(SnContext context, String indexId, SnSearchQuery searchQuery) throws SnException {
        //since we are using coveo just in the frontend , this method is never used , it is called by search api from occ
        //and probably from backoffice
        return null;
    }

    @Override
    public SnSuggestResult suggest(SnContext context, String indexId, SnSuggestQuery suggestQuery) throws SnException {
        //since we are using coveo just in the frontend , this method is never used , it is called for autocomplete via occ
        return null;
    }

    protected static class ConverterContext {
        private final Set<Locale> locales;

        public ConverterContext(final List<Locale> locales) {
            this.locales = Set.copyOf(locales);
        }

        public Set<Locale> getLocales() {
            return locales;
        }
    }

    protected DocumentBuilder createCoveoDocument(SnDocument document, String language) {
        Locale locale = new Locale(language);
        Map<String, Object> documentFields = document.getFields();
        String documentName = null;
        if (documentFields.containsKey("name") && ((HashMap<Locale, String>) documentFields.get("name")).containsKey(locale)) {
            documentName = ((HashMap<Locale, String>) documentFields.get("name")).get(locale);
        }

        // If the value is still blank at this point we are unable to build the document
        if (StringUtils.isBlank(documentName)) {
            LOG.warn("SnDocument with id " + document.getId() + " does not have a name field, will not push this document");
            return null;
        }

        DocumentBuilder documentBuilder = new DocumentBuilder(getUri(document), documentName)
                .withMetadata(document.getFields());
        if (LOG.isDebugEnabled()) LOG.debug("Coveo Document " + documentBuilder);
        return documentBuilder;
    }

    private static String getUri(SnDocument document) {
        //TODO just to make the URI valid
        return "https://sapcommerce/product/p/" + (String) document.getFields().get("code");
    }

    private void closeService(SnContext context) throws SnException {
        if (LOG.isDebugEnabled()) LOG.debug("Closing Service");
        try {
            if (SnIndexerOperationType.FULL.equals(operationType)) {
                rebuildStreamService.close();
            } else if (SnIndexerOperationType.INCREMENTAL.equals(operationType)) {
                updateStreamService.close();
            }
        } catch (IOException | InterruptedException | NoOpenStreamException | NoOpenFileContainerException exception) {
            throw new SnException(exception);
        }
    }
}
