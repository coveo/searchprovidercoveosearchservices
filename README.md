# SAP Search Provider Coveo Search Services Extension
this extension provides configuration of integration with SAP Commerce (based on apiregistry) and implements a client that handles communication between SAP Commerce and Coveo

## Installation

1. Copy the extension folder to the `hybris/bin/custom` directory of your project.

1. In the project folder, open the `hybris/config/localextensions.xml` file and add the `searchprovidercoveosearchservices` extension:

   ```xml
   <extension name='searchprovidercoveosearchservices' />
   ```

1. From the root of your project folder, run the following command:

   ```bash
   ant clean all
   ```

1. Start the server by executing the `hybris/bin/platform/hybrisserver.sh` script.

1. Update the project in the Hybris Administration Console:

   1. Open the Hybris Administration console at https://localhost:9002/platform/update.
   
   1. Find and select the checkboxes for `searchprovidercoveosearchservices` extension to create the newly introduced fields.

   1. At the top of the page, click the *Update* button.

