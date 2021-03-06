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
package vrpsim.core.model.structure.customer;

import java.util.ArrayList;
import java.util.List;

import vrpsim.core.model.VRPSimulationModelElementParameters;
import vrpsim.core.model.behaviour.activities.util.TimeCalculationInformationContainer;
import vrpsim.core.model.events.IEvent;
import vrpsim.core.model.events.IEventType;
import vrpsim.core.model.events.OrderEvent;
import vrpsim.core.model.events.UncertainEvent;
import vrpsim.core.model.solution.Order;
import vrpsim.core.model.structure.AbstractVRPSimulationModelStructureElementWithStorage;
import vrpsim.core.model.structure.VRPSimulationModelStructureElementParameters;
import vrpsim.core.model.structure.util.storage.DefaultStorageManager;
import vrpsim.core.model.util.exceptions.EventException;
import vrpsim.core.model.util.functions.ITimeFunction;
import vrpsim.core.model.util.uncertainty.UncertainParameterContainer;
import vrpsim.core.model.util.uncertainty.UncertainParamters;
import vrpsim.core.simulator.EventListService;
import vrpsim.core.simulator.IClock;
import vrpsim.core.simulator.ITime;

/**
 * {@link StaticCustomerWithConsumption} creates {@link OrderEvent} after
 * configuration in {@link UncertainParamters}.
 * 
 * At {@link UncertainParameterContainer#getNewRealizationFromStartDistributionFunction()} the first
 * {@link OrderEvent} is generated. The second {@link OrderEvent} is generated
 * at {@link UncertainParameterContainer#getNewRealizationFromStartDistributionFunction()} +
 * {@link UncertainParameterContainer#getNewRealizationOfCycleDistributionFunction()} and so on. No consumption of
 * goods is triggered.
 * 
 * @author mayert
 */
public class DynamicCustomer extends AbstractVRPSimulationModelStructureElementWithStorage implements ICustomer {

	private final UncertainParamters orderParameters;
	private final ITimeFunction serviceTimeFunction;
	private List<IEventType> eventTypes = new ArrayList<IEventType>();
	private List<Order> createdOrders = new ArrayList<>();

	public DynamicCustomer(final VRPSimulationModelElementParameters vrpSimulationModelElementParameters,
			final VRPSimulationModelStructureElementParameters vrpSimulationModelStructureElementParameters,
			final DefaultStorageManager storageManager, final UncertainParamters orderParameters,
			final ITimeFunction serviceTimeFunction) {
		super(vrpSimulationModelElementParameters, vrpSimulationModelStructureElementParameters, storageManager);

		this.orderParameters = orderParameters;
		this.serviceTimeFunction = serviceTimeFunction;

		/* The Order itself. */
		eventTypes.add(() -> IEventType.ORDER_EVENT);
		/* When to trigger new orders. */
		eventTypes.add(() -> IEventType.TRIGGERING_ORDER_EVENT);
	}

	@Override
	public List<IEventType> getAllEventTypes() {
		return this.eventTypes;
	}

	@Override
	public List<IEvent> getInitialEvents(IClock clock) {
		List<IEvent> initialEvents = new ArrayList<>();
		for (UncertainParameterContainer container : this.orderParameters.getParameter()) {
			initialEvents.add(createTRIGGERING_ORDER_EVENT(container, clock, true));
		}
		return initialEvents;
	}

	@Override
	public List<IEvent> processEvent(IEvent event, IClock clock, EventListService eventListAnalyzer)
			throws EventException {

		List<IEvent> events = null;
		if (event.getType().getType().equals(IEventType.TRIGGERING_ORDER_EVENT)) {
			UncertainEvent uncertainEvent = (UncertainEvent) event;

			// A new Order event and a new trigger order event has to be
			// created.
			events = new ArrayList<>();
			if (uncertainEvent.getContainer().isCyclic()) {
//				uncertainEvent.getContainer().resetInstances();
				events.add(createTRIGGERING_ORDER_EVENT(uncertainEvent.getContainer(), clock, false));
			}
			events.add(createORDER_EVENT(uncertainEvent.getContainer(), clock));
		}

		return events;
	}

	@Override
	public ITime getServiceTime(TimeCalculationInformationContainer container, IClock clock) {
		return clock.getCurrentSimulationTime().createTimeFrom(this.serviceTimeFunction.getTime(container, clock));
	}

	@Override
	public UncertainParamters getUncertainParameters() {
		return this.orderParameters;
	}

	private IEvent createTRIGGERING_ORDER_EVENT(UncertainParameterContainer container, IClock clock,
			boolean isInitialEvent) {
		double t = isInitialEvent ? container.getNewRealizationFromStartDistributionFunction() : container.getNewRealizationOfCycleDistributionFunction();
		return new UncertainEvent(this, () -> IEventType.TRIGGERING_ORDER_EVENT,
				clock.getCurrentSimulationTime().createTimeFrom(t), container);
	}

	private IEvent createORDER_EVENT(UncertainParameterContainer container, IClock clock) throws EventException {

		ITime earliestDueDate = null;
		if(container.getNewRealizationFromEarliestDueDateDistributionFunction() != null) {
			earliestDueDate = container.isAdaptDueDatesToSimulationTime() 
				? clock.getCurrentSimulationTime().add(clock.getCurrentSimulationTime().createTimeFrom(container.getNewRealizationFromEarliestDueDateDistributionFunction())) 
				: clock.getCurrentSimulationTime().createTimeFrom(container.getNewRealizationFromEarliestDueDateDistributionFunction());
		}
		
		ITime latestDueDate = null;
		if(container.getNewRealizationFromLatestDueDateDistributionFunction() != null) {
			latestDueDate = container.isAdaptDueDatesToSimulationTime() 
				? clock.getCurrentSimulationTime().add(clock.getCurrentSimulationTime().createTimeFrom(container.getNewRealizationFromLatestDueDateDistributionFunction())) 
				: clock.getCurrentSimulationTime().createTimeFrom(container.getNewRealizationFromLatestDueDateDistributionFunction());
		}
		
//		ITime earliestDueDate = container.getEarliestDueDate() != null
//				? clock.getCurrentSimulationTime().add(
//						clock.getCurrentSimulationTime().createTimeFrom(container.getEarliestDueDate()))
//				: null;
//		ITime latestDueDate = container.getLatestDueDate() != null
//				? clock.getCurrentSimulationTime()
//						.add(clock.getCurrentSimulationTime().createTimeFrom(container.getLatestDueDate()))
//				: null;

		Order order = new Order(createOrderId(clock.getCurrentSimulationTime()), earliestDueDate, latestDueDate,
				container.getStorableParameters(), container.getNewRealizationFromNumberDistributionFunction().intValue(),
				this);

		// Save order in history.
		this.createdOrders.add(order);

		// An order event always occurrs with no time delay.
		return new OrderEvent(this, () -> IEventType.ORDER_EVENT, 0,
				clock.getCurrentSimulationTime().createTimeFrom(0.0), order);
	}

	private String createOrderId(ITime currentTime) {
		return "ORDER_FROM_" + this.getVRPSimulationModelElementParameters().getId() + "_AT" + currentTime.getValue();
	}

	@Override
	public List<Order> getAllCreatedOrders() {
		return this.createdOrders;
	}

	@Override
	public ITimeFunction getServiceTimeFunction() {
		return this.serviceTimeFunction;
	}

}
