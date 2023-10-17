# Coveo Search Provider SAP Commerce Extension
this extension provides configuration of integration with SAP Commerce (based on apiregistry) and implements a client that handles communication between SAP Commerce and Coveo

## Installation

1. Copy the extension folder to the `hybris/bin/custom` directory of your project.

1. In the project folder, open the `hybris/config/localextensions.xml` file and add the `searchprovidercoveosearchservices` extension:

   ```xml
   <extension name='searchprovidercoveosearchservices' />
   ```

1. Perform step 1 of the installation guide of [Coveo Push Api client library for java](https://github.com/coveo/push-api-client.java) used in this extension

1. From the root of your project folder, run the following command:

   ```bash
   ant clean all
   ```

1. Start the server by executing the `hybris/bin/platform/hybrisserver.sh` script.

1. Update the project in the Hybris Administration Console:

   1. Open the Hybris Administration console at https://localhost:9002/platform/update.
   
   1. Find and select the checkboxes for `searchprovidercoveosearchservices` extension to create the newly coveo search provider.

   1. At the top of the page, click the *Update* button.
  
## Integrate With Coveo

1. Create [Coveo Org](https://docs.coveo.com/en/185/glossary/coveo-organization)
2. Create a [Catalog Source](https://docs.coveo.com/en/n8of0593/coveo-for-commerce/create-a-catalog-source)
3. Create an [API key](https://docs.coveo.com/en/1718/manage-an-organization/manage-api-keys) with privileges to edit the source
4. Create a Consumed Destination for Coveo:
     1. Go to System > API > Destinations > Consumed Destinations.
     2. Create new Consumed Destination
          1. Enter the Source url in URL
          2. Select any destination target
          3. Verify that Active option is set to true.
          4. Double-click Credential to edit the Destination Credentials.
          5. Enter the API key  as the client secret
5. Create a search provider
    1. Go to System > Search and Navigation > Search Provider Configurations.
    2. Create new Search Provider using Coveo Search Provider Template
    3. Add Consumed Destination you created in the previous step.
6. Change index Configuration :
   1. Navigate to System > Search and Navigation > Index Configurations. On the right, you can see a list of available configurations. Create or choose an index configuration that you want to run the Coveo search service on
   2. Navigate to the GENERAL tab.
   3. Set the search Provider configuration to the one created on step 5
7. Run the index
   1. Navigate to System > Search and Navigation > Index Types
   2. Select an index type and enable existing indexing cronjob
   3. Before running the cronjob make sure to create the [fields](https://docs.coveo.com/en/2036/index-content/about-fields) and [mapping](https://docs.coveo.com/en/1640/index-content/manage-source-mappings) in Coveo org for the fields you want to index
   4. Run the cronjob
