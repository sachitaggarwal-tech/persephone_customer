package com.company.customerservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sap.cloud.sdk.s4hana.connectivity.ErpConfigContext;
import com.sap.cloud.sdk.s4hana.connectivity.ErpEndpoint;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartner;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartnerAddress;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartnerField;
import com.sap.cloud.sdk.s4hana.datamodel.odata.namespaces.businesspartner.BusinessPartnerRole;
import com.sap.cloud.sdk.s4hana.datamodel.odata.services.DefaultBusinessPartnerService;
import com.sap.cloud.sdk.service.prov.api.operations.Query;
import com.sap.cloud.sdk.service.prov.api.operations.Read;
import com.sap.cloud.sdk.service.prov.api.EntityData;
import com.sap.cloud.sdk.service.prov.api.operations.Create;
import com.sap.cloud.sdk.service.prov.api.request.QueryRequest;
import com.sap.cloud.sdk.service.prov.api.request.ReadRequest;
import com.sap.cloud.sdk.service.prov.api.request.CreateRequest;
import com.sap.cloud.sdk.service.prov.api.response.ErrorResponse;
import com.sap.cloud.sdk.service.prov.api.response.QueryResponse;
import com.sap.cloud.sdk.service.prov.api.response.ReadResponse;
import com.sap.cloud.sdk.service.prov.api.response.CreateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
	Customer Service Handler to map Customer Odata calls to S4Hana backend
*/
public class CustomerService {

	private static final String S4HANA_DEST_NAME = "s4hana";
	private Logger logger = LoggerFactory.getLogger(CustomerService.class);
	private static final ErpEndpoint endpoint = new ErpEndpoint(new ErpConfigContext(S4HANA_DEST_NAME));
	private static final String APPROVED_BY = "YY1_ApprovedBy_bus";
	private static final String PROPOSED_BY = "YY1_ProposedBy_bus";

	//get customers operation
	@Query(serviceName = "CustomerService", entity = "Customer")
	public QueryResponse queryA_Customer(QueryRequest qryRequest) {
		logger.error("querying for customers");
		QueryResponse queryResponse = null;
		try {
			List<BusinessPartner> businessPartnerList = new DefaultBusinessPartnerService().getAllBusinessPartner()
					.filter(BusinessPartner.CUSTOMER.ne("")).filter(BusinessPartner.BUSINESS_PARTNER_CATEGORY.eq("1"))
					.select(BusinessPartner.BUSINESS_PARTNER, BusinessPartner.FIRST_NAME, BusinessPartner.LAST_NAME,
							BusinessPartner.BUSINESS_PARTNER_CATEGORY, BusinessPartner.CORRESPONDENCE_LANGUAGE,
							BusinessPartner.TO_BUSINESS_PARTNER_ADDRESS.select(BusinessPartnerAddress.COUNTRY,
									BusinessPartnerAddress.CITY_NAME),
							new BusinessPartnerField<String>(APPROVED_BY),
							new BusinessPartnerField<String>(PROPOSED_BY))
					.execute(endpoint);

			List<Customer> customersList = convertBusinessPartnersToCustomers(businessPartnerList);
			queryResponse = QueryResponse.setSuccess().setData(customersList).response();
		} catch (Exception e) {
			logger.error("!!Some error occurred while querying Business Partners!!" + e.getMessage(), e);
			ErrorResponse er = ErrorResponse.getBuilder()
					.setMessage("Query error occurred for reading business partners. " + e.getMessage())
					.setStatusCode(500).setCause(e).response();
			queryResponse = QueryResponse.setError(er);
		}
		return queryResponse;
	}

	// Read A_Customer
	@Read(serviceName = "CustomerService", entity = "Customer")
	public ReadResponse readA_Customer(ReadRequest readRequest) {
		logger.error("reading customer");
		ReadResponse readResponse = null;
		Map<String, Object> keys = readRequest.getKeys();
		if (keys != null && keys.size() == 1) {
			logger.error(keys.toString());
			try {
				final BusinessPartner businessPartnerEntity = new DefaultBusinessPartnerService()
						.getBusinessPartnerByKey(keys.get("CustomerId").toString())
						.select(BusinessPartner.BUSINESS_PARTNER, BusinessPartner.FIRST_NAME, BusinessPartner.LAST_NAME,
								BusinessPartner.BUSINESS_PARTNER_CATEGORY, BusinessPartner.CORRESPONDENCE_LANGUAGE,
								BusinessPartner.TO_BUSINESS_PARTNER_ADDRESS.select(BusinessPartnerAddress.COUNTRY,
										BusinessPartnerAddress.CITY_NAME),
								new BusinessPartnerField<String>(APPROVED_BY),
								new BusinessPartnerField<String>(PROPOSED_BY))
						.execute(endpoint);

				readResponse = ReadResponse.setSuccess()
						.setData(convertBusinessPartnerToCustomer(businessPartnerEntity)).response();
			} catch (final Exception e) {
				logger.error("Failed to read BusinessPartner entity service from S/4HANA: " + e.getMessage() + ".", e);
				ErrorResponse errorResponse = ErrorResponse.getBuilder()
						.setMessage("Read error occurred for business partner from S/4Hana. " + e.getMessage())
						.setStatusCode(500).setCause(e).response();
				readResponse = ReadResponse.setError(errorResponse);
			}
		} else {
			logger.error("Missing Business Partner Id for read request");
			ErrorResponse errorResponse = ErrorResponse.getBuilder()
					.setMessage("Invalid Request.Missing business partner id for request").setStatusCode(500)
					.response();
			readResponse = ReadResponse.setError(errorResponse);
		}
		return readResponse;
	}

	// Create A_Customer
	@Create(serviceName = "CustomerService", entity = "Customer")
	public CreateResponse writeA_Customer(CreateRequest createRequest) {
		logger.error("Creating business partner");
		CreateResponse createResponse = null;
		try {
			BusinessPartner bpInput = initializeBusinessPartner(createRequest);
			BusinessPartner businessPartner = new DefaultBusinessPartnerService().createBusinessPartner(bpInput)
					.execute(endpoint);
			String result = businessPartner.getBusinessPartner();
			logger.error("Created - " + result);
			Customer customer = convertBusinessPartnerToCustomer(businessPartner);
			createResponse = CreateResponse.setSuccess().setData(customer).response();
		} catch (Exception e) {
			logger.error("==> Exception calling S4Hana API for Create of a Product: " + e.getMessage(), e);
			ErrorResponse errorResponse = ErrorResponse.getBuilder()
					.setMessage("Error occurred for creating business partner. " + e.getMessage()).setStatusCode(500)
					.setCause(e).response();
			createResponse = CreateResponse.setError(errorResponse);
		}
		return createResponse;
	}

	private BusinessPartner initializeBusinessPartner(CreateRequest createRequest) {
		//build business partner proposal
		EntityData entityData = createRequest.getData();
		final BusinessPartnerRole role = BusinessPartnerRole.builder().businessPartnerRole("FLCU01").build();
		final BusinessPartnerAddress address = BusinessPartnerAddress.builder()
				.cityName((String) entityData.getElementValue("CustomerCity"))
				.country((String) entityData.getElementValue("CustomerCountry")).build();
		final BusinessPartner businessPartner = BusinessPartner.builder()
				.firstName((String) entityData.getElementValue("CustomerFirstName"))
				.lastName((String) entityData.getElementValue("CustomerLastName")).businessPartnerCategory("1")
				.correspondenceLanguage("EN").businessPartnerAddress(address).businessPartnerRole(role).build();
		businessPartner.setCustomField("YY1_ApprovedBy_bus", (String) entityData.getElementValue("ProposalApprover"));
		businessPartner.setCustomField("YY1_ProposedBy_bus", (String) entityData.getElementValue("ProposalCreator"));
		return businessPartner;
	}

	private List<Customer> convertBusinessPartnersToCustomers(List<BusinessPartner> businessPartnerList) {
		List<Customer> customersList = new ArrayList<>();
		try {
			if (businessPartnerList != null) {
				for (int i = 0; i < businessPartnerList.size(); i++) {
					Customer customer = convertBusinessPartnerToCustomer(businessPartnerList.get(i));
					if (customer != null) {
						customersList.add(customer);
					}
				}
			} else {
				logger.error("Result element is null");
			}
		} catch (Exception e) {
			logger.error(
					"!!Some error occurred while calling API_BUSINESS_PARTNER/A_Customer service!!" + e.getMessage(),
					e);
		}
		return customersList;
	}

	private Customer convertBusinessPartnerToCustomer(BusinessPartner businessPartner) {
		try {
			Customer customer = new Customer();
			customer.setCustomerId(businessPartner.getBusinessPartner());
			customer.setCustomerFirstName(businessPartner.getFirstName());
			customer.setCustomerLastName(businessPartner.getLastName());
			customer.setCustomerCategory(businessPartner.getBusinessPartnerCategory());
			customer.setCustomerLanguage(businessPartner.getCorrespondenceLanguage());
			customer.setProposalCreator(businessPartner.getCustomField(PROPOSED_BY));
			customer.setProposalApprover(businessPartner.getCustomField(APPROVED_BY));

			List<BusinessPartnerAddress> addressList = businessPartner.getBusinessPartnerAddressOrFetch();

			if (!addressList.isEmpty()) {
				BusinessPartnerAddress businessPartnerAddress = addressList.get(0);
				customer.setCustomerCountry(businessPartnerAddress.getCountry());
				customer.setCustomerCity(businessPartnerAddress.getCityName());
			}
			return customer;
		} catch (Exception e) {
			logger.error(
					"!!Some error occurred while calling API_BUSINESS_PARTNER/A_Customer service!!" + e.getMessage(),
					e);
			return null;
		}
	}
}
