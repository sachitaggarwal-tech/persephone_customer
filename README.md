# persephone_customer_service

## Description
This app enables user to read/write customers to S4Hana cloud. This app is part of project 'Persephone' to develop extensions for S4Hana using SAP Cloud Platform CF. 
Persephone Appplication enables Sales Team to create new proposals which are stored in CP for approval . Once approved these proposals are created as BusinessPartners in S4Hana with two additional metadata fields ProposedBy and ApprovedBy

In Below application we use S4Hana SDK APIs to connect to S4Hana backend and expose Create/Read operations on BuPa and we use CAP to generate OData service.

# Table of Contents
* [Installation](README.md#2.Installation)
* [Running Application](README.md#3.Running-Application)
* [Contribution Guide](README.md#4.Contributing)
* [Code Walkthrough](README.md#5.Code-Walkthrough)


# 1.Installation
## Create new destination - "destination" instance
1. Create new destination instance with name 'destination'
2. Open dashboard for destinations and create new destination for S4Hana with name 's4hana'

## Create new UAA - "web-uaa" instance


##  Get project sources in Web IDE 
1. Right click on workspace in your WebIDE 'Files' navigation view and select Git > Clone Repository
2. Specify this repo URL
3. Project should be imported and available in your workspace
4. Select project , right click and go to project settings and reinstall builder . Wait till installation is done , once installation is done , click save and close
5. Select project and perform Build CDS action
6. Select project and perform build action using right click
6. If build is succesful it will generate MTA folder for your project in workspace
7. Navigate to mta file for your project in MTA folder and click on deploy. Select required CF space to deploy app

##  Get project sources local and deploy (Optional)
1. Use git clone \<project URL.git\>_ to download the zip of this project to a local folder. 
2. Follow steps mentioned [here](https://help.sap.com/viewer/58746c584026430a890170ac4d87d03b/Cloud/en-US/9f778dba93934a80a51166da3ec64a05.html) to enable MTA build in local system
3. Open a command window and perform MTA build for your app
4. Via console login to your account. eg. if working on Europe: 
    ```
    cf api https://api.cf.eu10.hana.ondemand.com
    cf login 
    ```
    >**Hint:** If you want to find out which target are you currently using:
    > ```
    >  cf target
    >  ```
5. Run the command **deploy** of the **CLI**:
    ```
    cf deploy <app name>
    ```


   
# 3.Running Application
* Open APP - https://cfappurldeployedabove/odata/v2/CatalogService/Customer to view list of customers from S4Hana  
* Open APP - https://cfappurldeployedabove/odata/v2/CatalogService/Customer('<customer id>') to view list of customers from S4Hana
* Perform 'post' operation to https://cfappurldeployedabove/odata/v2/CatalogService/Customer to create new customer in S4Hana
    * Payload - {  "CustomerFirstName" : "Yelo", "CustomerLastName"  : "Dhawan", "CustomerCountry": "IN", "CustomerCity": "Mumbai"}
  

# 4.Code Walkthrough
* my-service.cds file defines metadata for Customer Entity to be exposed as OData service
* CustomerService.java as handlers for different methods on entity like Read, Query and Create ; this method uses S4Hana SDK APIs to work with S4Hana using destinations


# 5.Contributing
Find the contribution guide here: [Contribution Guidelines](docs/CONTRIBUTING.md)
