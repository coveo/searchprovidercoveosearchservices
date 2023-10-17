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
4. 

