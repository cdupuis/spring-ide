/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.model;

/**
 * @author Christian Dupuis
 */
public interface IViewEnabled extends IWebflowModelElement {

	/**
	 * Gets the view.
	 * 
	 * @return the view
	 */
	String getView();

	/**
	 * Sets the view.
	 * 
	 * @param view the view
	 */
	void setView(String view);

}
