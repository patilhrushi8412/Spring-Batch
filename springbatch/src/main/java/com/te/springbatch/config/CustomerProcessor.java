package com.te.springbatch.config;

import com.te.springbatch.entity.Customer;

public class CustomerProcessor implements org.springframework.batch.item.ItemProcessor<Customer, Customer> {

	@Override
	public Customer process(Customer item) throws Exception {
		// TODO Auto-generated method stub
		return item;
	}

}
