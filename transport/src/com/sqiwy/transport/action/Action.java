/**
 * Created by abrysov
 */
package com.sqiwy.transport.action;

import java.io.Serializable;

/**
 * Representation of action which can be executed on tile click/touch.
 */
public interface Action extends Serializable {
	
	public void execute();
}
