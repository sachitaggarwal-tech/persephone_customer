using my.app from '../db/data-model';

service CustomerService {

	@cds.persistence.skip
	entity Customer {
	    key CustomerId : String(10);
	    CustomerFirstName : String(40);
	    CustomerLastName : String(40);
	    CustomerCategory : String(1);
	    CustomerLanguage : String(2);
	    CustomerCountry : String(3);
	    CustomerCity : String(40);
	    ProposalCreator : String(100);
	    ProposalApprover : String(100);
	}

}
