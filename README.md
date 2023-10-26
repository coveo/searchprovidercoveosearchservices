# Coveo Search Provider SAP Commerce Extension
This extension implements a client that handles communication between SAP Commerce Cloud and Coveo.
The configuration that the extension provides is based upon the [API Registry](https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/aa417173fe4a4ba5a473c93eb730a417/3eed84aaa70d47dfbe3cec317f37e020.html).

## Installation

1. Clone or download the repository to the `hybris/bin/custom` directory of your project.

1. In the project directory, open the `hybris/config/localextensions.xml` file and add the `searchprovidercoveosearchservices` extension:

   ```xml
   <extension name='searchprovidercoveosearchservices' />
   ```

1. Perform step 1 of the installation guide for the [Coveo Push Api client library for java](https://github.com/coveo/push-api-client.java) that's used in this extension.

1. From the root of your project directory, run the following command:

   ```bash
   ant clean all
   ```

1. After the command execution, start the server by executing the `hybris/bin/platform/hybrisserver.sh` script.

1. Update the project from the Hybris Administration Console:

   1. Open the Hybris Administration console at https://localhost:9002/platform/update.
   
   1. Find and select the checkboxes for `searchprovidercoveosearchservices` extension to create the new search provider.

   1. At the top of the page, click the *Update* button.
  
## Integrate With Coveo

1. Create a [Coveo organization](https://docs.coveo.com/en/185)

1. Create a [Catalog Source](https://docs.coveo.com/en/n8of0593)

1. Create an [API key](https://docs.coveo.com/en/1718) with  with the [privilege of editing all sources](https://docs.coveo.com/en/3151/).

   | Service | Privilege | Access level |
   |---|---|---|
   | Content | Sources | `Edit all` |

1. In SAP Backoffice Administration Cockpit, create a Consumed Destination for Coveo:

    1. Go to **System** → **API** → **Destinations** → **Consumed Destinations**.

    1. Create a new Consumed Destination:

         1. **ID**. Enter a unique ID for a new destination.
         
         1. **URL**. Paste the [Stream API URL](https://docs.coveo.com/en/n8of0593#stream-api-url) of your Coveo Catalog source.

         1. In the **Active** section, verify that the **Active** checkbox is selected.

         1. **Destination Target**. Select any destination target.

         
1. Create a credential for the consumed destination:

   1. Open the destination and switch to the **Destination Configuration** tab.

   1. In the **Credential** field, create a **Consumed OAuth Credential**:

      * **ID**. Enter a unique ID for a new credential. 

      * **Client ID**. Enter a client ID for a new credential.
   
   1. In the *Client Secret* section, paste a Coveo API key to the **Password** and **Verify password** fields.

1. Create a search provider:

   1. Go to **System** → **Search and Navigation** → **Search Provider Configurations**.

   1. Create a new search Provider using the **Coveo Search Provider Configuration**.

   1. Once the search provider is created, switch to the **Administration** tab.
   
   1. In the **Consumed Destinations** field, set the Consumed Destination you created in the previous step.

1. Change index Configuration :

   1. Navigate to **System** → **Search and Navigation** → **Index Configurations**. 
   On the right, you can see a list of available configurations. 
   Create or choose an index configuration corresponding to the site you’re adding the Coveo search to.

   1. On the **General** tab, in the **Search Provider Configuration** field, set select the provider you created in the previous steps.

1. Map the catalog fields you want to send to Coveo:

   1. Navigate to the **System** → **Search and Navigation** → **Index Types**.

   1. Within a target index, switch to the **Fields** tab.

   1. Examine the *Identifier* column. 
   Use the values in this column to map the catalog fields to the [Coveo fields](https://docs.coveo.com/en/2036).
   See [Manage source mappings](https://docs.coveo.com/en/1640/) and [Mapping rule syntax reference](https://docs.coveo.com/en/1839/).

1. Run the index:

   1. Navigate to **System** → **Search and Navigation** → **Index Types**.

   1. Within the target index type, switch to the **Cron jobs** tab. 
   
   1. Enable the required cronjob.

   1. While having the index type open, click the **Run indexer** button.

   1. In the modal window that appears, select the cronjob you've enabled and click the **Run** button.


For details, see Coveo documentation on how to [Push data to Coveo (SAP Commerce Cloud 2205 or later)](https://docs.coveo.com/en/ladf2205).
