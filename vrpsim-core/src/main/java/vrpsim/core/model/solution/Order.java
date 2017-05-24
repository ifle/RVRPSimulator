/**
 * Copyright © 2016 Thomas Mayer (thomas.mayer@unibw.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vrpsim.core.model.solution;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import vrpsim.core.model.network.IVRPSimulationModelNetworkElement;
import vrpsim.core.model.structure.IVRPSimulationModelStructureElementWithStorage;
import vrpsim.core.model.structure.util.storage.IStorable;
import vrpsim.core.model.structure.util.storage.StorableParameters;
import vrpsim.core.model.structure.util.storage.StorableType;
import vrpsim.core.simulator.ITime;

/**
 * The {@link Order} is generated by the ordering instance, called the owner of
 * the order. The owner decides what to order {@link StorableType}, how many to
 * order {@link Integer} and when the due date {@link ITime} of the order is.
 * 
 * The provider {@link IVRPSimulationModelStructureElementWithStorage} of the
 * concrete {@link IStorable} is not defined by the ordering instance. It is
 * defined by the providing instance itself. For example: The pickup depot and
 * location is set from the company who is distributing the {@link IStorable}.
 * 
 * The owner of the delivery decides how much he is willing to spent for the
 * delivery by setting the final initialCost attribute. The default initial
 * costs are 0.0. The provider of the delivered amount of goods is able to set
 * additional costs, which he is willing to pay for the delivery. So the cost
 * for example an occasional driver gets for the delivery, is calculated from
 * the initial and the additional costs.
 * 
 * @author mayert
 */
public class Order extends Observable {

	/* Which element executes the order, either an OD or an normal driver. */
	private IVRPSimulationModelStructureElementWithStorage servicedBy;

	/* Owner of the order. */
	private final IVRPSimulationModelStructureElementWithStorage owner;
	/* Destination of the order equals to the home of the owner. */
	private final IVRPSimulationModelNetworkElement destination;

	/* Where to pick up the ordered amount, structural element. */
	private IVRPSimulationModelStructureElementWithStorage provider;
	/* Where to pick up the ordered amount, network node. */
	private IVRPSimulationModelNetworkElement pickup;

	/* Identifier of the order. */
	private final String id;
	/* Earliest due date of the order. */
	private final ITime earliestDueDate;
	/* latest due date of the order. */
	private final ITime latestDueDate;
	/* What is ordered. */
	private final StorableParameters storableParameters;
	/* How many of what is ordered. */
	private final int amount;

	/* The owner is willing to pay for the delivery. */
	private final OrderCost initialCost;
	/* The organization is additional willing to pay for the delivery. */
	private OrderCost additionalCost;

	private OrderState orderState;
	private List<OrderState> orderStateHistory = new ArrayList<>();

	/**
	 * Creates an {@link Order} with all needed final information.
	 * 
	 * @param id
	 * @param earliestDueDate
	 * @param latestDueDate
	 * @param storableType
	 * @param amount
	 * @param owner
	 * @param initialCots
	 */
	public Order(String id, ITime earliestDueDate, ITime latestDueDate, StorableParameters storableParameters, int amount,
			IVRPSimulationModelStructureElementWithStorage owner, OrderCost initialCots) {

		this.id = id;
		this.earliestDueDate = earliestDueDate;
		this.latestDueDate = latestDueDate;
		this.storableParameters = storableParameters;
		this.amount = amount;
		this.owner = owner;
		this.initialCost = initialCots;

		this.destination = this.owner.getVRPSimulationModelStructureElementParameters().getHome();
		this.orderState = OrderState.CREATED;
	}

	/**
	 * Creates an {@link Order} with initial costs of 0.0. The initial costs are
	 * the price the end customer (owner) is willing to pay for the delivery.
	 * 
	 * Note that you have to create the due dates dependent on the current simulation time.
	 * 
	 * @param id
	 * @param earliestDueDate
	 * @param latestDueDate
	 * @param storableType
	 * @param amount
	 * @param owner
	 */
	public Order(String id, ITime earliestDueDate, ITime latestDueDate, StorableParameters storableParameters, int amount,
			IVRPSimulationModelStructureElementWithStorage owner) {
		this(id, earliestDueDate, latestDueDate, storableParameters, amount, owner, new OrderCost());
	}

	/**
	 * Changes the current {@link OrderState} to the new {@link OrderState} only
	 * if the state change is valid. If the state change is not valid, the state
	 * is not changed and the method returns false.
	 * 
	 * @param newOrderState
	 * @return
	 */
	public boolean changeOrderStateTo(OrderState newOrderState) {
		boolean valid = validateStateChange(newOrderState);
		if (valid) {
			this.orderStateHistory.add(this.orderState);
			this.orderState = newOrderState;
			this.setChanged();
			this.notifyObservers();
		}
		return valid;
	}

	/**
	 * Returns the initial costs of the order. The initial costs is the value
	 * the owner (end customer of the delivery) is willing to pay for the
	 * delivery.
	 * 
	 * Note: the over all costs, an occasional driver gets for the delivery, are
	 * calculated from the initial and the additional costs.
	 * 
	 * @return
	 */
	public OrderCost getInitialCost() {
		return initialCost;
	}

	/**
	 * Returns the additional costs of the order. The additional costs is the
	 * value the delivery company (end pickup of the delivery) is additional
	 * willing to pay for the delivery.
	 * 
	 * Note: the over all costs, an occasional driver gets for the delivery, are
	 * calculated from the initial and the additional costs.
	 * 
	 * @return
	 */
	public OrderCost getAdditionalCost() {
		return additionalCost;
	}

	/**
	 * Set the additional cost for the delivery. The additional costs are the
	 * amount the delivery company is willing to spent for the delivery.
	 * 
	 * @param additionalCost
	 */
	@Deprecated
	public void setAdditionalCost(OrderCost additionalCost) {
		this.additionalCost = additionalCost;
		this.setChanged();
		this.notifyObservers();
	}

	/**
	 * Sets the provider {@link IVRPSimulationModelStructureElementWithStorage}
	 * of the order and the pickup {@link IVRPSimulationModelNetworkElement}.
	 * Should be set by the organization in charge of the order.
	 * 
	 * @param provider
	 */
	public void setProvider(IVRPSimulationModelStructureElementWithStorage provider) {
		this.provider = provider;
		this.pickup = this.provider.getVRPSimulationModelStructureElementParameters().getHome();
		this.setChanged();
		this.notifyObservers();
	}

	/**
	 * Returns the provider of the {@link IStorable}, means where to pickup the
	 * storables ordered.
	 * 
	 * @return
	 */
	public IVRPSimulationModelStructureElementWithStorage getProvider() {
		return this.provider;
	}

	/**
	 * Returns the pickup {@link IVRPSimulationModelNetworkElement} of the
	 * {@link IStorable}, equals to the home of the provider.
	 * 
	 * @return
	 */
	public IVRPSimulationModelNetworkElement getPickup() {
		return this.pickup;
	}

	/**
	 * The destination of the order, euqals to the home of the owner.
	 * 
	 * @return
	 */
	public IVRPSimulationModelNetworkElement getDestination() {
		return destination;
	}

	/**
	 * The owner of the order.
	 * 
	 * @return
	 */
	public IVRPSimulationModelStructureElementWithStorage getOwner() {
		return owner;
	}

	/**
	 * Identifier the order.
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the {@link IVRPSimulationModelStructureElementWithStorage} which
	 * executed the order. Only valid if {@link OrderState} is
	 * {@link OrderState#CONFIRMED}.
	 * 
	 * @return
	 */
	public IVRPSimulationModelStructureElementWithStorage getServicedBy() {
		return servicedBy;
	}

	/**
	 * Sets the {@link IVRPSimulationModelStructureElementWithStorage} which
	 * executes the order. {@link OrderState} should be changed to
	 * {@link OrderState#IN_PROCESSING}.
	 * 
	 * @param servicedBy
	 */
	public void setServicedBy(IVRPSimulationModelStructureElementWithStorage servicedBy) {
		this.servicedBy = servicedBy;
		this.setChanged();
		this.notifyObservers();
	}

	/**
	 * Returns the earliest due date. Compare VRP with time window (TW):
	 * earliest due date describes start from time window.
	 * 
	 * @return
	 */
	public ITime getEarliestDueDate() {
		return earliestDueDate;
	}

	/**
	 * Returns the latest due date. Compare VRP with time window (TW): latest
	 * due date describes end from time window.
	 * 
	 * @return
	 */
	public ITime getLatestDueDate() {
		return latestDueDate;
	}

	/**
	 * Returns all state changes tracked. A state change is triggered through
	 * calling {@link Order#changeOrderStateTo(OrderState)}
	 * 
	 * @return
	 */
	public List<OrderState> getOrderStateHistory() {
		return orderStateHistory;
	}

	/**
	 * What is ordered?
	 * 
	 * @return
	 */
	public StorableParameters getStorableParameters() {
		return storableParameters;
	}	

	/**
	 * How much is ordered?
	 * 
	 * @return
	 */
	public int getAmount() {
		return amount;
	}


	/**
	 * Returns true, if the order state change is valid.
	 * 
	 * @param newOrderState
	 * @return
	 */
	private boolean validateStateChange(OrderState newOrderState) {
		// TODO
		return true;
	}

	/**
	 * Returns the state of the order.
	 * 
	 * @return
	 */
	public OrderState getOrderState() {
		return orderState;
	}

}
