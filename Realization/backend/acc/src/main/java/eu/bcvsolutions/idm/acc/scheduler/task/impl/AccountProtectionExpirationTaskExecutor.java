package eu.bcvsolutions.idm.acc.scheduler.task.impl;

import java.time.ZonedDateTime;

import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Long running task for remove accounts with expired protection.
 * Expected usage is in cooperation with CronTaskTrigger, running
 * once a day after midnight.
 * 
 * @author Radek Tomiška
 * @since 7.3.0
 */
@DisallowConcurrentExecution
@Component(AccountProtectionExpirationTaskExecutor.TASK_NAME)
public class AccountProtectionExpirationTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {
	
	public static final String TASK_NAME = "acc-account-protection-expiration-long-running-task";
	private static final Logger LOG = LoggerFactory.getLogger(AccountProtectionExpirationTaskExecutor.class);
	//
	@Autowired private AccAccountService service;
	//
	private ZonedDateTime expiration;
	
	@Override
	public String getName() {
		return TASK_NAME;
	}
	
	@Override
	protected boolean start() {
		expiration = ZonedDateTime.now();
		LOG.debug("Start: Remove accounts with expired protection for expiration less than [{}]", expiration);
		//
		return super.start();
	}
	
	@Override
	public Boolean process() {
		this.counter = 0L;
		boolean canContinue = true;
		while(canContinue) {
			Page<AccAccountDto> expiredAccounts = service
					.findExpired(
						expiration,
						PageRequest.of(0, 100, Sort.by(Direction.ASC, AccAccountDto.PROPERTY_END_OF_PROTECTION)
					));
			// init count
			if (count == null) {
				count = expiredAccounts.getTotalElements();
			}
			//
			for(AccAccountDto account : expiredAccounts) {		
				service.delete(account);
				counter++;
				canContinue = updateState();
				if (!canContinue) {
					break;
				}
			}
			if (!expiredAccounts.hasNext()) {
				break;
			}
		}
		LOG.info("End: Remove accounts with expired protection for expiration less than [{}]", expiration);
		return Boolean.TRUE;
	}
	
	@Override
	public boolean isRecoverable() {
		return true;
	}
}
