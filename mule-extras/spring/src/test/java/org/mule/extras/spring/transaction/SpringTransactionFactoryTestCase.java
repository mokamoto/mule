/*
 * $Header$
 * $Revision$
 * $Date$
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.extras.spring.transaction;

import org.mule.tck.NamedTestCase;
import org.mule.transaction.TransactionCoordination;
import org.mule.umo.UMOTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;

public class SpringTransactionFactoryTestCase extends NamedTestCase {

	public void testCommit() throws Exception {
		Mock mockPTM = new Mock(PlatformTransactionManager.class);
		Mock mockTS  = new Mock(TransactionStatus.class);
		mockPTM.expectAndReturn("getTransaction", C.same(null), mockTS.proxy());
		mockPTM.expect("commit", C.same(mockTS.proxy()));
		
		SpringTransactionFactory factory = new SpringTransactionFactory();
		factory.setManager((PlatformTransactionManager) mockPTM.proxy());

		UMOTransaction tx = factory.beginTransaction();
		TransactionCoordination.getInstance().bindTransaction(tx);
		tx.commit();
	}
	
	public void testRollback() throws Exception {
		Mock mockPTM = new Mock(PlatformTransactionManager.class);
		Mock mockTS  = new Mock(TransactionStatus.class);
		mockPTM.expectAndReturn("getTransaction", C.same(null), mockTS.proxy());
		mockPTM.expect("rollback", C.same(mockTS.proxy()));
		mockTS.expect("setRollbackOnly");
		
		SpringTransactionFactory factory = new SpringTransactionFactory();
		factory.setManager((PlatformTransactionManager) mockPTM.proxy());

		UMOTransaction tx = factory.beginTransaction();
		TransactionCoordination.getInstance().bindTransaction(tx);
		tx.rollback();
	}
	
}
